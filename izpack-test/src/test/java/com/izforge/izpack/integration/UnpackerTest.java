package com.izforge.izpack.integration;

import com.izforge.izpack.installer.container.IInstallerContainer;
import com.izforge.izpack.installer.manager.PanelManager;
import com.izforge.izpack.installer.unpacker.IUnpacker;
import org.hamcrest.core.IsNull;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test of unpacker
 */
public class UnpackerTest extends AbstractInstallationTest {

    @Test
    public void testInstanciateUnpacker() throws Exception {
        compileAndUnzip("basicInstall.xml", getWorkingDirectory("samples/basicInstall"));
        installerContainer = applicationContainer.getComponent(IInstallerContainer.class);
        PanelManager panelManager = installerContainer.getComponent(PanelManager.class);
        panelManager.loadPanelsInContainer().instanciatePanels();
        IUnpacker unpacker = installerContainer.getComponent(IUnpacker.class);
        assertThat(unpacker, IsNull.notNullValue());
    }
}
