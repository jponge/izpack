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
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;
import org.picocontainer.injectors.ConstructorInjection;
import org.picocontainer.injectors.ProviderAdapter;

/**
 * Application Component. <br />
 * Encapsulate the pico provider for application level component.
 */
public class ApplicationContainer implements IApplicationContainer {

    private MutablePicoContainer pico;

    public void initBindings() {
        pico = new PicoBuilder(new ConstructorInjection())
                .withCaching()
//                .withAnnotatedMethodInjection()
//                .withConstructorInjection()
                .build();
        pico
                .addAdapter(new ProviderAdapter(new AutomatedInstallDataProvider()))
                .addAdapter(new ProviderAdapter(new GUIInstallDataProvider()))
                .addAdapter(new ProviderAdapter(new IconsProvider()))
                .addAdapter(new ProviderAdapter(new RulesProvider()));
        pico
                .addComponent(IInstallerContainer.class, InstallerContainer.class)
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

    public <T> T getComponent(Class<T> componentType) {
        return pico.getComponent(componentType);
    }

    public MutablePicoContainer makeChildContainer() {
        return pico.makeChildContainer();
    }


}
