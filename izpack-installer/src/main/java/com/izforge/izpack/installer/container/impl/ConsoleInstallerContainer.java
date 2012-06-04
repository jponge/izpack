package com.izforge.izpack.installer.container.impl;


import org.picocontainer.MutablePicoContainer;
import org.picocontainer.injectors.ProviderAdapter;

import com.izforge.izpack.api.exception.ContainerException;
import com.izforge.izpack.core.handler.ConsolePrompt;
import com.izforge.izpack.installer.automation.AutomatedInstaller;
import com.izforge.izpack.installer.console.ConsoleInstaller;
import com.izforge.izpack.installer.container.provider.AutomatedInstallDataProvider;
import com.izforge.izpack.installer.container.provider.ConsolePanelsProvider;
import com.izforge.izpack.util.Console;

/**
 * Installer container for console based installers.
 *
 * @author Tim Anderson
 */
public class ConsoleInstallerContainer extends InstallerContainer
{

    /**
     * Constructs a <tt>ConsoleInstallerContainer</tt>.
     *
     * @throws ContainerException if initialisation fails
     */
    public ConsoleInstallerContainer()
    {
        initialise();
    }

    /**
     * Constructs a <tt>ConsoleInstallerContainer</tt>.
     * <p/>
     * This constructor is provided for testing purposes.
     *
     * @param container the underlying container
     * @throws ContainerException if initialisation fails
     */
    protected ConsoleInstallerContainer(MutablePicoContainer container)
    {
        initialise(container);
    }

    /**
     * Registers components with the container.
     *
     * @param container the container
     */
    @Override
    protected void registerComponents(MutablePicoContainer container)
    {
        super.registerComponents(container);
        container.addAdapter(new ProviderAdapter(new AutomatedInstallDataProvider()));
        container.addAdapter(new ProviderAdapter(new ConsolePanelsProvider()));
        container.addComponent(Console.class);
        container.addComponent(ConsolePrompt.class);
        container.addComponent(ConsoleInstaller.class);
        container.addComponent(AutomatedInstaller.class);
    }
}
