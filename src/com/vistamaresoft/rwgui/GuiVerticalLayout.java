/****************************
	R W G U I  -  A Rising World Java library for GUI elements.

	GuiVerticalLayout.java - Implements automatic vertical layout of GuiElement's.

	Created by : Maurizio M. Gavioli 2016-11-19

	(C) Maurizio M. Gavioli (a.k.a. Miwarre), 2016
	Licensed under the Creative Commons by-sa 3.0 license (see http://creativecommons.org/licenses/by-sa/3.0/ for details)

*****************************/

package com.vistamaresoft.rwgui;

import com.vistamaresoft.rwgui.RWGui.Pair;
import net.risingworld.api.gui.GuiElement;
import net.risingworld.api.gui.GuiImage;
import net.risingworld.api.gui.PivotPosition;
import net.risingworld.api.objects.Player;
import net.risingworld.api.utils.Vector2i;

/**
	A class implementing an automatic vertical layout in which children
	GuiElement's are stacked one below the other.

	The first added child is placed at the top of the layout and each
	additional child is stacked below the previous.
	The layout automatically grows or shrinks to fit the children.
	<p>Children are added and removed with the usual addChild(GuiElement) and
	removeChild(GuiElement) methods.
	<p>A maximum number of visible items can be set (default is unlimited),
	then the layout will only show as many items; if there are more items an
	up arrow button and a down arrow button allow to scroll the full item list.
	<b>Note</b>: For this to work correctly, all items shall have the same
	height; if different items have different height, the layout is likely to turn
	out wrong.
	<p>The layout sets the font size of GuiLabel's, the border thickness and
	the background of GuiTextField's as well as the clickable status of all
	children when each child is added to the layout. These properties, as well
	as other visual properties, can be changed after adding the child.
	<p>The layout also sets the pivot position of each child on adding it;
	changing it is possible, but it is likely to disrupt the proper child
	placement within the layout.
	<p>The layout sets the position of each child each time the layout() is
	called. Setting those position manually has no effect.
 * <p><b>Important</b>: due to the way Rising World plug-ins are loaded,
 * <b>this class cannot be instantiated or used in any way</b> from within the onEnable()
 * method of a plug-in, as it is impossible to be sure that, at that moment,
 * the RWGui plug-in has already been loaded.
 * <p>The first moment one can be sure that all plug-ins have been loaded, and
 * it is safe to use this class, is when (or after) the first player connects
 * to the server (either dedicated or local).
*/
public class GuiVerticalLayout extends GuiLayout
{
	private	int			maxVisibleRows;
	private GuiImage	buttonNext;
	private GuiImage	buttonPrev;
	private	int			firstItem;			// the index of the first shown menu item in the list of
											// all the items;
	private	int			numOfVisibleRows;
	private	int			visibleRowsHeight;	// the total height of the visible rows, not adjusted for minHeight
	private	int			visibleRowsSpacing;
	private	int			visibleRowsTop;
	private int			visibleRowsWidth;

	/**
	Creates an empty vertical layout.

	The vertical distribution of the children is controlled with the
	.LAYOUT_V_TOP, .LAYOUT_V_MIDDLE, .LAYOUT_V_BOTTOM and .LAYOUT_V_SPREAD
	flags and the horizontal alignment with the .LAYOUT_H_LEFT,
	.LAYOUT_H_CENTRE and .LAYOUT_RIGHT flags.

	@param	flags	one of .LAYOUT_V_TOP, .LAYOUT_V_MIDDLE and .LAYOUT_V_BOTTOM
					or .LAYOUT_V_SPARSE to control the vertical distribution of
					children ORed with one of RWGui.LAYOUT_H_LEFT,
					.LAYOUT_H_CENTRE or .LAYOUT_H_RIGHT to control the vertical
					alignment.
	*/
	public GuiVerticalLayout(int flags)
	{
		super(flags);
		firstItem	= 0;
		maxVisibleRows = Integer.MAX_VALUE;
		buttonNext	= new GuiImage(0, 0, false, RWGui.BUTTON_SIZE, RWGui.BUTTON_SIZE, false);
		RWGui.setImage(buttonNext, RWGui.ICN_ARROW_DOWN);
		buttonNext.setPivot(PivotPosition.BottomLeft);
		buttonNext.setClickable(true);
		buttonNext.setVisible(false);
		baseAddChild(buttonNext);
		buttonPrev	= new GuiImage(0, 0, false, RWGui.BUTTON_SIZE, RWGui.BUTTON_SIZE, false);
		RWGui.setImage(buttonPrev, RWGui.ICN_ARROW_UP);
		buttonPrev.setPivot(PivotPosition.TopLeft);
		buttonPrev.setClickable(true);
		buttonPrev.setVisible(false);
		baseAddChild(buttonPrev);
	}

	/**
	 * Sets the maximum number of visible items.
	 * If the layout contains more items, the item list will become scrollable.
	 * By default this number is unlimited and the layout will grow
	 * indefinitely according to the number of children added to it.
	 * 
	 * @param newMaxVisibleRows	the new maximum number of visible items.
	 */
	public void setMaxVisibleRows(int newMaxVisibleRows)
	{
		if (newMaxVisibleRows < 1)
			newMaxVisibleRows	= Integer.MAX_VALUE;
		maxVisibleRows	= newMaxVisibleRows;
	}

	/**
	 * Returns the current maximum number of visible items.
	 */
	public int getMaxVisibleRows()
	{
		return maxVisibleRows;
	}

	/**
	 * Returns the id associated with element, if element is one of the
	 * children of the layout (recursively); or null otherwise.

	 * @param	element	the GuiElement to look for.
	 * @return	the id associated with element if present, null if not.
	 */
	@Override
	public Integer getItemId(GuiElement element)
	{
		Pair<Integer,Object>	myData	= getItemData(element);
		return myData.getL();
	}

	/**
	 * Returns the id and data pair associated with element, if element is one
	 * of the children of the layout (recursively); or null otherwise.

	 * @param	element	the GuiElement to look for.
	 * @return	the id and data pair associated with element if present,
	 * 			null if not.
	 */
	@Override
	public Pair<Integer,Object> getItemData(GuiElement element)
	{
		if (element == buttonPrev)
		{
			scrollUp();
			return new Pair<Integer, Object>(RWGui.PGUP_ID, null);
		}
		if (element == buttonNext)
		{
			scrollDown();
			return new Pair<Integer, Object>(RWGui.PGDN_ID, null);
		}
		return super.getItemData(element);
	}

	/**
	 * Adds a GuiElement with the associated id and data as a direct child of
	 * the layout. The element is positioned at the bottom of the layout.
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
	@Override
	public void addChild(GuiElement element, Integer id, Object data)
	{
		super.addChild(element, id, data);
		if (numOfVisibleRows < maxVisibleRows)
			numOfVisibleRows++;
	}

	/**
	 * Removes a GuiElement from the direct children of the layout.
	 * 
	 * If the element is not a direct child of the layout, the method does
	 * nothing.
	 * @param	element	The GuiElement to remove
	 */
	@Override
	public void removeChild(GuiElement element)
	{
		super.removeChild(element);
		if (numOfVisibleRows <= maxVisibleRows)
			numOfVisibleRows--;
	}

	/**
	 * Removes the itemIndex-th direct child of the layout.
	 * 
	 * If the element is not a direct child of the layout, the method does
	 * nothing.
	 * @param	element	The GuiElement to remove
	 */
	public int removeChild(int itemIndex)
	{
		if (itemIndex < 0 || itemIndex >= children.size())
			return RWGui.ERR_INVALID_PARAMETER;
		removeChild(children.get(itemIndex).getL());
		return itemIndex;
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
	@Override
	public void free()
	{
		baseRemoveChild(buttonNext);
		baseRemoveChild(buttonPrev);
		super.free();
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
	@Override
	public void hide(Player player)
	{
		player.removeGuiElement(buttonNext);
		player.removeGuiElement(buttonPrev);
		super.hide(player);
	}

	/**
	 * Displays the layout on the player screen.
	 * 
	 * It is necessary to call this method only for the top layout of a
	 * hierarchy and only if it not part of a managed element (like
	 * GuiDialogueBox).
	 * 
	 * @param	player	the player to show the layout to.
	 */
	@Override
	public void show(Player player)
	{
		player.addGuiElement(buttonNext);
		player.addGuiElement(buttonPrev);
		super.show(player);
	}

	/**
		Places child elements one above the other from the top down.

		As this method lays its children out recursively, it is usually
		necessary to call this method manually only for the top layout of a
		layout hierarchy.
	*/
	@Override
	void layout(int minWidth, int minHeight, boolean reset)
	{
		int		height, width;
		if (children == null || children.size() == 0)
			return;
		if (reset)
		{
			Vector2i	elemSizes;
			width				= 0;
			height				= minHeight	= 0;
			int			count	= 0;
			for (Pair<GuiElement,?> item : children)
			{
				GuiElement	element			= item.getL();
				if (element instanceof GuiLayout)
					((GuiLayout)element).layout(width, 0, reset);
				elemSizes	= RWGui.getElementSizes(element);
				if (width < elemSizes.x)
					width	= elemSizes.x;
				if (count >= firstItem && count < firstItem + maxVisibleRows)
				{
					height	+= elemSizes.y + padding;
				}
				count++;
			}
			height	-= padding;	// discount last bottom padding
			visibleRowsHeight	= height;
			visibleRowsWidth	= width;
			visibleRowsSpacing	= padding;			// default spacing
			visibleRowsTop		= height + margin;	// include bottom margin
			width				+= margin;			// add left margin
			height				+= margin * 2;		// add top and bottom margin
		}
		else
		{
			for (Pair<GuiElement,?> item : children)
			{
				GuiElement	element			= item.getL();
				if (element instanceof GuiLayout)
					((GuiLayout)element).layout(visibleRowsWidth, 0, reset);
			}
			width		= visibleRowsWidth + margin;			// add left margin
			height		= visibleRowsHeight + margin * 2;		// add top and bottom margin
			visibleRowsSpacing	= padding;						// default spacing
			visibleRowsTop		= height - margin;				// top row clears top margin
			// if required minHeight > computed height, place excess vertical space
			// according to arrangement flags
			if (minHeight > height)
			{
				if ( (flags & RWGui.LAYOUT_V_BOTTOM) != 0)		// BOTTOM: all space at top
					visibleRowsTop	-= minHeight - height;
				else if ( (flags & RWGui.LAYOUT_V_MIDDLE) != 0)	// MIDDLE: half space above and half below
					visibleRowsTop	-= (minHeight - height) / 2;
				else if ( (flags & RWGui.LAYOUT_V_SPREAD) != 0)	// SPREAD: distribute evenly 
				{
					visibleRowsTop = minHeight - margin;
					visibleRowsSpacing	= padding + (minHeight - height) / (children.size() - 1);
				}
				height	= minHeight;
			}
		}

		// add UP/DOWN button width, if required
		if (children.size() > maxVisibleRows)
		{
			width += padding;									// align width at items right edge
			buttonPrev.setPosition(width, visibleRowsTop, false);	// position the buttons
			buttonNext.setPosition(width, margin, false);
			width += RWGui.BUTTON_SIZE;							// add width for arrow buttons
		}
		width	+= margin;										// add right margin
		if (width < minWidth)
			width = minWidth;
		setSize(width, height, false);
		updateChildren();
	}

	//********************
	// PRIVATE HELPER METHODS
	//********************

	private void scrollDown()
	{
		firstItem	+= maxVisibleRows-1;
		if (firstItem + maxVisibleRows > children.size())
			firstItem	= children.size() - maxVisibleRows;
		updateChildren();
	}

	private void scrollUp()
	{
		firstItem	-= maxVisibleRows-1;
		if (firstItem < 0)
			firstItem	= 0;
		updateChildren();
	}

	private void updateChildren()
	{
		int	count	= 0;
		int	x;
		int	y		= visibleRowsTop;
		int	yDelta; 
		for (Pair<GuiElement,?> item : children)
		{
			GuiElement	element	= item.getL();
			if (count < firstItem || count >= firstItem + maxVisibleRows)
				element.setVisible(false);
			else
			{
				boolean	centred	= (flags & RWGui.LAYOUT_H_CENTRE) != 0;
				int		height	= RWGui.getElementSizes(element).y;
				// position the next element below previous children
				x	= margin + (centred ? visibleRowsWidth / 2 :
					( (flags & RWGui.LAYOUT_H_RIGHT) != 0 ? visibleRowsWidth : 0) );
				yDelta	= centred ? -(int)(height / 2) : 0;
				element.setPosition(x, y+yDelta, false);
				element.setVisible(true);
				y	-= height + visibleRowsSpacing;
			}
			count++;
		}
		buttonPrev.setVisible(firstItem > 0);
		buttonNext.setVisible(firstItem + maxVisibleRows < children.size());
	}

}
