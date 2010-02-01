package com.izforge.izpack.compiler;

import com.izforge.izpack.AssertionHelper;
import com.izforge.izpack.compiler.container.CompilerContainer;
import com.izforge.izpack.compiler.data.CompilerData;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.matchers.StringContains;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for an Izpack compilation
 */
public class CompilationTest {

    private File baseDir = new File(getClass().getClassLoader().getResource("samples").getFile());
    private File installerFile = new File(getClass().getClassLoader().getResource("samples/helloAndFinish.xml").getFile());
    private File out = new File(baseDir, "out.jar");

    private CompilerContainer compilerContainer;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private CompilerData data;

    @Before
    public void cleanFiles() {
        assertThat(baseDir.exists(), Is.is(true));
        out.delete();
        data = new CompilerData(installerFile.getAbsolutePath(), baseDir.getAbsolutePath(), out.getAbsolutePath());
        compilerContainer = new CompilerContainer();
        compilerContainer.initBindings();
        compilerContainer.addComponent(CompilerData.class, data);
    }

    @Test
    public void installerShouldContainInstallerClass() throws Exception {
        CompilerConfig c = compilerContainer.getComponent(CompilerConfig.class);
        c.executeCompiler();
        AssertionHelper.assertZipContainsMatch(out, StringContains.containsString("Installer.class"));
        AssertionHelper.assertZipContainsMatch(out, StringContains.containsString("HelloPanel.class"));
    }

    @Test
    public void installerShouldContainResources() throws Exception {
        CompilerConfig c = compilerContainer.getComponent(CompilerConfig.class);
        c.executeCompiler();
        AssertionHelper.assertZipContainsMatch(out, StringContains.containsString("resources/vars"));
    }

    @Test
    public void installerShouldContainImages() throws Exception {
        CompilerConfig c = compilerContainer.getComponent(CompilerConfig.class);
        c.executeCompiler();
        AssertionHelper.assertZipContainsMatch(out, Is.is("img/JFrameIcon.png"));
    }


}
