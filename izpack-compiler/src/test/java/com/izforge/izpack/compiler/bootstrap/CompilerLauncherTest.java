package com.izforge.izpack.compiler.bootstrap;

import com.izforge.izpack.compiler.CompilerConfig;
import com.izforge.izpack.compiler.container.CompilerContainer;
import com.izforge.izpack.compiler.data.CompilerData;
import org.hamcrest.core.IsNull;
import org.junit.Before;
import org.junit.Test;

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
        compilerContainer.processCompileDataFromArgs(new String[]{"install.xml"});
    }

    @Test
    public void testCompilerDataBinding() {
        CompilerData data = compilerContainer.getComponent(CompilerData.class);
        assertThat(data, IsNull.notNullValue());
    }

    @Test
    public void testCompilerBinding() throws Exception {
        CompilerConfig compiler = compilerContainer.getComponent(CompilerConfig.class);
        assertThat(compiler, IsNull.notNullValue());
    }
}
