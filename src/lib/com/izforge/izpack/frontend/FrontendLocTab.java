/*
 * IzPack Version 3.0.0 rc2 (build 2002.07.06)
 * Copyright (C) 2001,2002 Julien Ponge
 *
 * File :               FrontendLocTab.java
 * Description :        The Frontend locale tab class.
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

public class FrontendLocTab extends FrontendTab implements ActionListener
{
    //.....................................................................
    
    // The fields
    private GridBagLayout layout;
    private GridBagConstraints gbConstraints;
    private JList availableList;
    private JList selectedList;
    private HighlightJButton includeButton;
    private HighlightJButton removeButton;
    private Vector avPacks;
    private Vector selPacks;
    private int curAv = -1;
    private int curSel = -1;
    
    // The constants
    private static String LOC_DIR = Frontend.IZPACK_HOME + "bin" + File.separator + 
                                "langpacks" + File.separator + "installer";
    
    // The constructor
    public FrontendLocTab(XMLElement installation, IconsDatabase icons, 
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
        
        label = new JLabel(langpack.getString("tabs.loc.available"));
        FrontendFrame.buildConstraints(gbConstraints, 0, 0, 1, 1, 0.4, 0.0);
        gbConstraints.fill = GridBagConstraints.NONE;
        gbConstraints.anchor = GridBagConstraints.SOUTHWEST;
        layout.addLayoutComponent(label, gbConstraints);
        add(label);
        
        label = new JLabel(langpack.getString("tabs.loc.selected"));
        FrontendFrame.buildConstraints(gbConstraints, 2, 0, 1, 1, 0.4, 0.0);
        layout.addLayoutComponent(label, gbConstraints);
        add(label);
        
        availableList = new JList();
        availableList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        availableList.setCellRenderer(FrontendFrame.LIST_RENDERER);
        scroller = new JScrollPane(availableList);
        scroller.setPreferredSize(new Dimension(40, 150));
        FrontendFrame.buildConstraints(gbConstraints, 0, 1, 1, 2, 0.4, 0.0);
        gbConstraints.fill = GridBagConstraints.BOTH;
        gbConstraints.anchor = GridBagConstraints.CENTER;
        layout.addLayoutComponent(scroller, gbConstraints);
        add(scroller);
        
        selectedList = new JList();
        selectedList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        selectedList.setCellRenderer(FrontendFrame.LIST_RENDERER);
        scroller = new JScrollPane(selectedList);
        scroller.setPreferredSize(new Dimension(40, 150));
        FrontendFrame.buildConstraints(gbConstraints, 2, 1, 1, 2, 0.4, 0.0);
        layout.addLayoutComponent(scroller, gbConstraints);
        add(scroller);
        
        includeButton = new HighlightJButton(langpack.getString("tabs.loc.include"),
                                            icons.getImageIcon("forward"),
                                            FrontendFrame.buttonsHColor);
        includeButton.addActionListener(this);
        FrontendFrame.buildConstraints(gbConstraints, 1, 1, 1, 1, 0.2, 0.0);
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        gbConstraints.anchor = GridBagConstraints.NORTH;
        layout.addLayoutComponent(includeButton, gbConstraints);
        add(includeButton);
        
        removeButton = new HighlightJButton(langpack.getString("tabs.loc.remove"),
                                            icons.getImageIcon("back"),
                                            FrontendFrame.buttonsHColor);
        removeButton.addActionListener(this);
        FrontendFrame.buildConstraints(gbConstraints, 1, 2, 1, 1, 0.2, 0.0);
        gbConstraints.anchor = GridBagConstraints.SOUTH;
        layout.addLayoutComponent(removeButton, gbConstraints);
        add(removeButton);
        
        initLists();
    }
    
    // Initialises the lists
    private void initLists()
    {
        try
        {
            // We get the langpack files list
            File dir = new File(LOC_DIR);
            String[] packs = dir.list();
            
            // We initialize our vectors
            avPacks = new Vector();
            int size = packs.length;
            for (int i = 0; i < size; i++) avPacks.add(packs[i].substring(0, 3));
            selPacks = new Vector();
        }
        catch (Exception err)
        {
            err.printStackTrace();
            JOptionPane.showMessageDialog(this, err.toString(),
                                          langpack.getString("frontend.error"),
                                          JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Updates the components
    public void updateComponents()
    {
        // We fill the lists
        availableList.setListData(avPacks);
        selectedList.setListData(selPacks);
        
        // We select the items
        if (curAv != -1) availableList.setSelectedIndex(curAv);
        if (curSel != -1) selectedList.setSelectedIndex(curSel);
    }
    
    // Action events handler
    public void actionPerformed(ActionEvent e)
    {
        // We get the source
        Object source = e.getSource();
        
        // We act depending of the source
        if (source == includeButton)
        {
            // We get the selected index
            int index = availableList.getSelectedIndex();
            if (index == -1) return;
            
            // We make the transfer
            selPacks.add(avPacks.get(index));
            avPacks.removeElementAt(index);
            
            // We keep a selected item (can be -1 if nothing left)
            curAv = (index < (avPacks.size() - 1)) ? index : avPacks.size() - 1;
        }
        else if (source == removeButton)
        {
            // We get the selected index
            int index = selectedList.getSelectedIndex();
            if (index == -1) return;
            
            // We make the transfer
            avPacks.add(selPacks.get(index));
            selPacks.removeElementAt(index);
            
            // We keep a selected item (can be -1 if nothing left)
            curSel = (index < (selPacks.size() - 1)) ? index : selPacks.size() - 1;
        }
        
        updateComponents();
    }
    
    // Updates the central XML tree
    public void updateXMLTree()
    {
        // We get our base root and we clear it
        XMLElement root = installation.getFirstChildNamed("locale");
        int size = root.getChildrenCount(), i;
        for (i = 0; i < size; i++) root.removeChildAtIndex(0);
        
        // We put our selected langpacks
        size = selPacks.size();
        for (i = 0; i < size; i++)
        {
            XMLElement pack = new XMLElement("langpack");
            pack.setAttribute("iso3", (String) selPacks.get(i) );
            root.addChild(pack);
        }
    }
    
    // Called when the installation XML tree is changed
    public void installationUpdated(XMLElement newXML)
    {
        this.installation = newXML;
        
        // We get the data langpacks list
        XMLElement root = installation.getFirstChildNamed("locale");
        Vector packs = root.getChildrenNamed("langpack");
        
        // We adapt to the data
        initLists();
        int size = packs.size();
        for (int i = 0; i < size; i++)
        {
            XMLElement el = (XMLElement) packs.get(i);
            String iso3 = el.getAttribute("iso3");
            selPacks.add(iso3);
            int index = avPacks.indexOf(iso3);
            if (index != -1) avPacks.removeElementAt(index);
        }
        
        updateComponents();
    }
    
    //.....................................................................
}
