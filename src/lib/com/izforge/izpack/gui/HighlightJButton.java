/*
 * IzPack Version 3.1.0 pre1 (build 2002.09.21)
 * Copyright (C) 2001,2002 Julien Ponge
 *
 * File :               HighlightJButton.java
 * Description :        A button that highlights when the mouse passes over.
 * Author's email :     julien@izforge.com
 * Author's Website :   http://www.izforge.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */ 
 
package com.izforge.izpack.gui;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

public class HighlightJButton extends JButton
{
    //.....................................................................
    
    // The constructors
    public HighlightJButton(Icon icon, Color color)
    {
        super(icon);
        initButton(color);
    }

    public HighlightJButton(String text, Color color)
    {
        super(text);
        initButton(color);
    }

    public HighlightJButton(String text, Icon icon, Color color)
    {
        super(text, icon);
        initButton(color);
    }
    
    public HighlightJButton(Action a, Color color)
    {
        super(a);
        initButton(color);
    }

    //.....................................................................
    // The methods 
    
    // Does the extra initialisations
    protected void initButton(Color highlightColor)
    {
        this.highlightColor = highlightColor;
        defaultColor = getBackground();

        addMouseListener(new MouseHandler());
    }

    // Overriden to ensure that the button won't stay highlighted if it had the mouse over
    public void setEnabled(boolean b)
    {
        reset();
        super.setEnabled(b);
    }

    // Forces the button to unhighlight
    public void reset()
    {
        setBackground(defaultColor);
    }
    
    //.....................................................................
    // The fields
    
    private Color highlightColor;  // The highlighted color
    private Color defaultColor;    // The default color
    
    //.....................................................................
    
    // The mouse handler which makes the highlighting
    private class MouseHandler extends MouseAdapter
    {
        // The mouse passes over the button
        public void mouseEntered(MouseEvent e)
        {
            if (isEnabled()) setBackground(highlightColor);
        }

        // The mouse passes out of the button
        public void mouseExited(MouseEvent e)
        {
            if (isEnabled()) setBackground(defaultColor);
        }
    }
    
    //.....................................................................
}
