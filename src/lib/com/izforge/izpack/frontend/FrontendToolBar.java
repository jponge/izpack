/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001,2002 Julien Ponge
 *
 *  File :               FrontendToolBar.java
 *  Description :        The Frontend tool bar class.
 *  Author's email :     julien@izforge.com
 *  Author's Website :   http://www.izforge.com
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
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

/**
 *  The frontend toolbar class.
 *
 * @author     Julien Ponge
 * @created    October 27, 2002
 */
public class FrontendToolBar extends JToolBar
{
  /**  The actions. */
  private TreeMap actions;

  /**  The icons database. */
  private IconsDatabase icons;

  /**  The language packs. */
  private LocaleDatabase langpack;


  /**
   *  The constructor.
   *
   * @param  actions        The actions map.
   * @param  icons          The icons database.
   * @param  langpack       The language pack.
   * @exception  Exception  Description of the Exception
   */
  public FrontendToolBar(TreeMap actions, IconsDatabase icons, LocaleDatabase langpack)
     throws Exception
  {
    super();

    // Initialisations
    this.actions = actions;
    this.icons = icons;
    this.langpack = langpack;

    // Constructs the toolbar
    XMLElement data = getXMLData();
    Vector menus = data.getChildrenNamed("menu");
    int size = menus.size();
    for (int i = 0; i < size; i++)
    {
      createButtons((XMLElement) menus.get(i));
      if ((i + 1) < size)
        addSeparator();
    }
  }


  /**
   *  Creates a menu.
   *
   * @param  root  The root of the XML (sub)tree top consider.
   */
  private void createButtons(XMLElement root)
  {
    // Our variables
    HighlightJButton button;
    Vector items = root.getChildrenNamed("item");
    XMLElement item;
    String action;
    String img;
    String name;
    String tb;
    int size = items.size();

    // We create the buttons
    for (int i = 0; i < size; i++)
    {
      item = (XMLElement) items.get(i);
      tb = item.getAttribute("toolbar", "no");
      if (tb.equalsIgnoreCase("yes"))
      {
        // We get the attributes
        name = item.getAttribute("name");
        img = item.getAttribute("img");
        action = item.getAttribute("action");

        // We create the button
        button = new HighlightJButton(icons.getImageIcon(img),
          FrontendFrame.buttonsHColor);
        button.setToolTipText(langpack.getString(name));
        button.addActionListener((ActionListener) actions.get(action));
        button.setActionCommand(langpack.getString(name));
        add(button);
      }
    }
  }


  /**
   *  Parses the menubar file.
   *
   * @return                The XML tree.
   * @exception  Exception  Description of the Exception
   */
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
}

