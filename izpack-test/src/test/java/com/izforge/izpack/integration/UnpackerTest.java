package com.izforge.izpack.integration;

import com.izforge.izpack.bootstrap.IInstallerContainer;
import com.izforge.izpack.installer.unpacker.IUnpacker;
import com.izforge.izpack.panels.PanelManager;
import org.hamcrest.core.IsNull;
import org.junit.Test;

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
