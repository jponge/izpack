/*
 * IzPack Version 3.0.0 rc2 (build 2002.07.06)
 * Copyright (C) 2001,2002 Julien Ponge
 *
 * File :               ScriptParser.java
 * Description :        Parses the scripts files for special variables.
 * Author's email :     julien@izforge.com
 * Author's Website :   http://www.izforge.com
 *
 * Portions are Copyright (c) 2001 Johannes Lehtinen
 * johannes.lehtinen@iki.fi
 * http://www.iki.fi/jle/
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

package com.izforge.izpack.installer;

import com.izforge.izpack.ParsableFile;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class ScriptParser
{
    //.....................................................................
    // The constants

    public static final String INSTALL_PATH = "INSTALL_PATH";
    public static final String JAVA_HOME = "JAVA_HOME";
    public static final String USER_HOME = "USER_HOME";
    public static final String USER_NAME = "USER_NAME";
    public static final String FILE_SEPARATOR = "FILE_SEPARATOR";

    // The fields
    private Collection files; // The files to parse
    private VariableSubstitutor vs; // The variable substitutor to use

    //.....................................................................

    /**
     * Constructs a new parser. The parsable files specified must have
     * pretranslated paths (variables expanded and file separator characters
     * converted if necessary).
     *
     * @param files the parsable files to process
     * @param vs the variable substitutor to use
     */
    public ScriptParser(Collection files, VariableSubstitutor vs)
    {
        this.files = files;
        this.vs = vs;
    }

    //.....................................................................

    // Parses the files
    public void parseFiles() throws Exception
    {
        // Parses the files
        Iterator iter = files.iterator();
        while (iter.hasNext())
        {
            // Create a temporary file for the parsed data
            // (Use the same directory so that renaming works later)
            ParsableFile pfile = (ParsableFile) iter.next();
            File file = new File(pfile.path);
            File parsedFile
                = File.createTempFile("izpp", null, file.getParentFile());

            // Parses the file
            // (Use buffering because substitutor processes byte at a time)
            FileInputStream inFile = new FileInputStream(file);
            BufferedInputStream in = new BufferedInputStream(inFile, 5120);
            FileOutputStream outFile = new FileOutputStream(parsedFile);
            BufferedOutputStream out = new BufferedOutputStream(outFile, 5120);
            vs.substitute(in, out, pfile.type, pfile.encoding);
            in.close();
            out.close();

            // Replace the original file with the parsed one
            file.delete();
            if (!parsedFile.renameTo(file))
                throw new IOException
                    ("Could not rename file " + parsedFile + " to " + file);
        }
    }

    //.....................................................................
}
