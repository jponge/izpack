package com.izforge.izpack.merge.resolve;

import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.merge.Mergeable;
import com.izforge.izpack.merge.file.FileMerge;
import com.izforge.izpack.merge.jar.JarMerge;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipFile;

/**
 * Try to resolve paths by searching inside the classpath or files with the corresponding name
 *
 * @author Anthonin Bonnefoy
 */
public class PathResolver {


    /**
     * Search for the sourcePath in classpath (inside jar or directory) or as a normal path and then return the type or File.
     * Ignore all path containing test-classes.
     *
     * @param sourcePath Source path to search
     * @return url list
     */
    public static List<URL> resolvePath(String sourcePath) {
        List<URL> result = new ArrayList<URL>();
        URL path = getFileFromPath(sourcePath);
        if (path != null) {
            result.add(path);
        }

        try {
            Enumeration<URL> urlEnumeration = ClassLoader.getSystemClassLoader().getResources(sourcePath);
            while (urlEnumeration.hasMoreElements()) {
                URL url = urlEnumeration.nextElement();
                if (url.getPath().contains("test-classes")) {
                    continue;
                }
                result.add(url);
            }
        } catch (IOException e) {
            throw new IzPackException(e);
        }

        if (!result.isEmpty()) {
            return result;
        }
        throw new IzPackException("The path " + sourcePath + " is not present inside the classpath.\n The current classpath is :" + getCurrentClasspath("com/izforge/izpack/uninstaller/"));
    }

    public static boolean isJar(File classFile) {
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(classFile);
            zipFile.getName();
        } catch (IOException e) {
            return false;
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException ignored) {
                }
            }
        }
        return true;
    }

    /**
     * Extract file name from url and test jar file
     *
     * @param url url to test. If it is a jar content, the jar file is extracted and treated as a jar
     * @return true if the file is a jar
     */
    public static boolean isJar(URL url) {
        String file = url.getFile();
        file = file.substring(file.indexOf(":") + 1);
        if (file.contains("!")) {
            file = file.substring(0, file.lastIndexOf('!'));
        }
        File classFile = new File(file);
        return isJar(classFile);
    }

    public static String getCurrentClasspath(String packagePath) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            Enumeration<URL> urlEnumeration = Thread.currentThread().getContextClassLoader().getResources(packagePath);
            while (urlEnumeration.hasMoreElements()) {
                URL url = urlEnumeration.nextElement();
                stringBuilder.append(url.getPath());
                stringBuilder.append('\n');
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Search if the given path exist in the classpath and return it. If nothing is found,
     * try to load it as a file and return it if exists.
     *
     * @param path The path of File to load.
     * @return The file or null if nothing found.
     */
    static URL getFileFromPath(String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                return file.toURI().toURL();
            }
        } catch (MalformedURLException e) {
            throw new IzPackException(e);
        }
        return null;
    }

    /**
     * Return the mergeable from the given path.
     *
     * @param resourcePath Resource path to search
     * @return Mergeable list of mergeable. Empty if nothing found.
     */
    public static List<Mergeable> getMergeableFromPath(String resourcePath) {
        List<URL> urlList = resolvePath(resourcePath);
        List<Mergeable> result = new ArrayList<Mergeable>();
        for (URL url : urlList) {
            if (isJar(url)) {
                result.add(new JarMerge(url));
            } else {
                result.add(new FileMerge(url, resourcePath));
            }
        }
        return result;
    }

    /**
     * Return the mergeable from the given path.
     *
     * @param resourcePath Resource path to search
     * @param destination  The destination of resources when merging will ocure.
     * @return Mergeable list of mergeable. Empty if nothing found.
     */
    public static List<Mergeable> getMergeableFromPath(String resourcePath, String destination) {
        List<URL> urlList = resolvePath(resourcePath);
        List<Mergeable> result = new ArrayList<Mergeable>();
//        String fileDestination = (destination + "/" + resourcePath).replaceAll("//", "/");
        for (URL url : urlList) {
            if (isJar(url)) {
                result.add(new JarMerge(url, destination));
            } else {
                result.add(new FileMerge(url, destination));
            }
        }
        return result;
    }

    public static String processUrlToJarPath(URL resource) {
        String res = resource.getPath();
        res = res.replaceAll("file:", "");
        if (res.contains("!")) {
            return res.substring(0, res.lastIndexOf("!"));
        }
        return res;
    }

    public static String processUrlToJarPackage(URL resource) {
        String res = resource.getPath();
        res = res.replaceAll("file:", "");
        return res.substring(res.lastIndexOf("!") + 2, res.length());
    }
}
