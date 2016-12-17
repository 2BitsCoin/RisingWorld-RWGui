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

/**
 * Implements a modal message box. Each message box is made of a title bar,
 * with a title and a close button, and a number of text lines.
 * <p>The message box manages its own event Listener; it also turns the mouse
 * cursor on on display and off on hiding.
 * <p>The message box manages the close button in the title bar, hiding the box
 * from the player screen and turning off the mouse cursor.
 * <p>This message box is practically "fire-and-forget": once it is shown, the
 * player can only read it and then click on the close button to dismiss it.
 * <p><b>Important</b>: due to the way Rising World plug-ins are loaded,
 * <b>this class cannot instantiated or used in any way</b> from within the onEnable()
 * method of a plug-in, as it is impossible to be sure that, at that moment,
 * the RWGui plug-in has already been loaded.
 * <p>The first moment one can be sure that all plug-ins have been loaded, and
 * it is safe to use this class, is when (or after) the first player connects
 * to the server (either dedicated or local).
 */
public class GuiMessageBox extends GuiPanel implements Listener
{
	private static final	int		ITEM_HEIGHT	= RWGui.ITEM_SIZE + RWGui.DEFAULT_PADDING;

	private	MBThread	mbThread;
	private	Plugin		plugin;
	private	GuiTitleBar	titleBar;
	private	GuiLabel[]	textLbl;

	/**
	 * Creates a new GuiMesageBox.
	 * <p>The message box adapts its vertical and horizontal sizes to the number
	 * and length of the text strings.
	 * <p>As the underlying Rising World API does not support clipping or
	 * reformatting a fluent text, the text to display has to be broken into
	 * separate strings of convenient length, to avoid the message having an
	 * excessive width.
	 * @param	plugin	the plug-in the GuiMessageBox is intended for. This
	 * 					is only needed to manage the internal event listener
	 * 					and has no effects on the plug-in itself.
	 * @param	player	the player to show the message box to.
	 * @param	title	the text of the title.
	 * @param	texts	an array of String's with the text to display.
	 * @param	delay	a timed duration of the message box in seconds; once
	 * 					this time elapses, the message box closes down
	 * 					automatically. Use 0 for a non-closing box.
	 */
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
		int			height	= GuiTitleBar.TITLEBAR_HEIGHT + RWGui.DEFAULT_PADDING + ITEM_HEIGHT*texts.length;
		int			y		= height - (GuiTitleBar.TITLEBAR_HEIGHT + RWGui.DEFAULT_PADDING + RWGui.ITEM_SIZE);
		int			width	= (int)RWGui.getTextWidth(title, RWGui.TITLE_SIZE);
		textLbl				= new GuiLabel[texts.length];
		for (int i = 0; i < texts.length; i++)
		{
			int		textW	= (int)RWGui.getTextWidth(texts[i], RWGui.ITEM_SIZE);
			if (width < textW)
				width		= textW;
			textLbl[i]		= new GuiLabel(texts[i], RWGui.DEFAULT_PADDING, y, false);
			textLbl[i].setFontSize(RWGui.ITEM_SIZE);
			addChild(textLbl[i]);
			player.addGuiElement(textLbl[i]);
		}
		setSize(width, height + RWGui.DEFAULT_PADDING*2, false);
		titleBar.relayout();
		titleBar.addToPlayer(player);
		player.addGuiElement(this);
		plugin.registerEventListener(this);
		player.setMouseCursorVisible(true);
		if (delay > 0)
		{
			mbThread	= new MBThread(this, player, delay);
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
		titleBar.removeFromPlayer(player);
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
