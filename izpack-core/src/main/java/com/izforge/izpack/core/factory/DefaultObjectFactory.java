package com.izforge.izpack.core.factory;


import com.izforge.izpack.api.container.BindeableContainer;
import com.izforge.izpack.api.factory.AbstractObjectFactory;
import com.izforge.izpack.api.factory.ObjectFactory;
import org.picocontainer.MutablePicoContainer;


/**
 * Default implementation of {@link ObjectFactory}.
 *
 * @author Tim Anderson
 */
public class DefaultObjectFactory extends AbstractObjectFactory
{
    /**
     * The container.
     */
    private final MutablePicoContainer container;


    /**
     * Constructs a <tt>DefaultObjectFactory</tt>
     *
     * @param container the container
     */
    public DefaultObjectFactory(BindeableContainer container)
    {
        this.container = container.getContainer();
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
            container.removeChildContainer(child);
        }
        return result;
    }

}
