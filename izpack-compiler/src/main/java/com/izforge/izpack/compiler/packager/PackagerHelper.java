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

package com.izforge.izpack.compiler.packager;

import com.izforge.izpack.compiler.stream.JarOutputStream;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

/**
 * Helper class for packager classes
 *
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 */
public class PackagerHelper {
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

    /**
     * Copies specified contents of one jar to another.
     * <p/>
     * <p/>
     * TODO: it would be useful to be able to keep signature information from signed jar files, can
     * we combine manifests and still have their content signed?
     */
    static void copyZip(ZipInputStream zin, org.apache.tools.zip.ZipOutputStream out,
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
                // Create new entry for zip file.
                org.apache.tools.zip.ZipEntry newEntry = new org.apache.tools.zip.ZipEntry(currentName);
                // Get input file date and time.
                long fileTime = zentry.getTime();
                // Make sure there is date and time set.
                if (fileTime != -1) {
                    newEntry.setTime(fileTime); // If found set it into output file.
                }
                out.putNextEntry(newEntry);

                copyStream(zin, out);
                out.closeEntry();
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

    /**
     * Return a stream for the next jar.
     */
    public static JarOutputStream getJarOutputStream(String name, File parentFile) throws IOException {
        File file = new File(parentFile, name);
        JarOutputStream jar = new JarOutputStream(file);
        jar.setLevel(Deflater.BEST_COMPRESSION);
        jar.setPreventClose(true); // Needed at using FilterOutputStreams which calls close
        // of the slave at finalizing.
        return jar;
    }


    public static List<File> getClassesFileInClasspath(String packageName) throws IOException, URISyntaxException {
        String regexp = "([a-z0-9]+/)*[a-z0-9]+";
        if (!packageName.matches(regexp)) {
            throw new IllegalArgumentException("Invalid package name format, it should respect the regexp " + regexp);
        }
        URL resource = ClassLoader.getSystemClassLoader().getResource(packageName);
        if (isJar(resource)) {
            return getAllClassesFilesFromJar(resource);
        } else {
            return getAllClassesFiles(resource);
        }
    }

    private static List<File> getAllClassesFilesFromJar(URL resource) {
        File directory = new File(resource.getFile());
        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    private static List<File> getAllClassesFiles(URL resource) {
        File directory = new File(resource.getFile());
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException(resource.getFile() + " is not a directory");
        }
        ArrayList<File> res = new ArrayList<File>();
        addDirectoryContent(directory, res);
        return res;
    }

    private static void addDirectoryContent(File directory, ArrayList<File> res) {
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                addDirectoryContent(file, res);
            } else {
                res.add(file);
            }
        }
    }

    private static boolean isJar(URL resource) {
        return resource.toString().contains("jar:file");
    }

    public static File getClasseFile(String className) {
        File classFile = new File("");

        return classFile;
    }


    private static List<Class<?>> getClassesFromJARFile(String jar, String packageName) throws Error, IOException {
        final List<Class<?>> classes = new ArrayList<Class<?>>();
        JarInputStream jarFile = null;
        try {
            jarFile = new JarInputStream(new FileInputStream(jar));
            JarEntry jarEntry;
            do {
                try {
                    jarEntry = jarFile.getNextJarEntry();
                }
                catch (IOException ioe) {
//                    throw new CCException.Error("Unable to get next jar entry from jar file '" + jar + "'", ioe);
                    throw new RuntimeException("F");
                }
                if (jarEntry != null) {
                    extractClassFromJar(packageName, classes, jarEntry);
                }
            } while (jarEntry != null);
            jarFile.close();
        }
        catch (IOException ioe) {
//            throw new CCException.Error("Unable to get Jar input stream from '" + jar + "'", ioe);
            throw new RuntimeException("F");
        }
        finally {
            jarFile.close();
        }
        return classes;
    }

    private static void extractClassFromJar(final String packageName, final List<Class<?>> classes, JarEntry jarEntry) throws Error {
        String className = jarEntry.getName();
        if (className.endsWith(".class")) {
            className = className.substring(0, className.length() - ".class".length());
            if (className.startsWith(packageName)) {
                try {
                    classes.add(Class.forName(className.replace('/', '.')));
                } catch (ClassNotFoundException cnfe) {
                    throw new RuntimeException("F");
                }
            }
        }
    }

}
