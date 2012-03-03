package com.izforge.izpack.installer.container.impl;

import com.izforge.izpack.installer.automation.AutomatedInstaller;
import com.izforge.izpack.installer.console.Console;
import com.izforge.izpack.installer.console.ConsoleInstaller;
import com.izforge.izpack.installer.console.ConsolePrompt;
import com.izforge.izpack.installer.container.provider.AutomatedInstallDataProvider;
import com.izforge.izpack.installer.container.provider.RulesProvider;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.injectors.ProviderAdapter;

public class ConsoleInstallerContainer extends InstallerContainer
{
    /**
     * Registers components with the container.
     *
     * @param pico the container
     */
    @Override
    protected void registerComponents(MutablePicoContainer pico)
    {
        super.registerComponents(pico);
        pico.addAdapter(new ProviderAdapter(new AutomatedInstallDataProvider()));
        pico.addComponent(Console.class);
        pico.addComponent(ConsolePrompt.class);
        pico.addComponent(ConsoleInstaller.class);
        pico.addComponent(AutomatedInstaller.class);
    }
}
