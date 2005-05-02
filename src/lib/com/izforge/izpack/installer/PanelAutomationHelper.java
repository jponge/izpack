/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2003 Tino Schwarze, Julien Ponge
 *
 *  File :               PanelAutomationHelper.java
 *  Description :        Provides generic UI handler functions for automated panels.
 *  Author's email :     tino.schwarze@informati.tu-chemnitz.de
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

package com.izforge.izpack.installer;

import com.izforge.izpack.util.AbstractUIHandler;

/**
 * Abstract class implementing basic functions needed by all panel automation
 * helpers.
 * 
 * @author tisc
 */
abstract public class PanelAutomationHelper implements AbstractUIHandler
{

    /*
     * @see com.izforge.izpack.util.AbstractUIHandler#emitNotification(java.lang.String)
     */
    public void emitNotification(String message)
    {
        System.out.println(message);
    }

    /*
     * @see com.izforge.izpack.util.AbstractUIHandler#emitWarning(java.lang.String,
     *      java.lang.String)
     */
    public boolean emitWarning(String title, String message)
    {
        System.err.println("[ WARNING: " + message + " ]");
        // default: continue
        return true;
    }

    /*
     * @see com.izforge.izpack.util.AbstractUIHandler#emitError(java.lang.String,
     *      java.lang.String)
     */
    public void emitError(String title, String message)
    {
        System.err.println("[ ERROR: " + message + " ]");
    }

    /*
     * @see com.izforge.izpack.util.AbstractUIHandler#askQuestion(java.lang.String,
     *      java.lang.String, int)
     */
    public int askQuestion(String title, String question, int choices)
    {
        // don't know what to answer
        return AbstractUIHandler.ANSWER_CANCEL;
    }

    /*
     * @see com.izforge.izpack.util.AbstractUIHandler#askQuestion(java.lang.String,
     *      java.lang.String, int, int)
     */
    public int askQuestion(String title, String question, int choices, int default_choice)
    {
        return default_choice;
    }

}
