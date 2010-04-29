package com.izforge.izpack.compiler.container;

import com.izforge.izpack.api.rules.RulesEngine;

import org.picocontainer.Characteristics;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.injectors.ProviderAdapter;

import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.compiler.Compiler;
import com.izforge.izpack.compiler.CompilerConfig;
import com.izforge.izpack.compiler.cli.CliAnalyzer;
import com.izforge.izpack.compiler.container.provider.CompilerDataProvider;
import com.izforge.izpack.compiler.container.provider.CompressedOutputStreamProvider;
import com.izforge.izpack.compiler.container.provider.IzpackProjectProvider;
import com.izforge.izpack.compiler.container.provider.JarOutputStreamProvider;
import com.izforge.izpack.compiler.container.provider.PackCompressorProvider;
import com.izforge.izpack.compiler.container.provider.XmlCompilerHelperProvider;
import com.izforge.izpack.compiler.data.PropertyManager;
import com.izforge.izpack.compiler.helper.AssertionHelper;
import com.izforge.izpack.compiler.helper.CompilerHelper;
import com.izforge.izpack.compiler.listener.CmdlinePackagerListener;
import com.izforge.izpack.compiler.packager.IPackager;
import com.izforge.izpack.compiler.packager.impl.Packager;
import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.core.container.ConditionContainer;
import com.izforge.izpack.core.container.filler.ResolverContainerFiller;
import com.izforge.izpack.core.rules.RulesEngineImpl;
import com.izforge.izpack.merge.MergeManager;
import com.izforge.izpack.merge.MergeManagerImpl;
import com.izforge.izpack.merge.resolve.ClassPathCrawler;
import org.picocontainer.parameters.ComponentParameter;

/**
 * Container for compiler
 *
 * @author Anthonin Bonnefoy
 */
public class CompilerContainer extends AbstractContainer
{


    public void fillContainer(MutablePicoContainer pico)
    {
        pico
                .addComponent(Properties.class)
                .addComponent(CompilerContainer.class, this)
                .addComponent(CliAnalyzer.class)
                .addComponent(CmdlinePackagerListener.class)
                .addComponent(Compiler.class)
                .addComponent(CompilerConfig.class)
                .addComponent(ConditionContainer.class, ConditionContainer.class)
                .addComponent(MutablePicoContainer.class, pico)
                .as(Characteristics.USE_NAMES).addComponent(AssertionHelper.class)
                .as(Characteristics.USE_NAMES).addComponent(PropertyManager.class)
                .as(Characteristics.USE_NAMES).addComponent(VariableSubstitutor.class, VariableSubstitutorImpl.class)
                .as(Characteristics.USE_NAMES).addComponent(IPackager.class, Packager.class)
                .addComponent(CompilerHelper.class)
                .addComponent(RulesEngine.class, RulesEngineImpl.class, new ComponentParameter(ClassPathCrawler.class), new ComponentParameter(ConditionContainer.class))
                .addComponent(MergeManager.class, MergeManagerImpl.class)
                ;
        new ResolverContainerFiller().fillContainer(pico);
        pico.addAdapter(new ProviderAdapter(new IzpackProjectProvider()))
                .addAdapter(new ProviderAdapter(new XmlCompilerHelperProvider()))
                .addAdapter(new ProviderAdapter(new JarOutputStreamProvider()))
                .addAdapter(new ProviderAdapter(new CompressedOutputStreamProvider()))
                .addAdapter(new ProviderAdapter(new PackCompressorProvider()));
    }

    /**
     * Add CompilerDataComponent by processing command line args
     *
     * @param args command line args passed to the main
     */
    public void processCompileDataFromArgs(String[] args)
    {
        pico.addAdapter(new ProviderAdapter(new CompilerDataProvider(args)));
    }

}
