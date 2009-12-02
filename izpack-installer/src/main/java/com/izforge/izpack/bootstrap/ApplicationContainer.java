package com.izforge.izpack.bootstrap;

import com.izforge.izpack.data.ResourceManager;
import com.izforge.izpack.installer.base.AutomatedInstaller;
import com.izforge.izpack.installer.base.ConditionCheck;
import com.izforge.izpack.installer.base.ConsoleInstaller;
import com.izforge.izpack.installer.base.GUIInstaller;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.installer.provider.AutomatedInstallDataProvider;
import com.izforge.izpack.installer.provider.GUIInstallDataProvider;
import com.izforge.izpack.installer.provider.IconsProvider;
import com.izforge.izpack.installer.provider.RulesProvider;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.injectors.ProviderAdapter;

/**
 * Application Component. <br />
 * Encapsulate the pico provider for application level component.
 */
public class ApplicationContainer implements IApplicationContainer {

    private DefaultPicoContainer pico;

    public void initBindings() {
        pico = new DefaultPicoContainer(new Caching());
        pico
                .addAdapter(new ProviderAdapter(new AutomatedInstallDataProvider()))
                .addAdapter(new ProviderAdapter(new GUIInstallDataProvider()))
                .addAdapter(new ProviderAdapter(new IconsProvider()))
                .addAdapter(new ProviderAdapter(new RulesProvider()));
        pico
                .addComponent(IPanelContainer.class, PanelContainer.class)
                .addComponent(ConditionCheck.class)
                .addComponent(GUIInstaller.class)
                .addComponent(ResourceManager.class)
                .addComponent(ConsoleInstaller.class)
                .addComponent(UninstallDataWriter.class)
                .addComponent(AutomatedInstaller.class)
                .addComponent(IApplicationContainer.class, this);
    }

    public void dispose() {
        pico.dispose();
    }

    public DefaultPicoContainer getPico() {
        return pico;
    }

    public <T> T getComponent(Class<T> componentType) {
        return pico.getComponent(componentType);
    }

    public MutablePicoContainer makeChildContainer() {
        return pico.makeChildContainer();
    }


}
