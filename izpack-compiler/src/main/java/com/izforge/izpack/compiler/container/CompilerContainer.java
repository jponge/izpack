package com.izforge.izpack.compiler.container;

import com.izforge.izpack.compiler.CompilerConfig;
import com.izforge.izpack.compiler.cli.CliAnalyzer;
import com.izforge.izpack.compiler.listener.CmdlinePackagerListener;
import com.izforge.izpack.compiler.provider.CompilerDataProvider;
import com.izforge.izpack.container.AbstractContainer;
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
        pico.addComponent(CliAnalyzer.class);
        pico.addComponent(CmdlinePackagerListener.class);
        pico.addComponent(Compiler.class);
        pico.addComponent(CompilerConfig.class);
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
