package com.izforge.izpack.compiler;

import com.izforge.izpack.compiler.container.TestCompilerContainer;
import com.izforge.izpack.matcher.ZipMatcher;
import com.izforge.izpack.merge.MergeManagerImpl;
import com.izforge.izpack.merge.resolve.MergeableResolver;
import com.izforge.izpack.merge.resolve.PathResolver;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.junit.PicoRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for an Izpack compilation
 */
@RunWith(PicoRunner.class)
@Container(TestCompilerContainer.class)
public class CompilerConfigIzPackInstallTest
{
    private File out;
    private CompilerConfig compilerConfig;
    private PathResolver pathResolver;
    private MergeManagerImpl mergeManager;

    public CompilerConfigIzPackInstallTest(File out, CompilerConfig compilerConfig, PathResolver pathResolver, MergeManagerImpl mergeManager, MergeableResolver mergeableResolver)
    {
        this.out = out;
        this.compilerConfig = compilerConfig;
        this.pathResolver = pathResolver;
        this.mergeManager = mergeManager;
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
}