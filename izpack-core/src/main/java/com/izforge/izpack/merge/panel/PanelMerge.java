package com.izforge.izpack.merge.panel;

import com.izforge.izpack.merge.Mergeable;
import com.izforge.izpack.merge.resolve.PathResolver;
import org.apache.tools.zip.ZipOutputStream;

import java.io.File;
import java.io.FileFilter;
import java.util.List;

/**
 * Merge for a panel
 *
 * @author Anthonin Bonnefoy
 */
public class PanelMerge implements Mergeable {
    public static String CLASSNAME_PREFIX = "com.izforge.izpack.panels";
    public static String BASE_CLASSNAME_PATH = CLASSNAME_PREFIX.replaceAll("\\.", "/") + "/";

    private List<Mergeable> panelMerge;
    private String panelName;

    public PanelMerge(String panelName) {
        this.panelName = panelName;
        String packagePath = getPackagePathFromClassName(panelName);
        panelMerge = PathResolver.getMergeableFromPath(packagePath);
    }

    public void merge(ZipOutputStream outputStream) {
        for (Mergeable mergeable : panelMerge) {
            mergeable.merge(outputStream);
        }
    }

    public File find(FileFilter fileFilter) {
        for (Mergeable mergeable : panelMerge) {
            File file = mergeable.find(fileFilter);
            if (file != null) {
                return file;
            }
        }
        return null;
    }

    public void merge(java.util.zip.ZipOutputStream outputStream) {
        for (Mergeable mergeable : panelMerge) {
            mergeable.merge(outputStream);
        }
    }

    public String getPackagePathFromClassName(String className) {
        if (className.contains(".")) {
            return className.substring(0, className.lastIndexOf(".")).replaceAll("\\.", "/") + "/";
        }
        return BASE_CLASSNAME_PATH;
    }


    public String getFullClassNameFromPanelName() {
        if (panelName.contains(".")) {
            return panelName;
        }
        FileFilter fileFilter = new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getAbsolutePath().contains(panelName);
            }
        };
        for (Mergeable mergeable : panelMerge) {
            File file = mergeable.find(fileFilter);
            if (file != null) {
                System.out.println(file.getPath());
            }
        }
        return CLASSNAME_PREFIX + "." + panelName;
    }
}
