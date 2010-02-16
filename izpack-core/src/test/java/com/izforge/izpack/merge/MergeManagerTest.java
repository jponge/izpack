package com.izforge.izpack.merge;

import com.izforge.izpack.matcher.MergeMatcher;
import org.hamcrest.core.Is;
import org.hamcrest.text.StringEndsWith;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test a single file merge
 *
 * @author Anthonin Bonnefoy
 */
public class MergeManagerTest {
    private File zip;
    private MergeManagerImpl mergeManager;

    @Before
    public void setUp() {
        zip = new File("outputZip.zip");
        zip.delete();
        mergeManager = new MergeManagerImpl();
    }

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
        URL url = new File(getClass().getResource("MergeManagerTest.class").getFile()).getParentFile().toURI().toURL();
        FileMerge fileMerge = new FileMerge(url, "my/dest/path/");
        assertThat(fileMerge, MergeMatcher.isMergeableContainingFiles("my/dest/path/MergeManagerTest.class", "my/dest/path/test/.placeholder"));
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
        String jarPath = MergeManagerImpl.processUrlToJarPath(resource);
        System.out.println(jarPath);
        assertThat(jarPath, Is.is("/home/test/unjar.jar"));
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
