/*
 * IzPack Version 3.0.0 (build 2002.08.13)
 * Copyright (C) 2001,2002 Julien Ponge
 *
 * File :               FrontendInfoTab.java
 * Description :        The Frontend info tab class.
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

public class FrontendInfoTab extends FrontendTab implements ActionListener
{
    //.....................................................................
    
    // The fields
    private GridBagLayout layout;
    private GridBagConstraints gbConstraints;
    private JTextField appnameTextField;
    private JTextField appversionTextField;
    private JTextField appurlTextField;
    private JList authorsList;
    private JTextField widthTextField;
    private JTextField heightTextField;
    private HighlightJButton addButton;
    private HighlightJButton delButton;
    private JCheckBox resizeCheckBox;
    
    // The constructor
    public FrontendInfoTab(XMLElement installation, IconsDatabase icons, 
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
        
        // Creates the textfields controls
        
        label = new JLabel(langpack.getString("tabs.info.appname"));
        FrontendFrame.buildConstraints(gbConstraints, 0, 0, 1, 1, 0.5, 0.0);
        gbConstraints.fill = GridBagConstraints.NONE;
        gbConstraints.anchor = GridBagConstraints.SOUTHWEST;
        layout.addLayoutComponent(label, gbConstraints);
        add(label);
        
        label = new JLabel(langpack.getString("tabs.info.appversion"));
        FrontendFrame.buildConstraints(gbConstraints, 0, 2, 1, 1, 0.5, 0.0);
        layout.addLayoutComponent(label, gbConstraints);
        add(label);
        
        appnameTextField = new JTextField();
        appnameTextField.addActionListener(this);
        FrontendFrame.buildConstraints(gbConstraints, 0, 1, 1, 1, 0.5, 0.0);
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        gbConstraints.anchor = GridBagConstraints.NORTHWEST;
        layout.addLayoutComponent(appnameTextField, gbConstraints);
        add(appnameTextField);
        
        appversionTextField = new JTextField();
        appversionTextField.addActionListener(this);
        FrontendFrame.buildConstraints(gbConstraints, 0, 3, 1, 1, 0.5, 0.0);
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        gbConstraints.anchor = GridBagConstraints.NORTHWEST;
        layout.addLayoutComponent(appversionTextField, gbConstraints);
        add(appversionTextField);
        
        label = new JLabel(langpack.getString("tabs.info.appurl"));
        FrontendFrame.buildConstraints(gbConstraints, 0, 4, 1, 1, 0.5, 0.0);
        gbConstraints.fill = GridBagConstraints.NONE;
        gbConstraints.anchor = GridBagConstraints.SOUTHWEST;
        layout.addLayoutComponent(label, gbConstraints);
        add(label);
        
        appurlTextField = new JTextField();
        appurlTextField.addActionListener(this);
        FrontendFrame.buildConstraints(gbConstraints, 0, 5, 1, 1, 0.5, 0.0);
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        gbConstraints.anchor = GridBagConstraints.NORTHWEST;
        layout.addLayoutComponent(appurlTextField, gbConstraints);
        add(appurlTextField);
        
        // Creates the GUI prefs part
        
        label = new JLabel(langpack.getString("tabs.info.width"));
        FrontendFrame.buildConstraints(gbConstraints, 1, 0, 1, 1, 0.5, 0.0);
        gbConstraints.fill = GridBagConstraints.NONE;
        gbConstraints.anchor = GridBagConstraints.SOUTHWEST;
        layout.addLayoutComponent(label, gbConstraints);
        add(label);
        
        label = new JLabel(langpack.getString("tabs.info.height"));
        FrontendFrame.buildConstraints(gbConstraints, 1, 2, 1, 1, 0.5, 0.0);
        layout.addLayoutComponent(label, gbConstraints);
        add(label);
        
        widthTextField = new JTextField();
        widthTextField.addActionListener(this);
        FrontendFrame.buildConstraints(gbConstraints, 1, 1, 1, 1, 0.5, 0.0);
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        gbConstraints.anchor = GridBagConstraints.NORTHWEST;
        layout.addLayoutComponent(widthTextField, gbConstraints);
        add(widthTextField);
        
        heightTextField = new JTextField();
        heightTextField.addActionListener(this);
        FrontendFrame.buildConstraints(gbConstraints, 1, 3, 1, 1, 0.5, 0.0);
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        gbConstraints.anchor = GridBagConstraints.NORTHWEST;
        layout.addLayoutComponent(heightTextField, gbConstraints);
        add(heightTextField);
        
        resizeCheckBox = new JCheckBox(langpack.getString("tabs.info.resizable"));
        resizeCheckBox.addActionListener(this);
        FrontendFrame.buildConstraints(gbConstraints, 1, 5, 1, 1, 0.5, 0.0);
        gbConstraints.fill = GridBagConstraints.NONE;
        gbConstraints.anchor = GridBagConstraints.SOUTHWEST;
        layout.addLayoutComponent(resizeCheckBox, gbConstraints);
        add(resizeCheckBox);
        
        // Creates the authors part
        
        label = new JLabel(langpack.getString("tabs.info.authorslist"));
        FrontendFrame.buildConstraints(gbConstraints, 0, 6, 1, 1, 0.5, 0.0);
        gbConstraints.fill = GridBagConstraints.NONE;
        gbConstraints.anchor = GridBagConstraints.NORTHWEST;
        layout.addLayoutComponent(label, gbConstraints);
        add(label);
        
        authorsList = new JList();
        authorsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroller = new JScrollPane(authorsList);
        scroller.setPreferredSize(new Dimension(40, 150));
        authorsList.setCellRenderer(FrontendFrame.LIST_RENDERER);
        FrontendFrame.buildConstraints(gbConstraints, 0, 7, 1, 2, 0.5, 0.0);
        gbConstraints.fill = GridBagConstraints.BOTH;
        gbConstraints.anchor = GridBagConstraints.CENTER;
        layout.addLayoutComponent(scroller, gbConstraints);
        add(scroller);
        
        addButton = new HighlightJButton(langpack.getString("tabs.info.add"),
                                         icons.getImageIcon("new"),
                                         FrontendFrame.buttonsHColor);
        addButton.addActionListener(this);
        delButton = new HighlightJButton(langpack.getString("tabs.info.del"),
                                         icons.getImageIcon("delete"),
                                         FrontendFrame.buttonsHColor);
        delButton.addActionListener(this);
        
        FrontendFrame.buildConstraints(gbConstraints, 1, 7, 1, 1, 0.5, 0.0);
        gbConstraints.fill = GridBagConstraints.NONE;
        gbConstraints.anchor = GridBagConstraints.NORTHWEST;
        layout.addLayoutComponent(addButton, gbConstraints);
        add(addButton);
        
        FrontendFrame.buildConstraints(gbConstraints, 1, 8, 1, 1, 0.5, 0.0);
        gbConstraints.anchor = GridBagConstraints.SOUTHWEST;
        layout.addLayoutComponent(delButton, gbConstraints);
        add(delButton);
    }
    
    // Updates the components
    public void updateComponents()
    {
        // Our base root
        XMLElement root = installation.getFirstChildNamed("info");
        
        // We fill the trivials textfields
        appnameTextField.setText(root.getFirstChildNamed("appname").getContent());
        appversionTextField.setText(root.getFirstChildNamed("appversion").getContent());
        appnameTextField.setText(root.getFirstChildNamed("appname").getContent());
        appurlTextField.setText(root.getFirstChildNamed("url").getContent());
        
        // We fill the list
        Vector authors = root.getFirstChildNamed("authors").getChildrenNamed("author");
        int size = authors.size();
        Vector authorsVect = new Vector(size);
        for (int i = 0; i < size; i++)
        {
            XMLElement author = (XMLElement) authors.get(i);
            Info.Author aut = new Info.Author(author.getAttribute("name"),
                                              author.getAttribute("email"));
            authorsVect.add(aut);
        }
        authorsList.setListData(authorsVect);
        
        // The root for the GUI prefs
        root = installation.getFirstChildNamed("guiprefs");
        
        // We fill the trivial textfields
        widthTextField.setText(root.getAttribute("width"));
        heightTextField.setText(root.getAttribute("height"));
        resizeCheckBox.setSelected(root.getAttribute("resizable").equalsIgnoreCase("yes"));
    }
    
    // Updates the central XML tree
    public void updateXMLTree()
    {
        // Our base root
        XMLElement root = installation.getFirstChildNamed("info");
        
        // Text fields
        root.getFirstChildNamed("appname").setContent(appnameTextField.getText());
        root.getFirstChildNamed("appversion").setContent(appversionTextField.getText());
        root.getFirstChildNamed("url").setContent(appurlTextField.getText());
        
        // GUI prefs root
        root = installation.getFirstChildNamed("guiprefs");
        root.setAttribute("width", widthTextField.getText());
        root.setAttribute("height", heightTextField.getText());
        root.setAttribute("resizable", (resizeCheckBox.isSelected() ? "yes" : "no"));
    }
    
    // Action events handler
    public void actionPerformed(ActionEvent e)
    {
        // We get the source
        Object source = e.getSource();
        
        updateXMLTree();
        
        // We act depending of the source
        if (source == addButton)
        {
            // We get the author attributes
            String inputValue;
            XMLElement author = new XMLElement("author");
            inputValue = JOptionPane.showInputDialog(langpack.getString("tabs.info.author"));
            if (inputValue == null) 
                return;
            else
                author.setAttribute("name", inputValue);
            inputValue = JOptionPane.showInputDialog(langpack.getString("tabs.info.email"));
            if (inputValue == null) 
                return;
            else
                author.setAttribute("email", inputValue);
            
            // We add it to the XML data and update the components
            XMLElement root = installation.getFirstChildNamed("info").getFirstChildNamed("authors");
            root.addChild(author);
            updateComponents();
        }
        else if (source == delButton)
        {
            // We get the selected item index
            int index = authorsList.getSelectedIndex();
            if (index == -1) return;
            
            // We remove the author and update the components
            XMLElement root = installation.getFirstChildNamed("info").getFirstChildNamed("authors");
            root.removeChildAtIndex(index);
            updateComponents();
        }
    }
    
    //.....................................................................
}
