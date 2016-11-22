/****************************
	R W G U I  -  A Rising World Java library for GUI elements.

	GuiTitleBar.java - Implements a common title bar for all windows.

	Created by : Maurizio M. Gavioli 2016-11-09

	(C) Maurizio M. Gavioli (a.k.a. Miwarre), 2016
	Licensed under the Creative Commons by-sa 3.0 license (see http://creativecommons.org/licenses/by-sa/3.0/ for details)

*****************************/

package com.vistamaresoft.rwgui;

import net.risingworld.api.gui.GuiElement;
import net.risingworld.api.gui.GuiImage;
import net.risingworld.api.gui.GuiLabel;
import net.risingworld.api.gui.GuiPanel;
import net.risingworld.api.gui.PivotPosition;
import net.risingworld.api.objects.Player;

final class GuiTitleBar extends GuiPanel
{
	public static final		int		TITLEBAR_HEIGHT	= RWGui.TITLE_SIZE + RWGui.BORDER*2;
	private static final	int		CANCEL_YPOS		= TITLEBAR_HEIGHT - RWGui.BORDER;
	private static final	int		TITLE_XPOS		= RWGui.BORDER;
	private static final	int		TITLE_YPOS		= TITLEBAR_HEIGHT - RWGui.BORDER;

	private	GuiImage	cancelButton;
	private	GuiElement	parent;
	private GuiLabel	title;

	public GuiTitleBar(GuiElement parent, String titleText, boolean hasCancelButton)
	{
		super();
		setColor(RWGui.TITLEBAR_COLOUR);
		setPivot(PivotPosition.TopLeft);
		parent.addChild(this);
		this.parent	= parent;

		title	= new GuiLabel(titleText, TITLE_XPOS, TITLE_YPOS, false);
		title.setPivot(PivotPosition.TopLeft);
		title.setText(titleText);
		title.setFontSize(RWGui.TITLE_SIZE);
		title.setFontColor(RWGui.TITLE_COLOUR);
		addChild(title);

		if (hasCancelButton)
		{
			cancelButton	= new GuiImage(0, 0, false, RWGui.BUTTON_SIZE, RWGui.BUTTON_SIZE, false);
			RWGui.setImage(cancelButton, RWGui.ICN_CROSS);
			cancelButton.setPivot(PivotPosition.TopLeft);
			cancelButton.setClickable(true);
			cancelButton.setVisible(true);
			addChild(cancelButton);
		}
	}

	protected boolean isCancelButton(GuiElement element)
	{
		return (element == cancelButton);
	}

	protected String getTitleText()
	{
		return title.getText();
	}

	protected void free()
	{
		removeChild(title);
		title			= null;
		if (cancelButton != null)
		{
			removeChild(cancelButton);
			cancelButton	= null;
		}
		removeFromParent();
	}

	protected void relayout()
	{
		if (parent == null)
			return;
		int	parentHeight	= (int)parent.getHeight();
		int	parentWidth		= (int)parent.getWidth();
		setPosition(RWGui.BORDER_THICKNESS, parentHeight-RWGui.BORDER_THICKNESS, false);
		setSize(parentWidth - RWGui.BORDER_THICKNESS*2, TITLEBAR_HEIGHT, false);
		if (cancelButton != null)
			cancelButton.setPosition(parentWidth - (RWGui.BORDER + RWGui.BUTTON_SIZE), CANCEL_YPOS, false);
	}

	protected void addToPlayer(Player player)
	{
		player.addGuiElement(this);
		player.addGuiElement(title);
		if (cancelButton != null)
			player.addGuiElement(cancelButton);
	}

	protected void removeFromPlayer(Player player)
	{
		player.removeGuiElement(this);
		player.removeGuiElement(title);
		if (cancelButton != null)
			player.removeGuiElement(cancelButton);
	}

}
