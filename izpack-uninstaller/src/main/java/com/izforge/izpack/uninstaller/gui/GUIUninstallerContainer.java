package com.izforge.izpack.uninstaller.gui;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoException;

import com.izforge.izpack.api.exception.ContainerException;
import com.izforge.izpack.uninstaller.container.UninstallerContainer;

/**
 * GUI uninstaller container.
 *
 * @author Tim Anderson
 */
public class GUIUninstallerContainer extends UninstallerContainer
{

    /**
     * Constructs a <tt>GUIUninstallerContainer</tt>
     */
    public GUIUninstallerContainer()
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
        addComponent(UninstallerFrame.class);
        addComponent(GUIDestroyerHandler.class);
    }
}
