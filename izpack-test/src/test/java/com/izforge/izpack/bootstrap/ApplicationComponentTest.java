package com.izforge.izpack.bootstrap;

import com.izforge.izpack.data.ResourceManager;
import com.izforge.izpack.installer.base.AutomatedInstaller;
import com.izforge.izpack.installer.base.ConsoleInstaller;
import com.izforge.izpack.installer.base.GUIInstaller;
import com.izforge.izpack.installer.base.LanguageDialog;
import com.izforge.izpack.installer.provider.*;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.injectors.ProviderAdapter;

import javax.swing.*;

/**
 * Application Component for integration tests.<br />
 * Encapsulate the pico provider for application level component.
 */
public class ApplicationComponentTest implements IApplicationComponent {

    public DefaultPicoContainer pico;

    public void initBindings() {
        pico = new DefaultPicoContainer(new Caching());
        pico.addAdapter(new ProviderAdapter(new InstallDataProvider()))
                .addAdapter(new ProviderAdapter(new IconsProvider()))
                .addAdapter(new ProviderAdapter(new InstallerFrameProvider()))
                .addAdapter(new ProviderAdapter(new LanguageDialogProvider()))
                .addAdapter(new ProviderAdapter(new RulesProvider()));
        pico
                .addComponent(GUIInstaller.class)
                .addComponent(ResourceManager.class)
                .addComponent(ConsoleInstaller.class)
                .addComponent(AutomatedInstaller.class)
                .addComponent(IApplicationComponent.class,this);
    }

    public <T> T getComponent(Class<T> componentType) {
        return pico.getComponent(componentType);
    }
}