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

public class GuiMenu extends GuiPanel implements Listener
{
	private static final	int		MAX_NUM_OF_ITEMS= 6;
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
		panelHeight		= RWGui.TITLE_SIZE + /*RWGui.BUTTON_SIZE*2 + RWGui.BORDER*4*/ + RWGui.BORDER*4;
		// Initial panel width to contain title and [X] button in top right corner
		panelWidth		= (int)(RWGui.AVG_CHAR_WIDTH1 * titleText.length() * RWGui.TITLE_SIZE)
				+ RWGui.BUTTON_SIZE + RWGui.BORDER*3;
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
			callback.onCall(player, new Integer(RWGui.ABORT_ID), null);
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
				// To support GuiUsersMenu: if the data associated with a menu
				// item can be split in a Pair left and right parts, pass them
				// as separate parameters to callback.onCall(); if not, pass the
				// entire data as second parameter.
				Pair<Integer,Object>	data	= items.get(i).getR();
//				Object	id, addData;
//				if (data instanceof Pair)
//				{
//					id		= ((Pair<?, ?>)data).getR();
//					addData	= ((Pair<?, ?>)data).getL();
//				}
//				else
//				{
//					id		= data;
//					addData	= null;
//				}
				callback.onCall(player, data.getL(), data.getR());
				return;
			}
		}
	}

	//********************
	// PUBLIC METHODS
	//********************

	public int addItem(String text, Integer id, Object data)
	{
		Pair<String,Pair<Integer,Object>>	item	=
				new Pair<String,Pair<Integer,Object>>(text, new Pair<Integer,Object>(id,data));
		items.add(item);
		// adjust panel width
		int		textWidth;
		if ( (textWidth = (int)(RWGui.AVG_CHAR_WIDTH1 * text.length() * RWGui.ITEM_SIZE)+RWGui.BORDER*2) > panelWidth)
			panelWidth = textWidth;
		// if items exceed the max, we'll need to display [Prev] and [Next] icons
		if (numOfItems == MAX_NUM_OF_ITEMS)
		{
			panelHeight	+= (RWGui.BUTTON_SIZE + RWGui.BORDER) * 2;
		}
		// if not, create a new GuiLabel for the item
		else if (numOfItems < MAX_NUM_OF_ITEMS)
		{
			guiItems[numOfItems]	= new GuiLabel(0, 0, false);	// temporary position
			guiItems[numOfItems].setPivot(PivotPosition.TopLeft);
//			buttons[numOfItems].setColor(BUTTON_COLOUR);
			guiItems[numOfItems].setFontSize(RWGui.ITEM_SIZE);
			guiItems[numOfItems].setClickable(true);
			addChild(guiItems[numOfItems]);
			panelHeight	+= RWGui.ITEM_SIZE + RWGui.BORDER;					// adjust panel and height
		}
		// if items > MAX_NUM_OF_ITEMS, do nothing special
		numOfItems++;
		return numOfItems - 1;
	}

	public int removeItem(int itemIndex)
	{
		if (itemIndex >= items.size())
			return RWGui.ITEM_NOT_FOUND;
		items.remove(itemIndex);
		// TODO: adjust panel width
		numOfItems--;
		// if items was right above the max displayable, we no longer need [Prev] and [Next] icons
		if (numOfItems == MAX_NUM_OF_ITEMS)
		{
			panelHeight	-= (RWGui.BUTTON_SIZE + RWGui.BORDER) * 2;
		}
		return itemIndex;
	}

	public int removeItem(String itemText)
	{
		for (Pair<String,Pair<Integer,Object>> item : items)
		{
			if (item.getL().equals(itemText))
			{
				int itemIndex	= items.indexOf(item);
				if (itemIndex != -1)
					return removeItem(itemIndex);
			}
		}
		return RWGui.ITEM_NOT_FOUND;
	}

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
		int		yPos	= panelHeight - (GuiTitleBar.TITLEBAR_HEIGHT + RWGui.BORDER);
		if (numOfItems > MAX_NUM_OF_ITEMS)
		{
			buttonPrev.setPosition(RWGui.BORDER, yPos, false);
			player.addGuiElement(buttonPrev);
			yPos	-= RWGui.BORDER + RWGui.BUTTON_SIZE;
		}
		for (int i = 0; i < numOfShownItems; i++)
		{
			guiItems[i].setPosition(RWGui.BORDER, yPos, false);
			player.addGuiElement(guiItems[i]);
			yPos		-= RWGui.BORDER + RWGui.ITEM_SIZE;
		}
		if (numOfItems > MAX_NUM_OF_ITEMS)
		{
			buttonNext.setPosition(RWGui.BORDER, yPos, false);
			player.addGuiElement(buttonNext);
		}
		updateTexts();
		player.addGuiElement(this);
		setVisible(true);
		plugin.registerEventListener(this);
		player.setMouseCursorVisible(true);
	}

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
//		free();
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
