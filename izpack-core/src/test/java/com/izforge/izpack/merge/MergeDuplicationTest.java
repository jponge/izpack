package com.izforge.izpack.merge;

import com.izforge.izpack.matcher.DuplicateMatcher;
import com.izforge.izpack.matcher.ZipMatcher;
import com.izforge.izpack.merge.resolve.PathResolver;
import org.apache.tools.zip.ZipOutputStream;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for merge duplication
 *
 * @author Anthonin Bonnefoy
 */
public class MergeDuplicationTest
{
    private PathResolver pathResolver;

    @Before
    public void setUp()
    {
        this.pathResolver = new PathResolver();
    }


    @Test
    public void testAddJarDuplicated() throws Exception
    {
        URL resource = ClassLoader.getSystemResource("com/izforge/izpack/merge/test/jar-hellopanel-1.0-SNAPSHOT.jar");
        Mergeable jarMerge = pathResolver.getMergeableFromURL(resource);
        File tempFile = doDoubleMerge(jarMerge);
        assertThat(tempFile, ZipMatcher.isZipMatching(
                DuplicateMatcher.isEntryUnique("jar/izforge/izpack/panels/hello/HelloPanelConsoleHelper.class")
        ));
    }

    private File doDoubleMerge(Mergeable mergeable)
            throws IOException
    {
        File tempFile = File.createTempFile("test", ".zip");
        ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(tempFile));
        mergeable.merge(outputStream);
        mergeable.merge(outputStream);
        outputStream.close();
        return tempFile;
    }

    @Test
    public void testMergeDuplicateFile() throws Exception
    {
        Mergeable mergeable = pathResolver.getMergeableFromURL(getClass().getResource("MergeDuplicationTest.class"), "destFile");
        File tempFile = doDoubleMerge(mergeable);
        assertThat(tempFile, ZipMatcher.isZipMatching(
                DuplicateMatcher.isEntryUnique("destFile")
        ));
    }

    @Test
    public void testMergeDuplicatePanel() throws Exception
    {
        Mergeable mergeable = pathResolver.getPanelMerge("com.izforge.izpack.panels.hello.HelloPanel");
        File tempFile = doDoubleMerge(mergeable);
        assertThat(tempFile, ZipMatcher.isZipMatching(
                DuplicateMatcher.isEntryUnique("com/izforge/izpack/panels/hello/HelloPanel.class")
        ));
    }
}
