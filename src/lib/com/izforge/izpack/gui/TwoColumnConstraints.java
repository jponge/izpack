/*
 * IzPack version 3.1.0 pre1 (build 2002.09.21)
 * Copyright (C) 2002 Elmar Grom
 *
 * File :               TwoColumnConstraint.java
 * Description :        the constraint class used with TwoColumnLayout
 * Author's email :     elmar@grom.net
 * Author's Website :   http://www.izforge.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package   com.izforge.izpack.gui;

import    java.awt.*;

/*---------------------------------------------------------------------------*/
/**
 * The constraints class to use with <code>TwoColumnLayout</code>.
 *
 * @see      com.izforge.izpack.gui.TwoColumnLayout
 *
 * @version  0.0.1 / 11/15/02
 * @author   Elmar Grom
 */
/*---------------------------------------------------------------------------*/
public class TwoColumnConstraints
{
  public static final int       NORTH       = 9;
  public static final int       WEST        = 15;
  public static final int       EAST        = 26;
  public static final int       BOTH        = 27;
  public static final int       LEFT        = 31;
  public static final int       CENTER      = 35;
  public static final int       RIGHT       = 47;

  /** Indicates where to place the associated component. <code>NORTH</code>
      will place the component in the title margin. </code>WEST</code> will
      place the component in the left column and <code>EAST</code> will
      place it in the right column. If <code>BOTH</code> is used, the component
      will straddle both columns. */
  public int        position    = WEST;
  /** How to align the associated component, <code>LEFT</code>, <code>CENTER</code>
      or <code>RIGHT</code>. Note that this setting only taks effect in the
      component is placed in the title margin. */
  public int        align       = LEFT;
  /** If set to true, the indent setting in the layout manager will be applied. */
  public boolean    indent      = false;
  /** If set to true the associated component will be allowed to stretch to
      the width of the entire avaiable space. */
  public boolean    stretch     = false;

  /** for private use by the layout manager */
  Component         component   = null;
  
 /*--------------------------------------------------------------------------*/
 /**
  * Creates a copy of this two column constraint.
  *
  * @return    a copy of this <code>TwoColumnConstraints</code>
  */
 /*--------------------------------------------------------------------------*/
  public Object clone ()
  {
    TwoColumnConstraints newObject = new TwoColumnConstraints ();
    
    newObject.position  = position;
    newObject.align     = align;
    newObject.indent    = indent;
    newObject.stretch   = stretch;
    newObject.component = component;
    
    return (newObject);
  }
}
/*---------------------------------------------------------------------------*/
