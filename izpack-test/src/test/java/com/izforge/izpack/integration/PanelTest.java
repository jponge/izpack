package com.izforge.izpack.integration;

import com.izforge.izpack.bootstrap.IPanelContainer;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.panels.PanelManager;
import com.izforge.izpack.panels.TargetPanel;
import org.junit.Test;

import java.io.File;

/**
 * Integration test for panels
 */
public class PanelTest extends AbstractInstallationTest {


    @Test
    public void testTargetPanel() throws Exception {
        compileAndUnzip("basicInstall.xml", getWorkingDirectory("samples/basicInstall"));

        GUIInstallData installData = applicationContainer.getComponent(GUIInstallData.class);
        panelContainer = applicationContainer.getComponent(IPanelContainer.class);
        PanelManager panelManager = panelContainer.getComponent(PanelManager.class);
        panelManager.loadPanelsInContainer();
        panelManager.instanciatePanels();

        TargetPanel targetPanel = panelContainer.getComponent(TargetPanel.class);
        targetPanel.loadDefaultDir();
        String defaultDir = installData.getInstallPath();
        System.out.println(installData.getInstallPath());

        File file = new File(defaultDir);
        if (file.exists()) {
            file.delete();
        }
    }
}
