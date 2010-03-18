package com.izforge.izpack.merge.panel;

import com.izforge.izpack.api.merge.Mergeable;
import org.apache.tools.zip.ZipOutputStream;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Merge for a panel
 *
 * @author Anthonin Bonnefoy
 */
public class PanelMerge implements Mergeable
{
    private List<Mergeable> packageMerge;
    private Class panelClass;
    private FileFilter fileFilter;

    public PanelMerge(final Class panelClass, List<Mergeable> packageMergeable)
    {
        this.panelClass = panelClass;
        packageMerge = packageMergeable;
        fileFilter = new FileFilter()
        {
            public boolean accept(File pathname)
            {
                return pathname.isDirectory() ||
                        pathname.getAbsolutePath().contains("/" + panelClass + ".class");
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

    public Class getPanelClass()
    {
        return panelClass;
    }
}
