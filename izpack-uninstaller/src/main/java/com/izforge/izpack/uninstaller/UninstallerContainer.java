package com.izforge.izpack.uninstaller;

import com.izforge.izpack.api.container.BindeableContainer;
import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.util.DefaultTargetPlatformFactory;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.Librarian;
import com.izforge.izpack.util.TargetFactory;
import org.picocontainer.MutablePicoContainer;


/**
 * Uninstaller container.
 *
 * @author Tim Anderson
 */
public class UninstallerContainer extends AbstractContainer
{
    /**
     * Registers components with the Pico container.
     *
     * @param container the Pico container
     */
    @Override
    public void fillContainer(MutablePicoContainer container)
    {
        container.addComponent(Housekeeper.class);
        container.addComponent(Librarian.class);
        container.addComponent(TargetFactory.class);
        container.addComponent(DefaultTargetPlatformFactory.class);
        container.addComponent(RegistryDefaultHandler.class);
        container.addComponent(UninstallerFrame.class);
        container.addComponent(BindeableContainer.class, this);
        container.addComponent(UninstallerConsole.class);
    }
}
