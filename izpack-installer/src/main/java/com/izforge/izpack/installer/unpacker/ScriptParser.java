/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2001 Johannes Lehtinen
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

package com.izforge.izpack.installer.unpacker;

import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.data.ParsableFile;
import com.izforge.izpack.util.OsConstraintHelper;

import java.io.*;
import java.util.Collection;

/**
 * The script parser classe.
 *
 * @author Julien Ponge
 * @author Johannes Lehtinen
 */
public class ScriptParser
{
    /**
    /**
     * The files to parse.
     */
    private Collection<ParsableFile> files;

    /**
     * The variables substituror.
     */
    private VariableSubstitutor vs;

    /**
     * Constructs a new parser. The parsable files specified must have pretranslated paths
     * (variables expanded and file separator characters converted if necessary).
     *
     * @param files the parsable files to process
     * @param vs    the variable substitutor to use
     */
    public ScriptParser(Collection<ParsableFile> files, VariableSubstitutor vs)
    {
        this.files = files;
        this.vs = vs;
    }

    /**
     * Parses the files.
     *
     * @throws Exception Description of the Exception
     */
    public void parseFiles() throws Exception
    {
        // Parses the files
        for (ParsableFile pfile : files)
        {
            // Create a temporary file for the parsed data
            // (Use the same directory so that renaming works later)

            // If interrupt is desired, return immediately.
            if (Unpacker.isInterruptDesired())
            {
                return;
            }

            // check whether the OS matches
            if (!OsConstraintHelper.oneMatchesCurrentSystem(pfile.osConstraints))
            {
                continue;
            }

            File file = new File(pfile.path);
            File parsedFile = File.createTempFile("izpp", null, file.getParentFile());

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
            {
                throw new IOException("Could not rename file " + parsedFile + " to " + file);
            }
        }
    }
}
