package com.izforge.izpack.compiler;

import com.izforge.izpack.matcher.MergeMatcher;
import com.izforge.izpack.matcher.ZipMatcher;
import com.izforge.izpack.merge.MergeManagerImpl;
import com.izforge.izpack.merge.resolve.PathResolver;
import org.apache.maven.shared.jar.JarAnalyzer;
import org.apache.maven.shared.jar.classes.JarClasses;
import org.apache.maven.shared.jar.classes.JarClassesAnalysis;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.fail;

/**
 * Test for an Izpack compilation
 */
public class CompilationTest extends AbstractCompilationTest
{
    String getInstallFileName()
    {
        return "samples/helloAndFinish.xml";
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
        PathResolver pathResolver = new PathResolver();
        MergeManagerImpl mergeManager = new MergeManagerImpl(pathResolver);
        mergeManager.addResourceToMerge(pathResolver.getPanelMerge("HelloPanel"));
        mergeManager.addResourceToMerge(pathResolver.getPanelMerge("CheckedHelloPanel"));

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

    @Test(enabled = false)
    public void testImportAreResolved() throws Exception
    {
        CompilerConfig c = compilerContainer.getComponent(CompilerConfig.class);
        c.executeCompiler();
        JarAnalyzer jarAnalyzer = new JarAnalyzer(out);

        JarClassesAnalysis jarClassAnalyzer = new JarClassesAnalysis();
        JarClasses jarClasses = jarClassAnalyzer.analyze(jarAnalyzer);
        List<String> imports = jarClasses.getImports();
        List<String> listFromZip = ZipMatcher.getFileNameListFromZip(out);
        ArrayList<String> result = new ArrayList<String>();
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
                if (currentClass.contains("text/html"))
                {
                    continue;
                }
                if (currentClass.contains("packs/pack"))
                {
                    continue;
                }
                if (currentClass.contains("com/thoughtworks"))
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
                fail("Missing imports : " + stringBuilder);
            }
        }
    }
}
