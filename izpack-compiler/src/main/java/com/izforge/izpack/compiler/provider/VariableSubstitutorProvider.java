package com.izforge.izpack.compiler.provider;

import com.izforge.izpack.util.substitutor.VariableSubstitutor;
import com.izforge.izpack.util.substitutor.VariableSubstitutorImpl;
import org.picocontainer.injectors.Provider;

import java.io.IOException;
import java.util.Properties;

/**
 * Provider for variable substituor
 *
 * @author Anthonin Bonnefoy
 */
public class VariableSubstitutorProvider implements Provider {

    public VariableSubstitutor provide() {
        // initialize backed by system properties
        Properties properties = new Properties(System.getProperties());
        try {
            properties.load(getClass().getResourceAsStream("path.properties"));
        } catch (IOException e) {
            throw new RuntimeException("Can't load path.properties");
        }
        return new VariableSubstitutorImpl(properties);
    }
}
