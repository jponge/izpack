package com.izforge.izpack.bootstrap;

import com.izforge.izpack.installer.base.InstallerFrame;
import com.izforge.izpack.installer.provider.InstallerFrameProvider;
import com.izforge.izpack.installer.provider.LanguageDialogProvider;
import com.izforge.izpack.panels.PanelManager;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;
import org.picocontainer.PicoContainer;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.injectors.ProviderAdapter;

/**
 * Container for panel level component
 */
public class PanelComponent implements IPanelComponent {

    public MutablePicoContainer pico;

    public IApplicationComponent parent;

    public PanelComponent(IApplicationComponent parent) {
        pico = new PicoBuilder(parent.getPico()).withCaching().withConstructorInjection().addChildToParent().build();
        initBindings();
    }

    public void initBindings() {
        pico
                .addComponent(IPanelComponent.class, this)
                .addComponent(PanelManager.class)
                .addAdapter(new ProviderAdapter(new InstallerFrameProvider()))
                .addAdapter(new ProviderAdapter(new LanguageDialogProvider()));
    }

    public <T> void addComponent(Class<T> componentType) {
        pico.addComponent(componentType);
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
