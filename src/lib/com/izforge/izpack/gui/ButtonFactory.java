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
	
	
	public static void useButtonIcons()
	{
		useButtonIcons = true;
	}
	public static void useHighlightButtons()
	{
		useHighlightButtons = true;
		useButtonIcons = true;
	}
	
	public static JButton createButton(Icon icon, Color color)
	{
		if (useHighlightButtons)
		{
			return new HighlightJButton(icon, color);
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
			return new HighlightJButton(text,icon, color);
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

