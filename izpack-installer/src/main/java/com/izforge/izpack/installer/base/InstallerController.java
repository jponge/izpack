package com.izforge.izpack.installer.base;

import com.izforge.izpack.installer.manager.PanelManager;

/**
 * Installer frame controller
 *
 * @author Anthonin Bonnefoy
 */
public class InstallerController
{

    private InstallerFrame installerFrame;
    private PanelManager panelManager;

    public InstallerController(PanelManager panelManager, InstallDataConfiguratorWithRules installDataRulesEngineManager, InstallerFrame installerFrame)
    {
        this.panelManager = panelManager;
        this.installerFrame = installerFrame;
        installDataRulesEngineManager.configureInstallData();

    }

    public InstallerController buildInstallation() throws Exception
    {
        panelManager.loadPanelsInContainer();
        panelManager.instantiatePanels();
        installerFrame.buildGUI();
        installerFrame.sizeFrame();
        return this;
    }

    public void launchInstallation()
    {
        installerFrame.launchGUI();
    }
}
