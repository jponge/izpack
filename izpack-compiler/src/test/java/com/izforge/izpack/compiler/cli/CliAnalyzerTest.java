package com.izforge.izpack.compiler.cli;

import com.izforge.izpack.compiler.data.CompilerData;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test cli analyzer
 *
 * @author Anthonin Bonnefoy
 */
public class CliAnalyzerTest {
    private CliAnalyzer analyzer;

    @Before
    public void initAnalyzer() {
        analyzer = new CliAnalyzer();
    }

    @Test(expected = RuntimeException.class)
    public void voidArgumentShouldThrowRuntimeException() throws Exception {
        analyzer.parseArgs(new String[]{});
    }

    @Test
    public void fileNameShouldBeParsed() throws Exception {
        CompilerData data = analyzer.parseArgs(new String[]{"myInstall.xml"});
        assertThat(data.getInstallFile(), Is.is("myInstall.xml"));
    }

    @Test
    public void homeDirShouldBeParsed() throws Exception {
        CompilerData data = analyzer.parseArgs(new String[]{"myInstall.xml", "-h/mon/che min/"});
        assertThat(data.getInstallFile(), Is.is("myInstall.xml"));
        assertThat(CompilerData.IZPACK_HOME, Is.is("/mon/che min/"));
    }

    @Test
    public void baseDirShouldBeParsed() throws Exception {
        CompilerData data = analyzer.parseArgs(new String[]{"myInstall.xml", "-b/mon/che min/"});
        assertThat(data.getInstallFile(), Is.is("myInstall.xml"));
        assertThat(data.getBasedir(), Is.is("/mon/che min/"));
    }

    @Test
    public void multipleOptionShouldBeParsed() throws Exception {
        CompilerData data = analyzer.parseArgs(new String[]{"myInstall.xml", "-b/mon/che min/", "-k web", "-o graou.jar"});
        assertThat(data.getInstallFile(), Is.is("myInstall.xml"));
        assertThat(data.getBasedir(), Is.is("/mon/che min/"));
        assertThat(data.getKind(), Is.is("web"));
        assertThat(data.getOutput(), Is.is("graou.jar"));
    }

}
