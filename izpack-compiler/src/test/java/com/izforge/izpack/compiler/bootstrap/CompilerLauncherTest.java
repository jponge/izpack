package com.izforge.izpack.compiler.bootstrap;

import com.izforge.izpack.compiler.Compiler;
import com.izforge.izpack.compiler.CompilerConfig;
import com.izforge.izpack.compiler.container.CompilerContainer;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.compiler.stream.JarOutputStream;
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
public class CompilerLauncherTest
{
    private CompilerContainer compilerContainer;

    @Before
    public void initContainer()
    {
        compilerContainer = new CompilerContainer();
        compilerContainer.initBindings();
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
        compilerContainer.addComponent(CompilerData.class, new CompilerData("bindingTest.xml", "", "out.zip"));
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
        compilerContainer.addComponent(CompilerData.class, new CompilerData("bindingTest.xml", "", "out.zip"));
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
