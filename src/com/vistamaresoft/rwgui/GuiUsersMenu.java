/****************************
	R W G U I  -  A Rising World Java library for GUI elements.

	GuiUsersMenu.java - Displays and manages a menu with a list of users.

	Created by : Maurizio M. Gavioli 2016-11-01

	(C) Maurizio M. Gavioli (a.k.a. Miwarre), 2016
	Licensed under the Creative Commons by-sa 3.0 license (see http://creativecommons.org/licenses/by-sa/3.0/ for details)

*****************************/

package com.vistamaresoft.rwgui;

import java.util.List;
import com.vistamaresoft.rwgui.RWGui.Pair;
import com.vistamaresoft.rwgui.RWGui.RWGuiCallback;
import net.risingworld.api.Plugin;

/**
 * A sub-class of GuiMenu, displaying a list of all the players known to the
 * world (i.e. all the players who has connected at least once, not only the
 * players connected at the moment).
 * <p>The menu displays the names of the player as clickable items.
 * <p>Upon a click event, the menu reports to the callback object with an <i>id</i>
 * parameter equal to the player DB ID and a String <i>data</i> parameter  with
 * the name of the player.
 * <p>For the rest, a GuiUsersMenu is managed exactly like a regular GuiMenu.
 * <p><b>Important</b>: due to the way Rising World plug-ins are loaded,
 * <b>this class cannot instantiated or used in any way</b> from within the onEnable()
 * method of a plug-in, as it is impossible to be sure that, at that moment,
 * the RWGui plug-in has already been loaded.
 * <p>The first moment one can be sure that all plug-ins have been loaded, and
 * it is safe to use this class, is when (or after) the first player connects
 * to the server (either dedicated or local).
 */
public class GuiUsersMenu extends GuiMenu
{
	/**
	 * Creates a new GuiUsersMenu.
	 * @param	plugin		the plug-in the GuiMenu is intended for. This
	 * 						is only needed to manage the internal event listener
	 * 						and has no effects on the plug-in itself.
	 * @param	titleText	the text of the title.
	 * @param	callback	the callback object to which to report events. Can
	 * 						be null, but in this case no event will reported
	 * 						until an actual callback object is set with the
	 * 						setCallback() method.
	 * @param	excludeId	the DB ID of a player who should not be shown in
	 *						the menu; useful when a player is selecting <i>another</i>
	 *						and his own name should not appear in the list.
	 *						Use -1 to not exclude any player.
	 */
	public GuiUsersMenu(Plugin plugin, String titleText, RWGuiCallback callback, int excludeId)
	{
		super(plugin, titleText, callback);

		List<Pair<Integer,Object>>	users	= RWGui.getPlayers(plugin);
		if (users != null)
		{
			for (Pair<Integer,Object> entry : users)
				if (entry.getL() != excludeId)
					addItem((String)entry.getR(), entry.getL(), entry.getR());
		}
	}
	
}
