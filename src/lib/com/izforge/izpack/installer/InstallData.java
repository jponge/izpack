/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2003 Julien Ponge
 *
 *  File :               InstallData.java
 *  Description :        Installer internal data.
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
package com.izforge.izpack.installer;

import java.awt.Color;

import com.izforge.izpack.GUIPrefs;

/**
 *  Encloses information about the install process. This class is implemented as
 *  a singleton which can be easily accessed by different components of the
 *  installer. However, this implementation is not thread safe.
 *
 * @author     Julien Ponge <julien@izforge.com>
 * @author     Johannes Lehtinen <johannes.lehtinen@iki.fi>
 */
public class InstallData extends AutomatedInstallData
{
  /**  The GUI preferences. */
  public GUIPrefs guiPrefs;

  /**  The buttons highlighting color. */
  public Color buttonsHColor = new Color(230, 230, 230);

  /**  Constructs a new instance of this class.  */
  protected InstallData()
  {
    super();
  }
}
