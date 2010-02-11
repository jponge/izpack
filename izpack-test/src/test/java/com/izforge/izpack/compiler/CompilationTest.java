package com.izforge.izpack.compiler;

import com.izforge.izpack.AssertionHelper;
import com.izforge.izpack.compiler.container.CompilerContainer;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.merge.MergeManagerImpl;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.hamcrest.core.Is;
import org.hamcrest.text.StringContains;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;

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
        AssertionHelper.assertZipContainsMatch(out, StringContains.containsString("Installer.class"));
        AssertionHelper.assertZipContainsMatch(out, StringContains.containsString("com/izforge/izpack/panels/hello/HelloPanel.class"));
    }

    @Test
    public void mergeManagerShouldGetTheMergeableFromPanel() throws Exception {
        MergeManagerImpl mergeManager = new MergeManagerImpl();
        ZipOutputStream outputStream = Mockito.mock(ZipOutputStream.class);
        mergeManager.addPanelToMerge("HelloPanel");
        mergeManager.merge(outputStream);
        Mockito.verify(outputStream, times(2)).putNextEntry(Mockito.<ZipEntry>any());
//        Mockito.verify(outputStream).putNextEntry(new org.apache.tools.zip.ZipEntry("com/izforge/izpack/panels/hello/HelloPanelConsoleHelper.class"));
//        Mockito.verify(outputStream).putNextEntry(new org.apache.tools.zip.ZipEntry("com/izforge/izpack/panels/hello/HelloPanel.class"));
    }

    @Test
    public void mergeManagerShouldTransformClassNameToPackagePath() throws Exception {
        MergeManagerImpl mergeManager = new MergeManagerImpl();
        String pathFromClassName = mergeManager.getPackagePathFromClassName("com.test.sora.UneClasse");
        assertThat(pathFromClassName, Is.is("com/test/sora/"));
    }

    @Test
    public void mergeManagerShouldReturnDefaultPackagePath() throws Exception {
        MergeManagerImpl mergeManager = new MergeManagerImpl();
        String pathFromClassName = mergeManager.getPackagePathFromClassName("UneClasse");
        assertThat(pathFromClassName, Is.is("com/izforge/izpack/panels/"));
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
