package com.izforge.izpack.merge;

import com.izforge.izpack.merge.resolve.PathResolver;
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
    public static String BASE_CLASSNAME_PATH = CLASSNAME_PREFIX.replaceAll("\\.", "/") + "/";

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
        mergeableList.addAll(PathResolver.getMergeableFromPath(resourcePath));
    }

    public void addResourceToMerge(String resourcePath, String destination) {
        mergeableList.addAll(PathResolver.getMergeableFromPath(resourcePath, destination));
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

    public void merge(java.util.zip.ZipOutputStream outputStream) {
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
//        File classFile = getFileFromPanelClass(panelClassName, getPackagePathFromClassName(panelClassName));
//        if (PathResolver.isJar(classFile)) {
//            return new JarMerge(classFile.getParentFile());
//        }
//        return new FileMerge(classFile.getParentFile(), getPackagePathFromClassNameAndClassFile(classFile, panelClassName));
        return null;
    }

    private String getPackagePathFromClassNameAndClassFile(File classFile, String panelClassName) {
        String classFilePath = classFile.getAbsolutePath();
        String relativePanelPath = classFilePath.substring(classFilePath.lastIndexOf(getPackagePathFromClassName(panelClassName)));
        return relativePanelPath.substring(0, relativePanelPath.lastIndexOf("/")) + "/";
    }

//    public File getFileFromPanelClass(final String panelClassName, String globalPackage) {
//        // Get the mergeable for corresponding package
//        Mergeable mergeable = MergeHelper.getMergeableFromPath(globalPackage);
//
//        assert mergeable != null;
//        FileFilter fileFilter = new FileFilter() {
//            public boolean accept(File pathname) {
//                return pathname.isDirectory() ||
//                        pathname.getName().replaceAll(".class", "").equalsIgnoreCase(panelClassName);
//            }
//        };
//        File classFile = mergeable.find(fileFilter);
//        if (classFile == null) {
//            throw new RuntimeException("panel class " + panelClassName + " not found in package " + globalPackage + ". The current classpath is " + PathResolver.getCurrentClasspath("com/izforge/izpack/"));
//        }
//        return classFile;
//    }

    public String getPackagePathFromClassName(String className) {
        if (className.contains(".")) {
            return className.substring(0, className.lastIndexOf(".")).replaceAll("\\.", "/") + "/";
        }
        return BASE_CLASSNAME_PATH;
    }
}
