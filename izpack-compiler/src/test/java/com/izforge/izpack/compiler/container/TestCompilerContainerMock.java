package com.izforge.izpack.compiler.container;

import com.izforge.izpack.api.adaptator.impl.XMLParser;
import com.izforge.izpack.api.container.BindeableContainer;
import com.izforge.izpack.api.data.binding.IzpackProjectInstaller;
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
import com.izforge.izpack.compiler.packager.IPackager;
import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.core.container.filler.ResolverContainerFiller;
import com.izforge.izpack.merge.MergeManager;
import com.izforge.izpack.merge.resolve.PathResolver;
import org.mockito.Mockito;
import org.picocontainer.MutablePicoContainer;

import java.util.Map;
import java.util.Properties;

/**
 * Container for compiler test using mocks
 *
 * @author Anthonin Bonnefoy
 */
public class TestCompilerContainerMock extends AbstractContainer
{

    /**
     * Init component bindings
     *
     * @param pico
     */
    public void fillContainer(MutablePicoContainer pico)
    {
        pico
                .addComponent(Mockito.mock(CliAnalyzer.class))
                .addComponent(Mockito.mock(CmdlinePackagerListener.class))
                .addComponent(Mockito.mock(Compiler.class))
                .addComponent(Mockito.mock(PathResolver.class))
                .addComponent(Mockito.mock(CompilerHelper.class))
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
                .addComponent(BindeableContainer.class, this)
                .addComponent(CompilerConfig.class)
                .addComponent(IzpackProjectInstaller.class)
                .addComponent(XmlCompilerHelper.class)
                .addComponent(XMLParser.class)
                .addComponent(Properties.class)
                ;
        new ResolverContainerFiller().fillContainer(pico);

    }
}