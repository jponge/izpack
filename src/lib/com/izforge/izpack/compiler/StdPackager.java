/*
 * IzPack Version 3.0.0 rc2 (build 2002.07.06)
 * Copyright (C) 2001,2002 Julien Ponge
 *
 * File :               StdPackager.java
 * Description :        The standard installer packager class.
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

public class StdPackager extends Packager
{
    //.....................................................................
    // The fields
    
    protected JarOutputStream outJar;   // The zipped output stream (the jar file here)
    protected ArrayList packs;          // The packs informations
    protected ArrayList langpacks;      // The langpacks iso3 names

    //.....................................................................

    // The constructor
    public StdPackager(String outputFilename, PackagerListener plistener) throws Exception
    {
        packs = new ArrayList();
        langpacks = new ArrayList();
        setPackagerListener(plistener);
        
        sendStart();

        // Sets up the zipped output stream
        FileOutputStream outFile = new FileOutputStream(outputFilename);
        outJar = new JarOutputStream(outFile);
        outJar.setLevel(9);

        // Copies the skeleton installer
        sendMsg("Copying the skeleton installer ...");
        JarFile skeleton = new JarFile(SKELETON_PATH);
        Enumeration entries = skeleton.entries();
        while (entries.hasMoreElements())
        {
            // Puts a new entry
            ZipEntry zentry = (ZipEntry) entries.nextElement();
            InputStream zin = skeleton.getInputStream(zentry);
            outJar.putNextEntry(new ZipEntry(zentry.getName()));

            // Copy the data
            copyStream(zin, outJar);

            outJar.closeEntry();
            zin.close();
        }
    }

    //.....................................................................
    // The elements adding methods

    // Adds a pack (the compiler sends the merged data)
    public ZipOutputStream addPack(int packNumber, String name, boolean required,
                                   String description) throws Exception
    {
        sendMsg("Adding pack #" + packNumber + " : " + name + " ...");

        // Adds it in the packs array
        Pack pack = new Pack(name, description, required);
        packs.add(packNumber, pack);

        // Returns the suiting output stream
        String entryName = "packs/pack" + packNumber;
        ZipEntry entry = new ZipEntry(entryName);
        outJar.putNextEntry(entry);
        return outJar;
    }

    // Sets the GUI preferences
    public void setGUIPrefs(GUIPrefs prefs) throws Exception
    {
        sendMsg("Setting the GUI preferences ...");
        
        outJar.putNextEntry(new ZipEntry("GUIPrefs"));
        ObjectOutputStream objOut = new ObjectOutputStream(outJar);
        objOut.writeObject(prefs);
        objOut.flush();
        outJar.closeEntry();
    }

    // Adds a panel
    public void addPanelClass(String classFilename, InputStream input) throws Exception
    {
        sendMsg("Adding the (sub)classes for " + classFilename + " ...");
        
        outJar.putNextEntry(new ZipEntry("com/izforge/izpack/panels/" + classFilename));        
        copyStream(input, outJar);
        outJar.closeEntry();
        input.close();
    }

    // Sets the panels order
    public void setPanelsOrder(ArrayList order) throws Exception
    {
        sendMsg("Setting the panels order ...");

        outJar.putNextEntry(new ZipEntry("panelsOrder"));
        DataOutputStream datOut = new DataOutputStream(outJar);
        int size = order.size();
        datOut.writeInt(size);
        for (int i = 0; i < size; i++) datOut.writeUTF((String)order.get(i));
        datOut.flush();
        outJar.closeEntry();
    }

    // Sets the informations related to this installation
    public void setInfo(Info info) throws Exception
    {
        sendMsg("Setting the installer informations ...");

        outJar.putNextEntry(new ZipEntry("info"));
        ObjectOutputStream objOut = new ObjectOutputStream(outJar);
        objOut.writeObject(info);
        objOut.flush();
        outJar.closeEntry();
    }
    
    // Adds EnrollPanel define information
    public void setEnrollInfo(EnrollInfo enrollInfo) throws Exception  
    {
        sendMsg("Setting  the EnrollInfo ...");
        outJar.putNextEntry(new ZipEntry("enroll"));
        ObjectOutputStream objOut = new ObjectOutputStream(outJar);
        objOut.writeObject(enrollInfo);
        objOut.flush();
        outJar.closeEntry();
    }
    // Adds Variable Declaration
    public void setVariables(Properties varDef)throws Exception 
    {
        sendMsg("Setting  the variables ...");
        outJar.putNextEntry(new ZipEntry("vars"));
        ObjectOutputStream objOut = new ObjectOutputStream(outJar);
        objOut.writeObject(varDef);
        objOut.flush();
        outJar.closeEntry();
    }

    // Adds a resource
    public void addResource(String resId, InputStream input) throws Exception
    {
        sendMsg("Adding resource : " + resId + " ...");
        
        outJar.putNextEntry(new ZipEntry("res/" + resId));        
        copyStream(input, outJar);
        outJar.closeEntry();
        input.close();
    }
    
    // Adds a native library
    public void addNativeLibrary(String name, InputStream input) throws Exception
    {
        sendMsg("Adding native library : " + name + " ...");
        
        outJar.putNextEntry(new ZipEntry("native/" + name));
        copyStream(input, outJar);
        outJar.closeEntry();
        input.close();
    }
    
    // Adds a language pack
    public void addLangPack(String iso3, InputStream input) throws Exception
    {
        sendMsg("Adding langpack : " + iso3 + " ...");
        
        langpacks.add(iso3);
        
        outJar.putNextEntry(new ZipEntry("langpacks/" + iso3 + ".xml"));        
        copyStream(input, outJar);
        outJar.closeEntry();
        input.close();
    }

    // Adds a jar file content to the installer
    public void addJarContent(String file) throws Exception
    {
        sendMsg("Adding a jar file content ...");
        JarFile jar = new JarFile(file);
        Enumeration entries = jar.entries();
        while (entries.hasMoreElements())
        {
            // Puts a new entry
            ZipEntry zentry = (ZipEntry) entries.nextElement();
            try
            {
                InputStream zin = jar.getInputStream(zentry);
                outJar.putNextEntry(new ZipEntry(zentry.getName()));
                
                // Copy the data
                copyStream(zin, outJar);
                outJar.closeEntry();
                zin.close();
            } catch (ZipException zerr) {}
        }
    }
    
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
        datOut.writeUTF("standard");
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
