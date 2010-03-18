package com.izforge.izpack.merge.resolve;

import com.izforge.izpack.api.merge.Mergeable;
import com.izforge.izpack.merge.file.FileMerge;
import com.izforge.izpack.merge.jar.JarMerge;
import com.izforge.izpack.merge.panel.PanelMerge;

import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Helper methods for mergeable
 *
 * @author Anthonin Bonnefoy
 */
public class MergeableResolver
{
    private Map<OutputStream, List<String>> mergeContent;

    public MergeableResolver(Map<OutputStream, List<String>> mergeContent)
    {
        this.mergeContent = mergeContent;
    }

    public PanelMerge getPanelMerge(String className, PathResolver pathResolver)
    {
        return new PanelMerge(className, pathResolver.getMergeableFromPath(ResolveUtils.getPanelsPackagePathFromClassName(className)), mergeContent);
    }

    public Mergeable getMergeableFromURL(URL url)
    {
        if (!ResolveUtils.isJar(url))
        {
            return new FileMerge(url, mergeContent);
        }
        return new JarMerge(url, ResolveUtils.processUrlToJarPath(url), mergeContent);
    }

    public Mergeable getMergeableFromURL(URL url, String resourcePath)
    {
        if (ResolveUtils.isJar(url))
        {
            return new JarMerge(url, ResolveUtils.processUrlToJarPath(url), mergeContent);
        }
        else
        {
            return new FileMerge(url, resourcePath, mergeContent);
        }
    }

    public Mergeable getMergeableFromURLWithDestination(URL url, String destination)
    {
        if (ResolveUtils.isJar(url))
        {
            return new JarMerge(ResolveUtils.processUrlToJarPath(url), ResolveUtils.processUrlToJarPackage(url), destination, mergeContent);
        }
        else
        {
            return new FileMerge(url, destination, mergeContent);
        }
    }
}
