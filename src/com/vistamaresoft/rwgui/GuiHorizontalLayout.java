/****************************
	R W G U I  -  A Rising World Java library for GUI elements.

	GuiHorizontalLayout.java - A GuiLayout sub-class laying out its children in horizontal

	Created by : Maurizio M. Gavioli 2016-11-19

	(C) Maurizio M. Gavioli (a.k.a. Miwarre), 2016
	Licensed under the Creative Commons by-sa 3.0 license (see http://creativecommons.org/licenses/by-sa/3.0/ for details)

*****************************/

package com.vistamaresoft.rwgui;

//import java.util.ArrayList;
import com.vistamaresoft.rwgui.RWGui.Pair;
import net.risingworld.api.gui.GuiElement;
import net.risingworld.api.utils.Vector2i;

/**
	A class implementing an automatic horizontal layout in which children
	GuiElement's are placed side by side.

	The first added child is placed at the left of the layout and each
	additional child is placed at the right of the previous.
	Children are added and removed with the usual addChild(GuiElement) and
	removeChild(GuiElement) methods.
*/
public class GuiHorizontalLayout extends GuiLayout
{
	/**
		Creates an empty horizontal layout.

		The horizontal distribution of the children is controlled with the
		RWGui.LAYOUT_H_LEFT, .LAYOUT_H_CENTRE, .LAYOUT_H_RIGHT and
		.LAYOUT_H_SPREAD flags and the vertical alignment with the
		.LAYOUT_V_TOP, .LAYOUT_V_MIDDLE and .LAYOUT_V_BOTTOM flags.

		@param	flags	one of RWGui.LAYOUT_H_LEFT, .LAYOUT_H_CENTRE,
						.LAYOUT_H_RIGHT or .LAYOUT_H_SPARSE to control the
						horizontal distribution of children ORed with one of
						.LAYOUT_V_TOP, .LAYOUT_V_MIDDLE and .LAYOUT_V_BOTTOM
						to control the vertical alignment.
	*/
	public GuiHorizontalLayout(int flags)
	{
		super(flags);
	}

	/**
		Places child elements side by side from left to right.

		Usually it is not necessary to call it explicitly.
	*/
	@Override
	public void layout(int minWidth, int minHeight)
	{
		int	height	= 0;
		int	width	= 0;
		// if top down, re-layout all children before laying out this
//		if (!bottomUp)
		// scan children once to retrieve the max height and total width
			for (Pair<GuiElement,Object> item : children)
			{
				GuiElement	element			= item.getL();
				if (item.getL() instanceof GuiLayout)
					((GuiLayout)element).layout(0, 0);		// default layout flags for now
				Vector2i	elementSizes	= RWGui.getElementSizes(element);
				if (height < elementSizes.y)
					height	= elementSizes.y;
				width	+= elementSizes.x + RWGui.BORDER;
			}
		width	-= RWGui.BORDER;	// discount last right BORDER

		int		border	= RWGui.BORDER;
		int		x		= 0;
		int		y;
		if (minWidth > width)
		{
			if ( (flags & RWGui.LAYOUT_H_RIGHT) != 0)
				x	= minWidth - width;
			else if ( (flags & RWGui.LAYOUT_H_CENTRE) != 0)
				x	= (minWidth - width) / 2;
			else if ( (flags & RWGui.LAYOUT_H_SPREAD) != 0)
				border	= (minWidth - width) / (children.size() - 1);
		}
		for (Pair<GuiElement,Object> item : children)
		{
			// position the new element on the left of previous children
			Vector2i	elementSizes	= RWGui.getElementSizes(item.getL());
			y			= (flags & RWGui.LAYOUT_V_MIDDLE) != 0 ? (height + elementSizes.y) / 2 :
				( (flags & RWGui.LAYOUT_V_BOTTOM) != 0 ? height - elementSizes.y : 0);
			item.getL().setPosition(x, y, false);
			x	+= elementSizes.x + border;
		}
		setSize(width, height, false);
//		if (bottomUp && getParent() instanceof GuiLayout)
//			((GuiLayout)getParent()).layout(true);
	}

}
