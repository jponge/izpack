/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2003 Julien Ponge
 *
 *  File :               FrontendTab.java
 *  Description :        The Frontend tab abstract class.
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
 *  The frontend tab class. Shall be implemented by any tab appearing in the
 *  frontend GUI.
 *
 * @author     Julien Ponge.
 * @created    October 27, 2002
 */
public abstract class FrontendTab extends JPanel
{
  /**  The icons. */
  protected IconsDatabase icons;

  /**  The language pack. */
  protected LocaleDatabase langpack;

  /**  The installation XML tree. */
  protected XMLElement installation;


  /**
   *  The constructor.
   *
   * @param  installation  The installation XML tree.
   * @param  icons         The icons database.
   * @param  langpack      The language pack.
   */
  public FrontendTab(XMLElement installation, IconsDatabase icons,
                     LocaleDatabase langpack)
  {
    // Initialisations
    this.installation = installation;
    this.icons = icons;
    this.langpack = langpack;
  }


  /**
   *  Called when the installation XML tree is changed.
   *
   * @param  newXML  The new XML tree.
   */
  public void installationUpdated(XMLElement newXML)
  {
    this.installation = newXML;
    updateComponents();
  }


  /**  Updates the components. By default does nothing.  */
  public void updateComponents()
  {

  }


  /**  Updates the central XML tree. By default does nothing.  */
  public void updateXMLTree()
  {

  }
}

