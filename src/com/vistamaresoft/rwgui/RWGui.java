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
import net.risingworld.api.gui.GuiElement;
import net.risingworld.api.gui.GuiImage;
import net.risingworld.api.gui.GuiLabel;
import net.risingworld.api.objects.Player;
import net.risingworld.api.utils.ImageInformation;
import net.risingworld.api.utils.Vector2i;

/**
 * A 'holding' class with global definitions and ancillary utilities for the
 * whole plug-in.
 * <p><b>Important</b>: due to the way Rising World plug-ins are loaded,
 * <b>none</b> of the classes of this package (GuiDialogueBox, GuiLayout and
 * its sub-classes, GuiMenu and GuiUsersMenu, GuiModelessWindow, GuiTitleBar)
 * can be used from within the onEnable() method of a plug-in, as it is
 * impossible to be sure that, at that moment, this plug-in has already been
 * loaded.
 * <p>The first moment one can be sure that all plug-ins have been loaded is
 * when the first player connects to the server (either dedicated or local).
 */
public class RWGui extends Plugin implements Listener
{
	// Standard Sizes
	public static final	int		BUTTON_SIZE		= 18;
	public static final	int		ITEM_SIZE		= 15;
	public static final	int		TEXTENTRY_HEIGHT= (ITEM_SIZE + 8);
	public static final	int		TITLE_SIZE		= 18;
	public static final	int		BORDER_THICKNESS= 2;
//	public static final	int		BORDER			= 6;
	public static final	int		DEFAULT_PADDING	= 6;
	public static final	float	AVG_CHAR_WIDTH1	= 0.5f;		// the average char width at size 1
	// Standard Colours: backgrounds
	public static final	int		PANEL_COLOUR	= 0x202020FF;
	public static final	int		TITLEBAR_COLOUR	= 0x505050FF;
	public static final	int		BORDER_COLOUR	= 0x909090FF;
	public static final	int		ACTIVE_COLOUR	= 0x0060D0FF;
	public static final	int		INACTIVE_COLOUR	= 0x404040FF;
	// Standard colours: texts
	public static final	int		TEXT_COLOUR		= 0xFFFFFFFF;
	public static final	int		TITLE_COLOUR	= 0xFFFFFFFF;
	public static final	int		TEXT_SEL_COLOUR	= 0x00B0FFFF;
	public static final	int		TEXT_DIM_COLOUR	= 0x808080FF;
	// Stock Images
	public static final int		ICN_ARROW_DOWN	= 0;
	public static final int		ICN_ARROW_LEFT	= 1;
	public static final int		ICN_ARROW_RIGHT	= 2;
	public static final int		ICN_ARROW_UP	= 3;
	public static final int		ICN_CHECK		= 4;
	public static final int		ICN_CROSS		= 5;
	public static final int		ICN_UNCHECK		= 6;
	public static final int		ICN_PLUS		= 7;
	public static final int		ICN_MINUS		= 8;
	public static final	int		ICN_RADIO_CHECK	= 9;
	public static final	int		ICN_RADIO_UNCHECK	= 10;
	public static final int		ICN_MIN				= 0;
	public static final int		ICN_MAX				= ICN_RADIO_UNCHECK;
	// LAYOUT TYPE
	public static final int		LAYOUT_HORIZ	= 1;
	public static final int		LAYOUT_VERT		= 2;
	public static final int		LAYOUT_TABLE	= 3;
	// LAYOUT Arrangements
	public static final int		LAYOUT_H_LEFT	= 0x00;
	public static final int		LAYOUT_H_CENTRE	= 0x01;
	public static final int		LAYOUT_H_RIGHT	= 0x02;
	public static final int		LAYOUT_H_SPREAD	= 0x04;
	public static final int		LAYOUT_V_TOP	= 0x00;
	public static final int		LAYOUT_V_MIDDLE	= 0x08;
	public static final int		LAYOUT_V_BOTTOM	= 0x10;
	public static final int		LAYOUT_V_SPREAD	= 0x20;
	// SELECTION STANDARD RESULT
	/** The id reported by a click event on a close button. */
	public static final	int		ABORT_ID		= -1;
	/** The id reported by a click event on the default button of dialogue box. */
	public static final	int		OK_ID			= 0;
	// STANDARD RETURN CODES
	/** The operation has been successful. */
	public static final int		ERR_SUCCESS				= 0;
	/** A parameter was out of range or invalid. */
	public static final	int		ERR_INVALID_PARAMETER	= -1;
	/** A resource (icon) looked for did not exist. */
	public static final	int		ERR_MISSING_RESOURCE	= -2;
	/** An item looked for did not exist. */
	public static final	int		ERR_ITEM_NOT_FOUND		= -3;

	private static final String	version			= "0.4.1";

	//
	// FIELDS
	//
	private		static	ImageInformation[]	stockIcons		= new ImageInformation[ICN_MAX-ICN_MIN+1];
	private		static	String[]			stockIconPaths =
			{	"/assets/arrowDown.png", "/assets/arrowLeft.png",
				"/assets/arrowRight.png", "/assets/arrowUp.png",
				"/assets/check.png", "/assets/cross.png", "/assets/uncheck.png",
				"/assets/plus.png", "/assets/minus.png",
				"/assets/radioCheck.png", "/assets/radioUncheck.png" 
			};
	protected	static	String				pluginPath;
	private		static	List<Pair<Integer,String>>	users;

	//********************
	// EVENTS
	//********************

	@Override
	public void onEnable()
	{
		pluginPath	= getPath();
		registerEventListener(this);
		System.out.println("RWGui "+version+" loaded successfully!");
	}
	@Override
	public void onDisable()
	{
		unregisterEventListener(this);
		System.out.println("RWGui "+version+" unloaded successfully!");
	}
	@EventMethod
	public void onConnect(PlayerConnectEvent event)
	{
		if (event.isNewPlayer())
			users = null;
	}

	//********************
	// PUBLIC METHODS & CLASSES
	//********************

	/**
		Sets one of the stock icon image into a GuiImage element.

		@param	image	the GuiImage to set the icon image into
		@param	iconId	the id of the icon
		@return	INVALID_PARAMETER if iconId is out of range; SUCCESS otherwise.
	*/
	public static int setImage(GuiImage image, int iconId)
	{
		if (iconId < ICN_MIN || iconId > ICN_MAX)
			return ERR_INVALID_PARAMETER;
		if (stockIcons[iconId] == null)
		{
			try
			{
				stockIcons[iconId]	= new ImageInformation(pluginPath + stockIconPaths[iconId]);
			} catch (IOException e)
			{
				e.printStackTrace();
				return ERR_MISSING_RESOURCE;
			}
		}
		image.setImage(stockIcons[iconId]);
		return ERR_SUCCESS;
	}

	/**
		Returns (an estimate of) the width of a GuiLabel text. Assumes the
		default font is used.

		@param	text		the text to measure
		@param	fontSize	the size of the font used
		@return	an estimate of the text width in pixels corresponding to the
				given font size
	*/
	public static float getTextWidth(String text, float fontSize)
	{
		return (fontSize * RWGui.AVG_CHAR_WIDTH1 * text.length());
	}

	/**
		Returns the x, y sizes of a GuiElement as a Vector2i. Required to support
		(at least approximately) getHeight() and getWidth() for GuiLabel too.

		@param	element	the element to measure
		@return	the x and y sizes of the element as a Vector2i
	*/
	public static Vector2i getElementSizes(GuiElement element)
	{
		Vector2i	sizes	= new Vector2i();
		if (element != null)
		{
			if (element instanceof GuiLabel)
			{
				String[]	lines		= ((GuiLabel) element).getText().split("\n");
				int			fontSize	= ((GuiLabel) element).getFontSize();
				int			maxLength	= 0;
				for (String line : lines)
					if (line.length() > maxLength)
						maxLength		= line.length();
				sizes.x					= (int)(fontSize * RWGui.AVG_CHAR_WIDTH1 * maxLength);
				sizes.y					=  fontSize * lines.length;
			}
			else
			{
				sizes.y	= (int)element.getHeight();
				sizes.x	= (int)element.getWidth();
			}
		}
		return sizes;
	}

	/**
	 * Returns the name of a player given his DB ID. Returns null if such a
	 * name does not exists.
	 * @param	plugin	The plug-in making the request. This is only needed to
	 *					access the relevant World database and has no side
	 *					effects on the plug-in itself.
	 * @param	dbId	the DB ID of the player
	 * @return	the name of the player with that DB ID or null if not found.
	 */
	public static String getPlayerNameFromId(Plugin plugin, int dbId)
	{
		if (users == null)
			getPlayers(plugin);
		for(Pair<Integer,String> user : users)
		{
			if (user.getL() == dbId)
				return user.getR();
		}
		return null;
	}

	/**
	 * Returns the DB ID of a player given his name. Returns 0 if such a
	 * DB ID does not exists.
	 * @param	plugin	The plug-in making the request. This is only needed to
	 *					access the relevant World database and has no side
	 *					effects on the plug-in itself.
	 * @param	name	the name of the player
	 * @return	the DB ID of the player with that name or 0 if not found.
	 */
	public static int getPlayerDbIdFromName(Plugin plugin, String name)
	{
		if (users == null)
			getPlayers(plugin);
		for(Pair<Integer,String> user : users)
		{
			if (user.getR().equals(name))
				return user.getL();
		}
		return 0;
	}

	/**
	 * Returns the list of players known to the World (i.e. who connected at
	 * least once).
	 * <p>The list is a List of Pair's each made of an int and a String, the
	 * integer being the DB ID of player and the String his name of the player.
	 * <p>The list is sorted by names, in native alphabetical order.
	 * @param	plugin	The plug-in making the request. This is only needed to
	 *					access the relevant World database and has no side
	 *					effects on the plug-in itself.
	 * @return	a List of DB ID/name player data Pair's.
	 */
	public static List<Pair<Integer,String>> getPlayers(Plugin plugin)
	{
		if (users != null)
			return users;
		users	= new ArrayList<Pair<Integer,String>>();
		WorldDatabase	db = plugin.getWorldDatabase();
		try(ResultSet result = db.executeQuery("SELECT `ID`,`Name` FROM `Player` ORDER BY `Name`ASC"))
		{
			while(result.next())
			{
				int		id		= result.getInt(1);
				String	name	= result.getString(2);
				Pair<Integer,String>	item	= new Pair<Integer,String>(id, name);
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

	/**
		A utility class to hold two related objects.

		@param	<L>	the first (left) element of the pair; can be any Java object
		@param	<R>	the second (right)) element of the pair; can be any Java object
	*/
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

	/**
	 * An interface for the callback objects reporting click and text entry
	 * events to menus, dialogue boxes and similar.
	 * <p><b>Important</b>: Due to the way Rising World plug-ins are loaded,
	 * this interface <b>cannot be instantiated</b> from within the main Java
	 * class of a plug-in; a separate class has to be used to contain and
	 * instantiate the actual callback object (typically, a class sub-classing
	 * the GUI element for which the callback is used: a GuiMenu,
	 * a GuiDialogueBox, etc...).
	 */
	public abstract interface RWGuiCallback
	{
		public abstract void	onCall(Player player, int id, Object data);
	}

	//********************
	// INTERNAL HELPER METHODS
	//********************

}
