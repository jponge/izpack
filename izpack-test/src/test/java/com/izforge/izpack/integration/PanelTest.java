package com.izforge.izpack.integration;

import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.container.IInstallerContainer;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.panels.PanelManager;
import com.izforge.izpack.panels.TargetPanel;
import org.hamcrest.core.Is;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Integration test for panels
 */
public class PanelTest extends AbstractInstallationTest {
    @Test
    public void testInfoPanelResource() throws Exception {
        compileAndUnzip("basicInstall.xml", getWorkingDirectory("samples/basicInstall"));
        GUIInstallData installData = applicationContainer.getComponent(GUIInstallData.class);
        ResourceManager resourceManager = applicationContainer.getComponent(ResourceManager.class);
        String resNamePrifix = "InfoPanel.info";
        String info = resourceManager.getTextResource(resNamePrifix);
        assertThat(info, Is.is("A readme file ..."));
    }

    @Test
    public void testLicencePanelResource() throws Exception {
        compileAndUnzip("basicInstall.xml", getWorkingDirectory("samples/basicInstall"));
        GUIInstallData installData = applicationContainer.getComponent(GUIInstallData.class);
        ResourceManager resourceManager = applicationContainer.getComponent(ResourceManager.class);
        String resNamePrifix = "LicencePanel.licence";
        String info = resourceManager.getTextResource(resNamePrifix);
        assertThat(info, Is.is("(Consider it as a licence file ...)"));
    }

    @Test
    public void testTargetPanel() throws Exception {
        compileAndUnzip("basicInstall.xml", getWorkingDirectory("samples/basicInstall"));

        GUIInstallData installData = applicationContainer.getComponent(GUIInstallData.class);
        installerContainer = applicationContainer.getComponent(IInstallerContainer.class);
        PanelManager panelManager = installerContainer.getComponent(PanelManager.class);
        panelManager.loadPanelsInContainer();
        panelManager.instanciatePanels();

        TargetPanel targetPanel = installerContainer.getComponent(TargetPanel.class);
        targetPanel.loadDefaultDir();
        String defaultDir = installData.getInstallPath();
        System.out.println(installData.getInstallPath());

        File file = new File(defaultDir);
        if (file.exists()) {
            file.delete();
        }
    }
}
