/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2003 Julien Ponge
 *
 *  File :               Uninstaller.java
 *  Description :        The uninstaller class.
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
package com.izforge.izpack.uninstaller;

import com.izforge.izpack.gui.*;

import javax.swing.plaf.metal.*;

/**
 *  The uninstaller class.
 *
 * @author     Julien Ponge
 * @created    November 1, 2002
 */
public class Uninstaller
{

  /**
   *  The main method (program entry point).
   *
   * @param  args  The arguments passed on the command line.
   */
  public static void main(String[] args)
  {
    try
    {
      MetalLookAndFeel.setCurrentTheme(new IzPackMetalTheme());
      UninstallerFrame frame = new UninstallerFrame();
    }
    catch (Exception err)
    {
      System.err.println("- Error -");
      err.printStackTrace();
      System.exit(0);
    }
  }
}

