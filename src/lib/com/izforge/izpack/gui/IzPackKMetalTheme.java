/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2004 Julien Ponge
 *
 *  File :               IzPackKMetalTheme.java
 *  Description :        The metal theme for IzPack with the Kunststoff L&F.
 *  Author's email :     julien@izforge.com
 *  Author's Website :   http://www.izforge.com
 *
 *  (Some code comes from the Kunststoff source code which is under the LGPL)
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

import javax.swing.plaf.ColorUIResource;

/**
 *  The IzPack Kunststoff L&F theme.
 *
 * @author     Julien Ponge
 */
public class IzPackKMetalTheme extends IzPackMetalTheme
{
  /**  Primary color. */
  private final ColorUIResource primary1 = new ColorUIResource(32, 32, 64);

  /**  Primary color. */
  private final ColorUIResource primary2 = new ColorUIResource(160, 160, 180);

  /**  Primary color. */
  private final ColorUIResource primary3 = new ColorUIResource(200, 200, 224);

  /**  Secondary color. */
  private final ColorUIResource secondary1 = new ColorUIResource(130, 130, 130);

  /**  Secondary color. */
  private final ColorUIResource secondary2 = new ColorUIResource(180, 180, 180);

  /**  Secondary color. */
  private final ColorUIResource secondary3 = new ColorUIResource(224, 224, 224);

  /**  The constructor.  */
  public IzPackKMetalTheme()
  {
    super();
  }

  /**
   *  Returns the wished color.
   *
   * @return    The wished color.
   */
  public ColorUIResource getPrimary1()
  {
    return primary1;
  }

  /**
   *  Returns the wished color.
   *
   * @return    The wished color.
   */
  public ColorUIResource getPrimary2()
  {
    return primary2;
  }

  /**
   *  Returns the wished color.
   *
   * @return    The wished color.
   */
  public ColorUIResource getPrimary3()
  {
    return primary3;
  }

  /**
   *  Returns the wished color.
   *
   * @return    The wished color.
   */
  public ColorUIResource getSecondary1()
  {
    return secondary1;
  }

  /**
   *  Returns the wished color.
   *
   * @return    The wished color.
   */
  public ColorUIResource getSecondary2()
  {
    return secondary2;
  }

  /**
   *  Returns the wished color.
   *
   * @return    The wished color.
   */
  public ColorUIResource getSecondary3()
  {
    return secondary3;
  }
}
