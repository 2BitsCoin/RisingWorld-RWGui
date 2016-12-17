package com.vistamaresoft.rwgui;

import java.util.ArrayList;
import com.vistamaresoft.rwgui.RWGui.Pair;
import net.risingworld.api.gui.GuiElement;
import net.risingworld.api.gui.PivotPosition;
import net.risingworld.api.utils.Vector2i;

/**
	A class implementing an automatic table layout in which children GuiElement's
	are arranged in rows and columns of cells.

	Children are added sequentially from the left cell of the first row tothe
	the right cell of the bottom row.
	<p>Children are added and removed with the usual addChild(GuiElement) and
	removeChild(GuiElement) methods.
	<p>Removing a child has the additional effect of 'shifting' all the following
	children one cell to the left and from the first cell of a row to the last cell
	of the previous row.
	<p>The table can have 'empty cells', by adding a null child in the proper
	sequence.
	<p>The layout sets the font size of GuiLabel's, the border thickness and
	the background of GuiTextField's as well as the clickable status of all
	children when each child is added to the layout. These properties, as well
	as other visual properties, can be changed after adding the child.
	<p>The layout also sets the pivot position of each child on adding it;
	changing it is possible, but it is likely to disrupt the proper child
	placement within the layout.
	<p>The layout sets the position of each child each time the layout() is
	called; setting those position manually has no effect.
 * <p><b>Important</b>: due to the way Rising World plug-ins are loaded,
 * <b>this class cannot instantiated or used in any way</b> from within the onEnable()
 * method of a plug-in, as it is impossible to be sure that, at that moment,
 * the RWGui plug-in has already been loaded.
 * <p>The first moment one can be sure that all plug-ins have been loaded, and
 * it is safe to use this class, is when (or after) the first player connects
 * to the server (either dedicated or local).
*/
public class GuiTableLayout extends GuiLayout
{
	private	int		maxNumOfCols, maxNumOfRows;
	private	int[]	colFlags, rowFlags;

	/**
	 * Creates a new table layout with up to maxNumOfCol columns and up to
	 * maxNumOfRows rows.
	 * <p>The number of columns and of rows cannot be modified after creation.
	 * @param maxNumOfCols
	 * @param maxNumOfRows
	 * @param flags
	 */
	public GuiTableLayout(int maxNumOfCols, int maxNumOfRows, int flags)
	{
		super(flags);
		this.maxNumOfCols	= maxNumOfCols;
		this.maxNumOfRows	= maxNumOfRows;
		colFlags	= new int[maxNumOfCols];
		for (int i = 0; i < maxNumOfCols; i++)
			colFlags[i]	= RWGui.LAYOUT_H_LEFT;
		rowFlags	= new int[maxNumOfRows];
		for (int i = 0; i < maxNumOfRows; i++)
			rowFlags[i]	= RWGui.LAYOUT_V_MIDDLE;
	}

	/**
	 * Set the alignment flag of a column, which affects the alignment of
	 * <b>all</b> the cells of the column.
	 * <p><b>Important</b>: this method shall be called <b>after</b>all the
	 * children have been added. New children added after calling this method
	 * will not be affected by it!
	 * @param	idx		the index of the column (0 = leftmost column)
	 * @param	flag	the alignment flags: one of RWGui.LAYOUT_H_LEFT (which
	 * 					is the default), RWGui.LAYOUT_H_CENTRE or
	 *					RWGui.LAYOUT_H_RIGHT.
	 */
	public void setColFlag(int idx, int flag)
	{
		if (idx >= 0 && idx < maxNumOfCols)
		{
			colFlags[idx]	= flag;
			if ( (flag & RWGui.LAYOUT_H_RIGHT) != 0)
				for (int i = idx; i < children.size(); i += maxNumOfCols)
					if (children.get(i) != null)
						children.get(i).getL().setPivot(PivotPosition.TopRight);
		}
	}

	/**
	 * Set the alignment flag of a row, which affects the alignment of
	 * <b>all</b> the cells of the column.
	 * <p><b>Important</b>: this method shall be called <b>after</b>all the
	 * children have been added. New children added after calling this method
	 * will not be affected by it!
	 * @param	idx		the index of the row (0 = topmost row)
	 * @param	flag	the alignment flags: one of RWGui.LAYOUT_V_TOP,
	 *					RWGui.LAYOUT_V_MIDDLE (which is the default) or
	 *					RWGui.LAYOUT_V_BOTTOM.
	 */
	public void setRowFlag(int idx, int flag)
	{
		if (idx >= 0 && idx < maxNumOfRows)
			rowFlags[idx]	= flag;
	}

	/**
	 * Overrides the corresponding method of GuiLayout to allow for null
	 * children. See it for details.
	 */
	@Override
	public void addChild(GuiElement element, Integer id, Object data)
	{
		if (children == null)
			children	= new ArrayList<Pair<GuiElement,Pair<Integer,Object>>>(maxNumOfCols * maxNumOfRows);
		if (children.size() < maxNumOfCols * maxNumOfRows)
		{
			if (element == null)
				children.add(null);
			else
				super.addChild(element, id, data);
		}
	}

	/**
		Arranges child elements in a table made of rows and columns

		As this method lays its children out recursively, it is usually
		necessary to call this method manually only for the top layout of a
		layout hierarchy.
	 */
	@Override
	void layout(int minWidth, int minHeight, boolean reset)
	{
		if (children == null || children.size() == 0)
			return;
		Vector2i[]	elemSizes	= new Vector2i[maxNumOfCols * maxNumOfRows];
		int			col, row;
		int			count;
		int[]		colWidths	= new int[maxNumOfCols];
		int[]		rowHeights	= new int[maxNumOfRows];
		int			height	= 0;
		int			width	= 0;
		if (reset)
		{
			minHeight	= height	= 0;
			minWidth	= width		= 0;
		}
		else
		{
			count	= 0;
			for (Pair<GuiElement,?> item : children)
			{
				if (item != null)
				{
					elemSizes[count]	= RWGui.getElementSizes(item.getL());
					col		= count % maxNumOfCols;
					row		= count / maxNumOfCols;
					if (colWidths[col] < elemSizes[count].x)
						colWidths[col]	= elemSizes[count].x;
					if (rowHeights[row] < elemSizes[count].y)
						rowHeights[row]	= elemSizes[count].y;
				}
				count++;
				// do not enumerate beyond the max expected number of cells
				if (count >= maxNumOfCols * maxNumOfRows)
					break;
			}
			count++;
		}

		count		= 0;
		// scan all table cells to collect cell sizes and max col width / row height
		for (Pair<GuiElement,?> item : children)
		{
			if (item != null)
			{
				GuiElement	element			= item.getL();
				col		= count % maxNumOfCols;
				row		= count / maxNumOfCols;
				if (element instanceof GuiLayout)
					((GuiLayout)element).layout(colWidths[col], rowHeights[row], reset);
				elemSizes[count]	= RWGui.getElementSizes(element);
				if (colWidths[col] < elemSizes[count].x)
					colWidths[col]	= elemSizes[count].x;
				if (rowHeights[row] < elemSizes[count].y)
					rowHeights[row]	= elemSizes[count].y;
			}
			count++;
			// do not enumerate beyond the max expected number of cells
			if (count >= maxNumOfCols * maxNumOfRows)
				break;
		}

		// compute (min) total table width and (min) total table height
		for (int i = 0; i < maxNumOfCols; i++)
		{
			width	+= colWidths[i] + padding;
		}
		width	+= margin * 2 - padding;	// add left and right margin and discount last right padding
		for (int i = 0; i < maxNumOfRows; i++)
		{
			height	+= rowHeights[i] + padding;
		}
		height	+= margin * 2 - padding;	// add top and bottom margin and discount last bottom padding

		int		spacingH	= padding;
		int		spacingW	= padding;
		// if layout dimensions are greater than min dimensions
		// compute spacing between columns and between rows
/*		if (minWidth > width)
		{
			spacingW	= (minWidth - width) / (maxNumOfCols - 1);
			width	= minWidth;
		}
		if (minHeight > height)
		{
			spacingH	= (minHeight - height) / (maxNumOfRows - 1);
			height	= minHeight;
		}
*/		// scan cells again to position each cell
		count	= 0;
		int		x			= margin;			// x and y point to the table top left corner
		int		y			= height - margin;
		for (Pair<GuiElement,?> item : children)
		{
			col		= count % maxNumOfCols;
			row		= count / maxNumOfCols;
			if (item != null)
			{
				// position the next table cell
				Vector2i	elementSizes	= elemSizes[count];
				// if the COLUMN is right-aligned, its cells are expected to have
				// PivotPosition.TopRight: position at right edge of column;
				// if it is centre-aligned, position cell left edge to result centred in column;
				// otherwise (left-aligned), position cell left edge at column left edge
				int	elX	= (colFlags[col] & RWGui.LAYOUT_H_RIGHT) != 0 ? colWidths[col] :
					( (colFlags[col] & RWGui.LAYOUT_H_CENTRE) != 0 ? (colWidths[col] - elementSizes.x) / 2 : 0);
				// if the ROW is bottom-aligned, position cell top edge a whole cell height above row bottom;
				// if it is middle-aligned, position cell top edge to result centred in row;
				// otherwise (top-aligned), position cell top edge at row top edge
				int	elY	= (rowFlags[row] & RWGui.LAYOUT_V_BOTTOM) != 0 ? -(rowHeights[row] - elementSizes.y) :
					( (colFlags[col] & RWGui.LAYOUT_V_MIDDLE) != 0 ? (elementSizes.y - rowHeights[row]) / 2 : 0);
				item.getL().setPosition(x + elX, y + elY, false);
			}
			x	+= colWidths[col] + spacingW;		// left edge of next column
			if (col == maxNumOfCols - 1)			// if number of column exceeded
			{
				x	= 0;							// left edge of first column...
				y	-= rowHeights[row] + spacingH;	// ... and top edge of next row (below)
			}
			count++;
			// do not enumerate beyond the max expected number of cells
			if (count >= maxNumOfCols * maxNumOfRows)
				break;
		}
		setSize(width, height, false);
	}
}
