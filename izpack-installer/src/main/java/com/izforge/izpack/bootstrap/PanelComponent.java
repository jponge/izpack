package com.izforge.izpack.bootstrap;

import org.picocontainer.DefaultPicoContainer;

/**
 * Container for panel level component
 */
public class PanelComponent implements IPanelComponent{

    public DefaultPicoContainer pico;

    public ApplicationComponent applicationComponent;

    public PanelComponent(ApplicationComponent applicationComponent) {
        this.applicationComponent = applicationComponent;
    }

    public void initBindings() {
        pico = new DefaultPicoContainer(applicationComponent.pico);
        pico
                .addComponent(IPanelComponent.class,this);
    }

    public void dispose() {
        pico.dispose();
    }

    public <T> T getComponent(Class<T> componentType) {
        return pico.getComponent(componentType);
    }
}
