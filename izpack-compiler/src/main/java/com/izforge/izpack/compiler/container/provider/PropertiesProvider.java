package com.izforge.izpack.compiler.container.provider;

import org.picocontainer.injectors.Provider;

import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Anthonin Bonnefoy
 */
public class PropertiesProvider implements Provider {

    public Properties provide() {
        // initialize backed by system properties
        return new Properties(System.getProperties());
    }
}
