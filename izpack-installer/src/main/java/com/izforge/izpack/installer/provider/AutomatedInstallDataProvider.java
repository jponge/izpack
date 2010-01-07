package com.izforge.izpack.installer.provider;

import com.izforge.izpack.container.CustomDataContainer;
import com.izforge.izpack.data.AutomatedInstallData;
import com.izforge.izpack.data.ResourceManager;
import com.izforge.izpack.util.substitutor.VariableSubstitutor;

/**
 * Install data loader
 */
public class AutomatedInstallDataProvider extends AbstractInstallDataProvider {

    public AutomatedInstallData provide(ResourceManager resourceManager, CustomDataContainer customDataContainer, VariableSubstitutor variableSubstitutor) {
        try {
            this.resourceManager = resourceManager;
            this.variableSubstitutor = variableSubstitutor;
            final AutomatedInstallData automatedInstallData = new AutomatedInstallData();
            // Loads the installation data
            loadInstallData(automatedInstallData);
            // Load custom action data.
            loadCustomData(automatedInstallData, customDataContainer);

            // Load custom langpack if exist.
            addCustomLangpack(automatedInstallData);
            loadDefaultLocale(automatedInstallData);
            loadDynamicVariables(automatedInstallData);
            loadInstallerRequirements(automatedInstallData);
            return automatedInstallData;
        } catch (Exception e) {
            // TODO little workaround to get pico message. Should find a better way in the future            
            throw new RuntimeException(e);
        }
    }


}
