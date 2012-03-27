package com.izforge.izpack.core.factory;


import com.izforge.izpack.api.container.BindeableContainer;
import com.izforge.izpack.api.exception.IzPackClassNotFoundException;
import com.izforge.izpack.api.factory.ObjectFactory;
import org.picocontainer.MutablePicoContainer;


/**
 * Default implementation of {@link ObjectFactory}.
 *
 * @author Tim Anderson
 */
public class DefaultObjectFactory implements ObjectFactory
{
    /**
     * The container.
     */
    private final BindeableContainer container;


    /**
     * Constructs a <tt>DefaultObjectFactory</tt>
     *
     * @param container the container
     */
    public DefaultObjectFactory(BindeableContainer container)
    {
        this.container = container;
    }

    /**
     * Creates a new instance of the specified type.
     *
     * @param type the object type
     * @return a new instance
     */
    @Override
    public <T> T create(Class<T> type)
    {
        T result;
        MutablePicoContainer child = container.makeChildContainer();
        try
        {
            child.addComponent(type);
            result = child.getComponent(type);
        }
        finally
        {
            container.getContainer().removeChildContainer(child);
        }
        return result;
    }

    /**
     * Creates a new instance of the specified class name.
     *
     * @param className the class name
     * @param superType the super type
     * @return a new instance
     * @throws ClassCastException           if <tt>className</tt> does not implement or extend <tt>superType</tt>
     * @throws IzPackClassNotFoundException if the class cannot be found
     */
    @Override
    public <T> T create(String className, Class<T> superType)
    {
        Class<? extends T> type = container.getClass(className, superType);
        return create(type);
    }
}
