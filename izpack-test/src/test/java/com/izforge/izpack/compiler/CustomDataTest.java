package com.izforge.izpack.compiler;

import com.izforge.izpack.api.data.binding.IzpackProjectInstaller;
import com.izforge.izpack.api.data.binding.Listener;
import com.izforge.izpack.api.data.binding.Stage;
import com.izforge.izpack.matcher.ObjectInputMatcher;
import com.izforge.izpack.matcher.ZipMatcher;
import org.hamcrest.beans.HasPropertyWithValue;
import org.hamcrest.core.AllOf;
import org.hamcrest.core.Is;
import org.junit.internal.matchers.IsCollectionContaining;
import org.testng.annotations.Test;

import java.util.List;

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
    public void testCustomDataArePresent() throws Exception
    {
        CompilerConfig c = compilerContainer.getComponent(CompilerConfig.class);
        c.executeCompiler();
        assertThat(out, ZipMatcher.isZipContainingFile("com/izforge/izpack/event/SummaryLoggerInstallerListener.class"));
        IzpackProjectInstaller izpackModel = (IzpackProjectInstaller) ObjectInputMatcher.getObjectFromZip(out, "resources/izpackInstallModel");
        List<Listener> listenerList = izpackModel.getListeners();
        assertThat(listenerList.size(), Is.is(3));
        assertThat(listenerList, IsCollectionContaining.hasItems(
                AllOf.allOf(
                        HasPropertyWithValue.<Listener>hasProperty("classname", Is.is("SummaryLoggerInstallerListener")),
                        HasPropertyWithValue.<Listener>hasProperty("stage", Is.is(Stage.install))
                ),
                AllOf.allOf(
                        HasPropertyWithValue.<Listener>hasProperty("classname", Is.is("RegistryInstallerListener")),
                        HasPropertyWithValue.<Listener>hasProperty("stage", Is.is(Stage.install))
                )
        )
        );

    }


}
