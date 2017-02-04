/****************************
	R W G U I  -  A Rising World Java library for GUI elements.

	GuiMenu.java - Displays and manages a modal menu.

	Created by : Maurizio M. Gavioli 2016-11-01

	(C) Maurizio M. Gavioli (a.k.a. Miwarre), 2016
	Licensed under the Creative Commons by-sa 3.0 license (see http://creativecommons.org/licenses/by-sa/3.0/ for details)

*****************************/

package com.vistamaresoft.rwgui;

import com.vistamaresoft.rwgui.RWGui.Pair;
import com.vistamaresoft.rwgui.RWGui.RWGuiCallback;
import net.risingworld.api.Plugin;
import net.risingworld.api.gui.GuiElement;
import net.risingworld.api.gui.GuiLabel;

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
		((GuiVerticalLayout)layout).setMaxVisibleRows(MAX_NUM_OF_ITEMS);
		this.autoClose	= autoClose;
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
		super(plugin, titleText, RWGui.LAYOUT_VERT, callback);
		((GuiVerticalLayout)layout).setMaxVisibleRows(MAX_NUM_OF_ITEMS);
		this.autoClose	= true;
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
		super.addChild(new GuiLabel(text, 0, 0, false), id, data);
	}

	@Deprecated
	public int addItem(String text, Integer id, Object data)
	{
		addChild(text, id, data);
		return ((GuiVerticalLayout)layout).children.size() - 1;
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
		return ((GuiVerticalLayout)layout).removeChild(itemIndex);
	}

	@Deprecated
	public int removeItem(int itemIndex)
	{
		return ((GuiVerticalLayout)layout).removeChild(itemIndex);
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
		for (Pair<GuiElement, Pair<Integer, Object>> item : layout.children)
		{
			GuiElement	element	= item.getL();
			if ( ((GuiLabel)element).getText().equals(itemText))
			{
				int itemIndex	= layout.children.indexOf(item);
				if (itemIndex != -1)
					removeChild(element);
				return itemIndex;
			}
		}
		return RWGui.ERR_ITEM_NOT_FOUND;
	}

	@Deprecated
	public int removeItem(String itemText)
	{
		return removeChild(itemText);
	}

}
