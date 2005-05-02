/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2004 Klaus Bartz
 *
 *  File :               ExtendedUIProgressHandler.java
 *  Description :        Interface for extended UI progress handler.
 *  Author's email :     klaus.bartz@coi.de
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
 * This interface allowes an extended interaction with a user interface handler.
 * 
 * @author Dr. Klaus Bartz (COI-ES)
 * @version
 * @(#) $Revision$ $Date$
 * 
 */
public interface ExtendedUIProgressHandler
{

    static final int BEFORE = 0;

    static final int AFTER = 1;

    /**
     * The action restarts.
     * 
     * @param name
     *            The name of the action.
     * @param overallMsg
     *            message to be used in the overall label.
     * @param tipMsg
     *            message to be used in the tip label.
     * @param no_of_steps
     *            The number of steps the action consists of.
     */
    void restartAction(String name, String overallMsg, String tipMsg, int no_of_steps);

    /**
     * Notify of progress with automatic counting.
     * 
     * @param stepMessage
     *            an additional message describing the substep the type of the
     *            substep
     */
    public void progress(String stepMessage);

}
