package com.izforge.izpack.merge;

/**
 * Manager for all the merging logic.
 *
 * @author Anthonin Bonnefoy
 */
public interface MergeManager extends Mergeable {

    /**
     * Add the given panel to merge.<br />
     * If className has a canonical form (for example, HelloPanel), then the class is searched inside the default package containing standards IzPack panels.<br />
     * If className is a full class name (com.my.package.MyPanel), it simply check that the class exists at the given package.
     * The merge is applied to the package containing the class. So you can join resources and others helper classes, it will be avaible during installation.
     *
     * @param className ClassName to merge. Can be canonical (HelloPanel) or complete (com.izforge.panels.hello.HelloPanel)
     */
    void addPanelToMerge(String className);

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


    /**
     * Guess the path to the package of a given className
     *
     * @param className Can be a fullClassName or the canonical name
     * @return If it's the canonical name, return the default package. Else, return the package of the given classname.
     */
    String getPackagePathFromClassName(String className);

    void addResourceToMerge(Mergeable mergeable);
}
