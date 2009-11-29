package com.izforge.izpack.installer.provider;

import com.izforge.izpack.data.ResourceManager;
import com.izforge.izpack.installer.base.AutomatedInstaller;
import com.izforge.izpack.installer.base.ConsoleInstaller;
import com.izforge.izpack.installer.base.GUIInstaller;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.behaviors.ThreadCaching;
import org.picocontainer.injectors.ProviderAdapter;

/**
 * Provide pico instance
 */
public class PicoProvider {

    private static DefaultPicoContainer pico;

    public static DefaultPicoContainer getPico() {
        if (pico == null) {
            initBindings();
        }
        return pico;
    }

    public static void initBindings() {
        pico = new DefaultPicoContainer(new Caching());
        pico.addAdapter(new ProviderAdapter(new InstallDataProvider()))
                .addAdapter(new ProviderAdapter(new IconsProvider()))
                .addAdapter(new ProviderAdapter(new InstallerFrameProvider()))
                .addAdapter(new ProviderAdapter(new RulesProvider()));
        pico
                .addComponent(GUIInstaller.class)
                .addComponent(ResourceManager.class)
                .addComponent(ConsoleInstaller.class)
                .addComponent(AutomatedInstaller.class);
    }
}
