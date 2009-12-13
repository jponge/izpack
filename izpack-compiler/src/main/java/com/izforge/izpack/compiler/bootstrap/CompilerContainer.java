package com.izforge.izpack.compiler.bootstrap;

import com.izforge.izpack.container.AbstractContainer;
import org.picocontainer.PicoBuilder;

/**
 * Container for compiler
 */
public class CompilerContainer extends AbstractContainer {

    public void initBindings() {
        pico = new PicoBuilder().withConstructorInjection().withCaching().build();

    }

}
