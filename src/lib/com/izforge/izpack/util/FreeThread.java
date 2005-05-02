/*
 * $Id$
 * IzPack 
 * Copyright (C) 2002 by Elmar Grom
 *
 * File :               FreeThread.java
 * Description :        used to free native libraries
 * Author's email :     elmar@grom.net
 * Website :            http://www.izforge.com
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

package com.izforge.izpack.util;

/*---------------------------------------------------------------------------*/
/**
 * This class implements a thred that can be used to free native libraries
 * safely.
 * 
 * @version 0.0.1 / 2/6/02
 * @author Elmar Grom
 */
/*---------------------------------------------------------------------------*/
public class FreeThread extends Thread
{

    private String name = "";

    private NativeLibraryClient client = null;

    /*--------------------------------------------------------------------------*/
    /**
     * Standard constructor.
     * 
     * @param name
     *            the name of the library to free. The exact form of the name
     *            may be operating system dependent. On Microsoft Windows this
     *            must be just the library name, without path but with
     *            extension.
     * @param client
     *            reference of the client object that is linked with the library
     *            to be freed.
     */
    /*--------------------------------------------------------------------------*/
    public FreeThread(String name, NativeLibraryClient client)
    {
        this.name = name;
        this.client = client;
    }

    /*--------------------------------------------------------------------------*/
    /**
     * The run() method. Frees the library. Note that the thread is likely to
     * get 'frozen' and the application can only be treminated through a call to
     * <code>System.exit()</code>.
     */
    /*--------------------------------------------------------------------------*/
    public void run()
    {
        client.freeLibrary(name);
    }
}
/*---------------------------------------------------------------------------*/
