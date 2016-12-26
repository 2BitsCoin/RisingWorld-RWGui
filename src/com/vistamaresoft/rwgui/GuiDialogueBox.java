/****************************
	R W G U I  -  A Rising World Java library for GUI elements.

	GuiDialogueBox.java - A GuiPanel sub-class implementing a modal dialogue box

	Created by : Maurizio M. Gavioli 2016-11-19

	(C) Maurizio M. Gavioli (a.k.a. Miwarre), 2016
	Licensed under the Creative Commons by-sa 3.0 license (see http://creativecommons.org/licenses/by-sa/3.0/ for details)

*****************************/

package com.vistamaresoft.rwgui;

import com.vistamaresoft.rwgui.RWGui.Pair;
import com.vistamaresoft.rwgui.RWGui.RWGuiCallback;
import net.risingworld.api.Plugin;
import net.risingworld.api.events.EventMethod;
import net.risingworld.api.events.Listener;
import net.risingworld.api.events.player.gui.PlayerGuiElementClickEvent;
import net.risingworld.api.events.player.gui.PlayerGuiInputEvent;
import net.risingworld.api.gui.GuiElement;
import net.risingworld.api.gui.GuiPanel;
import net.risingworld.api.gui.GuiTextField;
import net.risingworld.api.gui.PivotPosition;
import net.risingworld.api.objects.Player;

/**
 * A class implementing a modal dialogue box. Each dialogue box is made of a
 * top title bar, with title and close button, and a panel underneath where
 * controls can be added.
 * 
 * The panel is one of the GuiLayout subclasses, with its specific arrangement
 * of children, according to the layoutType of the constructor. To the panel,
 * standard GuiElement's can be added with addChild() or nested layouts with
 * the addNewLayoutChild() or addNewTableLayoutChild() methods.
 * <p>The DialogueBox manages its own event Listener; it also turns the mouse
 * cursor on on display and off on hiding.
 * <p>The DialogueBox manages the close button in the title bar, hiding the box
 * from the player screen and turning off the mouse cursor. The callback object
 * is notified of a close event by passing an id parameter with a value of
 * RWGui.ABORT_ID. The consumer plug-in needs not to do any additional
 * management of the dialogue box itself in response to this notification (of
 * course, it would do any management of its own resources, including freeing
 * the dialogue box itself with its free() method, if no longer needed).
 * <p>The DialogueBox notifies of click and text entry events via an
 * RWGuiCallback object passed to the constructor or set after construction with
 * the setCallback() method.
 * <p>On events, the onCall() method of the callback object is called with
 * parameters for the player originating the event, the id of the GuiElement
 * and any additional data set for the GuiElement. Id and data for each child
 * are set when the child is added.
 * <p><b>Important</b>: due to the way Rising World plug-ins are loaded,
 * <b>this class cannot instantiated or used in any way</b> from within the onEnable()
 * method of a plug-in, as it is impossible to be sure that, at that moment,
 * the RWGui plug-in has already been loaded.
 * <p>The first moment one can be sure that all plug-ins have been loaded, and
 * it is safe to use this class, is when (or after) the first player connects
 * to the server (either dedicated or local).
 */
public class GuiDialogueBox extends GuiPanel implements Listener
{
	private	RWGuiCallback	callback;
	private	GuiLayout	layout;
	private	int			listenerRef;
	private	Plugin		plugin;
	private	GuiTitleBar	titleBar;

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
		setPosition(0.5f, 0.5f, true);
		setPivot(PivotPosition.Center);
		setBorderColor(RWGui.BORDER_COLOUR);
		setBorderThickness(RWGui.BORDER_THICKNESS, false);
		setColor(RWGui.PANEL_COLOUR);
		this.callback	= callback;
		this.plugin		= plugin;
		if (layoutType == RWGui.LAYOUT_HORIZ)
			layout		= new GuiHorizontalLayout(RWGui.LAYOUT_V_TOP & RWGui.LAYOUT_H_LEFT);
		else
			layout		= new GuiVerticalLayout(RWGui.LAYOUT_V_TOP & RWGui.LAYOUT_H_LEFT);
		layout.setMargin(RWGui.DEFAULT_PADDING);
		layout.setPivot(PivotPosition.BottomLeft);
		layout.setPosition(/*RWGui.DEFAULT_PADDING, RWGui.DEFAULT_PADDING*/0, 0, false);
		super.addChild(layout);
		// we can't directly add the title bar, as this.addChild()
		// is overriden to add to the layout
		titleBar		= new GuiTitleBar(null, title, true);
		super.addChild(titleBar);
		listenerRef		= 0;
	}

	//********************
	// EVENTS
	//********************

	@EventMethod
	public void onClick(PlayerGuiElementClickEvent event)
	{
		if (callback == null)
			return;
		GuiElement	element	= event.getGuiElement();
		Player		player	= event.getPlayer();
		// on cancel button press, close the dlg and notify the caller
		if (titleBar.isCancelButton(element))
		{
			close(player);
			callback.onCall(player, RWGui.ABORT_ID, null);
			return;
		}
		// on other click events, notify the caller.
		// GuiTextField's are treated differently, as a click on them is only
		// reported with an id, without any data.
		Pair<Integer,Object>	data;
		if ( (data=layout.getItemData(element)) != null)
		{
			callback.onCall(player, data.getL(), (element instanceof GuiTextField) ? null : data.getR());
			return;
		}
	}

	@EventMethod
	public void onTextEntry(PlayerGuiInputEvent event)
	{
		if (callback == null)
			return;
		Integer	id;
		if ( (id=layout.getItemId(event.getGuiElement())) != null)
		{
			callback.onCall(event.getPlayer(), id, event.getInput());
		}
	}

	//********************
	// PUBLIC METHODS
	//********************

	/**
	 * Sets the callback function called upon click and text entry events.

	 * @param	callback	the new callback
	 */
	public void setCallback(RWGuiCallback callback)
	{
		this.callback	= callback;
	}

	/**
	 * Sets the margin between the contents of the dialogue box and its edges.
	 * @param value	the new margin (in pixels).
	 */
	public void setMargin(int value)	{ layout.setMargin(value);	}

	/**
	 * Sets the padding (i.e. the minimum distance) between two side-by-side
	 * elements of the main dialogue box panel (in pixels),
	 * @param value	the new padding (in pixels).
	 */
	public void setPadding(int value)	{ layout.setPadding(value);	}
	/**
	 * Lays the dialogue box out, arranging all the children of the layout
	 * hierarchy.
	 * 
	 * This method is always called before showing the dialogue box to a player
	 * and it is usually not necessary to call it manually.
	 */

	public void layout()
	{
		int	tbw		= titleBar.getMinWidth();
		layout.layout(tbw, 0);			// require the layout to be at least as wide as the title bar
		int height	= (int)layout.getHeight();
		int	width	= (int)layout.getWidth();
/*		// add a border around the internal layout
//		width		+= RWGui.DEFAULT_PADDING*2;
		height		+= (int)titleBar.getHeight();
//		if (tbw > width)
//			width	= tbw;*/
		// place the layout inside any dlg box border
		int	borderW	= (int)getBorderThickness();
		layout.setPosition(borderW, borderW, false);
		layout.setSize(width - borderW*2, height - borderW, false);
		// final size of the dialogue box
		height		+= (int)titleBar.getHeight();
		setSize(width, height, false);
		// tell the title bar to re-position itself within the dialogue box
		titleBar.relayout();
	}

	/**
	 * Adds an inactive GuiElement (with no associated data) as a direct child
	 * of the dialogue box. The element is positioned beside or below the last
	 * added child, depending on the type (RWGui.LAYOUT_HORIZ or
	 * RWGui.LAYOUT_VERT) of the dialogue box.
	 * 
	 * @param	element	the element to add.
	 */
	@Override
	public void addChild(GuiElement element)
	{
		layout.addChild(element);
	}

	/**
	 * Adds a GuiElement with the associated id and data as a direct child of
	 * the dialogue box. The element is positioned beside or below the last
	 * added child, depending on the type (RWGui.LAYOUT_HORIZ or
	 * RWGui.LAYOUT_VERT) of the dialogue box.
	 * 
	 * If id is not null, the element is active (the player can click on it),
	 * and events on it will be reported to the callback function;
	 * if id is null, the element is not active.
	 * 
	 * <p>id can be any Integer and id's should be all different from one
	 * another within each dialogue box.
	 * 
	 * <p>The data parameter can be any Java object and can store additional
	 * information required to deal with the element, when a click event is
	 * reported for it via the callback object. It can also be null if no
	 * additional info is needed for the element.
	 * 
	 * <p>id and data are reported by the callback object upon click and text
	 * entry events. GuiTextField's would generate both click and text entry
	 * events: they can be distinguished because click events on a GuiTextField
	 * will call the callback object with null data parameter, while text entry
	 * events will have the data parameter set to the element current text.
	 * 
	 * @param	element	the element to add.
	 * @param	id		the id associated with the element; may be null for
	 * 					inactive elements.
	 * @param	data	the data associated with the element; may be null for
	 * 					elements which need no additional data other than their id.
	 */
	public void addChild(GuiElement element, Integer id, Object data)
	{
		layout.addChild(element, id, data);
	}

	/**
	 * Removes a GuiElement from the direct children of the dialogue box.
	 * 
	 * @param	element	The GuiElement to remove
	 */
	@Override
	public void removeChild(GuiElement element)
	{
		layout.removeChild(element);
	}

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

	/**
	 * Closes (hides) the dialogue box from the player screen, turning the
	 * mouse cursor off.
	 * 
	 * <p>The dlg resources are <b>not freed</b> and the dlg can be re-used
	 * if needed; when the dlg is no longer needed, its resources must be freed
	 * with the free() method, in addition to closing it.
	 * @param	player	the player from whose screen to remove the dialogue
	 * 					box. Removing the same dialogue box from the same
	 * 					player multiple times has no effect and does no harm.
	 */
	public void close(Player player)
	{
		titleBar.removeFromPlayer(player);
		layout.hide(player);
		player.removeGuiElement(this);
		listenerRef--;
		if (listenerRef <= 0)
			plugin.unregisterEventListener(this);
		player.setMouseCursorVisible(false);
	}

	/**
	 * Displays the dialogue box on the player screen.
	 * 
	 * The dialogue box is laid out before being shown and the mouse cursor
	 * is turned on.
	 * @param	player	the player to show the dialogue box to.
	 */
	public void show(Player player)
	{
		layout(/*false*/);
		titleBar.addToPlayer(player);
		layout.show(player);
		listenerRef++;
		if (listenerRef == 1)
			plugin.registerEventListener(this);
		player.addGuiElement(this);
		player.setMouseCursorVisible(true);
	}

	/**
	 * Returns the id associated with element, if element is one of the
	 * children of the dialogue box (recursively); or null otherwise.

	 * @param	element	the GuiElement to look for.
	 * @return	the id associated with element if present, null if not.
	 */
	public Integer getItemId(GuiElement element)
	{
		return layout.getItemId(element);
	}

	/**
	 * Releases the resources used by the dialogue box. After this method has
	 * been called, the dialogue box cannot be used or displayed any longer.
	 * 
	 * The resources are in any case garbage collected once the dialogue box
	 * goes out of scope or all the references to it elapse. Using this method
	 * might be useful to speed up the garbage collection process, once the
	 * dialogue box is not longer needed.
	 */
	public void free()
	{
		titleBar.free();
		titleBar	= null;
		layout.free();
		layout		= null;
	}

}
