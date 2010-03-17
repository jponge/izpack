package com.izforge.izpack.merge.panel;

import com.izforge.izpack.api.exception.MergeException;
import com.izforge.izpack.api.merge.Mergeable;
import com.izforge.izpack.merge.ClassResolver;
import org.apache.tools.zip.ZipOutputStream;

import java.io.File;
import java.io.FileFilter;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Merge for a panel
 *
 * @author Anthonin Bonnefoy
 */
public class PanelMerge implements Mergeable
{
    private List<Mergeable> packageMerge;
    private String panelName;
    private FileFilter fileFilter;

    public PanelMerge(final String panelName, List<Mergeable> packageMergeable, Map<OutputStream, List<String>> mergeContent)
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

    public List<File> recursivelyListFiles(FileFilter fileFilter)
    {
        ArrayList<File> result = new ArrayList<File>();
        for (Mergeable mergeable : packageMerge)
        {
            result.addAll(mergeable.recursivelyListFiles(fileFilter));
        }
        return result;
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
                return ClassResolver.processFileToClassName(file);
            }
        }
        throw new MergeException("Panel file " + panelName + " not found");
    }

}
