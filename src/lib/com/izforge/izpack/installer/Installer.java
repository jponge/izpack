/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2003 Jonathan Halliday, Julien Ponge
 *
 *  File :               Installer.java
 *  Description :        Entry point, selects between GUI and headless modes.
 *  Author's email :     jonathan.halliday@arjuna.com
 *  Author's Website :   http://www.arjuna.com
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

/**
 *  The program entry point. Selects between GUI and text
 *  install modes.
 *
 * @author Jonathan Halliday
 */
public class Installer
{
  /**
   *  The main method (program entry point).
   *
   * @param  args  The arguments passed on the command-line.
   */
  public static void main(String[] args)
  {
	// OS X tweakings
	if (System.getProperty("mrj.version") != null)
	{
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "IzPack");
		System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
		System.setProperty("com.apple.mrj.application.live-resize", "true");
	}
  
    try
    {
      if(args.length == 0) {
        // can't load the GUIInstaller class on headless machines,
        // so we use Class.forName to force lazy loading.
        Class.forName("com.izforge.izpack.installer.GUIInstaller").newInstance();
      } else {
        new AutomatedInstaller(args[0]);
      }
    }
    catch (Exception e)
    {
      System.err.println("- Error -");
      System.err.println(e.toString());
      e.printStackTrace();
      System.exit(0);
    }
  }
}
