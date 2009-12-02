package com.izforge.izpack.integration;

import com.izforge.izpack.bootstrap.IPanelContainer;
import com.izforge.izpack.installer.unpacker.IUnpacker;
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
        panelContainer = applicationComponent.getComponent(IPanelContainer.class);
        IUnpacker unpacker = panelContainer.getComponent(IUnpacker.class);
        assertThat(unpacker, IsNull.notNullValue());
    }
}
