/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2004 Julien Ponge
 *
 *  File :               IzPackMetalTheme.java
 *  Description :        The metal theme for IzPack.
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

import java.awt.Font;

import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;

/**
 *  The IzPack metal theme.
 *
 * @author     Julien Ponge
 */
public class IzPackMetalTheme extends DefaultMetalTheme
{
  /**  The fonts color. */
  private ColorUIResource color;

  private FontUIResource controlFont;
  private FontUIResource menuFont;
  private FontUIResource windowTitleFont;
  private FontUIResource monospacedFont;

  /**  The constructor.  */
  public IzPackMetalTheme()
  {
    color = new ColorUIResource(0, 0, 0);

    Font font1 = createFont("Tahoma", Font.PLAIN, 11);
    Font font2 = createFont("Tahoma", Font.BOLD, 11);

    menuFont = new FontUIResource(font1);
    controlFont = new FontUIResource(font1);
    windowTitleFont = new FontUIResource(font2);
    monospacedFont = new FontUIResource(font1);
  }

  private Font createFont(String name, int style, int size)
  {
    Font font = new Font(name, style, size);
    return ((font == null) ? new Font("Dialog", style, size) : font);
  }

  /**
   *  Returns the color.
   *
   * @return    The color.
   */
  public ColorUIResource getControlTextColor()
  {
    return color;
  }

  /**
   *  Returns the color.
   *
   * @return    The color.
   */
  public ColorUIResource getMenuTextColor()
  {
    return color;
  }

  /**
   *  Returns the color.
   *
   * @return    The color.
   */
  public ColorUIResource getSystemTextColor()
  {
    return color;
  }

  /**
   *  Returns the color.
   *
   * @return    The color.
   */
  public ColorUIResource getUserTextColor()
  {
    return color;
  }

  /**
   * The Font of Labels in many cases
   */
  public FontUIResource getControlTextFont()
  {
    return controlFont;
  }

  /**
   * The Font of Menus and MenuItems
   */
  public FontUIResource getMenuTextFont()
  {
    return menuFont;
  }

  /**
   * The Font of Nodes in JTrees
   */
  public FontUIResource getSystemTextFont()
  {
    return controlFont;
  }

  /**
   * The Font in TextFields, EditorPanes, etc.
   */
  public FontUIResource getUserTextFont()
  {
    return controlFont;
  }

  /**
   * The Font of the Title of JInternalFrames
   */
  public FontUIResource getWindowTitleFont()
  {
    return windowTitleFont;
  }

}
