package com.izforge.izpack.compiler;

import org.hamcrest.core.Is;
import org.hamcrest.text.StringContains;
import org.izforge.izpack.AssertionHelper;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test of the compiler standalone packaging.<br />
 * You have to call mvn process:resources to get standalone artifact in test resources.
 */
public class CompilerPackagingTest {

    private File standaloneCompiler = new File(getClass().getClassLoader().getResource("lib/izpack-compiler-standalone.jar").getFile());
    private Properties pathProperties = new Properties();

    @Before
    public void setUp() throws IOException {
        pathProperties.load(getClass().getResourceAsStream("path.properties"));
        assertThat(standaloneCompiler.exists(), Is.is(true));
    }

    @Test
    public void standaloneCompilerShouldContainJarResources() throws IOException {
        AssertionHelper.assertZipContainsMatch(standaloneCompiler, StringContains.containsString(pathProperties.getProperty("installer")));
        AssertionHelper.assertZipContainsMatch(standaloneCompiler, StringContains.containsString(pathProperties.getProperty("uninstaller")));
        AssertionHelper.assertZipContainsMatch(standaloneCompiler, StringContains.containsString(pathProperties.getProperty("uninstaller-ext")));
    }
}
