package com.izforge.izpack.compiler.helper;

import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;


/**
 * Helper for compiler
 */
public class CompilerHelper {
    /**
     * Given an event class, return the jar path
     *
     * @param name Name of the event class
     * @return Path to the jar
     */
    public String resolveCustomActionsJarPath(String name) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("bin/customActions/");
        stringBuilder.append("izpack");
        stringBuilder.append(convertNameToDashSeparated(name));
        stringBuilder.append(".jar");
        return stringBuilder.toString();
    }

    /**
     * Convert a camel-case class name to a separate dashes nanem
     *
     * @param name Class name
     * @return minuscule separate dashe name
     */
    public StringBuilder convertNameToDashSeparated(String name) {
        StringBuilder res = new StringBuilder();
        for (String part : StringUtils.splitByCharacterTypeCamelCase(name)) {
            res.append('-');
            res.append(part.toLowerCase());
        }
        return res;
    }

    /**
     * Returns the qualified class name for the given class. This method expects as the url param a
     * jar file which contains the given class. It scans the zip entries of the jar file.
     *
     * @param url       url of the jar file which contains the class
     * @param className short name of the class for which the full name should be resolved
     * @return full qualified class name
     * @throws java.io.IOException
     */
    public String getFullClassName(URL url, String className) throws IOException {
        JarInputStream jis = new JarInputStream(url.openStream());
        ZipEntry zentry;
        while ((zentry = jis.getNextEntry()) != null) {
            String name = zentry.getName();
            int lastPos = name.lastIndexOf(".class");
            if (lastPos < 0) {
                continue; // No class file.
            }
            name = name.replace('/', '.');
            int pos = -1;
            int nonCasePos = -1;
            if (className != null) {
                pos = name.indexOf(className);
                nonCasePos = name.toLowerCase().indexOf(className.toLowerCase());
            }
            if (pos != -1 && name.length() == pos + className.length() + 6) // "Main" class found
            {
                jis.close();
                return (name.substring(0, lastPos));
            }

            if (nonCasePos != -1 && name.length() == nonCasePos + className.length() + 6)
            // "Main" class with different case found
            {
                throw new IllegalArgumentException(
                        "Fatal error! The declared panel name in the xml file (" + className
                                + ") differs in case to the founded class file (" + name + ").");
            }
        }
        jis.close();
        return (null);
    }
}
