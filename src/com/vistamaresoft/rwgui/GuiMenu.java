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
import com.vistamaresoft.rwgui.RWGui.RWGuiCallback;
import net.risingworld.api.Plugin;
import net.risingworld.api.events.EventMethod;
import net.risingworld.api.events.player.gui.PlayerGuiElementClickEvent;
import net.risingworld.api.gui.GuiElement;
import net.risingworld.api.gui.GuiImage;
import net.risingworld.api.gui.GuiLabel;
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
 * <p>GuiMenu inherits all the behaviours and methods of GuiModalWindow,
 * including the management of the mouse cursor, of click events, of the close
 * button and of the 'display stack'.
 * <p>If the autoClose parameter is set to true in the constructor, the menu
 * will automatically 'pop' itself away, closing and freeing itself, when an
 * item is clicked on. If the autoClose parameter is set to false, the menu
 * will remain on the screen, and another GuiModalWindow can be 'pushed' above
 * it, or it can be closed and freed manually.
 * <p><b>Important</b>: due to the way Rising World plug-ins are loaded,
 * <b>this class cannot instantiated or used in any way</b> from within the onEnable()
 * method of a plug-in, as it is impossible to be sure that, at that moment,
 * the RWGui plug-in has already been loaded.
 * <p>The first moment one can be sure that all plug-ins have been loaded, and
 * it is safe to use this class, is when (or after) the first player connects
 * to the server (either dedicated or local).
 */
public class GuiMenu extends GuiModalWindow
{
	private static final	int		MAX_NUM_OF_ITEMS= 12;

	// FIELDS
	//
	private	GuiLabel[]		guiItems;		// the menu visible items
	private GuiImage		buttonNext;
	private GuiImage		buttonPrev;
	private	int				firstItem;		// the index of the first shown menu item in the list of
											// all the items;
	private	boolean			autoClose;
	private	int				numOfItems;
	private	int				numOfShownItems;
	private	List<String>	items;

	/**
	 * Creates a new GuiMenu.
	 * @param	plugin		the plug-in the GuiMenu is intended for. This
	 *						is only needed to manage the internal event listener
	 *						and has no effects on the plug-in itself.
	 * @param	titleText	the text of the title.
	 * @param	callback	the callback object to which to report events. Can
	 *						be null, but in this case no event will reported
	 *						until an actual callback object is set with the
	 *						setCallback() method.
	 * @param	autoClose	whether the menu should automatically close when an
	 *						item is selected or not.
	 */
	public GuiMenu(Plugin plugin, String titleText, RWGuiCallback callback, boolean autoClose)
	{
		super(plugin, titleText, RWGui.LAYOUT_VERT, callback);
		this.autoClose	= autoClose;
		guiItems		= new GuiLabel[MAX_NUM_OF_ITEMS];
		items			= new ArrayList<String>();
		firstItem		= numOfItems = numOfShownItems = 0;
		buttonNext	= new GuiImage(0, 0, false, RWGui.BUTTON_SIZE, RWGui.BUTTON_SIZE, false);
		RWGui.setImage(buttonNext, RWGui.ICN_ARROW_DOWN);
		buttonNext.setPivot(PivotPosition.TopLeft);
		buttonNext.setClickable(true);
		buttonNext.setVisible(false);
		buttonPrev	= new GuiImage(0, 0, false, RWGui.BUTTON_SIZE, RWGui.BUTTON_SIZE, false);
		RWGui.setImage(buttonPrev, RWGui.ICN_ARROW_UP);
		buttonPrev.setPivot(PivotPosition.TopLeft);
		buttonPrev.setClickable(true);
		buttonPrev.setVisible(false);
	}

	/**
	 * Creates a new GuiMenu with autoClose enabled.
	 * @param	plugin		the plug-in the GuiMenu is intended for. This
	 *						is only needed to manage the internal event listener
	 *						and has no effects on the plug-in itself.
	 * @param	titleText	the text of the title.
	 * @param	callback	the callback object to which to report events. Can
	 *						be null, but in this case no event will reported
	 *						until an actual callback object is set with the
	 *						setCallback() method.
	 */
	public GuiMenu(Plugin plugin, String titleText, RWGuiCallback callback)
	{
		this(plugin, titleText, callback, true);
	}

	@EventMethod
	public void onClick(PlayerGuiElementClickEvent event)
	{
		GuiElement	element	= event.getGuiElement();
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

		Player player	= event.getPlayer();
		// it is not possible to simply use pop(), as pop() calls free()
		// and super.onClick() requires the children of this to still be
		// there. Then, the contents of pop() has been split in everything
		// but free() before the call to super.onClick() and the call to
		// free() after it.
		if (autoClose)
			close(player);
		if (prevWindow != null)
		{
			prevWindow.show(player);
			player.setMouseCursorVisible(true);
		}
		else
			player.setMouseCursorVisible(false);
		super.onClick(event);
		if (autoClose)
			free();
	}

	//********************
	// PUBLIC METHODS
	//********************

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
	public void addChild(String text, Integer id, Object data)
	{
		items.add(text);
		if (numOfItems < MAX_NUM_OF_ITEMS)
		{
			guiItems[numOfItems]	= new GuiLabel(0, 0, false);	// temporary position
			super.addChild(guiItems[numOfItems], id, data);
			numOfShownItems++;
		}
		numOfItems++;
	}

	@Deprecated
	public int addItem(String text, Integer id, Object data)
	{
		addChild(text, id, data);
		return numOfItems - 1;
	}

	/**
	 * Removes the menu item at the itemIndex index. Item indices start from 0.
	 * 
	 * @param	itemIndex	the index of the menu item to remove.
	 * @return	the index of the removed item or ERR_INVALID_PARAMETER if the
	 * 			index was not valid.
	 */
	public int removeChild(int itemIndex)
	{
		if (itemIndex < 0 || itemIndex >= items.size())
			return RWGui.ERR_INVALID_PARAMETER;
		items.remove(itemIndex);
		// TODO: adjust panel width
		numOfItems--;
		// if items was right above the max displayable, we no longer need [Prev] and [Next] icons
		if (numOfItems < MAX_NUM_OF_ITEMS)
		{
			removeChild(guiItems[itemIndex]);		// remove GuiLabel from layout
			// shift items below one slot up
			for (int i = itemIndex; i < MAX_NUM_OF_ITEMS-1; i++)
				guiItems[i]	= guiItems[i+1];
			guiItems[MAX_NUM_OF_ITEMS-1]= null;		// clear last slot
			numOfShownItems--;
		}
		return itemIndex;
	}

	@Deprecated
	public int removeItem(int itemIndex)
	{
		return removeChild(itemIndex);
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
	public int removeChild(String itemText)
	{
		for (String item : items)
		{
			if (item.equals(itemText))
			{
				int itemIndex	= items.indexOf(item);
				if (itemIndex != -1)
					return removeChild(itemIndex);
			}
		}
		return RWGui.ERR_ITEM_NOT_FOUND;
	}

	@Deprecated
	public int removeItem(String itemText)
	{
		return removeChild(itemText);
	}

	@Override
	public void layout()
	{
		super.layout();			// default layout, without UP and DOWN arrows
		// increment width to include arrow buttons
		setSize(getWidth() + RWGui.BUTTON_SIZE, getHeight(), false);
		titleBar.relayout();
		// determine position of UP and DOWN arrows
		int		x	= (int)(layout.getPositionX() + layout.getWidth());
		int		yDn	= (int)layout.getPositionY() + RWGui.BUTTON_SIZE;
		int		yUp	= yDn + (int)layout.getHeight() - RWGui.BUTTON_SIZE;
		buttonPrev.setPosition(x,  yUp, false);
		buttonNext.setPosition(x,  yDn, false);
	}

	/**
	 * Displays the menu on the player screen.
	 * 
	 * @param	player	the player to show the dialogue box to.
	 */
	@Override
	public void show(Player player)
	{
		updateTexts();
		player.addGuiElement(buttonNext);
		player.addGuiElement(buttonPrev);
		super.show(player);
	}

	/**
	 * Closes (hides) the menu from the player screen, turning the
	 * mouse cursor off.
	 * 
	 * <p>The menu resources are <b>not freed</b> and the menu can be re-used
	 * if needed; when the menu is no longer needed, its resources must be
	 * freed with the free() method, in addition to closing it.
	 * @param	player	the player from whose screen to remove the menu.
	 * 					Removing the same menu from the same player multiple
	 *					times has no effect and does no harm.
	 */
	@Override
	public void close(Player player)
	{
		player.removeGuiElement(buttonNext);
		player.removeGuiElement(buttonPrev);
		super.close(player);
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
	@Override
	public void free()
	{
		buttonNext		= null;
		buttonPrev		= null;
		items.clear();
		super.free();
	}

	//********************
	// PRIVATE HELPER METHODS
	//********************

	private void scrollDown()
	{
		firstItem	+= MAX_NUM_OF_ITEMS-1;
		if (firstItem + MAX_NUM_OF_ITEMS > numOfItems)
			firstItem	= numOfItems - MAX_NUM_OF_ITEMS;
		updateTexts();
	}

	private void scrollUp()
	{
		firstItem	-= MAX_NUM_OF_ITEMS-1;
		if (firstItem < 0)
			firstItem	= 0;
		updateTexts();
	}

	private void updateTexts()
	{
		for (int i = 0; i < numOfShownItems; i++)
		{
			guiItems[i].setText(items.get(firstItem+i));
		}
		buttonPrev.setVisible(firstItem > 0);
		buttonNext.setVisible(firstItem + MAX_NUM_OF_ITEMS < numOfItems);
	}

}
