/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2002 Jan Blok (jblok@profdata.nl - PDM - www.profdata.nl)
 *
 *  File :               ButtonFactory.java
 *  Description :        a ButtonFactory.
 *  Author's email :     jblok@profdata.nl
 *  Author's Website :   http://www.profdata.nl
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

import java.awt.Color;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;

/**
 * This class makes it possible to use default buttons on macosx platform
 */
public class ButtonFactory
{
	private static boolean useHighlightButtons = false;
	private static boolean useButtonIcons = false;


  /**
   * Enable icons for buttons
   * This setting has no effect on OSX
   */
	public static void useButtonIcons()
	{
    useButtonIcons(true);
	}

  /**
   * Enable or disable icons for buttons
   * This setting has no effect on OSX
   * @param useit flag which determines the behavior
   */
  public static void useButtonIcons(boolean useit)
  {
    if(System.getProperty("mrj.version")==null)
    {
      useButtonIcons = useit;
    }
  }

  /**
   * Enable highlight buttons
   * This setting has no effect on OSX
   */
	public static void useHighlightButtons()
	{
    useHighlightButtons(true);
	}

  /**
   * Enable or disable highlight buttons
   * This setting has no effect on OSX
   * @param useit flag which determines the behavior
  */
  public static void useHighlightButtons(boolean useit)
  {
    if(System.getProperty("mrj.version")==null)
    {
      useHighlightButtons = useit;
    }
    useButtonIcons(useit);
  }

	public static JButton createButton(Icon icon, Color color)
	{
		if (useHighlightButtons)
		{
      if (useButtonIcons)     
        return new HighlightJButton(icon, color);
      else
        return new HighlightJButton("", color);
       
		}
		else
		{
			if (useButtonIcons)
			{
				return new JButton(icon);
			}
			else
			{
				return new JButton();
			}
		}
	}

	public static JButton createButton(String text, Color color)
	{
		if (useHighlightButtons)
		{
			return new HighlightJButton(text, color);
		}
		else
		{
			return new JButton(text);
		}
	}

	public static JButton createButton(String text, Icon icon, Color color)
	{
		if (useHighlightButtons)
		{
      if (useButtonIcons)     
        return new HighlightJButton(text,icon, color);
      else
        return new HighlightJButton(text, color);
		}
		else
		{
			if (useButtonIcons)
			{
				return new JButton(text,icon);
			}
			else
			{
				return new JButton(text);
			}
		}
	}

	public static JButton createButton(Action a, Color color)
	{
		if (useHighlightButtons)
		{
			return new HighlightJButton(a, color);
		}
		else
		{
			return new JButton(a);
		}
	}

}

