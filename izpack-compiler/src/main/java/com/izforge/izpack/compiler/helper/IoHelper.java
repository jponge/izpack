/*
 * $Id: Packager.java 1671 2007-01-02 10:28:58Z dreil $
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2006 Dennis Reil
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

package com.izforge.izpack.compiler.helper;

import org.apache.tools.zip.ZipOutputStream;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

/**
 * Helper class for packager classes
 *
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 */
public class IoHelper {
    /**
     * Copies specified contents of one jar to another.
     * <p/>
     * <p/>
     * TODO: it would be useful to be able to keep signature information from signed jar files, can
     * we combine manifests and still have their content signed?
     */
    public static void copyZip(ZipInputStream zin, org.apache.tools.zip.ZipOutputStream out,
                               List<String> files, HashMap<FilterOutputStream, HashSet<String>> alreadyWrittenFiles)
            throws IOException {
        ZipEntry zentry;
        if (!alreadyWrittenFiles.containsKey(out)) {
            alreadyWrittenFiles.put(out, new HashSet<String>());
        }
        HashSet<String> currentSet = alreadyWrittenFiles.get(out);
        while ((zentry = zin.getNextEntry()) != null) {
            String currentName = zentry.getName();
            String testName = currentName.replace('/', '.');
            testName = testName.replace('\\', '.');
            if (files != null) {
                Iterator<String> iterator = files.iterator();
                boolean founded = false;
                while (iterator.hasNext()) {   // Make "includes" self to support regex.
                    String doInclude = iterator.next();
                    if (testName.matches(doInclude)) {
                        founded = true;
                        break;
                    }
                }
                if (!founded) {
                    continue;
                }
            }
            if (currentSet.contains(currentName)) {
                continue;
            }
            try {
                // Get input file date and time.
                long fileTime = zentry.getTime();
                copyStreamToJar(zin, out, currentName, fileTime);
                zin.closeEntry();
                currentSet.add(currentName);
            }
            catch (ZipException x) {
                // This avoids any problem that can occur with duplicate
                // directories. for instance all META-INF data in jars
                // unfortunately this do not work with the apache ZipOutputStream...
            }
        }
    }

    public static void copyStreamToJar(InputStream zin, ZipOutputStream out, String currentName, long fileTime) throws IOException {
        // Create new entry for zip file.
        org.apache.tools.zip.ZipEntry newEntry = new org.apache.tools.zip.ZipEntry(currentName);
        // Make sure there is date and time set.
        if (fileTime != -1) {
            newEntry.setTime(fileTime); // If found set it into output file.
        }
        out.putNextEntry(newEntry);
        copyStream(zin, out);
        out.closeEntry();
    }

    /**
     * Copies all the data from the specified input stream to the specified output stream.
     *
     * @param in  the input stream to read
     * @param out the output stream to write
     * @return the total number of bytes copied
     * @throws IOException if an I/O error occurs
     */
    public static long copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[5120];
        long bytesCopied = 0;
        int bytesInBuffer;
        while ((bytesInBuffer = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesInBuffer);
            bytesCopied += bytesInBuffer;
        }
        return bytesCopied;
    }
}
