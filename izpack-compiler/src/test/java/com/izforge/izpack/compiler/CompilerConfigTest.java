package com.izforge.izpack.compiler;

import com.izforge.izpack.compiler.container.TestCompilerContainer;
import com.izforge.izpack.matcher.MergeMatcher;
import com.izforge.izpack.matcher.ZipMatcher;
import com.izforge.izpack.merge.MergeManagerImpl;
import com.izforge.izpack.merge.resolve.PathResolver;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.junit.PicoRunner;
import org.apache.maven.shared.jar.JarAnalyzer;
import org.apache.maven.shared.jar.classes.JarClasses;
import org.apache.maven.shared.jar.classes.JarClassesAnalysis;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for an Izpack compilation
 */
@RunWith(PicoRunner.class)
@Container(TestCompilerContainer.class)
@InstallFile("samples/helloAndFinish.xml")
public class CompilerConfigTest
{
    private File out;
    private CompilerConfig compilerConfig;

    public CompilerConfigTest(File out, CompilerConfig compilerConfig)
    {
        this.out = out;
        this.compilerConfig = compilerConfig;
    }

    @Test
    public void installerShouldContainInstallerClassResourcesAndImages() throws Exception
    {
        compilerConfig.executeCompiler();
        assertThat(out, ZipMatcher.isZipContainingFile("com/izforge/izpack/installer/bootstrap/Installer.class"));
        assertThat(out, ZipMatcher.isZipContainingFile("com/izforge/izpack/panels/hello/HelloPanel.class"));
        assertThat(out, ZipMatcher.isZipContainingFile("resources/vars"));
        assertThat(out, ZipMatcher.isZipContainingFile("img/JFrameIcon.png"));
    }

    @Test
    public void mergeManagerShouldGetTheMergeableFromPanel() throws Exception
    {
        PathResolver pathResolver = new PathResolver();
        MergeManagerImpl mergeManager = new MergeManagerImpl(pathResolver);
        mergeManager.addResourceToMerge(pathResolver.getPanelMerge("HelloPanel"));
        mergeManager.addResourceToMerge(pathResolver.getPanelMerge("CheckedHelloPanel"));

        assertThat(mergeManager, MergeMatcher.isMergeableContainingFiles("com/izforge/izpack/panels/hello/HelloPanelConsoleHelper.class",
                "com/izforge/izpack/panels/hello/HelloPanel.class",
                "com/izforge/izpack/panels/checkedhello/CheckedHelloPanel.class"));
    }

    @Test
    @Ignore
    public void testImportAreResolved() throws Exception
    {
        JarAnalyzer jarAnalyzer = new JarAnalyzer(out);
        JarClassesAnalysis jarClassAnalyzer = new JarClassesAnalysis();
        JarClasses jarClasses = jarClassAnalyzer.analyze(jarAnalyzer);
        List<String> imports = jarClasses.getImports();
        List<String> listFromZip = ZipMatcher.getFileNameListFromZip(out);
        ArrayList<String> result = new ArrayList<String>();
        List<String> ignorePackage = Arrays.asList("java/", "org/w3c/", "org/xml/", "javax/", "text/html", "packs/pack", "com/thoughtworks");
        for (String anImport : imports)
        {
            if (anImport.matches("([a-z]+\\.)+[a-zA-Z]+"))
            {
                String currentClass = anImport.replaceAll("\\.", "/") + ".class";
                if (ignorePackage.contains(currentClass))
                {
                    continue;
                }
                if (!listFromZip.contains(currentClass))
                {
                    result.add(currentClass);
                }
            }
            if (!result.isEmpty())
            {
                StringBuilder stringBuilder = new StringBuilder();
                for (String s : result)
                {
                    stringBuilder.append(s).append('\n');
                }
                Assert.fail("Missing imports : " + stringBuilder);
            }
        }
    }
}
