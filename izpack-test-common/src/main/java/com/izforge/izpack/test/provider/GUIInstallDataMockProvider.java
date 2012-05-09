package com.izforge.izpack.test.provider;

import org.picocontainer.injectors.Provider;

import com.izforge.izpack.api.data.GUIPrefs;
import com.izforge.izpack.api.data.Info;
import com.izforge.izpack.api.data.LocaleDatabase;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.installer.data.GUIInstallData;

/**
 * Mock provider for guiInstallData
 */
public class GUIInstallDataMockProvider implements Provider
{

    public GUIInstallData provide(Variables variables) throws Exception
    {
        final GUIInstallData guiInstallData = new GUIInstallData(variables);
        GUIPrefs guiPrefs = new GUIPrefs();
        guiPrefs.height = 600;
        guiPrefs.width = 480;
        guiInstallData.guiPrefs = guiPrefs;

        Info info = new Info();
        guiInstallData.setInfo(info);

        LocaleDatabase localDataBase = new LocaleDatabase(
                ClassLoader.getSystemClassLoader().getResource(
                        "com/izforge/izpack/bin/langpacks/installer/eng.xml").openStream());
        guiInstallData.setAndProcessLocal("eng", localDataBase);

        return guiInstallData;
    }

}
