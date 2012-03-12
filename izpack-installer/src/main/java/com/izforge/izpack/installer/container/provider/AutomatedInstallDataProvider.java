package com.izforge.izpack.installer.container.provider;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.installer.data.InstallData;
import com.izforge.izpack.merge.resolve.ClassPathCrawler;
import com.izforge.izpack.util.Housekeeper;

import java.util.Properties;

/**
 * Install data loader
 */
public class AutomatedInstallDataProvider extends AbstractInstallDataProvider
{

    public AutomatedInstallData provide(ResourceManager resourceManager, VariableSubstitutor variableSubstitutor,
                                        Properties variables, ClassPathCrawler classPathCrawler,
                                        Housekeeper housekeeper)
    {
        try
        {
            this.resourceManager = resourceManager;
            this.variableSubstitutor = variableSubstitutor;
            this.classPathCrawler = classPathCrawler;
            this.housekeeper = housekeeper;

            final AutomatedInstallData automatedInstallData = new InstallData(variables);
            // Loads the installation data
            loadInstallData(automatedInstallData);

            // Load custom langpack if exist.
            addCustomLangpack(automatedInstallData);
            loadDefaultLocale(automatedInstallData);
            loadDynamicVariables(automatedInstallData);
            loadDynamicConditions(automatedInstallData);
            loadInstallerRequirements(automatedInstallData);
            return automatedInstallData;
        }
        catch (Exception e)
        {
            // TODO little workaround to get pico message. Should find a better way in the future
            throw new RuntimeException(e);
        }
    }


}
