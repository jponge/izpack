/*
 * IzPack Version 3.0.0 rc1 (build 2002.07.03)
 * Copyright (C) 2001,2002 Julien Ponge
 *
 * File :               HelloPanel.java
 * Description :        A panel to welcome the user.
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

package com.izforge.izpack.panels;

import com.izforge.izpack.*;
import com.izforge.izpack.gui.*;
import com.izforge.izpack.installer.*;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

public class HelloPanel extends IzPanel
{
    //.....................................................................
    
    // The fields
    private BoxLayout layout;
    private JLabel welcomeLabel;
    private JLabel appAuthorsLabel;
    private JLabel appURLLabel;
    
    
    // The constructor
    public HelloPanel(InstallerFrame parent, InstallData idata)
    {
        super(parent, idata);
        
        // The 'super' layout
        GridBagLayout superLayout = new GridBagLayout();
        setLayout(superLayout);
        GridBagConstraints gbConstraints = new GridBagConstraints();
        gbConstraints.insets = new Insets(0, 0, 0, 0);
        gbConstraints.fill = GridBagConstraints.NONE;
        gbConstraints.anchor = GridBagConstraints.CENTER;
        
        // We initialize our 'real' layout
        JPanel centerPanel = new JPanel();
        layout = new BoxLayout(centerPanel, BoxLayout.Y_AXIS);
        centerPanel.setLayout(layout);
        superLayout.addLayoutComponent(centerPanel, gbConstraints);
        add(centerPanel);
        
        // We create and put the labels
        String str;
        
        centerPanel.add(Box.createVerticalStrut(10));
        
        str = parent.langpack.getString("HelloPanel.welcome1") + idata.info.getAppName() +
              " " + idata.info.getAppVersion() + parent.langpack.getString("HelloPanel.welcome2");
        welcomeLabel = new JLabel(str, parent.icons.getImageIcon("host"), JLabel.TRAILING);
        centerPanel.add(welcomeLabel);
        
        centerPanel.add(Box.createVerticalStrut(20));
        
        str = parent.langpack.getString("HelloPanel.authors");
        appAuthorsLabel = new JLabel(str, parent.icons.getImageIcon("information"), 
                                     JLabel.TRAILING);
        centerPanel.add(appAuthorsLabel);
        
        ArrayList authors = idata.info.getAuthors();
        int size = authors.size();
        JLabel label;
        for (int i = 0; i < size; i++)
        {
            Info.Author a = (Info.Author) authors.get(i);
            label = new JLabel(" - " + a.getName() + " <" + a.getEmail() + ">", 
                               parent.icons.getImageIcon("empty"), JLabel.TRAILING);
            centerPanel.add(label);
        }
        
        centerPanel.add(Box.createVerticalStrut(20));
        
        str = parent.langpack.getString("HelloPanel.url") + idata.info.getAppURL();
        appURLLabel = new JLabel(str, parent.icons.getImageIcon("bookmark"), JLabel.TRAILING);
        centerPanel.add(appURLLabel);
    }
    
    //.....................................................................
    
    //.....................................................................
    // The methods
    
    // Indicates wether the panel has been validated or not
    public boolean isValidated()
    {
        return true;
    }
    
    //.....................................................................
}
