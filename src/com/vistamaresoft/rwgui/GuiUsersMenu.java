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

public class GuiUsersMenu extends GuiMenu
{

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
