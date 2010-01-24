package com.izforge.izpack.compiler.container.provider;

import org.picocontainer.injectors.Provider;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Anthonin Bonnefoy
 */
public class PropertiesProvider implements Provider {

    public Properties provide() {
        // initialize backed by system properties
        Properties properties = new Properties(System.getProperties());
        try {
            properties.load(getClass().getResourceAsStream("path.properties"));
        } catch (IOException e) {
            throw new RuntimeException("Can't load path.properties");
        }
        return properties;
    }
}
