package com.izforge.izpack.compiler.bootstrap;

import com.izforge.izpack.compiler.Compiler;
import com.izforge.izpack.compiler.CompilerConfig;
import com.izforge.izpack.compiler.container.CompilerContainer;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.compiler.stream.JarOutputStream;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.junit.PicoRunner;
import org.hamcrest.core.IsNull;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test compiler bindings
 *
 * @author Anthonin Bonnefoy
 */
@RunWith(PicoRunner.class)
@Container(CompilerContainer.class)
public class CompilerLauncherTest
{
    private CompilerContainer compilerContainer;

    public CompilerLauncherTest(CompilerContainer compilerContainer)
    {
        this.compilerContainer = compilerContainer;
    }

    @Test
    public void testPropertiesBinding() throws Exception
    {
        Properties properties = compilerContainer.getComponent(Properties.class);
        assertThat(properties, IsNull.notNullValue());
    }

    @Test
    public void testJarOutputStream() throws Exception
    {
        compilerContainer.addComponent(CompilerData.class, new CompilerData("bindingTest.xml", "", "out.zip", false));
        JarOutputStream jarOutputStream = compilerContainer.getComponent(JarOutputStream.class);
        assertThat(jarOutputStream, IsNull.notNullValue());
    }

    @Test
    public void testCompilerBinding() throws Exception
    {
        compilerContainer.processCompileDataFromArgs(new String[]{"bindingTest.xml"});
        Compiler compiler = compilerContainer.getComponent(Compiler.class);
        assertThat(compiler, IsNull.notNullValue());
    }

    @Test
    public void testCompilerDataBinding()
    {
        compilerContainer.addComponent(CompilerData.class, new CompilerData("bindingTest.xml", "", "out.zip", false));
        CompilerData data = compilerContainer.getComponent(CompilerData.class);
        assertThat(data, IsNull.notNullValue());
    }

    @Test
    public void testCompilerConfigBinding() throws Exception
    {
        compilerContainer.processCompileDataFromArgs(new String[]{"bindingTest.xml"});
        CompilerData data = compilerContainer.getComponent(CompilerData.class);
        assertThat(data, IsNull.notNullValue());
        CompilerConfig compiler = compilerContainer.getComponent(CompilerConfig.class);
        assertThat(compiler, IsNull.notNullValue());
    }
}
