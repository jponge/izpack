package com.izforge.izpack.compiler.container;

import com.izforge.izpack.container.AbstractContainer;
import org.picocontainer.PicoBuilder;

/**
 * Container for compiler
 *
 * @author Anthonin Bonnefoy
 */
public class CompilerContainer extends AbstractContainer {

    public void initBindings() {
        pico = new PicoBuilder().withConstructorInjection().withCaching().build();
    }

}
