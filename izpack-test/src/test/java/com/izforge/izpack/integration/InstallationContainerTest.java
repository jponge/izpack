package com.izforge.izpack.integration;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.compiler.container.TestInstallationContainer;
import com.izforge.izpack.installer.base.InstallerController;
import com.izforge.izpack.installer.container.impl.InstallerContainer;
import com.izforge.izpack.panels.hello.HelloPanel;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.junit.PicoRunner;
import org.hamcrest.core.Is;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Anthonin Bonnefoy
 */
@RunWith(PicoRunner.class)
@Container(TestInstallationContainer.class)
public class InstallationContainerTest
{
    private InstallerContainer installerContainer;
    private AutomatedInstallData installData;
    private InstallerController installerController;


    public InstallationContainerTest(InstallerContainer installerContainer, AutomatedInstallData installData, InstallerController installerController)
    {

        this.installerContainer = installerContainer;
        this.installData = installData;
        this.installerController = installerController;
    }

    @Test
    @InstallFile("samples/doublePanel.xml")
    public void testMultiplePanels() throws Exception
    {
        installerController.preloadInstaller().buildInstallation();

        HelloPanel firstHelloPanel = (HelloPanel) installerContainer.getComponent("42");
        assertThat(firstHelloPanel.getMetadata().getPanelid(), Is.is("42"));

        HelloPanel secondHelloPanel = (HelloPanel) installerContainer.getComponent("34");
        assertThat(secondHelloPanel.getMetadata().getPanelid(), Is.is("34"));
    }
}
