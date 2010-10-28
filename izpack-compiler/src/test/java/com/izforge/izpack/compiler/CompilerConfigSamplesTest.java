package com.izforge.izpack.compiler;

import com.izforge.izpack.compiler.container.TestCompilerContainer;
import com.izforge.izpack.matcher.ZipMatcher;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.junit.PicoRunner;
import org.hamcrest.collection.IsCollectionContaining;
import org.hamcrest.core.IsNot;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for an Izpack compilation
 */
@RunWith(PicoRunner.class)
@Container(TestCompilerContainer.class)
public class CompilerConfigSamplesTest
{
    private File out;
    private CompilerConfig compilerConfig;

    public CompilerConfigSamplesTest(File out, CompilerConfig compilerConfig)
    {
        this.out = out;
        this.compilerConfig = compilerConfig;
    }

    @Test
    @InstallFile("samples/izpack.xml")
    public void installerShouldContainInstallerClassResourcesAndImages() throws Exception
    {
        compilerConfig.executeCompiler();
        assertThat(out, ZipMatcher.isZipContainingFile("com/izforge/izpack/panels/checkedhello/CheckedHelloPanel.class"));
        assertThat(out, ZipMatcher.isZipContainingFile("resources/vars"));
        assertThat(out, ZipMatcher.isZipContainingFile("img/JFrameIcon.png"));
    }


    @Test
    @InstallFile("samples/silverpeas/silverpeas.xml")
    public void installerShouldMergeProcessPanelCorrectly() throws Exception
    {
        compilerConfig.executeCompiler();
        assertThat(out, ZipMatcher.isZipMatching(IsNot.not(IsCollectionContaining.hasItem("com/izforge/izpack/panels/process/VariableCondition.class"))));
        assertThat(out, ZipMatcher.isZipMatching(IsCollectionContaining.hasItem("com/sora/panel/VimPanel.class")));
        assertThat(out, ZipMatcher.isZipMatching(IsCollectionContaining.hasItem("resource/32/help-browser.png")));
    }


}