package com.izforge.izpack.integration;

import com.izforge.izpack.compiler.container.TestCompilationContainer;
import com.izforge.izpack.matcher.ZipMatcher;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.junit.PicoRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.runner.RunWith;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for an installation
 */

@RunWith(PicoRunner.class)
@Container(TestCompilationContainer.class)
public class IzpackGenerationTest {
    @Rule
    public MethodRule globalTimeout = new org.junit.rules.Timeout(HelperTestMethod.TIMEOUT);

    private File generatedInstallJar;

    private TestCompilationContainer testInstallationContainer;

    public IzpackGenerationTest(File generatedInstallJar, TestCompilationContainer testInstallationContainer) {
        this.generatedInstallJar = generatedInstallJar;
        this.testInstallationContainer = testInstallationContainer;
    }

    @Before
    public void before() {
        testInstallationContainer.launchCompilation();
    }

    @Test
    @InstallFile("samples/izpack/install.xml")
    public void testGeneratedIzpackInstaller() throws Exception {
        assertThat(generatedInstallJar, ZipMatcher.isZipContainingFiles(
                "com/izforge/izpack/panels/hello/HelloPanel.class"
        ));
    }
}