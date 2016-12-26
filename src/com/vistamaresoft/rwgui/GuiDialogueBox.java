/****************************
	R W G U I  -  A Rising World Java library for GUI elements.

	GuiDialogueBox.java - A GuiPanel sub-class implementing a modal dialogue box

	Created by : Maurizio M. Gavioli 2016-11-19

	(C) Maurizio M. Gavioli (a.k.a. Miwarre), 2016
	Licensed under the Creative Commons by-sa 3.0 license (see http://creativecommons.org/licenses/by-sa/3.0/ for details)

*****************************/

package com.vistamaresoft.rwgui;

import com.vistamaresoft.rwgui.RWGui.RWGuiCallback;
import net.risingworld.api.Plugin;

/**
 * A class implementing a modal dialogue box.
 * <p>A GuiDialogueBox is simply a GuiModalWindow allowing more complex layout
 * structures by nesting other GuiLayout element hierarchically. All notes about
 * GuiModalWindow apply to GuiDialogueBox too.
 * <p><b>Important</b>: due to the way Rising World plug-ins are loaded,
 * <b>this class cannot instantiated or used in any way</b> from within the onEnable()
 * method of a plug-in, as it is impossible to be sure that, at that moment,
 * the RWGui plug-in has already been loaded.
 * <p>The first moment one can be sure that all plug-ins have been loaded, and
 * it is safe to use this class, is when (or after) the first player connects
 * to the server (either dedicated or local).
 */
public class GuiDialogueBox extends GuiModalWindow	// GuiPanel implements Listener
{

	/**
	 * Creates a new GuiDialogueBox.
	 * @param	plugin		the plug-in the DialogueBox is intended for. This
	 * 						is only needed to manage the internal event listener
	 * 						and has no effects on the plug-in itself.
	 * @param	title		the text of the title.
	 * @param	layoutType	the type of the layout (one of the RWGui.LAYOUT_HORIZ
	 * 						or RWGui.LAYOUT_VERT values)
	 * @param	callback	the callback object to which to report events. Can
	 * 						be null, but in this case no event will reported
	 * 						until an actual callback object is set with the
	 * 						setCallback() method.
	 */
	public GuiDialogueBox(Plugin plugin, String title, int layoutType, RWGuiCallback callback)
	{
		super(plugin, title, layoutType, callback);
	}

	//********************
	// PUBLIC METHODS
	//********************

	/**
	 * Adds a new GuiLayout as a direct child of this dialogue box.
	 * 
	 * This method only supports the RWGui.LAYOUT_HORIZ and RWGui.LAYOUT_VERT
	 * layout types. To add a new table layout child, use the
	 * addNewTableLayoutChild() method.
	 * 
	 * @param	layoutType	either RWGui.LAYOUT_HORIZ or RWGui.LAYOUT_VERT	
	 * @param	layoutFlags	one of the RWGui.LAYOUT_H_* and/or RWGui.LAYOUT_V_*
	 * 						flags, according to the layout type.
	 * @return	the new GuiLayout.
	 */
	public GuiLayout addNewLayoutChild(int layoutType, int layoutFlags)
	{
		GuiLayout	newLayout;
		if (layoutType == RWGui.LAYOUT_HORIZ)
			newLayout		= new GuiHorizontalLayout(layoutFlags);
		else
			newLayout		= new GuiVerticalLayout(layoutFlags);
		layout.addChild(newLayout, null);
		return newLayout;
	}

	/**
	 * Adds a new GuiTableLayout as a direct child of this dialogue box.
	 * 
	 * This method only supports the RWGui.LAYOUT_TABLE layout type.
	 * To add a new horizontal or vertical layout child, use the
	 * addNewLayoutChild() method.
	 * 
	 * @param	colNum	the number of columns the table will have
	 * @param	rowNum	the number of rows the table will have.
	 * @param	flags	currently not used, set to 0.
	 * @return	the new GuiTableLayout as a GuiLayout.
	 */
	public GuiLayout addNewTableLayoutChild(int colNum, int rowNum, int flags)
	{
		GuiLayout	newLayout	= new GuiTableLayout(colNum, rowNum, flags);
		layout.addChild(newLayout, null, null);
		return newLayout;
	}

}
