package com.izforge.izpack.uninstaller.console;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoException;

import com.izforge.izpack.api.exception.ContainerException;
import com.izforge.izpack.uninstaller.container.UninstallerContainer;


/**
 * Console uninstaller container.
 *
 * @author Tim Anderson
 */
public class ConsoleUninstallerContainer extends UninstallerContainer
{

    /**
     * Constructs a <tt>ConsoleUninstallerContainer</tt>.
     */
    public ConsoleUninstallerContainer()
    {
        initialise();
    }

    /**
     * Invoked by {@link #initialise} to fill the container.
     * <p/>
     *
     * @param container the underlying container
     * @throws ContainerException if initialisation fails
     * @throws PicoException      for any PicoContainer error
     */
    @Override
    protected void fillContainer(MutablePicoContainer container)
    {
        super.fillContainer(container);
        addComponent(ConsoleDestroyerHandler.class);
    }
}
