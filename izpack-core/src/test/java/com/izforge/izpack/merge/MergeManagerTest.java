package com.izforge.izpack.merge;

import org.apache.tools.zip.ZipOutputStream;
import org.hamcrest.collection.IsCollectionContaining;
import org.hamcrest.core.Is;
import org.hamcrest.text.StringEndsWith;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test a single file merge
 *
 * @author Anthonin Bonnefoy
 */
public class MergeManagerTest {
    private File zip;
    private MergeManager mergeManager;

    @Before
    public void setUp() {
        zip = new File("outputZip.zip");
        zip.delete();
        mergeManager = new MergeManagerImpl();
    }

    @Test
    public void testMergeSingleFile() throws Exception {
        File file = new File(getClass().getResource("FileMerge.class").getFile());
        assertThat(file.exists(), Is.is(true));
        FileMerge fileMerge = new FileMerge(file);

        doMerge(fileMerge);

        ZipInputStream inputStream = new ZipInputStream(new FileInputStream(zip));
        assertThat(inputStream.available(), Is.is(1));
        ZipEntry zipEntry = inputStream.getNextEntry();
        assertThat(zipEntry.getName(), Is.is("FileMerge.class"));
    }

    @Test
    public void testMergeDirectory() throws Exception {
        File file = new File(getClass().getResource("MergeManagerTest.class").getFile()).getParentFile();
        assertThat(file.exists(), Is.is(true));
        FileMerge fileMerge = new FileMerge(file);

        doMerge(fileMerge);

        ArrayList<String> arrayList = getFileNameInZip(zip);
        assertThat(arrayList, IsCollectionContaining.hasItems("MergeManagerTest.class", "test/.placeholder"));
    }

    @Test
    public void testMergeDirectoryWithDestination() throws Exception {
        File file = new File(getClass().getResource("MergeManagerTest.class").getFile()).getParentFile();
        FileMerge fileMerge = new FileMerge(file, "my/dest/path/");

        doMerge(fileMerge);

        ArrayList<String> arrayList = getFileNameInZip(zip);
        assertThat(arrayList, IsCollectionContaining.hasItems("my/dest/path/MergeManagerTest.class", "my/dest/path/test/.placeholder"));
    }

    private ArrayList<String> getFileNameInZip(File zip) throws IOException {
        ZipInputStream inputStream = new ZipInputStream(new FileInputStream(zip));
        ArrayList<String> arrayList = new ArrayList<String>();
        ZipEntry zipEntry;
        while ((zipEntry = inputStream.getNextEntry()) != null) {
            arrayList.add(zipEntry.getName());
        }
        return arrayList;
    }

    private void doMerge(Mergeable fileMerge) throws IOException {
        ZipOutputStream outputStream = new ZipOutputStream(zip);
        fileMerge.merge(outputStream);
        outputStream.close();
    }

    @Test
    public void testMergeClassFromJarFile() throws Exception {
        Mergeable jarMerge = MergeManagerImpl.getMergeableFromPath("junit/framework/Assert.class");
        assertThat(jarMerge, Is.is(JarMerge.class));

        doMerge(jarMerge);

        ZipInputStream inputStream = new ZipInputStream(new FileInputStream(zip));
        assertThat(inputStream.available(), Is.is(1));
        ZipEntry zipEntry = inputStream.getNextEntry();
        assertThat(zipEntry.getName(), Is.is("junit/framework/Assert.class"));
    }


    @Test
    public void testMergePackageFromJar() throws Exception {
        Mergeable jarMerge = MergeManagerImpl.getMergeableFromPath("junit/framework/");
        assertThat(jarMerge, Is.is(JarMerge.class));

        doMerge(jarMerge);

        ZipInputStream inputStream = new ZipInputStream(new FileInputStream(zip));
        ZipEntry zipEntry = inputStream.getNextEntry();
        assertThat(zipEntry.getName(), Is.is("junit/framework/Assert.class"));
        zipEntry = inputStream.getNextEntry();
        assertThat(zipEntry.getName(), Is.is("junit/framework/AssertionFailedError.class"));
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
        FileMerge fileMerge = new FileMerge(MergeManagerImpl.getFileFromPath("com/izforge/izpack/merge/test"));
        File file = fileMerge.find(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().equals(".placeholder") || pathname.isDirectory();
            }
        });
        assertThat(file.getName(), Is.is(".placeholder"));
    }

    @Test
    public void findFileInJar() throws Exception {
        URL urlJar = ClassLoader.getSystemResource("com/izforge/izpack/merge/test/jar-hellopanel-1.0-SNAPSHOT.jar");
        URLClassLoader loader = URLClassLoader.newInstance(new URL[]{urlJar}, ClassLoader.getSystemClassLoader());

        JarMerge jarMerge = new JarMerge(loader.getResource("jar/izforge"));
        File file = jarMerge.find(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().matches(".*HelloPanel\\.class") || pathname.isDirectory();
            }
        });
        assertThat(file.getName(), Is.is("HelloPanel.class"));
    }

    @Test
    public void testAddResourceToMerge() throws Exception {
        mergeManager.addResourceToMerge("com/izforge/izpack/merge/");

        doMerge(mergeManager);

        ArrayList<String> arrayList = getFileNameInZip(zip);
        assertThat(arrayList, IsCollectionContaining.hasItems("com/izforge/izpack/merge/MergeManagerTest.class"));
    }

    @Test
    public void testAddResourceToMergeWithDestination() throws Exception {
        mergeManager.addResourceToMerge("com/izforge/izpack/merge/", "com/dest/");

        doMerge(mergeManager);

        ArrayList<String> arrayList = getFileNameInZip(zip);
        assertThat(arrayList, IsCollectionContaining.hasItems("com/dest/MergeManagerTest.class"));
    }

}
