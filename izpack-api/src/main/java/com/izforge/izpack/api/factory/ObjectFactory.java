package com.izforge.izpack.api.factory;


/**
 * An object factory that supports dependency injection.
 *
 * @author Tim Anderson
 */
public interface ObjectFactory
{

    /**
     * Creates a new instance of the specified type.
     *
     * @param type the object type
     * @return a new instance
     */
    <T> T create(Class<T> type);

    /**
     * Creates a new instance of the specified class name.
     *
     * @param className the class name
     * @param superType the super type
     * @return a new instance
     * @throws IllegalArgumentException if <tt>className</tt> does not implement or extend <tt>superType</tt>
     */
    <T> T create(String className, Class<T> superType);

}