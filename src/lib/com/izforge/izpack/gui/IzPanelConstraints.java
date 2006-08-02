package com.izforge.izpack.gui;

import java.awt.Component;
import java.awt.Rectangle;

public class IzPanelConstraints implements Cloneable, LayoutConstants
{

    /**
     * Current defined gaps. Here are the defaults which can be overwritten at the first call to
     * method getGap. The gap type will be determined by the array index and has to be synchron to
     * the gap identifier and the indices of array GAP_NAME_LOOK_UP
     */
    protected static int[] DEFAULT_Y_GAPS = { 0, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, -1, 0};

    protected static int[] DEFAULT_X_GAPS = { 0, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, -1, 0};
    
    protected static int[] DEFAULT_X_ALIGNMENT = { LEFT, LEFT, LEFT, LEFT};

    protected static int[] DEFAULT_Y_ALIGNMENT = { CENTER, CENTER, CENTER, CENTER};

    private int xCellAlignment = DEFAULT_X_ALIGNMENT[0];

    private int yCellAlignment = DEFAULT_Y_ALIGNMENT[0];

    private int xPos = 0;

    private int yPos = NEXT_ROW;

    private int xWeight = 1;

    private int yWeight = 1;

    private int xGap = DEFAULT_X_GAPS[-LABEL_GAP];

    private int yGap = DEFAULT_Y_GAPS[-LABEL_GAP];

    private double stretch = 0.0;
    
    private Rectangle bounds;

    /** for private use by the layout manager */
    Component component = null;

    public static IzPanelConstraints LABEL_CONSTRAINT = new IzPanelConstraints();


    public double getStretch()
    {
        return stretch;
    }

    public void setStretch(double stretch)
    {
        this.stretch = stretch;
    }

    public int getXGap()
    {
        return xGap;
    }

    public void setXGap(int gap)
    {
        xGap = gap;
    }

    public int getYGap()
    {
        return yGap;
    }

    public void setYGap(int gap)
    {
        yGap = gap;
    }

    public IzPanelConstraints(int xCellAlignment, int yCellAlignment, int xPos, int yPos,
            int xWeight, int yWeight, int xGap, int yGap, double stretch)
    {
        this.xCellAlignment = xCellAlignment;
        this.yCellAlignment = yCellAlignment;
        this.xPos = xPos;
        this.yPos = yPos;
        this.xWeight = xWeight;
        this.yWeight = yWeight;
        setXGap(xGap);
        setYGap(yGap);
        setStretch(stretch);
    }

    public IzPanelConstraints()
    {
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#clone()
     */
    public Object clone()
    {
        try
        {
            IzPanelConstraints c = (IzPanelConstraints) super.clone();
            return c;
        }
        catch (CloneNotSupportedException e)
        {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError();
        }
    }

    public int getXCellAlignment()
    {
        return xCellAlignment;
    }

    public void setXCellAlignment(int cellAlignment)
    {
        xCellAlignment = cellAlignment;
    }

    public int getXPos()
    {
        return xPos;
    }

    public void setXPos(int pos)
    {
        xPos = pos;
    }

    public int getXWeight()
    {
        return xWeight;
    }

    public void setXWeight(int weight)
    {
        xWeight = weight;
    }

    public int getYCellAlignment()
    {
        return yCellAlignment;
    }

    public void setYCellAlignment(int cellAlignment)
    {
        yCellAlignment = cellAlignment;
    }

    public int getYPos()
    {
        return yPos;
    }

    public void setYPos(int pos)
    {
        yPos = pos;
    }

    public int getYWeight()
    {
        return yWeight;
    }

    public void setYWeight(int weight)
    {
        yWeight = weight;
    }

    
    public Rectangle getBounds()
    {
        if(bounds != null )
            return(Rectangle) (bounds.clone());
        return( new Rectangle());
    }

    
    public void setBounds(Rectangle bounds)
    {
        this.bounds = (Rectangle) bounds.clone();
    }
}
