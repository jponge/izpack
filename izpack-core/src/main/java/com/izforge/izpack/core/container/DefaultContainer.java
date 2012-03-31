package com.izforge.izpack.core.container;

import com.izforge.izpack.api.container.Container;
import com.izforge.izpack.api.exception.ContainerException;

/**
 * Default implementation of the {@link Container} interface.
 *
 * @author Tim Anderson
 */
public class DefaultContainer extends AbstractContainer
{

    /**
     * Constructs a <tt>DefaultContainer</tt>.
     *
     * @throws ContainerException if initialisation fails
     */
    public DefaultContainer()
    {
        initialise();
    }

}
