package com.izforge.izpack.compiler.container;

import com.izforge.izpack.compiler.Compiler;
import com.izforge.izpack.compiler.CompilerConfig;
import com.izforge.izpack.compiler.cli.CliAnalyzer;
import com.izforge.izpack.compiler.compressor.BZip2PackCompressor;
import com.izforge.izpack.compiler.compressor.DefaultPackCompressor;
import com.izforge.izpack.compiler.compressor.RawPackCompressor;
import com.izforge.izpack.compiler.data.PropertyManager;
import com.izforge.izpack.compiler.helper.CompilerHelper;
import com.izforge.izpack.compiler.helper.IXmlCompilerHelper;
import com.izforge.izpack.compiler.helper.impl.XmlCompilerHelper;
import com.izforge.izpack.compiler.listener.CmdlinePackagerListener;
import com.izforge.izpack.compiler.packager.IPackager;
import com.izforge.izpack.compiler.packager.Packager;
import com.izforge.izpack.compiler.provider.CompilerDataProvider;
import com.izforge.izpack.compiler.provider.PropertiesProvider;
import com.izforge.izpack.container.AbstractContainer;
import com.izforge.izpack.util.substitutor.VariableSubstitutor;
import com.izforge.izpack.util.substitutor.VariableSubstitutorImpl;
import org.picocontainer.PicoBuilder;
import org.picocontainer.injectors.ProviderAdapter;

/**
 * Container for compiler
 *
 * @author Anthonin Bonnefoy
 */
public class CompilerContainer extends AbstractContainer {

    /**
     * Init component bindings
     */
    public void initBindings() {
        pico = new PicoBuilder().withConstructorInjection().withCaching().build();
        pico.addComponent(CompilerContainer.class, this);
        pico.addComponent(CliAnalyzer.class);
        pico.addComponent(CmdlinePackagerListener.class);
        pico.addComponent(Compiler.class);
        pico.addComponent(IXmlCompilerHelper.class, XmlCompilerHelper.class);
        pico.addComponent(CompilerConfig.class);
        pico.addComponent(CompilerHelper.class);
        pico.addComponent(PropertyManager.class);
        pico.addComponent(CompilerResourceManagers.class);
        pico.addComponent(VariableSubstitutor.class, VariableSubstitutorImpl.class);

        pico.addComponent(IPackager.class, Packager.class);

        // Compressors
        pico.addComponent("raw", RawPackCompressor.class);
        pico.addComponent("default", DefaultPackCompressor.class);
        pico.addComponent("bzip2", BZip2PackCompressor.class);

        pico.addAdapter(new ProviderAdapter(new PropertiesProvider()));
    }

    /**
     * Add CompilerDataComponent by processing command line args
     *
     * @param args command line args passed to the main
     */
    public void processCompileDataFromArgs(String[] args) {
        pico.addAdapter(new ProviderAdapter(new CompilerDataProvider(args)));
    }

}
