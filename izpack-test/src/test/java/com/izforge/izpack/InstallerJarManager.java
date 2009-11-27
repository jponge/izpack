package com.izforge.izpack;

import com.izforge.izpack.installer.base.InstallerFrame;
import com.izforge.izpack.installer.provider.*;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.behaviors.ThreadCaching;
import org.picocontainer.injectors.ConstructorInjection;
import org.picocontainer.injectors.ProviderAdapter;

/**
 * Get the installerFrame from the install jar
 */
public class InstallerJarManager {
    private DefaultPicoContainer pico;

    public InstallerJarManager() {
        initBinding();
    }

    public void initBinding() {
        pico = new DefaultPicoContainer(new ThreadCaching());
        pico.addAdapter(new ProviderAdapter(new InstallDataProvider()))
                .addAdapter(new ProviderAdapter(new GUIInstallerProvider()))
                .addAdapter(new ProviderAdapter(new IconsProvider()))
                .addAdapter(new ProviderAdapter(new InstallerFrameProvider()))
                .addAdapter(new ProviderAdapter(new RulesProvider()));
    }

    public InstallerFrame getFrame(){
        return pico.getComponent(InstallerFrame.class);
    }
}
