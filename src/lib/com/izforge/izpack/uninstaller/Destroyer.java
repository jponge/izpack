/*
 * IzPack Version 3.1.0 pre1 (build 2002.09.21)
 * Copyright (C) 2001,2002 Julien Ponge
 *
 * File :               Destroyer.java
 * Description :        The destroyer.
 * Author's email :     julien@izforge.com
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

package com.izforge.izpack.uninstaller;

import java.io.*;
import java.net.*;
import java.util.*;

public class Destroyer extends Thread
{
    //.....................................................................
    
    // The fields
    private boolean forceDestroy;
    private String installPath;
    private DestroyerListener listener;
    
    // The constructor
    public Destroyer(String installPath, boolean forceDestroy, DestroyerListener listener)
    {
        super("IzPack - Destroyer");
        
        this.installPath = installPath;
        this.forceDestroy = forceDestroy;
        this.listener = listener;
    }
    
    //.....................................................................
    
    // The run method
    public void run()
    {
        try
        {
            // We get the list of the files to delete
            ArrayList files = getFilesList();
            int size = files.size();
            
            listener.destroyerStart(0, size);
            
            // We destroy the files
            for (int i = 0; i < size; i++)
            {
                File file = (File) files.get(i);
                if (file.exists()) file.delete();
                listener.destroyerProgress(i, file.getAbsolutePath());
            }
            
            // We make a complementary cleanup
            listener.destroyerProgress(size, "[ cleanups ]");
            cleanup(new File(installPath));
            askUninstallerRemoval();
            
            listener.destroyerStop();
        }
        catch (Exception err)
        {
            listener.destroyerStop();
            listener.destroyerError(err.toString());
        }
    }
    
    // Asks the JVM for the uninstaller deletion
    private void askUninstallerRemoval() throws Exception
    {
        // Initialisations
        InputStream in = getClass().getResourceAsStream("/jarlocation.log");
        InputStreamReader inReader = new InputStreamReader(in);
        BufferedReader reader = new BufferedReader(inReader);
        
        // We delete
        File jar = new File(reader.readLine());
        File path = new File(reader.readLine());
        File inst = new File(installPath);
        jar.deleteOnExit();
        path.deleteOnExit();
        inst.deleteOnExit();
    }
    
    // Returns an ArrayList of the files to delete
    private ArrayList getFilesList() throws Exception
    {
        // Initialisations
        ArrayList files = new ArrayList();
        InputStream in = getClass().getResourceAsStream("/install.log");
        InputStreamReader inReader = new InputStreamReader(in);
        BufferedReader reader = new BufferedReader(inReader);
        
        // We skip the first line (the installation path)
        reader.readLine();
        
        // We read it
        String read = reader.readLine();
        while (read != null) 
        {
            files.add(new File(read));
            read = reader.readLine();
        }
        
        // We return it
        return files;
    }
    
    // Makes some reccursive cleanups
    private void cleanup(File file) throws Exception
    {
        if (file.isDirectory())
        {
            File[] files = file.listFiles();
            int size = files.length;
            for (int i = 0; i < size; i++) cleanup(files[i]);
            file.delete();
        }
        else
        {
            if (forceDestroy) file.delete();
        }
    }
    
    //.....................................................................
}
