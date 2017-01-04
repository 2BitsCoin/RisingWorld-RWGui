/****************************
	R W G U I  -  A Rising World Java library for GUI elements.

	GuicheckBox.java - A GuiPanel sub-class implementing a check box.

	Created by : Maurizio M. Gavioli 2016-12-30

	(C) Maurizio M. Gavioli (a.k.a. Miwarre), 2016
	Licensed under the Creative Commons by-sa 3.0 license (see http://creativecommons.org/licenses/by-sa/3.0/ for details)

*****************************/

package com.vistamaresoft.rwgui;

import com.vistamaresoft.rwgui.RWGui.Pair;
import net.risingworld.api.gui.GuiElement;
import net.risingworld.api.gui.GuiImage;
import net.risingworld.api.gui.GuiLabel;
import net.risingworld.api.gui.PivotPosition;

public class GuiCheckBox extends GuiLayout
{
	public static final	int		DISABLED	= -1;
	public static final	int		UNCHECKED	= 0;
	public static final	int		CHECKED		= 1;

	//
	// FIELDS
	//
	private	GuiImage	checkBox;
	private	Object		data;
	private	int			id;
	private	GuiLabel	label;
	private boolean		radio;
	private	int			state;

	public GuiCheckBox(String text, int initialState, boolean radio, Integer id, Object data)
	{
		super(RWGui.LAYOUT_H_LEFT | RWGui.LAYOUT_V_MIDDLE);
		this.data	= data;
		this.id		= id;
		this.radio	= radio;
		// The CHECK BOX
		checkBox	= new GuiImage(0, 0, false, RWGui.BUTTON_SIZE, RWGui.BUTTON_SIZE, false);
		RWGui.setImage(checkBox, state == CHECKED ?
				(radio ? RWGui.ICN_RADIO_CHECK : RWGui.ICN_CHECK) :
				(radio ? RWGui.ICN_RADIO_UNCHECK : RWGui.ICN_UNCHECK) );
		checkBox.setPivot(PivotPosition.BottomLeft);
		addChild(checkBox);
		// The LABEL
		label	= new GuiLabel(text, RWGui.BUTTON_SIZE + RWGui.DEFAULT_PADDING,
				(RWGui.BUTTON_SIZE - RWGui.ITEM_SIZE) / 2, false);
		checkBox.setPivot(PivotPosition.BottomLeft);
		addChild(label);
		setState(initialState);
	}

	public boolean isRadio()	{ return radio; }

	public int getState()		{ return state; }

	public void setState(int newState)
	{
		state	= newState;
		RWGui.setImage(checkBox, state == CHECKED ?
				(radio ? RWGui.ICN_RADIO_CHECK : RWGui.ICN_CHECK) :
				(radio ? RWGui.ICN_RADIO_UNCHECK : RWGui.ICN_UNCHECK) );
		label.setFontColor(state == DISABLED ? RWGui.TEXT_DIM_COLOUR : RWGui.TEXT_COLOUR);
		// with radio buttons, toggle other radios in the same parent
		if (radio && state == CHECKED)
		{
			GuiElement	parent	= getParent();
			if (parent != null && parent instanceof GuiLayout)
			{
				// if parent is a layout, scan other children
				for (Pair<GuiElement,Pair<Integer,Object>>item : ((GuiLayout)parent).children)
				{
					if (item.getL() instanceof GuiCheckBox)
					{
						// if the child is a GuiCheckBox, is not a radio box and is checked
						// un-check it
						GuiCheckBox	box	= (GuiCheckBox)item.getL();
						if (!box.isRadio() && box.getState() == CHECKED)
							box.setState(UNCHECKED);
					}
				}
			}
		}
	}

	@Override
	public void layout(int minWidth, int minHeight, boolean reset)
	{
		int		fontSize	= label.getFontSize();
		int		height;
		// if label taller than check box, align check box at the middle of label font
		if (fontSize > RWGui.BUTTON_SIZE)
		{
			height	= label.getFontSize();
			checkBox.setPosition(margin, margin + (height - RWGui.BUTTON_SIZE) / 2, false);
			label.setPosition(margin + RWGui.BUTTON_SIZE + padding, 0, false);
		}
		// if check box taller than label, align label at the middle of check box
		else
		{
			height	= RWGui.BUTTON_SIZE;
			checkBox.setPosition(margin, margin, false);
			label.setPosition(margin + RWGui.BUTTON_SIZE + padding,
					margin + (RWGui.BUTTON_SIZE - fontSize) / 2, false);
		}
		// set total panel sizes
		setSize(margin * 2 + RWGui.BUTTON_SIZE + padding + RWGui.getTextWidth(label.getText(), fontSize),
				height + padding * 2, false);
	}

	@Override
	public Integer getItemId(GuiElement element)
	{
		if (state != DISABLED && (element == this || element == checkBox || element == label) )
		{
			// flip state
			setState(1 - state);
			return id;
		}
		return null;
	}

	@Override
	public Pair<Integer,Object> getItemData(GuiElement element)
	{
		Integer		myId = getItemId(element);
		if (myId != null)
			return new Pair<Integer, Object>(id, data);
		return null;
	}

	/** Overridden, does nothing */
	@Override
	public void addChild(GuiElement element)	{	}

	/** Overridden, does nothing */
	@Override
	public void addChild(GuiElement element, Integer id)	{	}

	/** Overridden, does nothing */
	@Override
	public void addChild(GuiElement element, Integer id, Object data)	{	}

	/** Overridden, does nothing */
	@Override
	public void removeChild(GuiElement element)	{	}

	/** Overridden, does nothing */
	@Override
	public GuiLayout addNewLayoutChild(int layoutType, int layoutFlags)	{ return null;	}

	/** Overridden, does nothing */
	@Override
	public GuiLayout addNewTableLayoutChild(int colNum, int rowNum, int flags)	{ return null;	}

}
