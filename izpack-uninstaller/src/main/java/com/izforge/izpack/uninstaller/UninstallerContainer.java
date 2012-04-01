package com.izforge.izpack.uninstaller;

import java.util.Properties;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoException;
import org.picocontainer.injectors.ProviderAdapter;

import com.izforge.izpack.api.container.Container;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.api.exception.ContainerException;
import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.core.container.PlatformProvider;
import com.izforge.izpack.core.factory.DefaultObjectFactory;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.util.DefaultTargetPlatformFactory;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.Librarian;
import com.izforge.izpack.util.Platforms;
import com.izforge.izpack.util.TargetFactory;


/**
 * Uninstaller container.
 *
 * @author Tim Anderson
 */
public class UninstallerContainer extends AbstractContainer
{

    /**
     * Constructs an <tt>UninstallerContainer</tt>.
     *
     * @throws ContainerException if initialisation fails
     */
    public UninstallerContainer()
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
        addComponent(Housekeeper.class);
        addComponent(Librarian.class);
        addComponent(TargetFactory.class);
        addComponent(DefaultObjectFactory.class);
        addComponent(DefaultTargetPlatformFactory.class);
        addComponent(RegistryDefaultHandler.class);
        addComponent(UninstallerFrame.class);
        addComponent(Container.class, this);
        addComponent(Properties.class);
        addComponent(ResourceManager.class);
        addComponent(UninstallerConsole.class);
        addComponent(Platforms.class);
        container.addAdapter(new ProviderAdapter(new PlatformProvider()));
    }
}
