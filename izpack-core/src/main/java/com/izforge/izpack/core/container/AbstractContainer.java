package com.izforge.izpack.core.container;

import com.izforge.izpack.api.container.BindeableContainer;
import com.izforge.izpack.api.container.DependenciesFillerContainer;
import com.izforge.izpack.api.exception.IzPackClassNotFoundException;
import com.izforge.izpack.api.exception.IzPackException;
import org.picocontainer.Characteristics;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Parameter;
import org.picocontainer.PicoBuilder;

/**
 * Abstract container for commons methods
 */
public abstract class AbstractContainer implements BindeableContainer, DependenciesFillerContainer
{

    protected MutablePicoContainer pico;

    /**
     * Init component bindings
     */
    public void initBindings() throws IzPackException
    {
        pico = new PicoBuilder().withConstructorInjection().withCaching().build();
        fillContainer(pico);
    }

    public <T> void addComponent(Class<T> componentType)
    {
        pico.as(Characteristics.USE_NAMES).addComponent(componentType);
    }

    public void addComponent(Object componentType, Object implementation, Parameter... parameters)
    {
        pico.addComponent(componentType, implementation, parameters);
    }

    public <T> T getComponent(Class<T> componentType)
    {
        return pico.getComponent(componentType);
    }

    public Object getComponent(Object componentKeyOrType)
    {
        return pico.getComponent(componentKeyOrType);
    }

    public void addConfig(String name, Object val)
    {
        pico.addConfig(name, val);
    }

    public void dispose()
    {
        pico.dispose();
    }

    @Override
    public MutablePicoContainer makeChildContainer()
    {
        return pico.makeChildContainer();
    }

    public MutablePicoContainer getContainer()
    {
        return pico;
    }

    /**
     * Returns a class given its name.
     *
     * @param className the class name
     * @param superType the super type
     * @return the corresponding class
     * @throws ClassCastException           if <tt>className</tt> does not implement or extend <tt>superType</tt>
     * @throws IzPackClassNotFoundException if the class cannot be found
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> Class<T> getClass(String className, Class<T> superType)
    {
        Class type;
        try
        {
            // Using the superclass class loader to load the child to avoid multiple copies of the superclass being
            // loaded in separate class loaders. This is typically an issue during testing where
            // the same classes may be loaded twice - once by maven, and once by the installer.
            type = superType.getClassLoader().loadClass(className);
            if (!superType.isAssignableFrom(type))
            {
                throw new ClassCastException("Class '" + type.getName() + "' does not implement "
                                                     + superType.getName());
            }
        }
        catch (ClassNotFoundException exception)
        {
            throw new IzPackClassNotFoundException(className, exception);
        }
        return (Class<T>) type;
    }

}
