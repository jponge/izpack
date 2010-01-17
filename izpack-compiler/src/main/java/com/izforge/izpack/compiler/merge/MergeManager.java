package com.izforge.izpack.compiler.merge;

import org.apache.commons.lang.StringUtils;
import org.apache.tools.zip.ZipOutputStream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * A mergeable file allow to chose files to merge in the installer.<br />
 * The source can be files in jar or directory. You may also filters sources with a given regexp.
 *
 * @author Anthonin Bonnefoy
 */
public class MergeManager implements Mergeable {

    /**
     * Filter
     */
    private String regexpFilter;

    /**
     * Path to the resource to merge. Can be a inside the classpath or the absolute path of a file.
     */
    private String sourcePath;

    private Stage mergeStage;

    private TypeFile typeFile;

    public MergeManager(String sourcePath) {
        this(sourcePath, Stage.BOTH);
    }

    public MergeManager(String sourcePath, Stage mergeStage) {
        this(sourcePath, mergeStage, "");
    }

    public MergeManager(String sourcePath, Stage mergeStage, String regexpFilter) {
        this.mergeStage = mergeStage;
        this.sourcePath = sourcePath;
        this.regexpFilter = regexpFilter;
        resolvePath(sourcePath);
    }

    private List<InputStream> getInputStream() {
        return null;
    }

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


    public static List<File> getClassesFileInClasspath(String packageName) throws IOException, URISyntaxException {
        String regexp = "([a-z0-9]+/)*[a-z0-9]+";
        if (!packageName.matches(regexp)) {
            throw new IllegalArgumentException("Invalid package name format, it should respect the regexp " + regexp);
        }
        URL resource = ClassLoader.getSystemClassLoader().getResource(packageName);
        if (resource.toString().contains("jar:file")) {
            return getAllClassesFilesFromJarDirectory(resource);
        } else {
            return getAllClassesFilesFromDirectory(resource);
        }
    }

    private static List<File> getAllClassesFilesFromJarDirectory(URL resource) throws IOException {
        File tempDirectory = new File(System.getProperty("java.io.tmpdir"), "izpackTemp");
        tempDirectory.mkdir();
        copyJarToDirectory(resource, tempDirectory);
        ArrayList<File> res = new ArrayList<File>();
        addDirectoryContent(tempDirectory, res);
        return res;
    }

    private static void copyJarToDirectory(URL resource, File tempDirectory) throws IOException {
        assert resource.openStream() != null;
        JarInputStream inputStream = new JarInputStream(resource.openStream());
        JarEntry jarEntry;
        while ((jarEntry = inputStream.getNextJarEntry()) != null) {
            System.out.println(jarEntry.getName());
        }
    }

    /**
     * Get all classes files from a normal directory
     *
     * @param resource
     * @return
     */
    private static List<File> getAllClassesFilesFromDirectory(URL resource) {
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

    public static File getClasseFile(String className) {
        File classFile = new File("");

        return classFile;
    }

    public void merge(ZipOutputStream outputStream) {
        //To change body of implemented methods use File | Settings | File Templates.
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
