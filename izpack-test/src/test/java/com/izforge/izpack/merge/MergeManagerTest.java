package com.izforge.izpack.merge;

import com.izforge.izpack.matcher.MergeMatcher;
import com.izforge.izpack.merge.resolve.PathResolver;
import org.hamcrest.core.Is;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URL;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test a single file merge
 *
 * @author Anthonin Bonnefoy
 */
public class MergeManagerTest {
    private MergeManagerImpl mergeManager;

    @BeforeMethod
    public void setUp() {
        mergeManager = new MergeManagerImpl();
    }

    @Test
    public void testProcessJarPath() throws Exception {
        URL resource = new URL("file:/home/test/unjar.jar!com/package/in/jar");
        String jarPath = PathResolver.processUrlToJarPath(resource);
        System.out.println(jarPath);
        assertThat(jarPath, Is.is("/home/test/unjar.jar"));
    }


    @Test
    public void testAddResourceToMerge() throws Exception {
        mergeManager.addResourceToMerge("com/izforge/izpack/merge/");
        assertThat(mergeManager, MergeMatcher.isMergeableContainingFiles("com/izforge/izpack/merge/MergeManager.class"));
    }

    @Test
    public void testAddResourceToMergeWithDestination() throws Exception {
        mergeManager.addResourceToMerge("com/izforge/izpack/merge/", "com/dest/");
        assertThat(mergeManager, MergeMatcher.isMergeableContainingFiles("com/dest/MergeManager.class"));
    }

    @Test
    public void testAddSingleClassToMergeWithDestinationFromAJar() throws Exception {
        mergeManager.addResourceToMerge("org/junit/Assert.class", "com/dest/Assert.class");
        assertThat(mergeManager, MergeMatcher.isMergeableContainingFiles("com/dest/Assert.class"));
    }

    @Test
    public void testAddPackageToMergeWithDestinationFromAJar() throws Exception {
        mergeManager.addResourceToMerge("org/junit", "com/dest");
        assertThat(mergeManager, MergeMatcher.isMergeableContainingFiles("com/dest/Assert.class"));
    }


}
