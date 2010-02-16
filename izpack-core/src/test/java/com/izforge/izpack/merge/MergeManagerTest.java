package com.izforge.izpack.merge;

import com.izforge.izpack.matcher.MergeMatcher;
import com.izforge.izpack.merge.resolve.PathResolver;
import org.hamcrest.core.Is;
import org.hamcrest.text.StringEndsWith;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test a single file merge
 *
 * @author Anthonin Bonnefoy
 */
public class MergeManagerTest {
    private MergeManagerImpl mergeManager;

    @Before
    public void setUp() {
        mergeManager = new MergeManagerImpl();
    }

    @Test
    public void testGetJarPath() throws Exception {
        String jarPath = MergeManagerImpl.getJarAbsolutePath("junit/framework/Assert.class");
        assertThat(jarPath, StringEndsWith.endsWith("junit-4.7.jar"));
        assertThat(new File(jarPath).exists(), Is.is(true));
    }

    @Test
    public void testGetJarFromPackage() throws Exception {
        String jarPath = MergeManagerImpl.getJarAbsolutePath("junit/framework");
        assertThat(jarPath, StringEndsWith.endsWith("junit-4.7.jar"));
        assertThat(new File(jarPath).exists(), Is.is(true));
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
