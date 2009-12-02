package com.izforge.izpack.installer.provider;

import com.izforge.izpack.data.AutomatedInstallData;
import com.izforge.izpack.data.ResourceManager;

/**
 * Install data loader
 */
public class AutomatedInstallDataProvider extends AbstractInstallDataProvider {

    public AutomatedInstallData provide(ResourceManager resourceManager) throws Exception, InterruptedException {
        this.resourceManager = resourceManager;
        final AutomatedInstallData automatedInstallData = new AutomatedInstallData();
        // Loads the installation data
        loadInstallData(automatedInstallData);

        // Load custom langpack if exist.
        addCustomLangpack(automatedInstallData);

        loadDynamicVariables(automatedInstallData);
        loadInstallerRequirements(automatedInstallData);
        return automatedInstallData;
    }


}
