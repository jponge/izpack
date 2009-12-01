package com.izforge.izpack.bootstrap;

import com.izforge.izpack.data.ResourceManager;
import com.izforge.izpack.installer.base.*;
import com.izforge.izpack.installer.provider.*;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.injectors.ProviderAdapter;

/**
 * Application Component. <br />
 * Encapsulate the pico provider for application level component.
 */
public class ApplicationComponent implements IApplicationComponent {

    DefaultPicoContainer pico;
    
    public void initBindings() {
        pico = new DefaultPicoContainer(new Caching());
        pico.addAdapter(new ProviderAdapter(new InstallDataProvider()))
                .addAdapter(new ProviderAdapter(new IconsProvider()))
                .addAdapter(new ProviderAdapter(new InstallerFrameProvider()))
                .addAdapter(new ProviderAdapter(new LanguageDialogProvider()))
                .addAdapter(new ProviderAdapter(new RulesProvider()));
        pico
                .addComponent(ConditionCheck.class)
                .addComponent(GUIInstaller.class)
                .addComponent(ResourceManager.class)
                .addComponent(ConsoleInstaller.class)
                .addComponent(AutomatedInstaller.class)
                .addComponent(IApplicationComponent.class,this);
    }

    public void dispose() {
        pico.dispose();
    }

    public <T> T getComponent(Class<T> componentType) {
        return pico.getComponent(componentType);
    }
}
