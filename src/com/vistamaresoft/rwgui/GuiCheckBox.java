/****************************
	R W G U I  -  A Rising World Java library for GUI elements.

	GuicheckBox.java - A GuiPanel sub-class implementing a check box.

	Created by : Maurizio M. Gavioli 2016-12-30

	(C) Maurizio M. Gavioli (a.k.a. Miwarre), 2016
	Licensed under the Creative Commons by-sa 3.0 license (see http://creativecommons.org/licenses/by-sa/3.0/ for details)

*****************************/

package com.vistamaresoft.rwgui;

import net.risingworld.api.gui.GuiImage;
import net.risingworld.api.gui.GuiLabel;
import net.risingworld.api.gui.PivotPosition;

public class GuiCheckBox extends GuiImage
{
	public static final	int		DISABLED	= -1;
	public static final	int		UNCHECKED	= 0;
	public static final	int		CHECKED		= 1;

	//
	// FIELDS
	//
	private	GuiLabel	label;

	public GuiCheckBox(String text, int initialState)
	{
		super(0, 0, false, RWGui.BUTTON_SIZE, RWGui.BUTTON_SIZE, false);
		RWGui.setImage(this, initialState == CHECKED ? RWGui.ICN_CHECK : RWGui.ICN_UNCHECK);
		setPivot(PivotPosition.TopLeft);
		label	= new GuiLabel(text, RWGui.BUTTON_SIZE + RWGui.DEFAULT_PADDING,
				(RWGui.BUTTON_SIZE - RWGui.ITEM_SIZE) / 2, false);
		addChild(label);
	}
}
