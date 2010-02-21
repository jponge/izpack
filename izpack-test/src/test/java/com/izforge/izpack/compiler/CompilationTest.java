package com.izforge.izpack.compiler;

import com.izforge.izpack.compiler.container.CompilerContainer;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.matcher.MergeMatcher;
import com.izforge.izpack.matcher.ZipMatcher;
import com.izforge.izpack.merge.MergeManagerImpl;
import com.izforge.izpack.merge.panel.PanelMerge;
import org.hamcrest.core.Is;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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

    private CompilerData data;

    @BeforeMethod
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
        assertThat(out, ZipMatcher.isZipContainingFile("com/izforge/izpack/installer/bootstrap/Installer.class"));
        assertThat(out, ZipMatcher.isZipContainingFile("com/izforge/izpack/panels/hello/HelloPanel.class"));
    }

    @Test
    public void mergeManagerShouldGetTheMergeableFromPanel() throws Exception {
        MergeManagerImpl mergeManager = new MergeManagerImpl();
        mergeManager.addResourceToMerge(new PanelMerge("HelloPanel"));
        mergeManager.addResourceToMerge(new PanelMerge("CheckedHelloPanel"));

        assertThat(mergeManager, MergeMatcher.isMergeableContainingFiles("com/izforge/izpack/panels/hello/HelloPanelConsoleHelper.class",
                "com/izforge/izpack/panels/hello/HelloPanel.class",
                "com/izforge/izpack/panels/checkedhello/CheckedHelloPanel.class"));
    }

    @Test
    public void installerShouldContainResources() throws Exception {
        CompilerConfig c = compilerContainer.getComponent(CompilerConfig.class);
        c.executeCompiler();
        assertThat(out, ZipMatcher.isZipContainingFile("resources/vars"));
    }

    @Test
    public void installerShouldContainImages() throws Exception {
        CompilerConfig c = compilerContainer.getComponent(CompilerConfig.class);
        c.executeCompiler();
        assertThat(out, ZipMatcher.isZipContainingFile("img/JFrameIcon.png"));
    }


}
