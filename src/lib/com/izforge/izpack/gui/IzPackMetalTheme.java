/*
 * IzPack Version 3.1.0 pre1 (build 2002.09.21)
 * Copyright (C) 2001,2002 Julien Ponge
 *
 * File :               IzPackMetalTheme.java
 * Description :        The metal theme for IzPack.
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

import java.awt.*;

import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;

public class IzPackMetalTheme extends DefaultMetalTheme
{
    //.....................................................................

    // The fields
    private ColorUIResource color; 
    private FontUIResource font1;
    private FontUIResource font2;

    // The constructor
    public IzPackMetalTheme()
    {
        super();
        
        color = new ColorUIResource(0, 0, 0);
        font1 = new FontUIResource(new Font("Dialog", Font.PLAIN, 12));
        font2 = new FontUIResource(new Font("Monospaced", Font.PLAIN, 12));
    }
    
    //.....................................................................
    // The methods

    public ColorUIResource getControlTextColor()
    {
        return color;
    }

    public ColorUIResource getMenuTextColor()
    {
        return color;
    }

    public ColorUIResource getSystemTextColor()
    {
        return color;
    }

    public ColorUIResource getUserTextColor()
    {
        return color;
    }

    public FontUIResource getControlTextFont()
    {
        return font1;
    }

    public FontUIResource getMenuTextFont()
    {
        return font1;
    }

    public FontUIResource getSystemTextFont()
    {
        return font1;
    }

    public FontUIResource getUserTextFont()
    {
        return font2;
    }

    public FontUIResource getWindowTitleFont()
    {
        return font1;
    }

    //.....................................................................
}

