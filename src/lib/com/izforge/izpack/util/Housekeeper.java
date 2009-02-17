/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2002 Elmar Grom
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.util;

import java.util.Vector;

/*---------------------------------------------------------------------------*/
/**
 * This class performs housekeeping and cleanup tasks. There can only be one instance of
 * <code>Housekeeper</code> per Java runtime, therefore this class is implemented as a
 * 'Singleton'. <br>
 * <br>
 * It is VERY important to perform pre-shutdown cleanup operations through this class. Do NOT rely
 * on operations like <code>deleteOnExit()</code> shutdown hooks or <code>finalize()</code>for
 * cleanup. Because <code>shutDown()</code> uses <code>System.exit()</code> to terminate, these
 * methods will not work at all or will not work reliably.
 *
 * @author Elmar Grom
 * @version 0.0.1 / 2/9/02
 */
/*---------------------------------------------------------------------------*/
public class Housekeeper
{

    // ------------------------------------------------------------------------
    // Variable Declarations
    // ------------------------------------------------------------------------
    private static Housekeeper me = null;

    private Vector<CleanupClient> cleanupClients = new Vector<CleanupClient>();

    /*--------------------------------------------------------------------------*/
    /**
     * This class is implemented as a 'Singleton'. Therefore the constructor is private to prevent
     * instantiation of this class. Use <code>getInstance()</code> to obtain an instance for use.
     * <br>
     * <br>
     * For more information about the 'Singleton' pattern I highly recommend the book Design
     * Patterns by Gamma, Helm, Johnson and Vlissides ISBN 0-201-63361-2.
     */
    /*--------------------------------------------------------------------------*/
    private Housekeeper()
    {
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Returns an instance of <code>Housekeeper</code> to use.
     *
     * @return an instance of <code>Housekeeper</code>.
     */
    /*--------------------------------------------------------------------------*/
    public static Housekeeper getInstance()
    {
        if (me == null)
        {
            me = new Housekeeper();
        }

        return (me);
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Use to register objects that need to perform cleanup operations before the application shuts
     * down.
     *
     * @param client reference of to an object that needs to perform cleanup operations.
     */
    /*--------------------------------------------------------------------------*/
    public void registerForCleanup(CleanupClient client)
    {
        // IZPACK-276:
        // if the client is an instance of Librarian hold it at a special place to call it at the
        // very last time
        if (client instanceof Librarian)
        {
            cleanupClients.add(0, client);
        }
        else
        {
            cleanupClients.add(client);
        }
    }

    /*--------------------------------------------------------------------------*/
    /**
     * This methods shuts the application down. First, it will call all clients that have registered
     * for cleanup operations. Once this has been accomplished, the application will be forceably
     * terminated. <br>
     * <br>
     * <b>THIS METHOD DOES NOT RETURN!</b>
     *
     * @param exitCode the exit code that should be returned to the calling process.
     */
    /*--------------------------------------------------------------------------*/
    public void shutDown(int exitCode)
    {
        // IZPACK-276
		// Do the cleanup of the last registered client at the fist time (first in last out)
        for (int i = cleanupClients.size() - 1; i >= 0; i--)
        {
            try
            {
                (cleanupClients.elementAt(i)).cleanUp();
            }
            catch (Throwable exception)
            {
                // At this point we can not afford to treat exceptions. Cleanup
                // that
                // can not be completed might unfortunately leave some garbage
                // behind.
                // If we have a logging module, any exceptions received here
                // should
                // be written to the log.
            }
        }

        System.exit(exitCode);
    }
}
/*---------------------------------------------------------------------------*/
