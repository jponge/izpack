package com.izforge.izpack.compiler.container;

import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.compiler.Compiler;
import com.izforge.izpack.compiler.CompilerConfig;
import com.izforge.izpack.compiler.cli.CliAnalyzer;
import com.izforge.izpack.compiler.data.PropertyManager;
import com.izforge.izpack.compiler.helper.CompilerHelper;
import com.izforge.izpack.compiler.helper.CompilerResourceManager;
import com.izforge.izpack.compiler.helper.IXmlCompilerHelper;
import com.izforge.izpack.compiler.helper.impl.XmlCompilerHelper;
import com.izforge.izpack.compiler.listener.CmdlinePackagerListener;
import com.izforge.izpack.compiler.merge.MergeManager;
import com.izforge.izpack.compiler.packager.IPackager;
import com.izforge.izpack.compiler.packager.Packager;
import com.izforge.izpack.compiler.provider.*;
import com.izforge.izpack.core.container.AbstractContainer;
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
        pico.addComponent(CompilerResourceManager.class);
        pico.addComponent(MergeManager.class);
        pico.addComponent(VariableSubstitutor.class, VariableSubstitutorImpl.class);

        pico.addComponent(IPackager.class, Packager.class);
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
    public void processCompileDataFromArgs(String[] args) {
        pico.addAdapter(new ProviderAdapter(new CompilerDataProvider(args)));
    }

}
