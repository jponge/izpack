package com.izforge.izpack.compiler.container;

import java.util.Map;
import java.util.Properties;

import org.mockito.Mockito;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoException;

import com.izforge.izpack.api.adaptator.impl.XMLParser;
import com.izforge.izpack.api.container.Container;
import com.izforge.izpack.api.data.binding.IzpackProjectInstaller;
import com.izforge.izpack.api.exception.ContainerException;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.compiler.Compiler;
import com.izforge.izpack.compiler.CompilerConfig;
import com.izforge.izpack.compiler.cli.CliAnalyzer;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.compiler.data.PropertyManager;
import com.izforge.izpack.compiler.helper.AssertionHelper;
import com.izforge.izpack.compiler.helper.CompilerHelper;
import com.izforge.izpack.compiler.helper.XmlCompilerHelper;
import com.izforge.izpack.compiler.listener.CmdlinePackagerListener;
import com.izforge.izpack.compiler.merge.resolve.CompilerPathResolver;
import com.izforge.izpack.compiler.packager.IPackager;
import com.izforge.izpack.compiler.resource.ResourceFinder;
import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.merge.MergeManager;

/**
 * Container for compiler test using mocks
 *
 * @author Anthonin Bonnefoy
 */
public class TestCompilerContainerMock extends AbstractContainer
{

    /**
     * Constructs a <tt>TestCompilerContainerMock</tt>.
     *
     * @throws ContainerException if initialisation fails
     */
    public TestCompilerContainerMock()
    {
        initialise();
    }

    /**
     * Invoked by {@link #initialise} to fill the container.
     *
     * @param container the underlying container
     * @throws ContainerException if initialisation fails
     * @throws PicoException      for any PicoContainer error
     */
    @Override
    protected void fillContainer(MutablePicoContainer container)
    {
        container
                .addComponent(Mockito.mock(CliAnalyzer.class))
                .addComponent(Mockito.mock(CmdlinePackagerListener.class))
                .addComponent(Mockito.mock(Compiler.class))
                .addComponent(Mockito.mock(CompilerPathResolver.class))
                .addComponent(Mockito.mock(CompilerHelper.class))
                .addComponent(Mockito.mock(ResourceFinder.class))
                .addComponent(Mockito.mock(CompilerContainer.class))
                .addComponent(Mockito.mock(PropertyManager.class))
                .addComponent(Mockito.mock(MergeManager.class))
                .addComponent(Mockito.mock(RulesEngine.class))
                .addComponent(Mockito.mock(VariableSubstitutor.class))
                .addComponent(Mockito.mock(IPackager.class))
                .addComponent(Mockito.mock(CompilerData.class))
                .addComponent(Mockito.mock(AssertionHelper.class))
                .addComponent("mapStringListDyn", Mockito.mock(Map.class))
                .addComponent("installFile", "installFile")
                .addComponent(Container.class, this)
                .addComponent(CompilerConfig.class)
                .addComponent(IzpackProjectInstaller.class)
                .addComponent(XmlCompilerHelper.class)
                .addComponent(XMLParser.class)
                .addComponent(Properties.class)
        ;
        new ResolverContainerFiller().fillContainer(this);

    }
}