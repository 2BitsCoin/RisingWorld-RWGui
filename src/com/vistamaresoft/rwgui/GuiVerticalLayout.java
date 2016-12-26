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
import net.risingworld.api.utils.Vector2i;

/**
	A class implementing an automatic vertical layout in which children
	GuiElement's are stacked one below the other.

	The first added child is placed at the top of the layout and each
	additional child is stacked below the previous.
	The layout automatically grows or shrinks to fit the children.
	<p>Children are added and removed with the usual addChild(GuiElement) and
	removeChild(GuiElement) methods.
	<p>The layout sets the font size of GuiLabel's, the border thickness and
	the background of GuiTextField's as well as the clickable status of all
	children when each child is added to the layout. These properties, as well
	as other visual properties, can be changed after adding the child.
	<p>The layout also sets the pivot position of each child on adding it;
	changing it is possible, but it is likely to disrupt the proper child
	placement within the layout.
	<p>The layout sets the position of each child each time the layout() is
	called. setting those position manually has no effect.
 * <p><b>Important</b>: due to the way Rising World plug-ins are loaded,
 * <b>this class cannot instantiated or used in any way</b> from within the onEnable()
 * method of a plug-in, as it is impossible to be sure that, at that moment,
 * the RWGui plug-in has already been loaded.
 * <p>The first moment one can be sure that all plug-ins have been loaded, and
 * it is safe to use this class, is when (or after) the first player connects
 * to the server (either dedicated or local).
*/
public class GuiVerticalLayout extends GuiLayout
{
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
	}

	/**
		Places child elements one above the other from the bottom up.

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
		Vector2i[]	elemSizes	= new Vector2i[children.size()];
		if (reset)
		{
			width	= minWidth;
			height	= minHeight	= 0;
		}
		else
		{
			width		= (int)getWidth() - margin*2;	// the width within which to fit children
			height		= 0;
		}												// excludes left and right margin
		int		count	= 0;
		for (Pair<GuiElement,?> item : children)
		{
			GuiElement	element			= item.getL();
			if (element instanceof GuiLayout)
				((GuiLayout)element).layout(width, 0, reset);
			elemSizes[count]	= RWGui.getElementSizes(element);
			if (width < elemSizes[count].x)
				width	= elemSizes[count].x;
			height	+= elemSizes[count].y + padding;
			count++;
		}
		height	+= margin * 2 - padding;	// add top and bottom margin and discount last bottom padding
		width	+=	margin * 2;				// add back left and right margin

		int		spacing	= padding;
		int		x		= 0;
		int		y		= height - margin;
		if (minHeight > height)
		{
			if ( (flags & RWGui.LAYOUT_V_BOTTOM) != 0)
				y	-= minHeight - height;
			else if ( (flags & RWGui.LAYOUT_V_MIDDLE) != 0)
				y	-= (minHeight - height) / 2;
			else if ( (flags & RWGui.LAYOUT_V_SPREAD) != 0)
			{
				y = minHeight - margin;
				spacing	= padding + (minHeight - height) / (children.size() - 1);
			}
			height	= minHeight;
		}
		count	= 0;
		for (Pair<GuiElement,?> item : children)
		{
			// position the next element below previous children
			Vector2i	elementSizes	= elemSizes[count];
			x			= (flags & RWGui.LAYOUT_H_CENTRE) != 0 ? (width - elementSizes.x) / 2 :
				( (flags & RWGui.LAYOUT_H_RIGHT) != 0 ? width - margin - elementSizes.x : margin);
			item.getL().setPosition(x, y, false);
			y	-= elementSizes.y + spacing;
			count++;
		}
		setSize(width, height, false);
	}

}
