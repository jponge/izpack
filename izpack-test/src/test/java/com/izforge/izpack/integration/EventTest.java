package com.izforge.izpack.integration;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.event.InstallerListener;
import com.izforge.izpack.compiler.container.TestInstallationContainer;
import com.izforge.izpack.data.CustomData;
import com.izforge.izpack.event.RegistryInstallerListener;
import com.izforge.izpack.event.SummaryLoggerInstallerListener;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.junit.PicoRunner;
import org.hamcrest.core.Is;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test for event binding.
 * 
 * @see com.izforge.izpack.installer.container.impl.EventFiller
 * @author Anthonin Bonnefoy
 */
@RunWith(PicoRunner.class)
@Container(TestInstallationContainer.class)
public class EventTest
{
    private final AutomatedInstallData automatedInstallData;
    
    private final UninstallData uninstallData;

    public EventTest(AutomatedInstallData automatedInstallData, UninstallData uninstallData)
    {
        this.automatedInstallData = automatedInstallData;
        this.uninstallData = uninstallData;
    }

    @Test
    @InstallFile("samples/event/event.xml")
    @SuppressWarnings("unchecked")
    public void eventInitialization() throws Exception
    {
        List<InstallerListener> installerListeners = automatedInstallData.getInstallerListener();
        assertThat(installerListeners.size(), Is.is(2));
        assertThat(installerListeners.get(0), Is.is(SummaryLoggerInstallerListener.class));
        assertThat(installerListeners.get(1), Is.is(RegistryInstallerListener.class));
        
        List<CustomData> uninstallListeners 
                = (List<CustomData>) uninstallData.getAdditionalData().get("uninstallerListeners");
        assertNotNull(uninstallListeners);
        assertThat(uninstallListeners.size(), Is.is(1));
        CustomData customData = uninstallListeners.get(0);
        assertEquals("RegistryUninstallerListener", customData.listenerName);
    }
}
