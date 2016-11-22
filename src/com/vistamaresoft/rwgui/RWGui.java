/****************************
	R W G U I  -  A Rising World Java library for GUI elements.

	RWGui.java - Library-wide definitions and and interfaces

	Created by : Maurizio M. Gavioli 2016-11-04

	(C) Maurizio M. Gavioli (a.k.a. Miwarre), 2016
	Licensed under the Creative Commons by-sa 3.0 license (see http://creativecommons.org/licenses/by-sa/3.0/ for details)

*****************************/

package com.vistamaresoft.rwgui;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import net.risingworld.api.Plugin;
import net.risingworld.api.database.WorldDatabase;
import net.risingworld.api.events.EventMethod;
import net.risingworld.api.events.Listener;
import net.risingworld.api.events.player.PlayerConnectEvent;
import net.risingworld.api.gui.GuiImage;
import net.risingworld.api.objects.Player;
import net.risingworld.api.utils.ImageInformation;

public class RWGui extends Plugin implements Listener
{
	// Standard Sizes
	public static final	int		BUTTON_SIZE		= 18;
	public static final	int		ITEM_SIZE		= 15;
	public static final	int		TEXTENTRY_HEIGHT= (ITEM_SIZE + 8);
	public static final	int		TITLE_SIZE		= 18;
	public static final	int		BORDER_THICKNESS= 2;
	public static final	int		BORDER			= 6;
	public static final	float	AVG_CHAR_WIDTH1	= 0.5f;		// the average char width at size 1
	// Standard Colours
	public static final	int		PANEL_COLOUR	= 0x202020E0;
	public static final	int		TITLEBAR_COLOUR	= 0x505050FF;
	public static final	int		TITLE_COLOUR	= 0xFFFFFFFF;
	public static final	int		BORDER_COLOUR	= 0x909090FF;
	public static final	int		ACTIVE_COLOUR	= 0x0000C0FF;
	public static final	int		INACTIVE_COLOUR	= 0x404040FF;
	// Stock Images
	public static final int		ICN_ARROW_DOWN	= 0;
	public static final int		ICN_ARROW_LEFT	= 1;
	public static final int		ICN_ARROW_RIGHT	= 2;
	public static final int		ICN_ARROW_UP	= 3;
	public static final int		ICN_CHECK		= 4;
	public static final int		ICN_CROSS		= 5;
	public static final int		ICN_UNCHECK		= 6;
	public static final int		ICN_MIN				= 0;
	public static final int		ICN_MAX				= ICN_UNCHECK;
	// SELECTION STANDARD RESULT
	public static final	int		ABORT_ID		= -1;
	public static final	int		OK_ID			= 0;
	// STANDARD RETURN CODES
	public static final int		SUCCESS			= 0;
	public static final	int		INVALID_PARAMETER	= -1;
	public static final	int		MISSING_RESOURCE	= -2;

	public static interface SelectionResult
	{
		void onSelect(Player player, int id, int item);
	}
	//
	// FIELDS
	//
	private		static	ImageInformation[]	stockIcons		= new ImageInformation[ICN_MAX-ICN_MIN+1];
	private		static	String[]			stockIconPaths =
			{	"/assets/arrowDown.png", "/assets/arrowLeft.png",
				"/assets/arrowRight.png", "/assets/arrowUp.png",
				"/assets/check.png", "/assets/cross.png", "/assets/uncheck.png"
			};
	protected	static	String				pluginPath;
	private		static	List<Pair<String,Integer>>	users;		
/*
	public RWGui()
	{
	}
*/
	@Override
	public void onEnable()
	{
		// HACK !! HACK
		// Waiting for a better way to let the classes of the package to know
		// the plug-in path in order to load assets
		pluginPath	= getPath();
		registerEventListener(this);
	}
	@Override
	public void onDisable()
	{
		// do nothing
	}
	@EventMethod
	public void onConnect(PlayerConnectEvent event)
	{
		if (event.isNewPlayer())
			users = null;
	}

	public static int setImage(GuiImage image, int iconId)
	{
		if (iconId < ICN_MIN || iconId > ICN_MAX)
			return INVALID_PARAMETER;
		if (stockIcons[iconId] == null)
		{
			try
			{
				stockIcons[iconId]	= new ImageInformation(pluginPath + stockIconPaths[iconId]);
			} catch (IOException e)
			{
				e.printStackTrace();
				return MISSING_RESOURCE;
			}
		}
		image.setImage(stockIcons[iconId]);
		return SUCCESS;
	}

	//
	// A utility class
	//
	public static class Pair<L,R>
	{
		private L l;
		private R r;
		public Pair(L l, R r)
		{
			this.l = l;
			this.r = r;
		}
		public	L		getL()		{ return l; }
		public	R		getR()		{ return r; }
		public	void	setL(L l)	{ this.l = l; }
		public	void	setR(R r)	{ this.r = r; }
	}

	protected static List<Pair<String,Integer>> getPlayers(Plugin plugin)
	{
		if (users != null)
			return users;
		users	= new ArrayList<Pair<String,Integer>>();
		WorldDatabase	db = plugin.getWorldDatabase();
		try(ResultSet result = db.executeQuery("SELECT `ID`,`Name` FROM `Player` ORDER BY `Name`ASC"))
		{
			while(result.next())
			{
				int		id		= result.getInt(1);
				String	name	= result.getString(2);
				Pair<String,Integer>	item	= new Pair<String,Integer>(name, id);
				users.add(item);
			}
			result.close();
		}
		catch(SQLException e)
		{
			//on errors, do nothing and simply use what we got.
		}
		return users;
	}

}
