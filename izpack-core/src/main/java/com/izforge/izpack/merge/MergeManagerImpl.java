package com.izforge.izpack.merge;

import org.apache.tools.zip.ZipOutputStream;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A mergeable file allow to chose files to merge in the installer.<br />
 * The source can be files in jar or directory. You may also filters sources with a given regexp.
 *
 * @author Anthonin Bonnefoy
 */
public class MergeManagerImpl implements MergeManager {

    public static String CLASSNAME_PREFIX = "com.izforge.izpack.panels";
    public static String BASE_CLASSNAME_PATH = CLASSNAME_PREFIX.replaceAll("\\.", "/");

    private List<Mergeable> mergeableList;

    public MergeManagerImpl() {
        mergeableList = new ArrayList<Mergeable>();
    }

    public void addPanelToMerge(String className) {
        Mergeable mergeable = getMergeableFromPanelClass(className);
        mergeableList.add(mergeable);
    }

    public void addResourceToMerge(Mergeable mergeable) {
        mergeableList.add(mergeable);
    }

    public void addResourceToMerge(String resourcePath) {
        mergeableList.add(getMergeableFromPath(resourcePath));
    }

    public void addResourceToMerge(String resourcePath, String destination) {
        mergeableList.add(getMergeableFromPath(resourcePath, destination));
    }


    /**
     * Search if the given path exist in the classpath and return it. If nothing is found,
     * try to load it as a file and return it if exists.
     *
     * @param path The path of File to load.
     * @return The file or null if nothing found.
     */
    static File getFileFromPath(String path) {
        URL resource = ClassLoader.getSystemClassLoader().getResource(path);
        if (resource != null) {
            File file = new File(resource.getFile());
            if (file.exists()) {
                return file;
            }
        }
        File file = new File(path);
        if (file.exists()) {
            return file;
        }
        return null;
    }


    /**
     * Search for the sourcePath in classpath (inside jar or directory) or as a normal path and then return the type or File.
     *
     * @param sourcePath Source path to search
     * @return Type of file (jar, file or directory)
     */
    private static TypeFile resolvePath(String sourcePath) {
        URL resource = ClassLoader.getSystemClassLoader().getResource(sourcePath);
        if (resource != null) {
            if (resource.getPath().contains("jar!")) {
                return TypeFile.JAR_CONTENT;
            }
        }
        File path = getFileFromPath(sourcePath);
        if (path != null) {
            if (path.isDirectory()) {
                return TypeFile.DIRECTORY;
            } else {
                return TypeFile.FILE;
            }
        }
        throw new IllegalArgumentException("Invalid source path : " + sourcePath);
    }

    /**
     * Return the mergeable from the given path.
     *
     * @param path path of the mergeable
     * @return Mergeable if found. null else.
     */
    static Mergeable getMergeableFromPath(String path) {
        TypeFile file = resolvePath(path);
        switch (file) {
            case DIRECTORY:
            case FILE:
                return new FileMerge(getFileFromPath(path), path);
            case JAR_CONTENT:
                return new JarMerge(path);
        }
        return null;
    }

    private Mergeable getMergeableFromPath(String path, String destination) {
        TypeFile file = resolvePath(path);
        switch (file) {
            case DIRECTORY:
            case FILE:
                return new FileMerge(getFileFromPath(path), destination);
            case JAR_CONTENT:
                return new JarMerge(path, destination);
        }
        return null;
    }


    /**
     * Return the mergeable from the given file.
     *
     * @param file file to merge
     * @return Mergeable of the given file
     */
    static Mergeable getMergeableFromPath(File file) {
        if (isJar(file)) {
            return new JarMerge(file.getAbsolutePath());
        }
        return new FileMerge(file);
    }


    /**
     * Get the path to jar containing the given resource.
     *
     * @param resourcePath Resource inside a jar in the classPath.<br />
     *                     You might give class files like "junit/framework/Assert.class" or a package "junit/framework/" to get path of the junit jar.
     * @return Absolute path of the jar.
     */
    static String getJarAbsolutePath(String resourcePath) {
        URL resource = ClassLoader.getSystemClassLoader().getResource(resourcePath);
        return processUrlToJarPath(resource);
    }

    static String processUrlToJarPath(URL resource) {
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

    public void merge(ZipOutputStream outputStream) {
        for (Mergeable mergeable : mergeableList) {
            mergeable.merge(outputStream);
        }
        mergeableList.clear();
    }

    public File find(FileFilter fileFilter) {
        for (Mergeable mergeable : mergeableList) {
            File file = mergeable.find(fileFilter);
            if (file != null) {
                return file;
            }
        }
        return null;
    }

    /**
     * Get the package containing the class with the given name.
     *
     * @param panelClassName Can be a full class name or only the canonical name.<br />
     *                       For example com.izforge.izpack.panels.hello.HelloPanel and HelloPanel will return the same
     *                       mergeable.
     * @return
     */
    Mergeable getMergeableFromPanelClass(final String panelClassName) {
        File classFile = getFileFromPanelClass(panelClassName, getPackagePathFromClassName(panelClassName));
        if (isJar(classFile)) {
            return new JarMerge(classFile.getParentFile());
        }
        return new FileMerge(classFile.getParentFile());
    }

    private static boolean isJar(File classFile) {
        return classFile.getAbsolutePath().contains(".jar!");
    }

    public File getFileFromPanelClass(final String panelClassName, String globalPackage) {
        // Get the mergeable for corresponding package
        Mergeable mergeable = MergeManagerImpl.getMergeableFromPath(globalPackage);

        FileFilter fileFilter = new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isDirectory() ||
                        pathname.getName().replaceAll(".class", "").equalsIgnoreCase(panelClassName);
            }
        };
        File classFile = mergeable.find(fileFilter);
        if (classFile == null) {
            throw new RuntimeException("panel class " + panelClassName + " not found in package " + globalPackage);
        }
        return classFile;
    }

    public String getPackagePathFromClassName(String className) {
        if (className.contains(".")) {
            return className.substring(0, className.lastIndexOf(".")).replaceAll("\\.", "/");
        }
        return BASE_CLASSNAME_PATH;
    }
}
