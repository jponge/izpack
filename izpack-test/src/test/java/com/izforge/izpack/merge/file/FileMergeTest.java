package com.izforge.izpack.merge.file;

import com.izforge.izpack.matcher.MergeMatcher;
import org.hamcrest.core.Is;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileFilter;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for fileMerge
 *
 * @author Anthonin Bonnefoy
 */
public class FileMergeTest
{
    private Map<OutputStream, List<String>> mergeContent = new HashMap<OutputStream, List<String>>();

    @Test
    public void testMergeSingleFile() throws Exception
    {
        FileMerge fileMerge = new FileMerge(getClass().getResource("FileMergeTest.class"), mergeContent);
        assertThat(fileMerge, MergeMatcher.isMergeableContainingFile("FileMergeTest.class"));
    }

    @Test
    public void testMergeDirectory() throws Exception
    {
        URL url = ClassLoader.getSystemResource("com/izforge/izpack/merge/");
        FileMerge fileMerge = new FileMerge(url, mergeContent);
        assertThat(fileMerge, MergeMatcher.isMergeableContainingFile("merge/test/.placeholder"));
    }

    @Test
    public void testMergeDirectoryWithDestination() throws Exception
    {
        URL url = ClassLoader.getSystemResource("com/izforge/izpack/merge/");
        FileMerge fileMerge = new FileMerge(url, "my/dest/path/", mergeContent);
        assertThat(fileMerge, MergeMatcher.isMergeableContainingFiles("my/dest/path/MergeManagerTest.class", "my/dest/path/test/.placeholder"));
    }

    @Test
    public void testMergeFileWithDestination() throws Exception
    {
        URL url = ClassLoader.getSystemResource("com/izforge/izpack/merge/file/FileMergeTest.class");
        FileMerge fileMerge = new FileMerge(url, "my/dest/path/NewFile.ga", mergeContent);
        assertThat(fileMerge, MergeMatcher.isMergeableContainingFiles("my/dest/path/NewFile.ga"));
    }

    @Test
    public void testMergeFileWithRootDestination() throws Exception
    {
        URL url = ClassLoader.getSystemResource("com/izforge/izpack/merge/file/FileMergeTest.class");
        FileMerge fileMerge = new FileMerge(url, "NewFile.ga", mergeContent);
        assertThat(fileMerge, MergeMatcher.isMergeableContainingFiles("NewFile.ga"));
    }


    @Test
    public void findFileInDirectory() throws Exception
    {
        FileMerge fileMerge = new FileMerge(ClassLoader.getSystemResource("com/izforge/izpack/merge/test"), mergeContent);
        File file = fileMerge.find(new FileFilter()
        {
            public boolean accept(File pathname)
            {
                return pathname.getName().equals(".placeholder") || pathname.isDirectory();
            }
        });
        assertThat(file.getName(), Is.is(".placeholder"));
    }

}
