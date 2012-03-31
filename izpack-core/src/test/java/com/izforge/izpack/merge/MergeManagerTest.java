package com.izforge.izpack.merge;

import com.izforge.izpack.core.container.TestMergeContainer;
import com.izforge.izpack.matcher.MergeMatcher;
import com.izforge.izpack.merge.resolve.ResolveUtils;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.junit.PicoRunner;
import org.hamcrest.core.Is;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URL;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test a single file merge
 *
 * @author Anthonin Bonnefoy
 */
@RunWith(PicoRunner.class)
@Container(TestMergeContainer.class)
public class MergeManagerTest
{
    private MergeManagerImpl mergeManager;

    public MergeManagerTest(MergeManagerImpl mergeManager)
    {
        this.mergeManager = mergeManager;
    }

    @Test
    public void testProcessJarPath() throws Exception
    {
        URL resource = new URL("file:/home/test/unjar.jar!com/package/in/jar");
        String jarPath = ResolveUtils.processUrlToJarPath(resource);
        System.out.println(jarPath);
        assertThat(jarPath, Is.is("/home/test/unjar.jar"));
    }

    @Test
    public void testAddDirectoryWithFile() throws Exception
    {
        mergeManager.addResourceToMerge("dtd/");
        assertThat(mergeManager, MergeMatcher.isMergeableContainingFiles("dtd/conditions.dtd"));
    }

    @Test
    public void testAddResourceToMerge() throws Exception
    {
        mergeManager.addResourceToMerge("com/izforge/izpack/merge/");
        assertThat(mergeManager,
                   MergeMatcher.isMergeableContainingFiles("com/izforge/izpack/merge/MergeManager.class"));
    }

    @Test
    public void testAddResourceToMergeWithDestination() throws Exception
    {
        mergeManager.addResourceToMerge("com/izforge/izpack/merge/", "com/dest/");
        assertThat(mergeManager, MergeMatcher.isMergeableContainingFiles("com/dest/MergeManager.class"));
    }

    @Test
    public void testAddSingleClassToMergeWithDestinationFromAJar() throws Exception
    {
        mergeManager.addResourceToMerge("org/junit/", "com/dest/");
        assertThat(mergeManager, MergeMatcher.isMergeableContainingFiles("com/dest/Assert.class"));
    }

    @Test
    public void testAddPackageToMergeWithDestinationFromAJar() throws Exception
    {
        mergeManager.addResourceToMerge("org/junit", "com/dest");
        assertThat(mergeManager, MergeMatcher.isMergeableContainingFiles("com/dest/Assert.class"));
    }


}
