/*
 * $Id$
 * IzPack
 * 
 *  Copyright (C) 2001-2003 Tino Schwarze, Julien Ponge
 *
 *  File :               AbstractUIHandler.java
 *  Description :        An interface for user interaction.
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
 * This interface describes basic functionality neccessary for user interaction.
 * 
 * All methods or functions which perform work and need to notify or ask the user
 * use a listener for such purposes. This way, we can separate UI from function.
 *  
 */

public interface AbstractUIHandler
{
  /**
   * Notify the user about something.
   * 
   * The difference between notification and warning is that a notification should not
   * need user interaction and can savely be ignored.
   * 
   * @param message The notification.
   */
  public void emitNotification (String message);
  
  /**
   * Warn the user about something.
   * 
   * @param title The message title (used for dialog name, might not be displayed)
   * @param message The warning message.
   * @return true if the user decided not to continue
   */
  public boolean emitWarning (String title, String message);
  
  /**
   * Notify the user of some error.
   * 
   * @param title The message title (used for dialog name, might not be displayed)
   * @param message The error message.
   * @return true if the user decided not to continue
   */
  public void emitError (String title, String message);
  
  // constants for asking questions
  // must all be >= 0!
  public static final int ANSWER_CANCEL = 45;
  public static final int ANSWER_YES = 47;
  public static final int ANSWER_NO = 49;
  // values for choices to present to the user
  public static final int CHOICES_YES_NO = 37;
  public static final int CHOICES_YES_NO_CANCEL = 38;
  
  /**
   * Ask the user a question.
   * 
   * @param title The title of the question (useful for dialogs). Might be null.
   * @param question The question.
   * @param choices The set of choices to present. Either CHOICES_YES_NO or 
   *                 CHOICES_YES_NO_CANCEL
   * 
   * @return The user's choice. (ANSWER_CANCEL, ANSWER_YES or ANSWER_NO)
   */
  public int askQuestion (String title, String question, int choices);
  
  /**
   * Ask the user a question.
   * 
   * @param title The title of the question (useful for dialogs). Might be null.
   * @param question The question.
   * @param choices The set of choices to present. Either CHOICES_YES_NO or 
   *                 CHOICES_YES_NO_CANCEL
   * @param default_choice The default choice. One of ANSWER_CANCEL, ANSWER_YES 
   *                        or ANSWER_NO.
   * 
   * @return The user's choice. (ANSWER_CANCEL, ANSWER_YES or ANSWER_NO)
   */
  public int askQuestion (String title, String question, int choices, int default_choice);
  
}
