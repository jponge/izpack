package com.izforge.izpack.compiler.container;

import com.izforge.izpack.api.adaptator.impl.XMLParser;
import com.izforge.izpack.api.container.BindeableContainer;
import com.izforge.izpack.api.data.binding.IzpackProjectInstaller;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.compiler.Compiler;
import com.izforge.izpack.compiler.CompilerConfig;
import com.izforge.izpack.compiler.cli.CliAnalyzer;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.compiler.data.PropertyManager;
import com.izforge.izpack.compiler.helper.AssertionHelper;
import com.izforge.izpack.compiler.helper.CompilerHelper;
import com.izforge.izpack.compiler.helper.CompilerResourceManager;
import com.izforge.izpack.compiler.helper.XmlCompilerHelper;
import com.izforge.izpack.compiler.listener.CmdlinePackagerListener;
import com.izforge.izpack.compiler.packager.IPackager;
import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.merge.MergeManager;
import com.izforge.izpack.merge.resolve.PathResolver;
import org.mockito.Mockito;
import org.picocontainer.PicoBuilder;

import java.util.Map;

/**
 * Container for compiler test using mocks
 *
 * @author Anthonin Bonnefoy
 */
public class TestCompilerContainerMock extends AbstractContainer
{

    /**
     * Init component bindings
     */
    public void initBindings()
    {
        pico = new PicoBuilder().withConstructorInjection().build();
        pico.addComponent(Mockito.mock(CliAnalyzer.class));
        pico.addComponent(Mockito.mock(CmdlinePackagerListener.class));
        pico.addComponent(Mockito.mock(Compiler.class));
        pico.addComponent(Mockito.mock(PathResolver.class));
        pico.addComponent(Mockito.mock(CompilerHelper.class));
        pico.addComponent(Mockito.mock(PropertyManager.class));
        pico.addComponent(Mockito.mock(CompilerResourceManager.class));
        pico.addComponent(Mockito.mock(MergeManager.class));
        pico.addComponent(Mockito.mock(VariableSubstitutor.class));
        pico.addComponent(Mockito.mock(IPackager.class));
        pico.addComponent(Mockito.mock(CompilerData.class));
        pico.addComponent(Mockito.mock(AssertionHelper.class));
        pico.addComponent(Mockito.mock(Map.class));
        pico.addComponent("installFile", "installFile");
        pico.addComponent(BindeableContainer.class, this);
        pico.addComponent(CompilerConfig.class);
        pico.addComponent(IzpackProjectInstaller.class);
        pico.addComponent(XmlCompilerHelper.class);
        pico.addComponent(XMLParser.class);
        pico.addComponent(PathResolver.class);

    }
}