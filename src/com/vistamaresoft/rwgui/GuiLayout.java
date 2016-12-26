/****************************
	R W G U I  -  A Rising World Java library for GUI elements.

	GuiLayout.java - An abstract class which is the base for automatic layout classes.

	Created by : Maurizio M. Gavioli 2016-11-19

	(C) Maurizio M. Gavioli (a.k.a. Miwarre), 2016
	Licensed under the Creative Commons by-sa 3.0 license (see http://creativecommons.org/licenses/by-sa/3.0/ for details)

*****************************/

package com.vistamaresoft.rwgui;

import java.util.ArrayList;
import com.vistamaresoft.rwgui.RWGui.Pair;
import net.risingworld.api.gui.GuiElement;
import net.risingworld.api.gui.GuiImage;
import net.risingworld.api.gui.GuiLabel;
import net.risingworld.api.gui.GuiPanel;
import net.risingworld.api.gui.GuiTextField;
import net.risingworld.api.gui.PivotPosition;
import net.risingworld.api.objects.Player;

/**
	A class which is the base for all RWGui classes supporting
	'automatic' layout of a collections of GuiElement's.
	
	This class is not expected to be instantiated directly. Use one of its
	sub-classes instead.

	<p>Each GuiLayout has a specific arrangement of its children, according to the
	specific subclass used (GuiHorizontalLayout, GuiVerticalLayout, ...).
	It can have more GuiLayout's as children to achieve complex structures.
 * <p><b>Important</b>: due to the way Rising World plug-ins are loaded,
 * <b>this class</b> (or its sub-classes) <b>cannot instantiated or used in any way</b>
 * from within the onEnable() method of a plug-in, as it is impossible to be
 * sure that, at that moment, the RWGui plug-in has already been loaded.
 * <p>The first moment one can be sure that all plug-ins have been loaded, and
 * it is safe to use this class, is when (or after) the first player connects
 * to the server (either dedicated or local).
*/
public class GuiLayout extends GuiPanel
{
	protected	ArrayList<Pair<GuiElement,Pair<Integer,Object>>>
													children	= null;
	protected	int									flags		= RWGui.LAYOUT_V_TOP & RWGui.LAYOUT_H_LEFT;
	int									margin	= 0;
	int									padding	= RWGui.DEFAULT_PADDING;
	protected	int									shown		= 0;

	public GuiLayout(int flags)
	{
		super (0, 0, false, 0, 0, false);
		setPivot(PivotPosition.TopLeft);
		this.flags	= flags;
	}

	/**
	Arranges the elements inside the layout according to the layout type and settings.

	As this method lays its children out recursively, it is usually
	necessary to call this method manually only for the top layout of a
	layout hierarchy.
	@param	minWidth	the minimum width which the context within which the
						is placed requires the layout to have; use 0 if there
						no external constrains.
	@param	minHeight	the minimum height which the context within which the
						is placed requires the layout to have; use 0 if there
						no external constrains.
*/
	public void layout(int minWidth, int minHeight)
	{
		layout(minWidth, minHeight, true);		// layout with no constrain
		int height	= (int)getHeight();
		int	width	= (int)getWidth();
		layout(width, height, false);			// re-layout within actual width and height
	}

	/**
	 * Releases the resources used by the layout and all its descending
	 * hierarchy of children. After this method has been called, the layout
	 * cannot be used or displayed any longer.
	 * 
	 * The resources are in any case garbage collected once the layout goes
	 * out of scope or all the references to it elapse. Using this method
	 * might be useful to speed up the garbage collection process, once the
	 * layout is not longer needed.
	 * <p>It is necessary to call this method only for the top layout of hierarchy
	 * and only if it not part of a managed element (like GuiDialogueBox).
	 */
	public void free()
	{
		for (Pair<GuiElement,Pair<Integer,Object>> item : children)
		{
			if (item == null)
				continue;
			GuiElement	element	= item.getL();
			if (element instanceof GuiLayout)
				((GuiLayout)element).free();
			super.removeChild(element);
		}
		children.clear();
	}

	/**
	 * Hides the layout and all its hierarchy of children removing it from the
	 * player screen.
	 * 
	 * It is necessary to call this method only for the top layout of a
	 * hierarchy and only if it not part of a managed element (like
	 * GuiDialogueBox).
	 * 
	 * @param	player	the player from whose screen to remove the dialogue
	 * 					box. Removing the same dialogue box from the same
	 * 					player multiple times has no effect and does no harm.
	 */
	public void hide(Player player)
	{
		for (Pair<GuiElement,Pair<Integer,Object>> item : children)
		{
			if (item == null)
				continue;
			GuiElement	element	= item.getL();
			if (element instanceof GuiLayout)
				((GuiLayout)element).hide(player);
			else
				player.removeGuiElement(element);
		}
		player.removeGuiElement(this);
	}

	/**
	 * Sets the margin between the contents of the layout and its edges.
	 * @param value	the new padding (in pixels).
	 */
	public void setMargin(int value)	{	this.margin		= value;	}

	/**
	 * Sets the padding (i.e. the minimum distance) between two side-by-side
	 * elements of the layout (in pixels),
	 * @param value	the new padding (in pixels).
	 */
	public void setPadding(int value)	{	this.padding	= value;	}

	/**
	 * Displays the layout on the player screen.
	 * 
	 * It is necessary to call this method only for the top layout of a
	 * hierarchy and only if it not part of a managed element (like
	 * GuiDialogueBox).
	 * 
	 * @param	player	the player to show the layout to.
	 */
	public void show(Player player)
	{
		player.addGuiElement(this);
		for (Pair<GuiElement,Pair<Integer,Object>> item : children)
		{
			if (item == null)
				continue;
			GuiElement	element	= item.getL();
			if (element instanceof GuiLayout)
				((GuiLayout)element).show(player);
			else
				player.addGuiElement(element);
		}
		shown++;
	}

	/**
	 * Adds an inactive GuiElement (with no associated data) as a direct child
	 * of the layout. The element is positioned according to the type of layout.
	 * 
	 * @param	element	the element to add.
	 */
	@Override
	public void addChild(GuiElement element)
	{
		addChild(element, null, null);
	}

	/**
	 * Adds a GuiElement with the associated id as a direct child of the layout.
	 * The element is positioned according to the type of layout.
	 * 
	 * <p>The element will have no data in addition to its id.
	 * 
	 * <p>If id is not null, the element is active (the player can click on it),
	 * if id is null, the element is not active.
	 * 
	 * @param	element	the element to add.
	 * @param	id		the id associated with the element; may be null for
	 * 					inactive elements.
	 */
	public void addChild(GuiElement element, Integer id)
	{
		addChild(element, id, null);
	}

	/**
	 * Adds a GuiElement with the associated id and data as a direct child of
	 * the layout. The element is positioned according to the type of layout.
	 * 
	 * <p>If id is not null, the element is active (the player can click on it),
	 * if id is null, the element is not active.
	 * 
	 * @param	element	the element to add.
	 * @param	id		the id associated with the element; may be null for
	 * 					inactive elements.
	 * @param	data	the data associated with the element; may be null for
	 * 					elements which need no additional data other than their id.
	 */
	public void addChild(GuiElement element, Integer id, Object data)
	{
		if (element == null)
			return;
		if (children == null)
			children	= new ArrayList<Pair<GuiElement,Pair<Integer,Object>>>(4);
		children.add(new Pair<GuiElement,Pair<Integer,Object>>(element, new Pair<Integer,Object>(id, data)));
		if (element instanceof GuiImage)
			((GuiImage)element).setClickable(id != null);
		else if (element instanceof GuiLabel)
		{
			((GuiLabel)element).setClickable(id != null);
			((GuiLabel)element).setFontSize(RWGui.ITEM_SIZE);
		}
		else if (element instanceof GuiPanel)
			((GuiPanel)element).setClickable(id != null);
		else if (element instanceof GuiTextField)
		{
			((GuiTextField)element).setClickable(id != null);
			((GuiTextField)element).setBorderThickness(1, false);
			((GuiTextField)element).setBackgroundPreset(1);
			((GuiTextField)element).setEditable(id != null);
			((GuiTextField)element).setListenForInput(id != null);
		}
		element.setPivot(PivotPosition.TopLeft);
		super.addChild(element);
	}

	/**
	 * Removes a GuiElement from the direct children of the dialogue box.
	 * 
	 * If the element is not a direct child of the layout, the method does
	 * nothing.
	 * @param	element	The GuiElement to remove
	 */
	@Override
	public void removeChild(GuiElement element)
	{
		if (children == null || element == null)
			return;
		for (Pair<GuiElement,Pair<Integer,Object>> item : children)
			if (item != null && item.getL() == element)
			{
				children.remove(item);
				super.removeChild(element);
				break;
			}
	}

	/**
	 * Adds a new GuiLayout as a direct child of this layout.
	 * 
	 * This method only supports the RWGui.LAYOUT_HORIZ and RWGui.LAYOUT_VERT
	 * layout types. To add a new table layout child, use the
	 * addNewTableLayoutChild() method.
	 * 
	 * @param	layoutType	either RWGui.LAYOUT_HORIZ or RWGui.LAYOUT_VERT.
	 * @param	layoutFlags	one of the RWGui.LAYOUT_H_* flags ORed with one of
	 * 			RWGui.LAYOUT_V_* flags; their meaning depends on the layout type
	 * 			(RWGui.LAYOUT_HORIZ or RWGui.LAYOUT_VERT).
	 * @return	the new GuiLayout.
	 */
	public GuiLayout addNewLayoutChild(int layoutType, int layoutFlags)
	{
		GuiLayout	layout;
		if (layoutType == RWGui.LAYOUT_HORIZ)
			layout		= new GuiHorizontalLayout(layoutFlags);
		else if (layoutType == RWGui.LAYOUT_VERT)
			layout		= new GuiVerticalLayout(layoutFlags);
		else
			return null;
		addChild(layout, null, null);
		return layout;
	}

	/**
	 * Adds a new GuiTableLayout as a direct child of this layout.
	 * 
	 * This method only supports the RWGui.LAYOUT_TABLE layout type.
	 * To add a new horizontal or vertical layout child, use the
	 * addNewLayoutChild() method.
	 * 
	 * @param	colNum	the number of columns the table will have
	 * @param	rowNum	the number of rows the table will have.
	 * @param	flags	currently not used, set to 0.
	 * @return	the new GuiTableLayout as a GuiLayout.
	 */
	public GuiLayout addNewTableLayoutChild(int colNum, int rowNum, int flags)
	{
		GuiLayout	layout	= new GuiTableLayout(colNum, rowNum, flags);
		addChild(layout, null, null);
		return layout;
	}

	/**
	 * Returns the id associated with element, if element is one of the
	 * children of the layout (recursively); or null otherwise.

	 * @param	element	the GuiElement to look for.
	 * @return	the id associated with element if present, null if not.
	 */
	public Integer getItemId(GuiElement element)
	{
		for (Pair<GuiElement,Pair<Integer, Object>> item : children)
		{
			if (item == null)
				continue;
			GuiElement	e	= item.getL();
			if (e instanceof GuiLayout)
			{
				Integer	id	= ((GuiLayout)e).getItemId(element);
				if (id != null)
					return id;
			}
			if (item.getL() == element)
				return item.getR().getL();
		}
		return null;
	}

	/**
	 * Returns the id and data pair associated with element, if element is one
	 * of the children of the layout (recursively); or null otherwise.

	 * @param	element	the GuiElement to look for.
	 * @return	the id and data pair associated with element if present,
	 * 			null if not.
	 */
	public Pair<Integer,Object> getItemData(GuiElement element)
	{
		for (Pair<GuiElement,Pair<Integer, Object>> item : children)
		{
			if (item == null)
				continue;
			GuiElement	e	= item.getL();
			if (e instanceof GuiLayout)
			{
				Pair<Integer,Object>	data	= ((GuiLayout)e).getItemData(element);
				if (data != null)
					return data;
			}
			if (item.getL() == element)
				return item.getR();
		}
		return null;
	}

	void layout(int minWidth, int minHeight, boolean reset)
	{
		
	}

}
