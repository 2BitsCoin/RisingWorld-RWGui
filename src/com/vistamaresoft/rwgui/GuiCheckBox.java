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

/**
 * Implements a check or radio box, according to the constructor parameters.
 * <p>Both types can have three states: DISABLED, UNCHECKED and CHECKED. Unless
 * they are in the DISABLED state, they generate notifications via the callback
 * object of the GuiModalWindow they belong to.
 * <p>Both also manage state changes (CHECKED to UNCHECKED and vice versa)
 * upon click events. Radio boxes also manage unchecking other radio boxes
 * belonging to the same immediate layout parent, when changing into CHECKED.
 * If click event notification is required, which is usually the case, a
 * non-null id (and optionally data) parameter in the constructor is required.
 * Passing an id (and a data object) when adding the box to a GuiLayout, as
 * when adding other non-layout children, has no effect.
 */
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

	/**
	 * Constructs a check/radio box with text as a label, initialState as
	 * current state and click id and data.
	 * @param	text			the text of the label
	 * @param	initialState	the initial state (one of DISABLED, CHECKED or UNCHECKED)
	 * @param	radio			if true the box is a radio box, if false it is a check box
	 * @param	id				the click id notified to the callback of the
	 *							GuiModalwindow this box belongs to
	 * @param	data			the click data notified to the callback of the
	 *							GuiModalwindow this box belongs to (optional)
	 */
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
		super.addChild(checkBox, RWGui.PGUP_ID, null);	// use a dummy id to have it clickable
		checkBox.setPivot(PivotPosition.BottomLeft);
		// The LABEL
		label	= new GuiLabel(text, RWGui.BUTTON_SIZE + RWGui.DEFAULT_PADDING,
				(RWGui.BUTTON_SIZE - RWGui.ITEM_SIZE) / 2, false);
		super.addChild(label, RWGui.PGUP_ID, null);
		label.setPivot(PivotPosition.BottomLeft);
		setState(initialState);
	}

	/**	Queries the type of the box.
	 * @return	true for radio boxes and false for check boxes.
	 */
	public boolean isRadio()	{ return radio; }

	/**
	 * Queries the state of the box.
	 * @return	one of DISABLED, CHECKED or UNCHECKED.
	 */
	public int getState()		{ return state; }

	/**
	 * Sets the new state of the box. Any unrecognised state is forced to UNCHECKED.
	 * @param newState	the new state (one of DISABLED, CHECKED or UNCHECKED).
	 */
	public void setState(int newState)
	{
		if (newState < DISABLED || newState > CHECKED)
			newState	= UNCHECKED;
		if (newState == state)
			return;
		state	= newState;
		RWGui.setImage(checkBox, state == CHECKED ?
				(radio ? RWGui.ICN_RADIO_CHECK : RWGui.ICN_CHECK) :
				(radio ? RWGui.ICN_RADIO_UNCHECK : RWGui.ICN_UNCHECK) );
		label.setFontColor(state == DISABLED ? RWGui.TEXT_DIM_COLOUR : RWGui.TEXT_COLOUR);
		setClickable(newState != DISABLED);
		checkBox.setClickable(newState != DISABLED);
		label.setClickable(newState != DISABLED);
		// with radio buttons, toggle other radios in the same parent
		if (radio && newState == CHECKED)
		{
			GuiElement	parent	= getParent();
			if (parent != null && parent instanceof GuiLayout)
			{
				// if parent is a layout, scan other children
				for (Pair<GuiElement,Pair<Integer,Object>>item : ((GuiLayout)parent).children)
				{
					if (item.getL() instanceof GuiCheckBox)
					{
						// if the child is a GuiCheckBox, is a radio box and is checked, un-check it
						GuiCheckBox	box	= (GuiCheckBox)item.getL();
						if (box.isRadio() && box.getState() == CHECKED && box != this)
							box.setState(UNCHECKED);
					}
				}
			}
		}
	}

	/**
	 * Places the image and the label in the containing panel and sets the
	 * panel sizes.
	 */
	@Override
	void layout(int minWidth, int minHeight, boolean reset)
	{
		int		fontSize	= label.getFontSize();
		int		height;
		// if label taller than check box, align check box at the middle of label font
		if (fontSize > RWGui.BUTTON_SIZE)
		{
			height	= label.getFontSize();
			checkBox.setPosition(margin, margin + (height - RWGui.BUTTON_SIZE) / 2, false);
			label.setPosition(margin + RWGui.BUTTON_SIZE + padding, margin, false);
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
				height + margin * 2, false);
	}

	/**
	 * Returns the id associated with the GuiCheckBox if element is any of the
	 * GuiElement making it up and the state is not DISABLED.
	 * 
	 * @param	element	the GuiElement to look for.
	 * @return	the id associated with the GuiCheckBox if element
	 *			belongs to it, null if does not.
	 */
	@Override
	public Integer getItemId(GuiElement element)
	{
		if (state != DISABLED && (element == this || element == checkBox || element == label) )
		{
			// set RADIO to CHECKED or FLIP CHECKBOX state
			setState(radio ? CHECKED : 1 - state);
			return id;
		}
		return null;
	}

	/**
	 * Returns the id and data pair associated with the GuiCheckBox if element
	 * is any of the GuiElement making it up and the state is not DISABLED.
	 * 
	 * @param	element	the GuiElement to look for.
	 * @return	the id and data pair associated with the GuiCheckBox if element
	 *			belongs to it, null if does not.
	 */
	@Override
	public Pair<Integer,Object> getItemData(GuiElement element)
	{
		Integer		myId = getItemId(element);
		if (myId != null)
			return new Pair<Integer, Object>(myId, data);
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
