/*
 * IzPack Version 3.0.0 (build 2002.08.13)
 * Copyright (C) 2001,2002 Julien Ponge
 *
 * File :               FrontendTab.java
 * Description :        The Frontend tab abstract class.
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

package com.izforge.izpack.frontend;

import com.izforge.izpack.*;
import com.izforge.izpack.gui.*;

import java.io.*;
import java.net.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import net.n3.nanoxml.*;

public abstract class FrontendTab extends JPanel
{
    //.....................................................................
    
    // The fields
    protected IconsDatabase icons;
    protected LocaleDatabase langpack;
    protected XMLElement installation;
    
    // The constructor
    public FrontendTab(XMLElement installation, IconsDatabase icons, 
                       LocaleDatabase langpack)
    {
        // Initialisations
        this.installation = installation;
        this.icons = icons;
        this.langpack = langpack;
    }
    
    //.....................................................................
    
    // Called when the installation XML tree is changed
    public void installationUpdated(XMLElement newXML)
    {
        this.installation = newXML;
        updateComponents();
    }
    
    // Updates the components
    public void updateComponents()
    {
        
    }
    
    // Updates the central XML tree
    public void updateXMLTree()
    {
        
    }
    
    //.....................................................................
}
