package com.izforge.izpack.integration;

import com.izforge.izpack.compiler.container.TestInstallationContainer;
import com.izforge.izpack.matcher.ZipMatcher;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.junit.PicoRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for an installation
 */

@RunWith(PicoRunner.class)
@Container(TestInstallationContainer.class)
public class IzpackGenerationTest
{
    private File generatedInstallJar;

    private TestInstallationContainer testInstallationContainer;

    public IzpackGenerationTest(File generatedInstallJar, TestInstallationContainer testInstallationContainer)
    {
        this.generatedInstallJar = generatedInstallJar;
        this.testInstallationContainer = testInstallationContainer;
    }

    @Before
    public void before()
    {
        testInstallationContainer.launchCompilation();
    }

    @Test
    @InstallFile("samples/izpack/install.xml")
    public void testGeneratedIzpackInstaller() throws Exception
    {
        assertThat(generatedInstallJar, ZipMatcher.isZipContainingFiles(
                "com/izforge/izpack/panels/hello/HelloPanel"
        ));
    }
}