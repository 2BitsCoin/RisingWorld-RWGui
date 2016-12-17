/****************************
	R W G U I  -  A Rising World Java library for GUI elements.

	GuiMenu.java - Displays and manages a modal menu.

	Created by : Maurizio M. Gavioli 2016-11-01

	(C) Maurizio M. Gavioli (a.k.a. Miwarre), 2016
	Licensed under the Creative Commons by-sa 3.0 license (see http://creativecommons.org/licenses/by-sa/3.0/ for details)

*****************************/

package com.vistamaresoft.rwgui;

import java.util.ArrayList;
import java.util.List;
import com.vistamaresoft.rwgui.RWGui.Pair;
import com.vistamaresoft.rwgui.RWGui.RWGuiCallback;
import net.risingworld.api.Plugin;
import net.risingworld.api.events.EventMethod;
import net.risingworld.api.events.Listener;
import net.risingworld.api.events.player.gui.PlayerGuiElementClickEvent;
import net.risingworld.api.gui.GuiElement;
import net.risingworld.api.gui.GuiImage;
import net.risingworld.api.gui.GuiLabel;
import net.risingworld.api.gui.GuiPanel;
import net.risingworld.api.gui.PivotPosition;
import net.risingworld.api.objects.Player;

/**
 * A class implementing a modal menu. Each menu is made of a top title bar,
 * with title and close button, and a number of text items which can be clicked
 * to select.
 *
 * The items are arranged vertically and the menu is shown in the middle of the
 * player screen. The menu adapts its vertical and horizontal sizes to the
 * number and length of the texts.
 * <p>If there are more than 12 items, the menu displays them in chunks of 12,
 * with an up and a down button to page among the chunks.
 * <p>The menu manages its own event Listener; it also turns the mouse
 * cursor on on display and off on hiding.
 * <p>The menu manages the close button in the title bar, hiding the box
 * from the player screen and turning off the mouse cursor. The callback object
 * is notified of a close event by passing an id parameter with a value of
 * RWGui.ABORT_ID.
 * <p>The menu notifies of click and text entry events via an RWGuiCallback
 * object passed to the constructor or set after construction with
 * the setCallback() method.
 * <p>On click events, the onCall() method of the callback object is called
 * with parameters for the player originating the event, the id of the menu
 * item and any additional data set for the GuiElement. Id and data for each
 * item are set when the item is added.
 * <p>On click events, the menu automatically closes and turns the mouse cursor
 * off. The consumer plug-in needs not to do any additional management of the
 * menu in response to notifications.
 * <p>Once closed, the menu still exists and can be reused again, if necessary.
 * Once the menu is no longer needed, its resources can be freed with the
 * free() method.
 * <p><b>Important</b>: due to the way Rising World plug-ins are loaded,
 * <b>this class cannot instantiated or used in any way</b> from within the onEnable()
 * method of a plug-in, as it is impossible to be sure that, at that moment,
 * the RWGui plug-in has already been loaded.
 * <p>The first moment one can be sure that all plug-ins have been loaded, and
 * it is safe to use this class, is when (or after) the first player connects
 * to the server (either dedicated or local).
 */
public class GuiMenu extends GuiPanel implements Listener
{
	private static final	int		MAX_NUM_OF_ITEMS= 12;
	private static final	float	PANEL_XPOS		= 0.5f;
	private static final	float	PANEL_YPOS		= 0.5f;

	// FIELDS
	//
	private	GuiLabel[]		guiItems;		// the menu visible items
	private GuiImage		buttonNext;
	private GuiImage		buttonPrev;
	private	int				firstItem;		// the index of the first shown menu item in the list of
											// all the items;
	private	RWGuiCallback	callback;
	private	int				numOfItems;
	private	int				numOfShownItems;
	private int				panelHeight;
	private int				panelWidth;
	private	Plugin			plugin;
	private List<Pair<String,Pair<Integer,Object>>>	items;
	private GuiTitleBar		guiTitleBar;

	/**
	 * Creates a new GuiMenu.
	 * @param	plugin		the plug-in the GuiMenu is intended for. This
	 * 						is only needed to manage the internal event listener
	 * 						and has no effects on the plug-in itself.
	 * @param	titleText	the text of the title.
	 * @param	callback	the callback object to which to report events. Can
	 * 						be null, but in this case no event will reported
	 * 						until an actual callback object is set with the
	 * 						setCallback() method.
	 */
	public GuiMenu(Plugin plugin, String titleText, RWGuiCallback callback)
	{
		super();
		guiItems		= new GuiLabel[MAX_NUM_OF_ITEMS];
		items			= new ArrayList<Pair<String,Pair<Integer,Object>>>();
		this.callback	= callback;
		this.plugin		= plugin;
		firstItem		= 0;
		numOfItems		= 0;
		// Initial panel height to contain title, prev. and next buttons
		// Borders: top, twice title/items, bottom
		panelHeight		= RWGui.TITLE_SIZE + RWGui.DEFAULT_PADDING*4;
		// Initial panel width to contain title and [X] button in top right corner
		panelWidth		= (int)(RWGui.AVG_CHAR_WIDTH1 * titleText.length() * RWGui.TITLE_SIZE)
				+ RWGui.BUTTON_SIZE + RWGui.DEFAULT_PADDING*3;
		// position a panel centred in the screen
		setPosition(PANEL_XPOS, PANEL_YPOS, true);
//		setSize(panelWidth, panelHeight, false);	// not yet known!!!
		setPivot(PivotPosition.Center);
		setBorderColor(RWGui.BORDER_COLOUR);
		setBorderThickness(RWGui.BORDER_THICKNESS, false);
		setColor(RWGui.PANEL_COLOUR);
		setVisible(true);

		guiTitleBar	= new GuiTitleBar(this, titleText, true);
		buttonNext	= new GuiImage(0, 0, false, RWGui.BUTTON_SIZE, RWGui.BUTTON_SIZE, false);
		RWGui.setImage(buttonNext, RWGui.ICN_ARROW_DOWN);
		buttonNext.setPivot(PivotPosition.TopLeft);
		buttonNext.setClickable(true);
		buttonNext.setVisible(false);
		addChild(buttonNext);
		buttonPrev	= new GuiImage(0, 0, false, RWGui.BUTTON_SIZE, RWGui.BUTTON_SIZE, false);
		RWGui.setImage(buttonPrev, RWGui.ICN_ARROW_UP);
		buttonPrev.setPivot(PivotPosition.TopLeft);
		buttonPrev.setClickable(true);
		buttonPrev.setVisible(false);
		addChild(buttonPrev);
	}

	@EventMethod
	public void onClick(PlayerGuiElementClickEvent event)
	{
		GuiElement	element	= event.getGuiElement();
		Player		player	= event.getPlayer();
		if (guiTitleBar.isCancelButton(element))
		{
			close(player);
			if (callback != null)
				callback.onCall(player, RWGui.ABORT_ID, null);
			return;
		}
		if (element == buttonPrev)
		{
			scrollUp();
			return;
		}
		if (element == buttonNext)
		{
			scrollDown();
			return;
		}
		for (int i = 0; i < numOfShownItems; i++)
		{
			if (event.getGuiElement() == guiItems[i])
			{
				close(player);
				Pair<Integer,Object>	data	= items.get(i).getR();
				if (callback != null)
					callback.onCall(player, data.getL(), data.getR());
				return;
			}
		}
	}

	//********************
	// PUBLIC METHODS
	//********************

	/**
	 * Sets the callback function called upon click and text entry events.

	 * @param	callback	the new callback
	 */
	public void setCallback(RWGuiCallback callback)
	{
		this.callback	= callback;
	}

	/**
	 * Adds a new menu item with the associated id and data.
	 * 
	 * <p>id can be any Integer and id's should be all different from one
	 * another within each dialogue box.
	 * 
	 * <p>The data parameter can be any Java object and can store additional
	 * information required to deal with the element, when a click event is
	 * reported for it via the callback object. It can also be null if no
	 * additional info is needed for the element.
	 * 
	 * <p>id and data are reported by the callback object upon click events.
	 * 
	 * @param	text	the text of the new menu item.
	 * @param	id		the id associated with the item.
	 * @param	data	the data associated with the element; may be null for
	 * 					elements which need no additional data other than their id.
	 */
	public int addItem(String text, Integer id, Object data)
	{
		Pair<String,Pair<Integer,Object>>	item	=
				new Pair<String,Pair<Integer,Object>>(text, new Pair<Integer,Object>(id,data));
		items.add(item);
		// adjust panel width
		int		textWidth	= (int)(RWGui.AVG_CHAR_WIDTH1 * text.length() * RWGui.ITEM_SIZE) +
				RWGui.DEFAULT_PADDING*2;
		if (textWidth > panelWidth)
			panelWidth = textWidth;
		// if items exceed the max, we'll need to display [Prev] and [Next] icons
		if (numOfItems == MAX_NUM_OF_ITEMS)
		{
			panelHeight	+= (RWGui.BUTTON_SIZE + RWGui.DEFAULT_PADDING) * 2;
		}
		// if not, create a new GuiLabel for the item
		else if (numOfItems < MAX_NUM_OF_ITEMS)
		{
			guiItems[numOfItems]	= new GuiLabel(0, 0, false);	// temporary position
			guiItems[numOfItems].setPivot(PivotPosition.TopLeft);
			guiItems[numOfItems].setFontSize(RWGui.ITEM_SIZE);
			guiItems[numOfItems].setClickable(true);
			addChild(guiItems[numOfItems]);
			panelHeight	+= RWGui.ITEM_SIZE + RWGui.DEFAULT_PADDING;					// adjust panel and height
		}
		// if items > MAX_NUM_OF_ITEMS, do nothing special
		numOfItems++;
		return numOfItems - 1;
	}

	/**
	 * Removes the menu item at the itemIndex index. Item indices start from 0.
	 * 
	 * @param	itemIndex	the index of the menu item to remove.
	 * @return	the index of the removed item or ERR_INVALID_PARAMETER if the
	 * 			index was not valid.
	 */
	public int removeItem(int itemIndex)
	{
		if (itemIndex < 0 || itemIndex >= items.size())
			return RWGui.ERR_INVALID_PARAMETER;
		items.remove(itemIndex);
		// TODO: adjust panel width
		numOfItems--;
		// if items was right above the max displayable, we no longer need [Prev] and [Next] icons
		if (numOfItems == MAX_NUM_OF_ITEMS)
		{
			panelHeight	-= (RWGui.BUTTON_SIZE + RWGui.DEFAULT_PADDING) * 2;
		}
		return itemIndex;
	}

	/**
	 * Removes the first menu item with given item text.
	 * 
	 * To match, the item text should be <b>exactly</b> the same,
	 * capitalisation included.
	 * 
	 * <p>If more items with the same text exist, only the first is removed.
	 * 
	 * @param	itemText	the text of the menu item to remove.
	 * @return	the index of the removed item or ERR_ITEM_NOT_FOUND if the
	 * 			the no item has that string as item text.
	 */
	public int removeItem(String itemText)
	{
		for (Pair<String,?> item : items)
		{
			if (item.getL().equals(itemText))
			{
				int itemIndex	= items.indexOf(item);
				if (itemIndex != -1)
					return removeItem(itemIndex);
			}
		}
		return RWGui.ERR_ITEM_NOT_FOUND;
	}

	/**
	 * Displays the menu on the player screen.
	 * 
	 * @param	player	the player to show the dialogue box to.
	 */
	public void show(Player player)
	{
		if (numOfItems > MAX_NUM_OF_ITEMS)
		{
			numOfShownItems	= MAX_NUM_OF_ITEMS;
			buttonNext.setVisible(true);
		}
		else
		{
			numOfShownItems	= numOfItems;
			buttonNext.setVisible(false);
		}

		setSize(panelWidth, panelHeight, false);	// set appropriate sizes
		// Now we know the panel sizes: update positions of GuiElement's
		guiTitleBar.relayout();
		guiTitleBar.addToPlayer(player);
		int		yPos	= panelHeight - (GuiTitleBar.TITLEBAR_HEIGHT + RWGui.DEFAULT_PADDING);
		if (numOfItems > MAX_NUM_OF_ITEMS)
		{
			buttonPrev.setPosition(RWGui.DEFAULT_PADDING, yPos, false);
			player.addGuiElement(buttonPrev);
			yPos	-= RWGui.DEFAULT_PADDING + RWGui.BUTTON_SIZE;
		}
		for (int i = 0; i < numOfShownItems; i++)
		{
			guiItems[i].setPosition(RWGui.DEFAULT_PADDING, yPos, false);
			player.addGuiElement(guiItems[i]);
			yPos		-= RWGui.DEFAULT_PADDING + RWGui.ITEM_SIZE;
		}
		if (numOfItems > MAX_NUM_OF_ITEMS)
		{
			buttonNext.setPosition(RWGui.DEFAULT_PADDING, yPos, false);
			player.addGuiElement(buttonNext);
		}
		updateTexts();
		player.addGuiElement(this);
		setVisible(true);
		plugin.registerEventListener(this);
		player.setMouseCursorVisible(true);
	}

	/**
	 * Releases the resources used by the menu. After this method has
	 * been called, the menu cannot be used or displayed any longer.
	 * 
	 * The resources are in any case garbage collected once the dialogue box
	 * goes out of scope or all the references to it elapse. Using this method
	 * might be useful to speed up the garbage collection process, once the
	 * dialogue box is not longer needed.
	 */
	public void free()
	{
		guiTitleBar.free();
		guiTitleBar		= null;
		removeChild(buttonNext);
		buttonNext		= null;
		removeChild(buttonPrev);
		buttonPrev		= null;
		for (int i=0; i < numOfShownItems; i++)
		{
			removeChild(guiItems[i]);
			guiItems[i]	= null;
		}
		items.clear();
	}

	//********************
	// PRIVATE HELPER METHODS
	//********************

	private void close(Player player)
	{
		plugin.unregisterEventListener(this);
		player.setMouseCursorVisible(false);
		setVisible(false);
		guiTitleBar.removeFromPlayer(player);
		player.removeGuiElement(buttonNext);
		player.removeGuiElement(buttonPrev);
		for (int j=0; j < numOfShownItems; j++)
			player.removeGuiElement(guiItems[j]);
		player.removeGuiElement(this);
	}

	private void scrollDown()
	{
		firstItem	+= MAX_NUM_OF_ITEMS-1;
		if (firstItem + MAX_NUM_OF_ITEMS > numOfItems)
		{
			firstItem	= numOfItems - MAX_NUM_OF_ITEMS;
			buttonNext.setVisible(false);
		}
		else
			buttonNext.setVisible(true);
		buttonPrev.setVisible(true);
		updateTexts();
	}

	private void scrollUp()
	{
		firstItem	-= MAX_NUM_OF_ITEMS-1;
		if (firstItem < 0)
		{
			firstItem	= 0;
			buttonPrev.setVisible(false);
		}
		else
			buttonPrev.setVisible(true);
		buttonNext.setVisible(true);
		updateTexts();
	}

	private void updateTexts()
	{
		for (int i = 0; i < numOfShownItems; i++)
		{
			guiItems[i].setText(items.get(firstItem+i).getL());
		}
	}

}
