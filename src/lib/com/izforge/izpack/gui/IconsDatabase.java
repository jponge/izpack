/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2003 Julien Ponge
 *
 *  File :               IconsDatabase.java
 *  Description :        Represents an icons database.
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
package com.izforge.izpack.gui;

import java.util.*;

import javax.swing.*;

/**
 *  The icons database class.
 *
 * @author     Julien Ponge
 * @created    October 27, 2002
 */
public class IconsDatabase extends TreeMap
{
  /**  The constructor.  */
  public IconsDatabase()
  {
    super();
  }


  /**
   *  Convenience method to retrieve an element.
   *
   * @param  key  The icon key.
   * @return      The icon as an ImageIcon object.
   */
  public ImageIcon getImageIcon(String key)
  {
    return (ImageIcon) get(key);
  }
}

