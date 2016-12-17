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
	called; setting those position manually has no effect.
 * <p><b>Important</b>: due to the way Rising World plug-ins are loaded,
 * <b>this class cannot instantiated or used in any way</b> from within the onEnable()
 * method of a plug-in, as it is impossible to be sure that, at that moment,
 * the RWGui plug-in has already been loaded.
 * <p>The first moment one can be sure that all plug-ins have been loaded, and
 * it is safe to use this class, is when (or after) the first player connects
 * to the server (either dedicated or local).
*/
public class GuiHorizontalLayout extends GuiLayout
{
	/**
		Creates an empty horizontal layout.

		The horizontal distribution of the children is controlled with the
		RWGui.LAYOUT_H_LEFT, LAYOUT_H_CENTRE, LAYOUT_H_RIGHT and
		LAYOUT_H_SPREAD flags and the vertical alignment with the
		LAYOUT_V_TOP, LAYOUT_V_MIDDLE and LAYOUT_V_BOTTOM flags.

		@param	flags	one of RWGui.LAYOUT_H_LEFT, LAYOUT_H_CENTRE,
						LAYOUT_H_RIGHT or LAYOUT_H_SPARSE to control the
						horizontal distribution of children ORed with one of
						LAYOUT_V_TOP, LAYOUT_V_MIDDLE and LAYOUT_V_BOTTOM
						to control the vertical alignment.
	*/
	public GuiHorizontalLayout(int flags)
	{
		super(flags);
	}

	/**
		Places child elements side by side from left to right.

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
			minHeight	= height	= 0;
			minWidth	= width		= 0;
		}
		else
		{
			width		= 0;
//			minWidth	= (int)getWidth();
			height		= (int)getHeight() - margin*2;	// the height within which to fit children
		}												// excludes top and bottom margin
		int		count	= 0;
		for (Pair<GuiElement,?> item : children)
		{
			GuiElement	element			= item.getL();
			if (element instanceof GuiLayout)
				((GuiLayout)element).layout(0, height, reset);
			elemSizes[count]	= RWGui.getElementSizes(element);
			if (height < elemSizes[count].y)
				height	= elemSizes[count].y;
			width	+= elemSizes[count].x + padding;
			count++;
		}
		height	+=	margin * 2;				// add back top and bottom margin
		width	+= margin * 2 - padding;	// add left and right margin and discount last right padding

		int		spacing	= padding;
		int		x		= margin;
		int		y;
		if (minWidth > width)
		{
			if ( (flags & RWGui.LAYOUT_H_RIGHT) != 0)
				x	= minWidth - width;
			else if ( (flags & RWGui.LAYOUT_H_CENTRE) != 0)
				x	= (minWidth - width) / 2;
			else if ( (flags & RWGui.LAYOUT_H_SPREAD) != 0)
				spacing	= (minWidth - width) / (children.size() - 1);
			width	= minWidth;
		}
		count	= 0;
		for (Pair<GuiElement,?> item : children)
		{
			// position the next element on the left of previous children
			Vector2i	elementSizes	= elemSizes[count];
			y			= (flags & RWGui.LAYOUT_V_MIDDLE) != 0 ? (height + elementSizes.y) / 2 :
				( (flags & RWGui.LAYOUT_V_BOTTOM) != 0 ? height - margin - elementSizes.y : height - margin);
			item.getL().setPosition(x, y, false);
			x	+= elementSizes.x + spacing;
			count++;
		}
		setSize(width, height, false);
	}

}
