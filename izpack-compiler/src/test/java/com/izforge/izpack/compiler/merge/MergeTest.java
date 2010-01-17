package com.izforge.izpack.compiler.merge;

import org.apache.tools.zip.ZipOutputStream;
import org.hamcrest.core.Is;
import org.hamcrest.text.StringEndsWith;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test a single file merge
 *
 * @author Anthonin Bonnefoy
 */
public class MergeTest {
    private File zip;

    @Before
    public void setUp() {
        zip = new File("outputZip.zip");
        zip.delete();
    }

    @Test
    public void testMergeSingleFile() throws Exception {
        File file = new File(getClass().getResource("SingleFileMerge.class").getFile());
        assertThat(file.exists(), Is.is(true));
        SingleFileMerge singleFileMerge = new SingleFileMerge(file);

        doMerge(singleFileMerge);

        ZipInputStream inputStream = new ZipInputStream(new FileInputStream(zip));
        assertThat(inputStream.available(), Is.is(1));
        ZipEntry zipEntry = inputStream.getNextEntry();
        assertThat(zipEntry.getName(), Is.is("SingleFileMerge.class"));
    }

    private void doMerge(Mergeable fileMerge) throws IOException {
        ZipOutputStream outputStream = new ZipOutputStream(zip);
        fileMerge.merge(outputStream);
        outputStream.close();
    }

    @Test
    public void testMergeJarFile() throws Exception {
        Mergeable jarMerge = MergeManager.getMergeableFromPath("junit/framework/Assert.class");
        assertThat(jarMerge, Is.is(JarMerge.class));

        doMerge(jarMerge);

        ZipInputStream inputStream = new ZipInputStream(new FileInputStream(zip));
        assertThat(inputStream.available(), Is.is(1));
        ZipEntry zipEntry = inputStream.getNextEntry();
        assertThat(zipEntry.getName(), Is.is("junit/framework/Assert.class"));
    }

    @Test
    public void testGetJarPath() throws Exception {
        String jarPath = MergeManager.getJarAbsolutePath("junit/framework/Assert.class");
        assertThat(jarPath, StringEndsWith.endsWith("junit-4.7.jar"));
        assertThat(new File(jarPath).exists(), Is.is(true));
    }

    @Test
    public void testGetJarFromPackage() throws Exception {
        String jarPath = MergeManager.getJarAbsolutePath("junit/framework/");
        assertThat(jarPath, StringEndsWith.endsWith("junit-4.7.jar"));
        assertThat(new File(jarPath).exists(), Is.is(true));
    }
}
