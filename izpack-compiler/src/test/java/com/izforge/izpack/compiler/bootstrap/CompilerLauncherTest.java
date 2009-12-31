package com.izforge.izpack.compiler.bootstrap;

import com.izforge.izpack.compiler.Compiler;
import com.izforge.izpack.compiler.CompilerConfig;
import com.izforge.izpack.compiler.container.CompilerContainer;
import com.izforge.izpack.compiler.data.CompilerData;
import org.hamcrest.core.IsNull;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test compiler bindings
 *
 * @author Anthonin Bonnefoy
 */
public class CompilerLauncherTest {
    private CompilerContainer compilerContainer;

    @Before
    public void initContainer() {
        compilerContainer = new CompilerContainer();
        compilerContainer.initBindings();
    }

    @Test
    public void testPropertiesBinding() throws Exception {
        Properties properties = compilerContainer.getComponent(Properties.class);
        assertThat(properties, IsNull.notNullValue());
    }

    @Test
    public void testCompilerBinding() throws Exception {
        compilerContainer.addComponent(CompilerData.class, new CompilerData("", "", ""));
        Compiler compiler = compilerContainer.getComponent(Compiler.class);
        assertThat(compiler, IsNull.notNullValue());
    }

    @Test
    public void testCompilerDataBinding() {
        compilerContainer.addComponent(CompilerData.class, new CompilerData("", "", "", "", "", "", 0));
        CompilerData data = compilerContainer.getComponent(CompilerData.class);
        assertThat(data, IsNull.notNullValue());
    }

    @Test
    public void testCompilerConfigBinding() throws Exception {
        compilerContainer.processCompileDataFromArgs(new String[]{"install.xml"});
        CompilerConfig compiler = compilerContainer.getComponent(CompilerConfig.class);
        assertThat(compiler, IsNull.notNullValue());
    }
}
