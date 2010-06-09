package com.izforge.izpack.integration;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.event.InstallerListener;
import com.izforge.izpack.compiler.container.TestInstallationContainer;
import com.izforge.izpack.event.RegistryInstallerListener;
import com.izforge.izpack.event.SummaryLoggerInstallerListener;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.junit.PicoRunner;
import org.hamcrest.core.Is;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for event binding
 *
 * @author Anthonin Bonnefoy
 */

@RunWith(PicoRunner.class)
@Container(TestInstallationContainer.class)
public class EventTest
{
    private AutomatedInstallData automatedInstallData;

    public EventTest(AutomatedInstallData automatedInstallData)
    {
        this.automatedInstallData = automatedInstallData;
    }

    @Test
    @InstallFile("samples/event/event.xml")
    public void eventInitialization() throws Exception
    {
        List<InstallerListener> installerListeners = automatedInstallData.getInstallerListener();
        assertThat(installerListeners.size(), Is.is(2));
        assertThat(installerListeners.get(0), Is.is(SummaryLoggerInstallerListener.class));
        assertThat(installerListeners.get(1), Is.is(RegistryInstallerListener.class));
    }
}
