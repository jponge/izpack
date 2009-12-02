package com.izforge.izpack.installer.provider;

import com.izforge.izpack.data.AutomatedInstallData;
import com.izforge.izpack.data.LocaleDatabase;
import com.izforge.izpack.data.ResourceManager;

import java.io.InputStream;

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
        loadDefaultLocale(automatedInstallData);
        loadDynamicVariables(automatedInstallData);
        loadInstallerRequirements(automatedInstallData);
        return automatedInstallData;
    }

    private void loadDefaultLocale(AutomatedInstallData automatedInstallData) throws Exception {
        // Loads the suitable langpack
        java.util.List<String> availableLangPacks = resourceManager.getAvailableLangPacks();
        String selectedPack = availableLangPacks.get(0);
        InputStream in = resourceManager.getInputStream("langpacks/" + selectedPack + ".xml");
        automatedInstallData.setAndProcessLocal(selectedPack, new LocaleDatabase(in));
        resourceManager.setLocale(selectedPack);
    }


}
