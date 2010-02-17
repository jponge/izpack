package com.izforge.izpack.merge.file;

import com.izforge.izpack.matcher.MergeMatcher;
import org.hamcrest.core.Is;
import org.junit.Test;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for fileMerge
 *
 * @author Anthonin Bonnefoy
 */
public class FileMergeTest {

    @Test
    public void testMergeSingleFile() throws Exception {
        FileMerge fileMerge = new FileMerge(getClass().getResource("FileMerge.class"));
        assertThat(fileMerge, MergeMatcher.isMergeableContainingFile("FileMerge.class"));
    }

    @Test
    public void testMergeDirectory() throws Exception {
        URL url = ClassLoader.getSystemResource("com/izforge/izpack/merge/");
        FileMerge fileMerge = new FileMerge(url);
        assertThat(fileMerge, MergeMatcher.isMergeableContainingFile("merge/test/.placeholder"));
    }

    @Test
    public void testMergeDirectoryWithDestination() throws Exception {
        URL url = ClassLoader.getSystemResource("com/izforge/izpack/merge/");
        FileMerge fileMerge = new FileMerge(url, "my/dest/path/");
        assertThat(fileMerge, MergeMatcher.isMergeableContainingFiles("my/dest/path/MergeManagerTest.class", "my/dest/path/test/.placeholder"));
    }

    @Test
    public void testMergeFileWithDestination() throws Exception {
        URL url = ClassLoader.getSystemResource("com/izforge/izpack/merge/Mergeable.class");
        FileMerge fileMerge = new FileMerge(url, "my/dest/path/NewFile.ga");
        assertThat(fileMerge, MergeMatcher.isMergeableContainingFiles("my/dest/path/NewFile.ga"));
    }

    @Test
    public void testMergeFileWithRootDestination() throws Exception {
        URL url = ClassLoader.getSystemResource("com/izforge/izpack/merge/Mergeable.class");
        FileMerge fileMerge = new FileMerge(url, "NewFile.ga");
        assertThat(fileMerge, MergeMatcher.isMergeableContainingFiles("NewFile.ga"));
    }


    @Test
    public void findFileInDirectory() throws Exception {
        FileMerge fileMerge = new FileMerge(ClassLoader.getSystemResource("com/izforge/izpack/merge/test"));
        File file = fileMerge.find(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().equals(".placeholder") || pathname.isDirectory();
            }
        });
        assertThat(file.getName(), Is.is(".placeholder"));
    }

}
