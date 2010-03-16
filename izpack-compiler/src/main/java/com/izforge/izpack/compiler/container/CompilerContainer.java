package com.izforge.izpack.compiler.container;

import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.compiler.Compiler;
import com.izforge.izpack.compiler.CompilerConfig;
import com.izforge.izpack.compiler.cli.CliAnalyzer;
import com.izforge.izpack.compiler.container.provider.*;
import com.izforge.izpack.compiler.data.PropertyManager;
import com.izforge.izpack.compiler.helper.AssertionHelper;
import com.izforge.izpack.compiler.helper.CompilerHelper;
import com.izforge.izpack.compiler.helper.CompilerResourceManager;
import com.izforge.izpack.compiler.listener.CmdlinePackagerListener;
import com.izforge.izpack.compiler.packager.IPackager;
import com.izforge.izpack.compiler.packager.impl.Packager;
import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.merge.MergeManager;
import com.izforge.izpack.merge.MergeManagerImpl;
import com.izforge.izpack.merge.resolve.ClassPathCrawler;
import com.izforge.izpack.merge.resolve.MergeableResolver;
import com.izforge.izpack.merge.resolve.PathResolver;
import com.izforge.izpack.util.substitutor.VariableSubstitutorImpl;
import org.picocontainer.Characteristics;
import org.picocontainer.PicoBuilder;
import org.picocontainer.injectors.ProviderAdapter;

/**
 * Container for compiler
 *
 * @author Anthonin Bonnefoy
 */
public class CompilerContainer extends AbstractContainer
{

    /**
     * Init component bindings
     */
    public void initBindings()
    {
        pico = new PicoBuilder().withConstructorInjection().withCaching().build();
        pico.addComponent(CompilerContainer.class, this);
        pico.addComponent(CliAnalyzer.class);
        pico.addComponent(CmdlinePackagerListener.class);
        pico.addComponent(Compiler.class);
        pico.addComponent(ClassPathCrawler.class);
        pico.addComponent(MergeableResolver.class);
        pico.addComponent(PathResolver.class);
        pico.addComponent(CompilerConfig.class);
        pico.as(Characteristics.USE_NAMES).addComponent(AssertionHelper.class);
        pico.addComponent(CompilerHelper.class);
        pico.addComponent(PropertyManager.class);
        pico.addComponent(CompilerResourceManager.class);
        pico.addComponent(MergeManager.class, MergeManagerImpl.class);
        pico.addComponent(VariableSubstitutor.class, VariableSubstitutorImpl.class);
        pico.addComponent(IPackager.class, Packager.class);


        pico.addAdapter(new ProviderAdapter(new IzpackProjectProvider()));
        pico.addAdapter(new ProviderAdapter(new XmlCompilerHelperProvider()));
        pico.addAdapter(new ProviderAdapter(new PropertiesProvider()));
        pico.addAdapter(new ProviderAdapter(new JarOutputStreamProvider()));
        pico.addAdapter(new ProviderAdapter(new CompressedOutputStreamProvider()));
        pico.addAdapter(new ProviderAdapter(new PackCompressorProvider()));
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
