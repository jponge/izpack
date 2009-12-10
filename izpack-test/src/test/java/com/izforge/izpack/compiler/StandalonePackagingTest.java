package com.izforge.izpack.compiler;

import com.izforge.izpack.AssertionHelper;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsNull;
import org.hamcrest.text.StringContains;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test of the compiler standalone packaging.<br />
 * You have to call mvn process:resources to get standalone artifact in test resources.
 */
public class StandalonePackagingTest {

    private Properties pathProperties = new Properties();
    private File standaloneCompiler;
    private File skeletonInstallerFile;

    @Before
    public void setUp() throws IOException {
        pathProperties.load(getClass().getResourceAsStream("path.properties"));
        URL resource = getClass().getClassLoader().getResource(pathProperties.getProperty("standalone"));
        assertThat("Lib : " + pathProperties.getProperty("standalone"), resource, IsNull.notNullValue());
        standaloneCompiler = new File(resource.getFile());
        skeletonInstallerFile = new File(getClass().
                getClassLoader().getResource(pathProperties.getProperty("installer")).getFile());
        assertThat(standaloneCompiler.exists(), Is.is(true));
    }

    @Test
    public void standaloneCompilerShouldContainJarResources() throws IOException {
        AssertionHelper.assertZipContainsMatch(standaloneCompiler, StringContains.containsString(pathProperties.getProperty("installer")));
        AssertionHelper.assertZipContainsMatch(standaloneCompiler, StringContains.containsString(pathProperties.getProperty("uninstaller")));
        AssertionHelper.assertZipContainsMatch(standaloneCompiler, StringContains.containsString(pathProperties.getProperty("uninstaller-ext")));
    }

    @Test
    public void skeletonInstallerShouldContainClasses() throws Exception {
        AssertionHelper.assertZipContainsMatch(skeletonInstallerFile, "Debug.class");
    }

}
