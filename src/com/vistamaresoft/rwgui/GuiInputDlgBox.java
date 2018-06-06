/****************************
	R W G U I  -  A Rising World Java library for GUI elements.

	GuiInputDlgBox.java - A GuiPanel sub-class implementing a single-line input dialogue box.

	Created by : Maurizio M. Gavioli 2017-01-21

	(C) Maurizio M. Gavioli (a.k.a. Miwarre), 2016
	Licensed under the Creative Commons by-sa 3.0 license (see http://creativecommons.org/licenses/by-sa/3.0/ for details)

*****************************/

package com.vistamaresoft.rwgui;

import com.vistamaresoft.rwgui.RWGui.RWGuiCallback;
import net.risingworld.api.Plugin;
import net.risingworld.api.gui.GuiLabel;
import net.risingworld.api.gui.GuiTextField;
import net.risingworld.api.objects.Player;

/**
 * A class implementing a simple, one-line input box.
 * <p>The GuiInputDlgBox has a title, a caption string and an input box to enter
 * a single numeric or textual value.
 * <p>It notifies the callback object passed to the constructor when the focus
 * leaves the input box or ENTER is pressed, with the given id.
 * <p>GuiInputDlgBox is practically a fire-and-forget component: the window
 * is automatically shown on creation and destroyed on selection.
 * <p><b>Important</b>: due to the way Rising World plug-ins are loaded,
 * <b>this class cannot instantiated or used in any way</b> from within the onEnable()
 * method of a plug-in, as it is impossible to be sure that, at that moment,
 * the RWGui plug-in has already been loaded.
 * <p>The first moment one can be sure that all plug-ins have been loaded, and
 * it is safe to use this class, is when (or after) the first player connects
 * to the server (either dedicated or local).
 */
public class GuiInputDlgBox extends GuiDialogueBox
{
	private static final	int		TEXTENTRY_WIDTH	= 300;

	private final		RWGuiCallback	dlgCallback;
	private final		int				dlgId;

	/**
	 * Constructs a new GuiInputDlgBox.
	 * 
	 * @param	plugin		the plug-in the DialogueBox is intended for. This
	 * 						is only needed to manage the internal event listener
	 * 						and has no effects on the plug-in itself.
	 * @param	player		the player to show the window to.
	 * @param	title		the text of the title.
	 * @param	caption		the text of the caption.
	 * @param	defaultText	the text to initially show in the input box.
	 * @param	id			the id to report to the callback object on text
	 *						input event
	 * @param	callback	the callback object to which to report events. Can
	 * 						be null, but in this case no event will reported.
	 */
	public GuiInputDlgBox(Plugin plugin, Player player, String title, String caption,
			String defaultText, int id, RWGuiCallback callback)
	{
		super(plugin, title, RWGui.LAYOUT_VERT, null);
		setCallback(new DlgHandler());
		dlgCallback	= callback;
		dlgId		= id;
		addChild(new GuiLabel(caption, 0, 0, false));
		GuiTextField	textField	= new GuiTextField(0, 0, false, TEXTENTRY_WIDTH, RWGui.TEXTENTRY_HEIGHT, false);
		addChild(textField, RWGui.PGDN_ID, null);
		if (defaultText != null)
			textField.setText(defaultText);
	}

	//********************
	// HANDLERS
	//********************

	private class DlgHandler implements RWGuiCallback
	{
		@Override
		public void onCall(Player player, int id, Object data)
		{
			switch (id)
			{
			case RWGui.PGDN_ID:
				if (data != null)
				{
					pop(player);
					dlgCallback.onCall(player, dlgId, data);
				}
				break;
			}
		}
	}
}
