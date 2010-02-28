package com.izforge.izpack.compiler;

import com.izforge.izpack.matcher.ZipMatcher;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for custom data
 *
 * @author Anthonin Bonnefoy
 */
public class CustomDataTest extends AbstractCompilationTest
{
    String getInstallFileName()
    {
        return "samples/listeners.xml";
    }

    @Test
    public void testCustomData() throws Exception
    {
        CompilerConfig c = compilerContainer.getComponent(CompilerConfig.class);
        c.executeCompiler();
        assertThat(out, ZipMatcher.isZipContainingFile("com/izforge/izpack/event/SummaryLoggerInstallerListener.class"));
    }
}
