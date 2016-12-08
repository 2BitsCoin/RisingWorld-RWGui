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
	Children are added and removed with the usual addChild(GuiElement) and
	removeChild(GuiElement) methods.
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

		Usually it is not necessary to call it explicitly.
	*/
	@Override
	public void layout(int minWidth, int minHeight)
	{
		int	height	= 0;
		int	width	= 0;
		// if top down, re-layout all children before laying out this
//		if (!bottomUp)
		// scan children once to retrieve the total height
			for (Pair<GuiElement,Object> item : children)
			{
				GuiElement	element			= item.getL();
				if (item.getL() instanceof GuiLayout)
					((GuiLayout)element).layout(0, 0);		// default layout flags for now
				Vector2i	elementSizes	= RWGui.getElementSizes(element);
				height	+= elementSizes.y + RWGui.BORDER;
				if (width < elementSizes.x)
					width	= elementSizes.x;
			}
		height	-= RWGui.BORDER;		// discount last bottom BORDER

		int	x;
		int	y		= height;
		for (Pair<GuiElement,Object> item : children)
		{
			// position the new element below previous children
			Vector2i	elementSizes	= RWGui.getElementSizes(item.getL());
			x				= (flags & RWGui.LAYOUT_H_CENTRE) != 0 ? (width - elementSizes.x) / 2 :
				( (flags & RWGui.LAYOUT_H_RIGHT) != 0 ? width - elementSizes.x : 0);
			item.getL().setPosition(x, y, false);
			y	-= elementSizes.y + RWGui.BORDER;
		}
		setSize(width, height, false);
//		if (bottomUp && getParent() instanceof GuiLayout)
//			((GuiLayout)getParent()).layout(true);
	}

}
