package com.izforge.izpack.installer.container;

import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.installer.base.AutomatedInstaller;
import com.izforge.izpack.installer.base.ConditionCheck;
import com.izforge.izpack.installer.base.ConsoleInstaller;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.installer.provider.GUIInstallDataProvider;
import com.izforge.izpack.installer.provider.IconsProvider;
import com.izforge.izpack.installer.provider.RulesProvider;
import com.izforge.izpack.util.substitutor.VariableSubstitutorImpl;
import org.picocontainer.PicoBuilder;
import org.picocontainer.injectors.ConstructorInjection;
import org.picocontainer.injectors.ProviderAdapter;

import java.util.Properties;

/**
 * Application Component. <br />
 * Encapsulate the pico provider for application level component.
 */
public class ApplicationContainer extends AbstractContainer implements IApplicationContainer {

    public void initBindings() {
        pico = new PicoBuilder(new ConstructorInjection())
                .withCaching()
                .build();
        pico
//                .addAdapter(new ProviderAdapter(new AutomatedInstallDataProvider()))
                .addAdapter(new ProviderAdapter(new GUIInstallDataProvider()))
                .addAdapter(new ProviderAdapter(new IconsProvider()))
                .addAdapter(new ProviderAdapter(new RulesProvider()));
        pico
                .addComponent(IInstallerContainer.class, InstallerContainer.class)
                .addComponent(ConditionCheck.class)
                .addComponent(CustomDataContainer.class)
                .addComponent(VariableSubstitutor.class, VariableSubstitutorImpl.class)
                .addComponent(Properties.class)
                .addComponent(ResourceManager.class)
                .addComponent(ConsoleInstaller.class)
                .addComponent(UninstallDataWriter.class)
                .addComponent(AutomatedInstaller.class)
                .addComponent(IApplicationContainer.class, this);
    }

}
