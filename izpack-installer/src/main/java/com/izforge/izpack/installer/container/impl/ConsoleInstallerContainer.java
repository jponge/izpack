package com.izforge.izpack.installer.container.impl;


import com.izforge.izpack.installer.automation.AutomatedInstaller;
import com.izforge.izpack.installer.console.ConsoleInstaller;
import com.izforge.izpack.installer.console.ConsolePrompt;
import com.izforge.izpack.installer.container.provider.AutomatedInstallDataProvider;
import com.izforge.izpack.util.Console;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.injectors.ProviderAdapter;

/**
 * Installer container for console based installers.
 *
 * @author Tim Anderson
 */
public class ConsoleInstallerContainer extends InstallerContainer
{
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
        container.addComponent(Console.class);
        container.addComponent(ConsolePrompt.class);
        container.addComponent(ConsoleInstaller.class);
        container.addComponent(AutomatedInstaller.class);
    }
}
