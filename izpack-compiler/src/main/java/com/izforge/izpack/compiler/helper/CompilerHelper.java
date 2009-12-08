package com.izforge.izpack.compiler.helper;

import org.apache.commons.lang.StringUtils;


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
}
