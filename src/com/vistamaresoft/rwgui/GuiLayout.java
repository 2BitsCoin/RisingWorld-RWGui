/****************************
	R W G U I  -  A Rising World Java library for GUI elements.

	GuiLayout.java - An abstract class which is the base for automatic layout classes.

	Created by : Maurizio M. Gavioli 2016-11-19

	(C) Maurizio M. Gavioli (a.k.a. Miwarre), 2016
	Licensed under the Creative Commons by-sa 3.0 license (see http://creativecommons.org/licenses/by-sa/3.0/ for details)

*****************************/

package com.vistamaresoft.rwgui;

import java.util.ArrayList;
//import com.vistamaresoft.rwgui.RWGui.GuiLayoutElement;
import com.vistamaresoft.rwgui.RWGui.Pair;
import net.risingworld.api.gui.GuiElement;
import net.risingworld.api.gui.GuiImage;
import net.risingworld.api.gui.GuiLabel;
import net.risingworld.api.gui.GuiPanel;
import net.risingworld.api.gui.GuiTextField;
import net.risingworld.api.gui.PivotPosition;
import net.risingworld.api.objects.Player;

/**
	An abstract class which is the base for all RWGui classes supporting 'automatic' layout.

*/
public class GuiLayout extends GuiPanel //implements GuiLayoutElement
{
	protected	ArrayList<Pair<GuiElement,Pair<Integer,Object>>>
													children	= null;
	protected	int									flags		= RWGui.LAYOUT_V_TOP & RWGui.LAYOUT_H_LEFT;
	protected	int									shown		= 0;

	public GuiLayout(int flags)
	{
		super (0, 0, false, 0, 0, false);
//		setSize(0, 0, false);
//		setPosition(0, 0, false);
		setPivot(PivotPosition.TopLeft);
		this.flags	= flags;
	}

//	@Override
	public void close(Player player)
	{
		for (Pair<GuiElement,Pair<Integer,Object>> item : children)
		{
			if (item.getL() instanceof GuiLayout)
				((GuiLayout)item.getL()).close(player);
			else
				player.removeGuiElement(item.getL());
		}
		shown--;
	}

//	@Override
	public void layout(int minWidth, int minHeight)
	{
		
	}

//	@Override
	public void free()
	{
		for (Pair<GuiElement,Pair<Integer,Object>> item : children)
		{
			GuiElement	element	= item.getL();
			if (element instanceof GuiLayout)
				((GuiLayout)element).free();
			removeChild(element);
		}
		children.clear();
	}

//	@Override
	public void hide(Player player)
	{
		for (Pair<GuiElement,Pair<Integer,Object>> item : children)
		{
			GuiElement	element	= item.getL();
			if (element instanceof GuiLayout)
				((GuiLayout)element).hide(player);
			else
				player.removeGuiElement(element);
			removeChild(element);
		}
		player.removeGuiElement(this);
	}

//	@Override
	public void show(Player player)
	{
		player.addGuiElement(this);
		for (Pair<GuiElement,Pair<Integer,Object>> item : children)
		{
			GuiElement	element	= item.getL();
			if (element instanceof GuiLayout)
				((GuiLayout)element).show(player);
			else
				player.addGuiElement(element);
		}
		shown++;
	}

	@Override
	public void addChild(GuiElement element)
	{
		addChild(element, null, null);
	}

	public void addChild(GuiElement element, Integer id)
	{
		addChild(element, id, null);
	}

	public void addChild(GuiElement element, Integer id, Object data)
	{
		if (element == null)
			return;
		if (children == null)
			children	= new ArrayList<Pair<GuiElement,Pair<Integer,Object>>>(4);
		children.add(new Pair<GuiElement,Pair<Integer,Object>>(element, new Pair<Integer,Object>(id, data)));
		if (element instanceof GuiImage)
			((GuiImage)element).setClickable(data != null);
		else if (element instanceof GuiLabel)
		{
			((GuiLabel)element).setClickable(data != null);
			((GuiLabel)element).setFontSize(RWGui.ITEM_SIZE);
		}
		else if (element instanceof GuiPanel)
			((GuiPanel)element).setClickable(data != null);
		else if (element instanceof GuiTextField)
		{
			((GuiTextField)element).setClickable(data != null);
			((GuiTextField)element).setBorderThickness(1, false);
			((GuiTextField)element).setBackgroundPreset(1);
			((GuiTextField)element).setClickable(data != null);
			((GuiTextField)element).setEditable(data != null);
			((GuiTextField)element).setListenForInput(data != null);
		}
		element.setPivot(PivotPosition.TopLeft);
		super.addChild(element);
//		layout(true);
	}

	@Override
	public void removeChild(GuiElement element)
	{
		if (children == null || element == null)
			return;
		for (Pair<GuiElement,Pair<Integer,Object>> item : children)
			if (item.getL() == element)
			{
				children.remove(item);
//				layout(true);
				super.removeChild(element);
			}
	}

	public GuiLayout addNewLayoutChild(int layoutType, int layoutFlags)
	{
		GuiLayout	layout;
		if (layoutType == RWGui.LAYOUT_HORIZ)
			layout		= new GuiHorizontalLayout(layoutFlags);
		else
			layout		= new GuiVerticalLayout(layoutFlags);
		addChild(layout, null, null);
		return layout;
	}

	public Integer getItemId(GuiElement element)
	{
		for (Pair<GuiElement,Pair<Integer, Object>> item : children)
		{
			GuiElement	e	= item.getL();
			if (e instanceof GuiLayout)
			{
				Integer	id	= ((GuiLayout)e).getItemId(element);
				if (id != null)
					return id;
			}
			if (item.getL() == element)
				return item.getR().getL();
		}
		return null;
	}
}
