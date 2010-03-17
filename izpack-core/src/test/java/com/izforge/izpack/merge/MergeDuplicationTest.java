package com.izforge.izpack.merge;

import com.izforge.izpack.api.merge.Mergeable;
import com.izforge.izpack.container.TestMergeContainer;
import com.izforge.izpack.matcher.DuplicateMatcher;
import com.izforge.izpack.matcher.ZipMatcher;
import com.izforge.izpack.merge.resolve.MergeableResolver;
import com.izforge.izpack.merge.resolve.PathResolver;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.MergeUtils;
import com.izforge.izpack.test.junit.PicoRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.net.URL;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for merge duplication
 *
 * @author Anthonin Bonnefoy
 */
@RunWith(PicoRunner.class)
@Container(TestMergeContainer.class)
public class MergeDuplicationTest
{
    private PathResolver pathResolver;
    private MergeableResolver mergeableResolver;

    public MergeDuplicationTest(PathResolver pathResolver, MergeableResolver mergeableResolver)
    {
        this.pathResolver = pathResolver;
        this.mergeableResolver = mergeableResolver;
    }

    @Test
    public void testAddJarDuplicated() throws Exception
    {
        URL resource = ClassLoader.getSystemResource("com/izforge/izpack/merge/test/jar-hellopanel-1.0-SNAPSHOT.jar");
        Mergeable jarMerge = mergeableResolver.getMergeableFromURL(resource);
        File tempFile = MergeUtils.doDoubleMerge(jarMerge);
        assertThat(tempFile, ZipMatcher.isZipMatching(
                DuplicateMatcher.isEntryUnique("jar/izforge/izpack/panels/hello/HelloPanelConsoleHelper.class")
        ));
    }

    @Test
    public void testMergeDuplicateFile() throws Exception
    {
        Mergeable mergeable = pathResolver.getMergeableFromURL(getClass().getResource("MergeDuplicationTest.class"), "destFile");
        File tempFile = MergeUtils.doDoubleMerge(mergeable);
        assertThat(tempFile, ZipMatcher.isZipMatching(
                DuplicateMatcher.isEntryUnique("destFile")
        ));
    }

}
