/*
 * IzPack Version 3.1.0 pre2 (build 2002.10.19)
 * Copyright (C) 2001,2002 Julien Ponge
 *
 * File :               FrontendMenuBar.java
 * Description :        The Frontend menu bar class.
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

public class FrontendMenuBar extends JMenuBar
{
    //.....................................................................
    
    // The fields
    private TreeMap actions;
    private IconsDatabase icons;
    private LocaleDatabase langpack;
    
    // The constructor
    public FrontendMenuBar(TreeMap actions, IconsDatabase icons, LocaleDatabase langpack)
           throws Exception
    {
        super();
        
        // Initialisations
        this.actions = actions;
        this.icons = icons;
        this.langpack = langpack;
        
        // Constructs the menu
        XMLElement data = getXMLData();
        Vector menus = data.getChildrenNamed("menu");
        int size = menus.size();
        for (int i = 0; i < size; i++) createMenu( (XMLElement) menus.get(i));
    }
    
    //.....................................................................
    
    // Creates a menu
    private void createMenu(XMLElement root)
    {
        // We create the JMenu
        JMenu menu = new JMenu(langpack.getString(root.getAttribute("name")));
        menu.setMnemonic(langpack.getString(root.getAttribute("mn")).charAt(0));
        add(menu);
        
        // We create its elements
        Vector children = root.getChildren();
        int size = children.size();
        for (int i = 0; i < size; i++) createMenuElement( (XMLElement) children.get(i), menu);
    }
    
    // Creates the menu element associated to element
    private void createMenuElement(XMLElement element, JMenu menu)
    {
        // We get the element name
        String name = element.getName();
        
        if (name.equalsIgnoreCase("item"))
        {
            String iname = langpack.getString(element.getAttribute("name"));
            String action = element.getAttribute("action");
            char mn = langpack.getString(element.getAttribute("mn")).charAt(0);
            String img = element.getAttribute("img");
            JMenuItem item = new JMenuItem(iname, icons.getImageIcon(img));
            item.setMnemonic(mn);
            item.addActionListener( (ActionListener) actions.get(action));
            menu.add(item);
        } else
        if (name.equalsIgnoreCase("separator"))
        {
            menu.addSeparator();
        } else
        if (name.equalsIgnoreCase("langpacks"))
        {
            // We add a submenu
            JMenu submenu = new JMenu(langpack.getString("menu.langpack"));
            submenu.setIcon(icons.getImageIcon("search"));
            submenu.setMnemonic((langpack.getString("menu.langpack.mn").charAt(0)));
            menu.add(submenu);
            
            // We get the langpacks list
            File path = new File(Frontend.IZPACK_HOME + "bin" + File.separator + "langpacks" 
                 + File.separator + "frontend");
            String[] packs = path.list();
            
            // We add them to the submenu
            int size = packs.length;
            ButtonGroup group = new ButtonGroup();
            for (int i = 0; i < size; i++)
            {
                JRadioButtonMenuItem item = new JRadioButtonMenuItem(packs[i].substring(0, 3));
                item.setSelected(packs[i].substring(0, 3).equalsIgnoreCase(Frontend.curLocale));
                item.addActionListener( (ActionListener) actions.get("others") );
                group.add(item);
                submenu.add(item);
            }
        } else
        if (name.equalsIgnoreCase("bookmarks"))
        {
            // We add a submenu
            JMenu submenu = new JMenu(langpack.getString("menu.bookmarks"));
            submenu.setIcon(icons.getImageIcon("bookmarks"));
            submenu.setMnemonic((langpack.getString("menu.bookmarks.mn").charAt(0)));
            menu.add(submenu);
            
            // We add them to the submenu
            int size = Frontend.bookmarks.size();
            if (size == 0) submenu.setEnabled(false);
            for (int i = 0; i < size; i++)
            {
                XMLElement el = (XMLElement) Frontend.bookmarks.get(i);
                JMenuItem item = new JMenuItem(el.getContent());
                item.addActionListener( (ActionListener) actions.get("files") );
                submenu.add(item);
            }
        }
    }
    
    // Parses the menubar file
    private XMLElement getXMLData() throws Exception
    {
        // The input stream
        InputStream inXML = getClass().getResourceAsStream("/com/izforge/izpack/frontend/menubar.xml");
        
        // Initialises the parser
        StdXMLParser parser = new StdXMLParser();
        parser.setBuilder(new StdXMLBuilder());
        parser.setReader(new StdXMLReader(inXML));
        parser.setValidator(new NonValidator());
        
        // We get the data
        return (XMLElement) parser.parse();
    }
    
    //.....................................................................
}
