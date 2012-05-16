package com.izforge.izpack.installer.container.provider;

import java.io.IOException;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.installer.data.InstallData;
import com.izforge.izpack.util.Housekeeper;

/**
 * Install data loader
 */
public class AutomatedInstallDataProvider extends AbstractInstallDataProvider
{

    public AutomatedInstallData provide(Resources resources, Locales locales, DefaultVariables variables,
                                        Housekeeper housekeeper)
            throws IOException, ClassNotFoundException, InstallerException
    {
        final AutomatedInstallData automatedInstallData = new InstallData(variables);
        // Loads the installation data
        loadInstallData(automatedInstallData, resources, housekeeper);

        loadDefaultLocale(automatedInstallData, locales);
        // Load custom langpack if exist.
        addCustomLangpack(automatedInstallData, locales);
        loadDynamicVariables(variables, automatedInstallData, resources);
        loadDynamicConditions(automatedInstallData, resources);
        loadInstallerRequirements(automatedInstallData, resources);
        return automatedInstallData;
    }

}
