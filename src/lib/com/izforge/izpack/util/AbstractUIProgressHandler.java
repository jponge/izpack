/*
 * $Id$
 * IzPack
 * 
 *  Copyright (C) 2001-2003 Tino Schwarze, Julien Ponge
 *
 *  File :               AbstractUIProgress.java
 *  Description :        An interface for user interaction and progress notification.
 *  Author's email :     tino.schwarze@informatik.tu-chemnitz.de
 *  Author's Website :   http://www.tisc.de
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
 
package com.izforge.izpack.util;

/**
 * This interface is used by functions which need to notify the user of some progress.
 * 
 * For example, the installation progress and compilation progress are communicated to
 * the user using this interface. The interface supports a two-stage progress indication:
 * The whole action is divided into steps (for example, packs when installing) and 
 * sub-steps (for example, files of a pack).
 */
public interface AbstractUIProgressHandler extends AbstractUIHandler
{
  /**
   * The action starts.
   * 
   * @param name The name of the action.
   * @param no_of_steps The number of steps the action consists of.
   */
  public void startAction (String name, int no_of_steps);
  
  /**
   * The action was finished.
   */
  public void stopAction ();

  /**
   * The next step starts.
   * 
   * @param step_name The name of the step which starts now.
   * @param step_no The number of the step.
   * @param no_of_substeps The number of sub-steps this step consists of.
   */
  public void nextStep (String step_name, int step_no, int no_of_substeps);
  
  /**
   * Notify of progress.
   * 
   * @param substep_no The substep which will be performed next.
   * @param message An additional message describing the substep.
   */
  public void progress (int substep_no, String message);
    
}
