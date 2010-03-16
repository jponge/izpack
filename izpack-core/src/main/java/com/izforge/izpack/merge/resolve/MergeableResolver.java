package com.izforge.izpack.merge.resolve;

import com.izforge.izpack.merge.panel.PanelMerge;

import java.io.OutputStream;
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
        return new PanelMerge(className, pathResolver.getMergeableFromPath(ResolveUtils.getPackagePathFromClassName(className)), mergeContent);
    }
}
