package com.izforge.izpack.compiler.merge;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.net.URL;

/**
 * A mergeable file allow to chose files to merge in the installer.<br />
 * The source can be files in jar or directory. You may also filters sources with a given regexp.
 *
 * @author Anthonin Bonnefoy
 */
public class MergeManager {

    private static TypeFile resolvePath(String sourcePath) {
        URL resource = ClassLoader.getSystemClassLoader().getResource(sourcePath);
        if (resource != null && resource.toString().contains("jar:file")) {
            return TypeFile.JAR_CONTENT;
        }
        File file = new File(sourcePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("Invalid source path : " + sourcePath);
        }
        if (file.isDirectory()) {
            return TypeFile.DIRECTORY;
        }
        return TypeFile.FILE;
    }

    public static Mergeable getMergeableFromPath(String path) {
        switch (resolvePath(path)) {
            case DIRECTORY:
                return null;
            case FILE:
                return null;
            case JAR_CONTENT:
                return new JarMerge(path);
        }
        return null;
    }

    /**
     * Get the path to jar containing the given resource.
     *
     * @param resourcePath Resource inside a jar in the classPath.<br />
     *                     You might give class files like "junit/framework/Assert.class" or a package "junit/framework/" to get path of the junit jar.
     * @return Absolute path of the jar.
     */
    public static String getJarAbsolutePath(String resourcePath) {
        URL resource = ClassLoader.getSystemClassLoader().getResource(resourcePath);
        String res;
        res = resource.getPath();
        res = StringUtils.substringAfter(res, "file:");
        return StringUtils.substringBefore(res, "!");
    }


}
