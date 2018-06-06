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
import net.risingworld.api.events.player.gui.PlayerGuiElementClickEvent;
import net.risingworld.api.gui.GuiLabel;
import net.risingworld.api.objects.Player;

/**
 * Implements a modal message box. Each message box is made of a title bar,
 * with a title and a close button, and a number of text lines.
 * <p>The message box manages its own event Listener; it also turns the mouse
 * cursor on on display and off on hiding.
 * <p>GuiMessageBox inherits all GuiModalWindow behaviours.
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
public class GuiMessageBox extends GuiModalWindow	//GuiPanel implements Listener
{
	private	MBThread	mbThread;

	/**
	 * Creates a new GuiMessageBox.
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
		super(plugin, title, RWGui.LAYOUT_VERT, null);
		for (String text : texts)
		{
			addChild(new GuiLabel(text, 0, 0, false));
		}
		if (delay > 0)
		{
			mbThread	= new MBThread(this, player, delay);
			mbThread.start();
		}
	}

	/**
	 * Overloaded constructor; similar to other constructor, but accepts a single
	 * line of text as contents.
	 * @param	plugin	the plug-in the GuiMessageBox is intended for. This
	 * 					is only needed to manage the internal event listener
	 * 					and has no effects on the plug-in itself.
	 * @param	player	the player to show the message box to.
	 * @param	title	the text of the title.
	 * @param	text	a String with the text to display.
	 * @param	delay	a timed duration of the message box in seconds; once
	 * 					this time elapses, the message box closes down
	 * 					automatically. Use 0 for a non-closing box.
	 */
	public GuiMessageBox(Plugin plugin, Player player, String title, String text, int delay)
	{
		this(plugin, player, title, new String[]{text}, delay);
	}

	/**
	 *
	 * @param event
	 */
	@EventMethod
	@Override
	public void onClick(PlayerGuiElementClickEvent event)
	{
		if (titleBar.isCancelButton(event.getGuiElement()))
		{
			if (mbThread != null)
				mbThread.interrupt();
			pop(event.getPlayer());
		}
	}

	private static class MBThread extends Thread
	{
		private final	int				delaySecs;
		private final	GuiMessageBox	messageBox;
		private final	Player			player;

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
			messageBox.pop(player);
		}
	}
}
