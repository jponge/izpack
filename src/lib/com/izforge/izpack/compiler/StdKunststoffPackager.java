/*
 * IzPack Version 3.1.0 pre2 (build 2002.10.19)
 * Copyright (C) 2001,2002 Julien Ponge
 *
 * File :               StdKunststoffPackager.java
 * Description :        The standard Kunststoff installer packager class.
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

package com.izforge.izpack.compiler;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.util.jar.*;

import com.izforge.izpack.*;

public class StdKunststoffPackager extends StdPackager
{
    //.....................................................................
    
    // The constants
    public static final String KUNSTSTOFF_PATH = Compiler.IZPACK_HOME + "lib" +
                                                 File.separator + "kunststoff.jar";
    
    // The constructor
    public StdKunststoffPackager(String outputFilename, PackagerListener plistener) 
           throws Exception
    {
        super(outputFilename, plistener);
        
        // Copies the Kunststoff library
        sendMsg("Copying the Kunststoff library ...");
        JarFile skeleton = new JarFile(KUNSTSTOFF_PATH);
        Enumeration entries = skeleton.entries();
        while (entries.hasMoreElements())
        {
            // Puts a new entry
            ZipEntry zentry = (ZipEntry) entries.nextElement();
            if (zentry.getName().equalsIgnoreCase("com/")) continue; // Avoids a stupid ZipException
            InputStream zin = skeleton.getInputStream(zentry);
            outJar.putNextEntry(new ZipEntry(zentry.getName()));
            
            // Copy the data
            copyStream(zin, outJar);
            outJar.closeEntry();
            zin.close();
        }        
    }
    
    //.....................................................................
    // The methods
    
    // Tells the packager to finish the job
    public void finish() throws Exception
    {
        // Usefull stuff
        DataOutputStream datOut;
        ObjectOutputStream objOut;
        int size;
        int i;
        
        sendMsg("Finishing the enpacking ...");
        
        // Writes the installation kind information
        outJar.putNextEntry(new ZipEntry("kind"));
        datOut = new DataOutputStream(outJar);
        datOut.writeUTF("standard-kunststoff");
        datOut.flush();
        outJar.closeEntry();        
        
        // Writes the packs informations
        outJar.putNextEntry(new ZipEntry("packs.info"));
        objOut = new ObjectOutputStream(outJar);
        size = packs.size();
        objOut.writeInt(size);
        for (i = 0; i < size; i++) objOut.writeObject(packs.get(i));
        objOut.flush();
        outJar.closeEntry();
        
        // Writes the langpacks informations
        outJar.putNextEntry(new ZipEntry("langpacks.info"));
        datOut = new DataOutputStream(outJar);
        size = langpacks.size();
        datOut.writeInt(size);
        for (i = 0; i < size; i++) datOut.writeUTF( (String) langpacks.get(i) );
        datOut.flush();
        outJar.closeEntry();
        
        // Closes the stream
        outJar.flush();
        outJar.close();
        
        sendStop();
    }    
    
    //.....................................................................
}
