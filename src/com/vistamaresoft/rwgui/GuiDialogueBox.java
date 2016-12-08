/****************************
	R W G U I  -  A Rising World Java library for GUI elements.

	GuiDialogueBox.java - A GuiPanel sub-class implementing a modal dialogue box

	Created by : Maurizio M. Gavioli 2016-11-19

	(C) Maurizio M. Gavioli (a.k.a. Miwarre), 2016
	Licensed under the Creative Commons by-sa 3.0 license (see http://creativecommons.org/licenses/by-sa/3.0/ for details)

*****************************/

package com.vistamaresoft.rwgui;

import net.risingworld.api.Plugin;
import net.risingworld.api.callbacks.Callback;
import net.risingworld.api.events.EventMethod;
import net.risingworld.api.events.Listener;
import net.risingworld.api.events.player.gui.PlayerGuiElementClickEvent;
import net.risingworld.api.events.player.gui.PlayerGuiInputEvent;
import net.risingworld.api.gui.GuiElement;
import net.risingworld.api.gui.GuiPanel;
import net.risingworld.api.gui.PivotPosition;
import net.risingworld.api.objects.Player;

public class GuiDialogueBox extends GuiPanel implements Listener
{
	private	Callback<Object>	callback;
	protected	GuiLayout	layout;
	protected	int			listenerRef;
	protected	Plugin		plugin;
	protected	GuiTitleBar	titleBar;

	public GuiDialogueBox(Plugin plugin, String title, int layoutType, Callback<Object> callback)
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
		layout.setPosition(RWGui.BORDER, RWGui.BORDER, false);
		layout.setPivot(PivotPosition.BottomLeft);
		super.addChild(layout);
		// we can't directly add the title bar, as this.addChild()
		// is overriden to add to the layout
		titleBar		= new GuiTitleBar(null, title, true);
		super.addChild(titleBar);
//		titleBar.setParent(this);
		listenerRef		= 0;
	}

	//********************
	// EVENTS
	//********************

	@EventMethod
	public void onClick(PlayerGuiElementClickEvent event)
	{
		GuiElement	element	= event.getGuiElement();
		Player		player	= event.getPlayer();
		if (titleBar.isCancelButton(element))
		{
			close(player);
			callback.onCall(new Integer(RWGui.ABORT_ID));
			return;
		}
		Object	data;
		if ( (data=layout.getItemData(element)) != null)
		{
//			close(player);
			callback.onCall(data);
			return;
		}
	}

	@EventMethod
	public void onTextEntry(PlayerGuiInputEvent event)
	{
		Object	data;
		if ( (data=layout.getItemData(event.getGuiElement())) != null)
		{
			callback.onCall(data);
		}
//		Player		player	= event.getPlayer();
//		GuiTransfer	gui		= (GuiTransfer)player.getAttribute(key_gui);
//		if (gui != null)
//			gui.textEntry(event.getGuiElement(), event.getInput(), player);
	}

	//********************
	// PUBLIC METHODS
	//********************

	public void setCallback(Callback<Object> callback)
	{
		this.callback	= callback;
	}

	/**
	 * Lays the dialogue box out, arranging all the children of the layout
	 * hierarchy.
	 * 
	 * This method is always called before showing the dialogue box to a player
	 * and it is usually not necessary to call it explicitly.
	 */
	public void layout(/*boolean bottomUp*/)
	{
		layout.layout(0, 0);			// layout with no constrain
		int height	= (int)layout.getHeight();
		int	width	= (int)layout.getWidth();
		layout.layout(width, height);	// re-layout within actual width and height
		height		+= (int)titleBar.getHeight();
		int	tbw		= (int)titleBar.getWidth();
		if (tbw > width)
			width	= tbw;
		// add a border around the internal layout
		setSize(width + RWGui.BORDER*2, height + RWGui.BORDER*2, false);
		titleBar.relayout();
	}

	/**
	 * Adds an inactive GuiElement with the associated data as a direct child
	 * of the dialogue box. The element is positioned beside or below the last
	 * added child, depending on the type (RWGui.LAYOUT_HORIZ or
	 * RWGui.LAYOUT_VERT) of the dialogue box.
	 * 
	 * @param	element	the element to add.
	 */
	@Override
	public void addChild(GuiElement element)
	{
		layout.addChild(element, null);
	}

	/**
	 * Adds a GuiElement with the associated data as a direct child of the
	 * dialogue box. The element is positioned beside or below the last added
	 * child, depending on the type (RWGui.LAYOUT_HORIZ or RWGui.LAYOUT_VERT)
	 * of the dialogue box.
	 * 
	 * If data is not null, the element is active (the player can click on it),
	 * if data is null, the element is not active.
	 * 
	 * @param	element	the element to add.
	 * @param	data	the data associated with the element; may be null for
	 * 					inactive elements.
	 */
	public void addChild(GuiElement element, Object data)
	{
		layout.addChild(element, data);
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
	 * @param	layoutType	
	 * @param	layoutFlags
	 * @return
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
	 * Closes (hides) the dialogue box from the player screen, turning the
	 * mouse cursor off.
	 * @param	player	the player from whose screen to remove the dialogue
	 * 					box. Removing the same dialogue box from the same
	 * 					player multiple times has no effect and does no harm.
	 */
	public void close(Player player)
	{
		titleBar.removeFromPlayer(player);
		layout.close(player);
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
	 * Returns the data associated with element, if element is one of the
	 * children of the dialogue box; or null otherwise.

	 * @param	element	the GuiElement to look for.
	 * @return	the data associated with element if present, null if not.
	 */
	public Object getItemData(GuiElement element)
	{
		return layout.getItemData(element);
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
