package com.izforge.izpack.merge.resolve;

import com.izforge.izpack.api.merge.Mergeable;
import com.izforge.izpack.merge.file.FileMerge;
import com.izforge.izpack.merge.jar.JarMerge;

import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper methods for mergeable
 *
 * @author Anthonin Bonnefoy
 */
public class MergeableResolver
{
    private Map<OutputStream, List<String>> mergeContent = new HashMap<OutputStream, List<String>>();

    public MergeableResolver()
    {
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
            if (ResolveUtils.isFileInJar(url))
            {
                return new JarMerge(ResolveUtils.processUrlToJarPath(url), ResolveUtils.processUrlToInsidePath(url), destination, mergeContent);
            }
            return new JarMerge(ResolveUtils.processUrlToJarPath(url), ResolveUtils.processUrlToJarPackage(url), destination, mergeContent);
        }
        else
        {
            return new FileMerge(url, destination, mergeContent);
        }
    }
}
