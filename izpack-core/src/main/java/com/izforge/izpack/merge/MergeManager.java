package com.izforge.izpack.merge;

import com.izforge.izpack.api.merge.Mergeable;

/**
 * Manager for all the merging logic.
 *
 * @author Anthonin Bonnefoy
 */
public interface MergeManager extends Mergeable
{

    /**
     * Add the given resource to merge in the produced installer. <br />
     * You can provide a resource, a className or a package to merge, it will be resolved by searching inside the classpath.<br />
     * By default, the destination path is the same as the source. If you give a package com/my/package, it will be added in com/my/package in the installer jar.
     *
     * @param resourcePath Resource path to merge in the installer. It should be of the form "com/izforge/izpack/"
     */
    void addResourceToMerge(String resourcePath);

    /**
     * Add the given resource to merge in the produced installer. <br />
     * You can provide a resource, a className or a package to merge, it will be resolved by searching inside the classpath.<br />
     * You can define the destination of the resource inside the produced installer jar.
     *
     * @param resourcePath Resource path to merge in the installer. It should be of the form "com/izforge/izpack/"
     * @param destination  Destination of the resource inside the jar.
     */
    void addResourceToMerge(String resourcePath, String destination);

    void addResourceToMerge(Mergeable mergeable);
}
