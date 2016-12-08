/****************************
	R W G U I  -  A Rising World Java library for GUI elements.

	GuiMessageBox.java - A GuiPanel sub-class implementing a message box.

	Created by : Maurizio M. Gavioli 2016-12-04

	(C) Maurizio M. Gavioli (a.k.a. Miwarre), 2016
	Licensed under the Creative Commons by-sa 3.0 license (see http://creativecommons.org/licenses/by-sa/3.0/ for details)

*****************************/

package com.vistamaresoft.rwgui;

import net.risingworld.api.Plugin;
import net.risingworld.api.events.EventMethod;
import net.risingworld.api.events.Listener;
import net.risingworld.api.events.player.gui.PlayerGuiElementClickEvent;
import net.risingworld.api.gui.GuiLabel;
import net.risingworld.api.gui.GuiPanel;
import net.risingworld.api.gui.PivotPosition;
import net.risingworld.api.objects.Player;

public class GuiMessageBox extends GuiPanel implements Listener
{
	private static final	int		ITEM_HEIGHT	= RWGui.ITEM_SIZE + RWGui.BORDER;

	private	MBThread	mbThread;
	private	Plugin		plugin;
	private	GuiTitleBar	titleBar;
	private	GuiLabel[]	textLbl;

	public GuiMessageBox(Plugin plugin, Player player, String title, String[] texts, int delay)
	{
		super();
		this.plugin	= plugin;
		setPivot(PivotPosition.Center);
		setPosition(0.5f, 0.5f, true);
		setBorderColor(RWGui.BORDER_COLOUR);
		setBorderThickness(RWGui.BORDER_THICKNESS, false);
		setColor(RWGui.PANEL_COLOUR);
		titleBar	= new GuiTitleBar(this, title, true);
		int			height	= GuiTitleBar.TITLEBAR_HEIGHT + RWGui.BORDER + ITEM_HEIGHT*texts.length;
		int			y		= height - (GuiTitleBar.TITLEBAR_HEIGHT + RWGui.BORDER + RWGui.ITEM_SIZE);
		int			width	= (int)RWGui.getTextWidth(title, RWGui.TITLE_SIZE);
		textLbl				= new GuiLabel[texts.length];
		for (int i = 0; i < texts.length; i++)
		{
			int		textW	= (int)RWGui.getTextWidth(texts[i], RWGui.ITEM_SIZE);
			if (width < textW)
				width		= textW;
			textLbl[i]		= new GuiLabel(texts[i], RWGui.BORDER, y, false);
			textLbl[i].setFontSize(RWGui.ITEM_SIZE);
			addChild(textLbl[i]);
			player.addGuiElement(textLbl[i]);
		}
		setSize(width, height + RWGui.BORDER*2, false);
		titleBar.relayout();
		titleBar.addToPlayer(player);
		player.addGuiElement(this);
		plugin.registerEventListener(this);
		player.setMouseCursorVisible(true);
		if (delay > 0)
		{
			/*MBThread*/	mbThread	= new MBThread(this, player, delay);
			mbThread.start();
		}
	}

	@EventMethod
	public void onClick(PlayerGuiElementClickEvent event)
	{
		if (titleBar.isCancelButton(event.getGuiElement()))
		{
			if (mbThread != null)
				mbThread.interrupt();
			free(event.getPlayer());
			return;
		}
	}

	private void free(Player player)
	{
		plugin.unregisterEventListener(this);
		for (int i = 0; i < textLbl.length; i++)
		{
			removeChild(textLbl[i]);
			player.removeGuiElement(textLbl[i]);
			textLbl[i]	= null;
		}
		titleBar.free();
		titleBar	= null;
		player.removeGuiElement(this);
		player.setMouseCursorVisible(false);
	}

	private static class MBThread extends Thread
	{
		private int				delaySecs;
		private	GuiMessageBox	messageBox;
		private	Player			player;

		public MBThread(GuiMessageBox messageBox, Player player, int delaySecs)
		{
			this.delaySecs	= delaySecs;
			this.messageBox	= messageBox;
			this.player		= player;
		}

		@Override
		public void run()
		{
			try
			{
				sleep(delaySecs * 1000);
			} catch (InterruptedException e)
			{
				return;
			}
			messageBox.free(player);
		}
	}
}
