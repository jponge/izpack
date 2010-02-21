package com.izforge.izpack.compiler;

import com.izforge.izpack.compiler.container.CompilerContainer;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.matcher.MergeMatcher;
import com.izforge.izpack.matcher.ZipMatcher;
import com.izforge.izpack.merge.MergeManagerImpl;
import com.izforge.izpack.merge.panel.PanelMerge;
import org.apache.maven.shared.jar.JarAnalyzer;
import org.apache.maven.shared.jar.classes.JarClasses;
import org.apache.maven.shared.jar.classes.JarClassesAnalysis;
import org.hamcrest.core.Is;
import org.junit.internal.matchers.IsCollectionContaining;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for an Izpack compilation
 */
public class CompilationTest
{

    private File baseDir = new File(getClass().getClassLoader().getResource("samples").getFile());
    private File installerFile = new File(getClass().getClassLoader().getResource("samples/helloAndFinish.xml").getFile());
    private File out = new File(baseDir, "out.jar");

    private CompilerContainer compilerContainer;

    private CompilerData data;

    @BeforeMethod
    public void cleanFiles()
    {
        assertThat(baseDir.exists(), Is.is(true));
        out.delete();
        data = new CompilerData(installerFile.getAbsolutePath(), baseDir.getAbsolutePath(), out.getAbsolutePath());
        compilerContainer = new CompilerContainer();
        compilerContainer.initBindings();
        compilerContainer.addComponent(CompilerData.class, data);
    }

    @Test
    public void installerShouldContainInstallerClass() throws Exception
    {
        CompilerConfig c = compilerContainer.getComponent(CompilerConfig.class);
        c.executeCompiler();
        assertThat(out, ZipMatcher.isZipContainingFile("com/izforge/izpack/installer/bootstrap/Installer.class"));
        assertThat(out, ZipMatcher.isZipContainingFile("com/izforge/izpack/panels/hello/HelloPanel.class"));
    }

    @Test
    public void mergeManagerShouldGetTheMergeableFromPanel() throws Exception
    {
        MergeManagerImpl mergeManager = new MergeManagerImpl();
        mergeManager.addResourceToMerge(new PanelMerge("HelloPanel"));
        mergeManager.addResourceToMerge(new PanelMerge("CheckedHelloPanel"));

        assertThat(mergeManager, MergeMatcher.isMergeableContainingFiles("com/izforge/izpack/panels/hello/HelloPanelConsoleHelper.class",
                "com/izforge/izpack/panels/hello/HelloPanel.class",
                "com/izforge/izpack/panels/checkedhello/CheckedHelloPanel.class"));
    }

    @Test
    public void installerShouldContainResources() throws Exception
    {
        CompilerConfig c = compilerContainer.getComponent(CompilerConfig.class);
        c.executeCompiler();
        assertThat(out, ZipMatcher.isZipContainingFile("resources/vars"));
    }

    @Test
    public void installerShouldContainImages() throws Exception
    {
        CompilerConfig c = compilerContainer.getComponent(CompilerConfig.class);
        c.executeCompiler();
        assertThat(out, ZipMatcher.isZipContainingFile("img/JFrameIcon.png"));
    }

    @Test
    public void testImportAreResolved() throws Exception
    {
        CompilerConfig c = compilerContainer.getComponent(CompilerConfig.class);
        c.executeCompiler();
        JarAnalyzer jarAnalyzer = new JarAnalyzer(out);

        JarClassesAnalysis jarClassAnalyzer = new JarClassesAnalysis();
        JarClasses jarClasses = jarClassAnalyzer.analyze(jarAnalyzer);
        List<String> imports = jarClasses.getImports();
        List<String> listFromZip = ZipMatcher.getFileNameListFromZip(out);
        for (String anImport : imports)
        {
            if (anImport.matches("([a-z]+\\.)+[a-zA-Z]+"))
            {
                String currentClass = anImport.replaceAll("\\.", "/") + ".class";
                if (currentClass.contains("java/"))
                {
                    continue;
                }
                if (currentClass.contains("org/w3c/"))
                {
                    continue;
                }
                if (currentClass.contains("org/xml/"))
                {
                    continue;
                }
                if (currentClass.contains("javax/"))
                {
                    continue;
                }
                assertThat(listFromZip, IsCollectionContaining.hasItem(currentClass));
            }


        }
    }
}
