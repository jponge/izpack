/*
 * $Id:$
 * IzPack - Copyright 2001-2006 Julien Ponge, All Rights Reserved.
 * 
 * http://www.izforge.com/izpack/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2006 Klaus Bartz
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.izforge.izpack.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.LayoutManager2;
import java.awt.Rectangle;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.text.JTextComponent;

import com.izforge.izpack.util.MultiLineLabel;

public class IzPanelLayout implements LayoutManager, LayoutManager2, LayoutConstants
{

    /** holds all the components and layout constraints. */
    private ArrayList components = new ArrayList();

    /** Maximum rows to handle symbolic values like NEXT_ROW in constraints. */
    private int currentYPos = -1;

    /** Current column to handle symbolic values like NEXT_COLUMN in constraints. */
    private int currentXPos = -1;

    /** Dimension object with prefered size. Will be computed new if invalidateLayout will be called. */
    private Dimension prefLayoutDim;

    private Dimension oldParentSize;
    
    private Insets oldParentInsets;

    public static IzPanelConstraints DEFAULT_CONSTRAINTS[] = {
            new IzPanelConstraints(DEFAULT_LABEL_ALIGNMENT, DEFAULT_LABEL_ALIGNMENT, NEXT_COLUMN,
                    CURRENT_ROW, 1, 1, LABEL_GAP, LABEL_GAP, 0.0),
            new IzPanelConstraints(DEFAULT_TEXT_ALIGNMENT, DEFAULT_TEXT_ALIGNMENT, NEXT_COLUMN,
                    CURRENT_ROW, 1, 1, TEXT_GAP, TEXT_GAP, 0.0),
            new IzPanelConstraints(DEFAULT_CONTROL_ALIGNMENT, DEFAULT_CONTROL_ALIGNMENT,
                    NEXT_COLUMN, CURRENT_ROW, 10, 10, CONTROL_GAP, CONTROL_GAP, 0.0),
            new IzPanelConstraints(DEFAULT_LABEL_ALIGNMENT, DEFAULT_LABEL_ALIGNMENT, 0, NEXT_ROW,
                    10, 10, LABEL_GAP, LABEL_GAP, 0.7),
            new IzPanelConstraints(DEFAULT_LABEL_ALIGNMENT, DEFAULT_LABEL_ALIGNMENT, NEXT_COLUMN,
                    CURRENT_ROW, 1, 1, 0, 0, 0.0),
            new IzPanelConstraints(DEFAULT_LABEL_ALIGNMENT, DEFAULT_LABEL_ALIGNMENT,
                    CURRENT_COLUMN, NEXT_ROW, 1, 1, 0, 0, 0.0)

    };

    /** Anchor to be used for the controls in all panels. */
    private static int ANCHOR = CENTER;

    private static int X_STRETCH_TYPE = RELATIVE_STRETCH;

    private static final int[][] GAP_INTERMEDIAER_LOOKUP = {
            { LABEL_GAP, LABEL_TO_TEXT_GAP, LABEL_TO_CONTROL_GAP, LABEL_GAP},
            { TEXT_TO_LABEL_GAP, TEXT_GAP, TEXT_TO_CONTROL_GAP, TEXT_GAP},
            { CONTROL_TO_LABEL_GAP, CONTROL_TO_TEXT_GAP, CONTROL_GAP, CONTROL_GAP},
            { NO_GAP, NO_GAP, NO_GAP, NO_GAP}};

    /**
     * Default constructor
     */
    public IzPanelLayout()
    {
    }

        public static int getYGap(IzPanelConstraints curConst, IzPanelConstraints nextYConst)
    {

        Class nextClass = (nextYConst != null) ? nextYConst.component.getClass()
                : DummyComponent.class;
        int interId = GAP_INTERMEDIAER_LOOKUP[getIntermediarId(curConst.component.getClass(), false)][getIntermediarId(nextClass, false)];
        return (IzPanelConstraints.DEFAULT_Y_GAPS[interId]);

    }

    public static int getXGap(IzPanelConstraints curConst, IzPanelConstraints nextXConst)
    {

        Class nextClass = (nextXConst != null) ? nextXConst.component.getClass()
                : DummyComponent.class;
        int interId = GAP_INTERMEDIAER_LOOKUP[getIntermediarId(curConst.component.getClass(), false)][getIntermediarId(nextClass, false)];
        return (IzPanelConstraints.DEFAULT_X_GAPS[interId]);

    }

    private static int getIntermediarId(Class clazz, boolean ext)
    {
        if(ext)
        {
            if (MultiLineLabel.class.isAssignableFrom(clazz)) return (3);
            if (DummyComponent.class.isAssignableFrom(clazz)) return (4);
        }
        if (JLabel.class.isAssignableFrom(clazz)) return (0);
        if (JTextComponent.class.isAssignableFrom(clazz)) return (1);
        if (DummyComponent.class.isAssignableFrom(clazz)) return (3);
        return (2); // Other controls.
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.LayoutManager#addLayoutComponent(java.lang.String, java.awt.Component)
     */
    public void addLayoutComponent(String name, Component comp)
    {
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.LayoutManager#removeLayoutComponent(java.awt.Component)
     */
    public void removeLayoutComponent(Component comp)
    {
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.LayoutManager#minimumLayoutSize(java.awt.Container)
     */
    public Dimension minimumLayoutSize(Container parent)
    {
        return preferredLayoutSize(parent);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.LayoutManager#preferredLayoutSize(java.awt.Container)
     */
    public Dimension preferredLayoutSize(Container parent)
    {
        return (determineSize());
    }

    /**
     * Method which determine minimum with and height of this layout. The size will be stored after
     * cumputing in a class member. With a call to invalidateLayout this will be deleted and at the
     * next call to this method the values are computed again.
     * 
     * @return current minimum size
     */
    private Dimension determineSize()
    {
        if (prefLayoutDim == null)
        {
            int width = minimumAllColumnsWidth();
            int height = minimumOverallHeight();
            prefLayoutDim = new Dimension(width, height);
        }
        return (Dimension) (prefLayoutDim.clone());
    }

    /**
     * Returns the number of rows that need to be laid out.
     */
    private int rows()
    {
        int maxRows = 0;
        for (int i = 0; i < components.size(); ++i)
        {
            int curRows = ((ArrayList) components.get(i)).size();
            if (curRows > maxRows) maxRows = curRows;

        }
        return (maxRows);
    }

    /**
     * Returns the number of columns that need to be laid out.
     */
    private int columns()
    {
        return (components.size());
    }

    /**
     * Minimum height of all rows.
     * 
     * @return minimum height of all rows
     */
    private int minimumOverallHeight()
    {
        int height = 0;

        for (int i = 0; i < rows(); i++)
        {
            height += rowHeight(i);
        }

        return (height);
    }

    /**
     * Measures and returns the minimum height required to render the components in the indicated
     * row.
     * 
     * @param row the index of the row to measure
     */
    private int rowHeight(int row)
    {
        int height = 0;
        for (int i = 0; i < components.size(); ++i)
        {
            int retval = cellSize(row, i).height;
            if (retval > height) height = retval;
        }
        return (height);
    }

    /**
     * Measures and returns the minimum size required to render the component in the indicated row
     * and column.
     * 
     * @param row the index of the row to measure
     * @param column the column of the component
     */
    private Dimension cellSize(int row, int column)
    {
        Dimension retval = new Dimension();
        Component component;
        IzPanelConstraints constraints;

        try
        {
            constraints = getConstraint(column, row);
            if (constraints != null)
            {
                component = constraints.component;
                Dimension dim = component.getMinimumSize();
                retval.height = dim.height;
                retval.width = dim.width;
                if (needsReEvaluation(component)) retval.width = 0;
            }
        }
        // ----------------------------------------------------
        // we might get an exception if one of the array list is
        // shorter, because we index out of bounds. If there
        // is nothing there then the height is 0, nothing
        // further to worry about!
        // ----------------------------------------------------
        catch (Throwable exception)
        {}

        return (retval);
    }

    /**
     * Returns the minimum width of the column requested. This contains not the gaps.
     * 
     * @param column the columns to measure
     * 
     * @return the minimum width required to fit the components in this column
     */
    private int minimumColumnWidth(int column)
    {
        int maxWidth = 0;
        for (int i = 0; i < rows(); ++i)
        {
            Dimension cs = cellSize(i, column);
            if (maxWidth < cs.width) maxWidth = cs.width;
        }
        return (maxWidth);
    }

    /**
     * Returns the minimum width needed by all columns
     * 
     * @return
     */
    private int minimumAllColumnsWidth()
    {
        int width = 0;
        for (int i = 0; i < this.components.size(); ++i)
            width += minimumColumnWidth(i);
        return (width);
    }

    /**
     * Returns the constraint object of the component at the given place.
     * 
     * @param col column of the component
     * @param row row of the component
     * @return the constraint object of the component at the given place
     */
    private IzPanelConstraints getConstraint(int col, int row)
    {
        Object obj = components.get(col);
        if (obj != null && obj instanceof ArrayList)
            obj = ((ArrayList) components.get(col)).get(row);
        if (obj != null) return ((IzPanelConstraints) obj);
        return (null);
    }

    private int getAdaptedXPos(int xpos, int curWidth, Dimension curDim,
            IzPanelConstraints currentConst)
    {
        int adaptedXPos = xpos + currentConst.getXGap();
        switch (currentConst.getXCellAlignment())
        {
        case LEFT:
            break;
        case RIGHT:
            adaptedXPos += curWidth - curDim.width;
            break;
        case CENTER:
        default:
            adaptedXPos += (curWidth - curDim.width) / 2;
            break;

        }
        return (adaptedXPos);

    }

    private int getAdaptedYPos(int ypos, int curHeight, Dimension curDim,
            IzPanelConstraints currentConst)
    {
        int adaptedYPos = ypos + currentConst.getYGap();
        switch (currentConst.getYCellAlignment())
        {
        case TOP:
            break;
        case BOTTOM:
            adaptedYPos += curHeight - curDim.height;
            break;
        case CENTER:
        default:
            adaptedYPos += (curHeight - curDim.height) / 2;
            break;

        }
        return (adaptedYPos);
    }

    private void resolveDefaultSettings(int col, int row)
    {
        IzPanelConstraints currentConst = getConstraint(col, row);
        IzPanelConstraints nextYConst = (row < rows() - 1) ? getConstraint(col, row + 1) : null;
        IzPanelConstraints nextXConst = (col < columns() - 1) ? getConstraint(col + 1, row) : null;
        int gap = currentConst.getYGap();
        if (gap == AUTOMATIC_GAP)
        { // Automatic gap; determine now.
            currentConst.setYGap(getYGap(currentConst, nextYConst));
        }
        else if (gap < 0)
        {
            currentConst.setYGap(IzPanelConstraints.DEFAULT_Y_GAPS[-gap]);
        }
        gap = currentConst.getXGap();
        if (gap == AUTOMATIC_GAP)
        { // Automatic gap; determine now.
            currentConst.setXGap(getXGap(currentConst, nextXConst));
        }
        else if (gap < 0)
        {
            currentConst.setXGap(IzPanelConstraints.DEFAULT_X_GAPS[-gap]);
        }

        if (currentConst.getXCellAlignment() < 0)
        {
            currentConst.setXCellAlignment(IzPanelConstraints.DEFAULT_X_ALIGNMENT[-currentConst
                    .getXCellAlignment()]);
        }
        if (currentConst.getYCellAlignment() < 0)
        {
            currentConst.setYCellAlignment(IzPanelConstraints.DEFAULT_Y_ALIGNMENT[-currentConst
                    .getYCellAlignment()]);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.LayoutManager#layoutContainer(java.awt.Container)
     */
    public void layoutContainer(Container parent)
    {
        if( ! needNewLayout(parent ))
        {
            fastLayoutContainer(parent);
            return;
        }
        prefLayoutDim = null;
        preferredLayoutSize(parent);
        Dimension realSizeDim = parent.getSize();
        Insets insets = parent.getInsets();

        int rowHeight;
        int onceAgain = 0;
        int[] generellOffset = new int[] { 0, 0};
        // int generellYOffset = 0;
        // int generellXOffset = 0;
        int maxWidth = 0;
        int overallHeight = 0;
        int anchorNeedsReEval = 0;
        Rectangle curRect = new Rectangle();
        while (anchorNeedsReEval < 2)
        {
            int ypos = insets.top;

            int row = 0;
            int minWidth = 0xffff;
            int minHeight = 0xffff;
            maxWidth = 0;
            overallHeight = 0;
            while (row < rows())
            {
                int outerRowHeight = 0;
                int xpos = insets.left;
                rowHeight = rowHeight(row);
                int col = 0;
                IzPanelConstraints[] colConstraints = new IzPanelConstraints[columns()];
                int usedWidth = 0;
                Dimension curDim;
                while (col < columns())
                {
                    resolveDefaultSettings(col, row);
                    IzPanelConstraints currentConst = getConstraint(col, row);
                    colConstraints[col] = currentConst;
                    Component currentComp = currentConst.component;
                    curDim = currentComp.getPreferredSize();
                    int curWidth = minimumColumnWidth(col);
                    col++;
                    if (currentConst.getXWeight() > 1)
                    {
                        int weight = currentConst.getXWeight();
                        while (weight > 1 && col < columns())
                        {
                            colConstraints[col] = getConstraint(col, row);
                            if (!(colConstraints[col].component instanceof DummyComponent)) break;
                            curWidth += minimumColumnWidth(col);
                            col++;
                            weight--;
                        }
                    }
                    // width known
                    int adaptedXPos = getAdaptedXPos(xpos, curWidth, curDim, currentConst);
                    int adaptedYPos = getAdaptedYPos(ypos, rowHeight, curDim, currentConst);
                    currentComp.setBounds(adaptedXPos + generellOffset[0], ypos
                            + currentConst.getYGap() + generellOffset[1], curWidth, rowHeight);
                    currentComp.getBounds(curRect);
                    
                    if (!(currentComp instanceof DummyComponent))
                    {
                        if (curRect.x < minWidth) minWidth = curRect.x;
                        if (curRect.y < minHeight) minHeight = curRect.y;
                    }
                    int curMax = (int) curRect.getMaxX();
                    if (curMax - minWidth > maxWidth) maxWidth = curMax - minWidth;
                    curMax = (int) curRect.getMaxY();
                    currentConst.setBounds(curRect);
                    if (curMax - minHeight > overallHeight) overallHeight = curMax - minHeight;
                    xpos += currentComp.getSize().width + currentConst.getXGap();
                    usedWidth += curWidth;
                    if (outerRowHeight < rowHeight + currentConst.getYGap())
                        outerRowHeight = rowHeight + currentConst.getYGap();
                }
                // Now we have made a row, but may be there are place across or/and a component
                // needs a reevaluation.
                double rowStretch = 0.0;
                int i;
                // Determine hole stretch of this row.
                for (i = 0; i < colConstraints.length; ++i)
                {
                    rowStretch += colConstraints[i].getStretch();
                }
                // Modify rowStretch depending on the current X-Stretch type.
                if (rowStretch > 0.0)
                {
                    switch (IzPanelLayout.getXStretchType())
                    {
                    case RELATIVE_STRETCH:
                        break;
                    case ABSOLUTE_STRETCH:
                        rowStretch = 1.0;
                        break;
                    case NO_STRETCH:
                    default:
                        rowStretch = 0.0;
                        break;
                    }
                }
                if (realSizeDim.width - insets.right > xpos && rowStretch > 0.0)
                { // Compute only if there is space to share and at least one control should be
                    // stretched.
                    int pixel = realSizeDim.width - insets.right - xpos; // How many pixel we
                    // can use for stretching.
                    int offset = 0;
                    int oldOnceAgain = onceAgain;
                    for (i = 0; i < colConstraints.length; ++i)
                    {
                        int curPixel = (int) ((colConstraints[i].getStretch() / rowStretch) * pixel);

                        Rectangle curBounds = colConstraints[i].component.getBounds();
                        int newWidth = curPixel + curBounds.width;
                        Dimension oldDim = colConstraints[i].component.getPreferredSize();

                        colConstraints[i].component.setBounds(curBounds.x + offset, curBounds.y,
                                newWidth, curBounds.height);
                        colConstraints[i].component.getBounds(curRect);
                        if (!(colConstraints[i].component instanceof DummyComponent))
                        {
                            if (curRect.x < minWidth) minWidth = curRect.x;
                            if (curRect.y < minHeight) minHeight = curRect.y;
                        }
                        int curMax = (int) curRect.getMaxX();
                        if (curMax - minWidth > maxWidth) maxWidth = curMax - minWidth;
                        curMax = (int) curRect.getMaxY();
                        colConstraints[i].setBounds(curRect);
                        
                        if (curMax - minHeight > overallHeight) overallHeight = curMax - minHeight;

                        offset += curPixel;
                        if (needsReEvaluation(colConstraints[i].component))
                        {
                            if (oldDim.height != colConstraints[i].component.getPreferredSize().height
                                    && oldOnceAgain == onceAgain) onceAgain++;
                        }
                    }

                }
                // Seems so that height has changed. Reevaluate only one time else it is possible
                // to go in a endless loop.

                if (onceAgain == 1) continue;
                onceAgain = 0;
                ypos = ypos + outerRowHeight;
                row++;
            }
            anchorNeedsReEval += resolveGenerellOffsets(generellOffset, realSizeDim, insets,
                    maxWidth, overallHeight);

        }
    }

    private void fastLayoutContainer(Container parent)
    {
        for( int row = 0; row < rows(); ++ row)
        {
            for( int col = 0; col < columns(); ++ col)
            {
                IzPanelConstraints currentConst = getConstraint(col, row);
                currentConst.component.setBounds(currentConst.getBounds());
                
            }
            
        }
    }

    private boolean needNewLayout(Container parent)
    {
        Dimension ops = oldParentSize;
        Insets opi = oldParentInsets;
        oldParentSize = parent.getSize();
        oldParentInsets = parent.getInsets();
        if( opi == null || opi == null)
            return(true);
        if( ops.equals(parent.getSize()) && opi.equals(parent.getInsets()))
            return(false);
        return(true);
        
    }

    private int resolveGenerellOffsets(int[] generellOffset, Dimension realSizeDim, Insets insets,
            int maxWidth, int overallHeight)
    {
        int retval = 1;
        switch (getAnchor())
        {
        case CENTER:
            generellOffset[0] = (realSizeDim.width - insets.left - insets.right - maxWidth) / 2;
            generellOffset[1] = (realSizeDim.height - insets.top - insets.bottom - overallHeight) / 2;
            break;
        case WEST:
            generellOffset[0] = 0;
            generellOffset[1] = (realSizeDim.height - insets.top - insets.bottom - overallHeight) / 2;
            break;
        case EAST:
            generellOffset[0] = realSizeDim.width - insets.left - insets.right - maxWidth;
            generellOffset[1] = (realSizeDim.height - insets.top - insets.bottom - overallHeight) / 2;
            break;
        case NORTH:
            generellOffset[0] = (realSizeDim.width - insets.left - insets.right - maxWidth) / 2;
            generellOffset[1] = 0;
            break;
        case SOUTH:
            generellOffset[0] = (realSizeDim.width - insets.left - insets.right - maxWidth) / 2;
            generellOffset[1] = realSizeDim.height - insets.top - insets.bottom - overallHeight;
            break;
        case NORTH_WEST:
            generellOffset[0] = 0;
            generellOffset[1] = 0;
            retval = 2;
            break;
        case NORTH_EAST:
            generellOffset[0] = realSizeDim.width - insets.left - insets.right - maxWidth;
            generellOffset[1] = 0;
            break;
        case SOUTH_WEST:
            generellOffset[0] = 0;
            generellOffset[1] = realSizeDim.height - insets.top - insets.bottom - overallHeight;
            break;
        case SOUTH_EAST:
            generellOffset[0] = realSizeDim.width - insets.left - insets.right - maxWidth;
            generellOffset[1] = realSizeDim.height - insets.top - insets.bottom - overallHeight;
            break;

        }
        return (retval);
    }

    /**
     * Returns whether the type of component needs potential a reevaluation or not.
     * 
     * @param comp component to check
     * @return whether the type of component needs potential a reevaluation or not
     */
    private boolean needsReEvaluation(Component comp)
    {
        return (comp instanceof com.izforge.izpack.util.MultiLineLabel);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.LayoutManager2#getLayoutAlignmentX(java.awt.Container)
     */
    public float getLayoutAlignmentX(Container target)
    {
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.LayoutManager2#getLayoutAlignmentY(java.awt.Container)
     */
    public float getLayoutAlignmentY(Container target)
    {
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.LayoutManager2#invalidateLayout(java.awt.Container)
     */
    public void invalidateLayout(Container target)
    {
        //prefLayoutDim = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.LayoutManager2#maximumLayoutSize(java.awt.Container)
     */
    public Dimension maximumLayoutSize(Container target)
    {
        return (minimumLayoutSize(target));
    }

    public void addLayoutComponent(Component comp, Object constraints)
    {
        if( comp == null )  throw new NullPointerException("component has to be not null");
        if (!(constraints instanceof IzPanelConstraints ))
        {
            Object oldVal = constraints;
            constraints = IzPanelLayout.DEFAULT_CONSTRAINTS[getIntermediarId(comp.getClass(), true)];
            if( NEXT_LINE.equals(oldVal))
            {
                ((IzPanelConstraints) constraints).setXPos(0);
                ((IzPanelConstraints) constraints).setYPos(NEXT_ROW);
            }
        }
        IzPanelConstraints cc = (IzPanelConstraints) ((IzPanelConstraints) constraints).clone();
        cc.component = comp;
        int i;
        // Modify positions if constraint value is one of the symbolic ints.
        int yPos = cc.getYPos();
        if (yPos == IzPanelConstraints.NEXT_ROW) yPos = currentYPos + 1;
        if (yPos == IzPanelConstraints.CURRENT_ROW) yPos = currentYPos;
        cc.setYPos(yPos);
        int xPos = cc.getXPos();
        if (xPos == IzPanelConstraints.NEXT_COLUMN) xPos = currentXPos + 1;
        if (xPos == IzPanelConstraints.CURRENT_COLUMN) xPos = currentXPos;
        cc.setXPos(xPos);
        // Now we know real x and y position. If needed, expand array or
        // array of array.
        if (components.size() <= cc.getXPos())
        {
            for (i = components.size() - 1; i < cc.getXPos(); ++i)
                components.add(new ArrayList());
        }
        ArrayList xComp = (ArrayList) components.get(xPos);

        if (xComp.size() <= yPos)
        {
            for (i = xComp.size() - 1; i < yPos - 1; ++i)
            {
                IzPanelConstraints dc = getDefaultConstraint(XDUMMY_CONSTRAINT);
                dc.component = new DummyComponent();
                xComp.add(dc);

            }
        }
        xComp.add(yPos, cc);
        if (currentYPos < xComp.size() - 1) currentYPos = xComp.size() - 1;
        currentXPos = xPos;

    }

    public static IzPanelConstraints getDefaultConstraint(int type)
    {
        return ((IzPanelConstraints) DEFAULT_CONSTRAINTS[type].clone());
    }

    /**
     * Component which will be used as placeholder if not extern component will be set.
     * 
     * @author Klaus Bartz
     * 
     */
    private static class DummyComponent extends Component
    {

        public Dimension getMinimumSize()
        {
            return (new Dimension(0, 0));
        }

        public Dimension getPreferredSize()
        {
            return getMinimumSize();
        }

        public Dimension getMaximumSize()
        {
            return getMinimumSize();
        }

        public Rectangle getBounds()
        {
            return (getBounds(new Rectangle()));
        }

        public Rectangle getBounds(Rectangle rect)
        {
            Rectangle rv = (rect != null) ? rect : new Rectangle();
            rv.setBounds(0, 0, 0, 0);
            return (rv);
        }

    }

    /**
     * Returns the anchor constant.
     * 
     * @return the anchor constant
     */
    public static int getAnchor()
    {
        return ANCHOR;
    }

    /**
     * Sets the anchor constant.
     * 
     * @param anchor symbolic constant to be used
     */
    public static void setAnchor(int anchor)
    {
        ANCHOR = anchor;
    }

    /**
     * Returns the current used type of stretching for the X-direction. Possible values are NO,
     * RELATIVE and ABSOLUTE.
     * 
     * @return the current used type of stretching for the X-direction
     */
    public static int getXStretchType()
    {
        return X_STRETCH_TYPE;
    }

    /**
     * Sets the type of stretching to be used for the X-Direction. Possible values are NO, RELATIVE
     * and ABSOLUTE.
     * 
     * @param x_stretch constant to be used for stretch type
     */
    public static void setXStretchType(int x_stretch)
    {
        X_STRETCH_TYPE = x_stretch;
    }

}
