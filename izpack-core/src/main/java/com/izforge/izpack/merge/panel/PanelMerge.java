package com.izforge.izpack.merge.panel;

import com.izforge.izpack.api.exception.MergeException;
import com.izforge.izpack.merge.Mergeable;
import org.apache.tools.zip.ZipOutputStream;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.List;

/**
 * Merge for a panel
 *
 * @author Anthonin Bonnefoy
 */
public class PanelMerge implements Mergeable
{

    // TODO Externalize this field in a property
    private final List<String> packageBegin = Arrays.asList("com/", "org/", "net/");
    private List<Mergeable> packageMerge;
    private String panelName;
    private FileFilter fileFilter;

    public PanelMerge(final String panelName, List<Mergeable> packageMergeable)
    {
        this.panelName = panelName;
        packageMerge = packageMergeable;
        fileFilter = new FileFilter()
        {
            public boolean accept(File pathname)
            {
                return pathname.isDirectory() ||
                        pathname.getAbsolutePath().contains("/" + panelName + ".class");
            }
        };
    }

    public void merge(ZipOutputStream outputStream)
    {
        for (Mergeable mergeable : packageMerge)
        {
            mergeable.merge(outputStream);
        }
    }

    public File find(FileFilter fileFilter)
    {
        for (Mergeable mergeable : packageMerge)
        {
            File file = mergeable.find(fileFilter);
            if (file != null)
            {
                return file;
            }
        }
        return null;
    }

    public void merge(java.util.zip.ZipOutputStream outputStream)
    {
        for (Mergeable mergeable : packageMerge)
        {
            mergeable.merge(outputStream);
        }
    }


    public String getFullClassNameFromPanelName()
    {
        if (packageMerge.isEmpty())
        {
            throw new MergeException("No mergeable found for panel " + panelName);
        }
        if (panelName.contains("."))
        {
            return panelName;
        }
        for (Mergeable mergeable : packageMerge)
        {
            File file = mergeable.find(fileFilter);
            if (file != null)
            {
                return processFileToClassName(file);
            }
        }
        throw new MergeException("Panel file " + panelName + " not found");
    }

    /**
     * Search for a standard package begin like com/ org/ net/
     *
     * @param file File to process
     * @return Full className
     */
    private String processFileToClassName(File file)
    {
        String absolutePath = file.getAbsolutePath();
        for (String packageString : packageBegin)
        {
            if (!absolutePath.contains(packageString))
            {
                continue;
            }
            return absolutePath.substring(absolutePath.lastIndexOf(packageString)).replaceAll("\\.class", "").replaceAll("/", ".");
        }
        throw new MergeException("No standard package begin found in " + file.getPath());
    }
}
