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
public class MergeManager implements Mergeable {

    public static String CLASSNAME_PREFIX = "com.izforge.izpack.panels";
    public static String BASE_CLASSNAME_PATH = CLASSNAME_PREFIX.replaceAll("\\.", "/");

    private List<Mergeable> mergeableList;

    public MergeManager() {
        mergeableList = new ArrayList<Mergeable>();
    }

    public static File getFileFromPath(String path) {
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

    public static Mergeable getMergeableFromPath(String path) {
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

    public void addResourceToMerge(String resourcePath) {
        mergeableList.add(getMergeableFromPath(resourcePath));
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
        return processUrlToJarPath(resource);
    }

    public static String processUrlToJarPath(URL resource) {
        String res = resource.getPath();
        res = res.replaceAll("file:", "");
        return res.substring(0, res.lastIndexOf("!"));
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
    public Mergeable getMergeableFromPanelClass(final String panelClassName) {
        File classFile = getFileFromPanelClass(panelClassName, getPackagePathFromClassName(panelClassName));
        String packagePath = classFile.getAbsolutePath().substring(0, classFile.getAbsolutePath().lastIndexOf('/'));
        return MergeManager.getMergeableFromPath(packagePath);
    }

    public File getFileFromPanelClass(final String panelClassName, String classPackage) {
        // Get the mergeable for corresponding package
        Mergeable mergeable = MergeManager.getMergeableFromPath(classPackage);

        FileFilter fileFilter = new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isDirectory() ||
                        pathname.getName().replaceAll(".class", "").equalsIgnoreCase(panelClassName);
            }
        };
        File classFile = mergeable.find(fileFilter);
        if (classFile == null) {
            throw new RuntimeException("panel class " + panelClassName + " not found in package " + classPackage);
        }
        return classFile;
    }


    /**
     * @param classFile
     * @param currentPackage
     * @return
     */
    public String getFullClassNameFromFileAndPackage(File classFile, String currentPackage) {
        if (currentPackage.contains("/")) {
            currentPackage = currentPackage.replaceAll("/", ".");
        }
        String fullClassName = classFile.getAbsolutePath().replaceAll("/", ".").replaceAll(".class", "");
        fullClassName = fullClassName.substring(fullClassName.indexOf(currentPackage), fullClassName.length());
        return fullClassName;
    }

    /**
     * Guess the path to the package of a given className
     *
     * @param className Can be a fullClassName or the canonical name
     * @return If it's the canonical name, return the default package. Else, return the package of the given classname.
     */
    public String getPackagePathFromClassName(String className) {
        if (className.contains(".")) {
            return className.substring(0, className.lastIndexOf(".")).replaceAll("\\.", "/");
        }
        return BASE_CLASSNAME_PATH;
    }

    public void addPanelToMerge(String className) {
        Mergeable mergeable = getMergeableFromPanelClass(className);
        mergeableList.add(mergeable);
    }
}
