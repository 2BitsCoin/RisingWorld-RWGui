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
	// Standard Colours: backgrounds
	public static final	int		PANEL_COLOUR	= 0x202020E0;
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
	public static final int		ICN_MIN				= 0;
	public static final int		ICN_MAX				= ICN_MINUS;
	// LAYOUT TYPE
	public static final int		LAYOUT_HORIZ	= 1;
	public static final int		LAYOUT_VERT		= 2;
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
	public static final	int		ABORT_ID		= -1;
	public static final	int		OK_ID			= 0;
	// STANDARD RETURN CODES
	public static final int		SUCCESS			= 0;
	public static final	int		INVALID_PARAMETER	= -1;
	public static final	int		MISSING_RESOURCE	= -2;
	public static final	int		ITEM_NOT_FOUND		= -3;

	private static final String	version			= "0.2.1";

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
				"/assets/check.png", "/assets/cross.png", "/assets/uncheck.png",
				"/assets/plus.png", "/assets/minus.png"
			};
	protected	static	String				pluginPath;
	private		static	List<Pair<String,Integer>>	users;		
/*
	public RWGui()
	{
	}
*/
	//********************
	// EVENTS
	//********************

	@Override
	public void onEnable()
	{
		// HACK !! HACK
		// Waiting for a better way to let the classes of the package to know
		// the plug-in path in order to load assets
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

	/**
		Returns (an estimate of) the width of a GuiLabel text. Assumes the
		default font is used.

		@param	text		the text to measure
		@param	fontSize	the size of the font used
		@return	an estimate of the text width corresponding to the given font size
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
				sizes.y	= ((GuiLabel) element).getFontSize();
				sizes.x	= (int)RWGui.getTextWidth(((GuiLabel) element).getText(), sizes.y);
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

	//********************
	// INTERNAL HELPER METHODS
	//********************

	/**
		An interface used by some RWGui classes implementing semi-automatic
		re-layout.
	*/
/*	protected static interface GuiLayoutElement
	{
		public void	close(Player player);
		public void free();
		public void	layout(boolean bottomUp);
		public void	show(Player player);
	}
*/
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
