/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2003 Julien Ponge
 *
 *  File :               HighlightJButton.java
 *  Description :        A button that highlights when the mouse passes over.
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

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;

/**
 *  A button that highlights when the button passes over.
 *
 * @author     Julien Ponge
 */
public class HighlightJButton extends JButton
{
  /**
   *  The constructor (use ButtonFactory to create button).
   *
   * @param  icon   The icon to display.
   * @param  color  The highlight color.
   */
  HighlightJButton(Icon icon, Color color)
  {
    super(icon);
    initButton(color);
  }


  /**
   *  The constructor (use ButtonFactory to create button).
   *
   * @param  text   The text to display.
   * @param  color  The highlight color.
   */
  HighlightJButton(String text, Color color)
  {
    super(text);
    initButton(color);
  }


  /**
   *  The constructor (use ButtonFactory to create button).
   *
   * @param  text   The text to display.
   * @param  icon   The icon to display.
   * @param  color  The highlight color.
   */
  HighlightJButton(String text, Icon icon, Color color)
  {
    super(text, icon);
    initButton(color);
  }


  /**
   *  The constructor (use ButtonFactory to create button).
   *
   * @param  a      The action.
   * @param  color  The highlight color.
   */
  HighlightJButton(Action a, Color color)
  {
    super(a);
    initButton(color);
  }


  /**
   *  Does the extra initialisations.
   *
   * @param  highlightColor  The highlight color.
   */
  protected void initButton(Color highlightColor)
  {
    this.highlightColor = highlightColor;
    defaultColor = getBackground();

    addMouseListener(new MouseHandler());
  }


  /**
   *  Overriden to ensure that the button won't stay highlighted if it had the
   *  mouse over it.
   *
   * @param  b  Button state.
   */
  public void setEnabled(boolean b)
  {
    reset();
    super.setEnabled(b);
  }


  /**  Forces the button to unhighlight.  */
  protected void reset()
  {
    setBackground(defaultColor);
  }


  /**  The highlighted color. */
  protected Color highlightColor;

  /**  The default color. */
  protected Color defaultColor;


  /**
   *  The mouse handler which makes the highlighting.
   *
   * @author     Julien Ponge
   */
  private class MouseHandler extends MouseAdapter
  {
    /**
     *  When the mouse passes over the button.
     *
     * @param  e  The event.
     */
    public void mouseEntered(MouseEvent e)
    {
      if (isEnabled())
        setBackground(highlightColor);
    }


    /**
     *  When the mouse passes out of the button.
     *
     * @param  e  The event.
     */
    public void mouseExited(MouseEvent e)
    {
      if (isEnabled())
        setBackground(defaultColor);
    }
  }
}

