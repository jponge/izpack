/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2004 Julien Ponge
 *
 *  File :               PackagerListener.java
 *  Description :        Interface for Packagers listeners.
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
package com.izforge.izpack.compiler;

/**
 * An interface for classes that want to listen to a packager events.
 * 
 * @author Julien Ponge
 */
public interface PackagerListener
{
    /** Message priority of "debug". */
    public static final int MSG_DEBUG = 0;

    /** Message priority of "error". */
    public static final int MSG_ERR = 1;

    /** Message priority of "information". */
    public static final int MSG_INFO = 2;

    /** Message priority of "verbose". */
    public static final int MSG_VERBOSE = 3;

    /** Message priority of "warning". */
    public static final int MSG_WARN = 4;

    /**
     * Send a message with the priority MSG_INFO.
     * 
     * @param info The information that has been sent.
     */
    public void packagerMsg(String info);

    /**
     * Send a message with the specified priority.
     * 
     * @param info The information that has been sent.
     * @param priority The priority of the message.
     */
    public void packagerMsg(String info, int priority);

    /** Called when the packager starts. */
    public void packagerStart();

    /** Called when the packager stops. */
    public void packagerStop();
}
