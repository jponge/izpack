package org.izforge.izpack;

import com.izforge.izpack.compiler.CompilerConfig;
import com.izforge.izpack.compiler.CompilerException;
import org.apache.commons.io.FileUtils;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.hamcrest.MatcherAssert.*;

/**
 * Test for an Izpack compilation
 */
public class CompilationTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testCompile() throws Exception {
        File baseDir = new File(getClass().getClassLoader().getResource("samples1").getFile());

        File installerFile = new File(getClass().getClassLoader().getResource("samples1/install.xml").getFile());

        System.out.println(installerFile.getAbsolutePath());
        System.out.println(baseDir.getAbsolutePath());
        CompilerConfig c = new CompilerConfig(installerFile.getAbsolutePath(), baseDir.getAbsolutePath(), "default", "out.jar");
        CompilerConfig.setIzpackHome(".");
        c.executeCompiler();
        assertThat(c.wasSuccessful(), Is.is(true));

    }

}
