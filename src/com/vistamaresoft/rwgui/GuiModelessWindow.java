/****************************
	R W G U I  -  A Rising World Java library for GUI elements.

	GuiModelessWindow.java - Displays and manages a modeless window.

	Created by : Maurizio M. Gavioli 2016-11-01

	(C) Maurizio M. Gavioli (a.k.a. Miwarre), 2016
	Licensed under the Creative Commons by-sa 3.0 license (see http://creativecommons.org/licenses/by-sa/3.0/ for details)

*****************************/

package com.vistamaresoft.rwgui;

import java.util.List;
import net.risingworld.api.Plugin;
import net.risingworld.api.gui.GuiLabel;
import net.risingworld.api.gui.GuiPanel;
import net.risingworld.api.gui.PivotPosition;
import net.risingworld.api.objects.Player;

/**
 * A class implementing a mode-less window, made of a title bar and a number
 * of textual strings displayed one below the other.
 *
 * The window is placed near the lower left corner and its height and width
 * depends on the number and length of the textual strings.
 * <p>The window has no behaviour, i.e. it just displays the texts and does
 * nothing else; in particular, the player cannot interact with it in any way.
 * <p><b>Important</b>: due to the way Rising World plug-ins are loaded,
 * <b>this class cannot instantiated or used in any way</b> from within the onEnable()
 * method of a plug-in, as it is impossible to be sure that, at that moment,
 * the RWGui plug-in has already been loaded.
 * <p>The first moment one can be sure that all plug-ins have been loaded, and
 * it is safe to use this class, is when (or after) the first player connects
 * to the server (either dedicated or local).
 */
public class GuiModelessWindow extends GuiPanel
{
	// CONSTANTS
	//
	// The default position (relative to screen size) and size (absolute) of the GUI panel
	private static final	int		PANEL_XPOS		= 20;
	private static final	int		PANEL_YPOS		= 70;
	private static final	int		PANEL_WIDTH		= 300;
	private static final	int		PANEL_COLOUR	= 0xe0e0a0E0;	// 0x20202080;
	private static final	int		BORDER_COLOUR	= 0x000000FF;
	private static final	int		TEXT_COLOUR		= 0x000000FF;

	// The various labels
	private static final	int		TEXT_XPOS		= RWGui.DEFAULT_PADDING;
	private static final	int		TEXT_YDELTA		= GuiTitleBar.TITLEBAR_HEIGHT + RWGui.DEFAULT_PADDING;

	// FIELDS
	//
	private	Player		player;
	private	GuiLabel[]	labels;
	private GuiTitleBar	titleBar;

	/**
	 * Creates a mode-less window with given title and sequence of textual strings.
	 * The window is placed near the lower left corner and its height depends
	 * on the number of textual strings.
	 * 
	 * @param plugin	the plug-in this window will belong to 
	 * @param player	the player this windows will appear for
	 * @param titleText	the title of the window
	 * @param texts		the textual strings
	 */
	public GuiModelessWindow(Plugin plugin, Player player, String titleText, List<String> texts)
	{
		super();
		this.player	= player;

		// create a panel in the lower left corner
		setPivot(PivotPosition.TopLeft);
		setSize(PANEL_WIDTH, 0, false);
		setBorderColor(BORDER_COLOUR);
		setBorderThickness(RWGui.BORDER_THICKNESS, false);
		setColor(PANEL_COLOUR);
		setVisible(true);
		titleBar	= new GuiTitleBar(this, titleText, false);
		titleBar.addToPlayer(player);
		player.addGuiElement(this);
		if (texts != null)
			setTexts(texts);
	}

	/**
	 * Set new texts into the window. Window position, height and width are
	 * adjusted to the new number and length of the textual strings.
	 * 
	 * @param texts
	 */
	public void setTexts(List<String> texts)
	{
		int		numOfTexts	= texts.size();
		// compute height from number of text lines
		int		panelHeight	= RWGui.TITLE_SIZE + RWGui.ITEM_SIZE*texts.size() +
				RWGui.DEFAULT_PADDING*(texts.size()+3);
		// compute width from number of chars in title and in text lines
		int		panelWidth	= (int)(RWGui.AVG_CHAR_WIDTH1 * titleBar.getTitleText().length() *
				RWGui.TITLE_SIZE);
		int		textWidth;
		for (int i = 0; i < numOfTexts; i++)
			if ( (textWidth = (int)(RWGui.AVG_CHAR_WIDTH1 * texts.get(i).length() * RWGui.ITEM_SIZE)) > panelWidth)
				panelWidth = textWidth;
		panelWidth	+= 2 * RWGui.DEFAULT_PADDING;
		// Panel size are known: compute position of each item
		setPosition(PANEL_XPOS, PANEL_YPOS+panelHeight, false);
		setSize(panelWidth, panelHeight, false);
		titleBar.relayout();
		// the strings
		releaseTexts();
		int		yPos	= panelHeight - TEXT_YDELTA;
		labels			= new GuiLabel[numOfTexts];
		for (int i = 0; i < numOfTexts; i++)
		{
			labels[i]	= new GuiLabel(texts.get(i), TEXT_XPOS, yPos, false);
			labels[i].setPivot(PivotPosition.TopLeft);
			labels[i].setFontColor(TEXT_COLOUR);
			labels[i].setFontSize(RWGui.ITEM_SIZE);
			labels[i].setClickable(false);
			addChild(labels[i]);
			player.addGuiElement(labels[i]);
			yPos		-= RWGui.DEFAULT_PADDING + RWGui.ITEM_SIZE;
		}
	}

	/**
	 * Closes the window and destroys allocated resources. To be used before
	 * relinquishing the object. After calling this method, the window cannot
	 * be used any longer.
	 */
	public void free()
	{
		titleBar.removeFromPlayer(player);
		titleBar.free();
		titleBar	= null;
		player.removeGuiElement(this);
		releaseTexts();
	}

	private void releaseTexts()
	{
		if (labels != null)
		{
			int		numOfTexts	= labels.length;
			for (int i = 0; i < numOfTexts; i++)
			{
				player.removeGuiElement(labels[i]);
				removeChild(labels[i]);
				labels[i]	= null;
			}
			labels	= null;
		}
	}
}
