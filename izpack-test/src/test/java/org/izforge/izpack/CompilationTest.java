package org.izforge.izpack;

import com.izforge.izpack.compiler.CompilerConfig;
import org.hamcrest.Matcher;
import org.hamcrest.collection.IsCollectionContaining;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsNot;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.matchers.StringContains;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for an Izpack compilation
 */
public class CompilationTest {

    private File baseDir = new File(getClass().getResource("samples1").getFile());
    private File installerFile = new File(getClass().getResource("samples1/install.xml").getFile());
    private File out = new File(baseDir, "out.jar");

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void cleanFiles() {
        assertThat(baseDir.exists(),Is.is(true));
        out.delete();
    }

    @Test
    public void compilerShouldCompile() throws Exception {
        CompilerConfig c = new CompilerConfig(installerFile.getAbsolutePath(), baseDir.getAbsolutePath(), "default", out.getAbsolutePath());
        c.executeCompiler();
        assertThat(c.wasSuccessful(), Is.is(true));
    }

    @Test
    public void installerShouldContainInstallerClass() throws Exception {
        CompilerConfig c = new CompilerConfig(installerFile.getAbsolutePath(), baseDir.getAbsolutePath(), "default", out.getAbsolutePath());
        c.executeCompiler();
        assertZipContainsMatch(out, StringContains.containsString("Installer.class"));
    }

    @Test
    public void installerShouldContainDebugClass() throws Exception {
        CompilerConfig c = new CompilerConfig(installerFile.getAbsolutePath(), baseDir.getAbsolutePath(), "default", out.getAbsolutePath());
        c.executeCompiler();
        assertZipContainsMatch(out, StringContains.containsString("Debug.class"));
    }

    private void assertZipContainsMatch(File inFile, Matcher<String> stringMatcher) throws IOException {
        List<String> fileList = new ArrayList<String>();
        FileInputStream fis = new FileInputStream(inFile);
        ZipInputStream zis = new ZipInputStream(fis);
        ZipEntry ze;
        while ((ze = zis.getNextEntry()) != null) {
            fileList.add(ze.getName());
            zis.closeEntry();
        }
        zis.close();
        assertThat(fileList, IsCollectionContaining.hasItem(
                stringMatcher
        ));
    }
}
