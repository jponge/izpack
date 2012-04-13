package com.izforge.izpack.compiler.container;

import java.util.Properties;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.injectors.ProviderAdapter;
import org.picocontainer.parameters.ComponentParameter;

import com.izforge.izpack.api.exception.ContainerException;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.compiler.Compiler;
import com.izforge.izpack.compiler.CompilerConfig;
import com.izforge.izpack.compiler.cli.CliAnalyzer;
import com.izforge.izpack.compiler.container.provider.CompilerDataProvider;
import com.izforge.izpack.compiler.container.provider.CompressedOutputStreamProvider;
import com.izforge.izpack.compiler.container.provider.JarOutputStreamProvider;
import com.izforge.izpack.compiler.container.provider.PackCompressorProvider;
import com.izforge.izpack.compiler.container.provider.XmlCompilerHelperProvider;
import com.izforge.izpack.compiler.data.PropertyManager;
import com.izforge.izpack.compiler.helper.AssertionHelper;
import com.izforge.izpack.compiler.helper.CompilerHelper;
import com.izforge.izpack.compiler.listener.CmdlinePackagerListener;
import com.izforge.izpack.compiler.packager.IPackager;
import com.izforge.izpack.compiler.packager.impl.Packager;
import com.izforge.izpack.compiler.resource.ResourceFinder;
import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.core.rules.ConditionContainer;
import com.izforge.izpack.core.rules.RulesEngineImpl;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;
import com.izforge.izpack.merge.MergeManager;
import com.izforge.izpack.merge.MergeManagerImpl;

/**
 * Container for compiler.
 *
 * @author Anthonin Bonnefoy
 */
public class CompilerContainer extends AbstractContainer
{

    /**
     * Constructs a <tt>CompilerContainer</tt>
     *
     * @throws ContainerException if initialisation fails
     */
    public CompilerContainer()
    {
        initialise();
    }

    /**
     * Constructs a <tt>CompilerContainer</tt>.
     *
     * @param container the underlying container. May be <tt>null</tt>
     * @throws ContainerException if initialisation fails
     */
    protected CompilerContainer(MutablePicoContainer container)
    {
        super(container);
    }

    /**
     * Fills the container.
     *
     * @param container the underlying container
     * @throws ContainerException if initialisation fails, or the container has already been initialised
     */
    @Override
    protected void fillContainer(MutablePicoContainer container)
    {
        addComponent(Properties.class);
        addComponent(CompilerContainer.class, this);
        addComponent(CliAnalyzer.class);
        addComponent(CmdlinePackagerListener.class);
        addComponent(Compiler.class);
        addComponent(ResourceFinder.class);
        addComponent(CompilerConfig.class);
        addComponent(ConditionContainer.class, ConditionContainer.class);
        addComponent(AssertionHelper.class);
        addComponent(PropertyManager.class);
        addComponent(VariableSubstitutor.class, VariableSubstitutorImpl.class);
        addComponent(IPackager.class, Packager.class);
        addComponent(CompilerHelper.class);
        container.addComponent(RulesEngine.class, RulesEngineImpl.class,
                               new ComponentParameter(ConditionContainer.class));
        addComponent(MergeManager.class, MergeManagerImpl.class);

        new ResolverContainerFiller().fillContainer(this);
        container.addAdapter(new ProviderAdapter(new XmlCompilerHelperProvider()))
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
        getContainer().addAdapter(new ProviderAdapter(new CompilerDataProvider(args)));
    }

}
