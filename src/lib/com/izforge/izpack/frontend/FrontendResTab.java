/*
 * IzPack Version 3.0.0 rc3 (build 2002.07.28)
 * Copyright (C) 2001,2002 Julien Ponge
 *
 * File :               FrontendResTab.java
 * Description :        The Frontend resources tab class.
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

public class FrontendResTab extends FrontendTab implements ActionListener
{
    //.....................................................................
    
    // The fields
    private GridBagLayout layout;
    private GridBagConstraints gbConstraints;
    private JList resList;
    private HighlightJButton addButton;
    private HighlightJButton delButton;
    private Vector resources;
    
    // The constructor
    public FrontendResTab(XMLElement installation, IconsDatabase icons, 
                          LocaleDatabase langpack)
    {
        super(installation, icons, langpack);
        
        // We initialize our layout
        layout = new GridBagLayout();
        gbConstraints = new GridBagConstraints();
        gbConstraints.insets = new Insets(5, 5, 5, 5);
        setLayout(layout);
        
        buildGUI();
    }
    
    //.....................................................................
    
    // Builds our GUI
    private void buildGUI()
    {
        // Usefull stuffs
        JLabel label;
        JScrollPane scroller;
        
        label = new JLabel(langpack.getString("tabs.res.list"));
        FrontendFrame.buildConstraints(gbConstraints, 0, 0, 1, 1, 1.0, 0.0);
        gbConstraints.fill = GridBagConstraints.NONE;
        gbConstraints.anchor = GridBagConstraints.SOUTHWEST;
        layout.addLayoutComponent(label, gbConstraints);
        add(label);
        
        resList = new JList();
        resList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resList.setCellRenderer(FrontendFrame.LIST_RENDERER);
        scroller = new JScrollPane(resList);
        scroller.setPreferredSize(new Dimension(40, 200));
        FrontendFrame.buildConstraints(gbConstraints, 0, 1, 1, 2, 1.0, 0.0);
        gbConstraints.fill = GridBagConstraints.BOTH;
        gbConstraints.anchor = GridBagConstraints.CENTER;
        layout.addLayoutComponent(scroller, gbConstraints);
        add(scroller);
        
        addButton = new HighlightJButton(langpack.getString("tabs.res.add"),
                                         icons.getImageIcon("new"),
                                         FrontendFrame.buttonsHColor);
        addButton.addActionListener(this);
        FrontendFrame.buildConstraints(gbConstraints, 1, 1, 1, 1, 0.0, 0.0);
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        gbConstraints.anchor = GridBagConstraints.NORTHWEST;
        layout.addLayoutComponent(addButton, gbConstraints);
        add(addButton);
        
        delButton = new HighlightJButton(langpack.getString("tabs.res.remove"),
                                         icons.getImageIcon("delete"),
                                         FrontendFrame.buttonsHColor);
        delButton.addActionListener(this);
        FrontendFrame.buildConstraints(gbConstraints, 1, 2, 1, 1, 0.0, 0.0);
        gbConstraints.anchor = GridBagConstraints.SOUTHWEST;
        layout.addLayoutComponent(delButton, gbConstraints);
        add(delButton);
    }
    
    // Updates the components
    public void updateComponents()
    {
        // We get our base XML root
        XMLElement root = installation.getFirstChildNamed("resources");
        
        // We fill our vector
        resources = new Vector();
        Vector res = root.getChildrenNamed("res");
        int size = res.size();
        for (int i = 0; i < size; i++)
        {
            XMLElement resource = (XMLElement) res.get(i);
            Resource r = new Resource(resource.getAttribute("src"),
                                      resource.getAttribute("id"));
            resources.add(r);
        }
        
        // We put it in the list
        resList.setListData(resources);
    }
    
    // Action events handler
    public void actionPerformed(ActionEvent e)
    {
        // We get the source
        Object source = e.getSource();
        
        // We act depending of the source
        if (source == addButton)
        {
            // We get the new resource attributes
            String inputValue;
            XMLElement res = new XMLElement("res");
            inputValue = JOptionPane.showInputDialog(langpack.getString("tabs.res.input"));
            if (inputValue == null) 
                return;
            else
                res.setAttribute("id", inputValue);
            JFileChooser fileChooser = new JFileChooser(Frontend.lastDir);
            fileChooser.addChoosableFileFilter(fileChooser.getAcceptAllFileFilter());
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
            {
                Frontend.lastDir = fileChooser.getSelectedFile().getParentFile().getAbsolutePath();
                res.setAttribute("src", fileChooser.getSelectedFile().getAbsolutePath());
                
                // We add it
                installation.getFirstChildNamed("resources").addChild(res);
            }
        }
        else if (source == delButton)
        {
            // We get the selected element index
            int index = resList.getSelectedIndex();
            if (index == -1) return;
            
            // We remove it
            XMLElement root = installation.getFirstChildNamed("resources");
            root.removeChildAtIndex(index);
        }
        
        updateComponents();
    }
    
    //.....................................................................
    
    // Represents a resource
    class Resource
    {
        public String src;
        public String id;
        
        public Resource(String src, String id)
        {
            this.src = src;
            this.id = id;
        }
        
        public String toString()
        {
            return (id + " : " + src);
        }
    }
    
    //.....................................................................
}
