package com.izforge.izpack.api.factory;

import org.picocontainer.PicoClassNotFoundException;


/**
 * Abstract implementation of {@link ObjectFactory}.
 *
 * @author Tim Anderson
 */
public abstract class AbstractObjectFactory implements ObjectFactory {

    /**
     * Creates a new instance of the specified class name.
     *
     * @param className the class name
     * @param superType the super type
     * @return a new instance
     * @throws IllegalArgumentException if <tt>className</tt> does not implement or extend <tt>superType</tt>
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T create(String className, Class<T> superType) {
        Class type;
        try {
            // Using the superclass class loader to load the child to avoid multiple copies of the superclass being
            // loaded in separate class loaders. This is typically an issue during testing where
            // the same classes may be loaded twice - once by maven, and once by the installer.
            type = superType.getClassLoader().loadClass(className);
            if (!superType.isAssignableFrom(type)) {
                throw new IllegalArgumentException("Class " + type.getName() + " does not implement "
                                                   + superType.getName());
            }
        } catch (ClassNotFoundException exception) {
            throw new PicoClassNotFoundException(className, exception);
        }
        return create((Class<T>) type);
    }

}
