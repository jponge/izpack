package com.izforge.izpack.installer.data;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.container.TestIntegrationContainer;
import com.izforge.izpack.integration.AbstractIntegrationTest;
import com.izforge.izpack.matcher.ZipMatcher;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.junit.PicoRunner;
import com.izforge.izpack.util.IoHelper;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsNull;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test of unpacker
 */
@RunWith(PicoRunner.class)
@Container(TestIntegrationContainer.class)
@InstallFile("samples/basicInstall/basicInstall.xml")
public class UninstallWriteTest extends AbstractIntegrationTest
{
    private UninstallDataWriter uninstallDataWriter;

    public UninstallWriteTest(UninstallDataWriter uninstallDataWriter)
    {
        this.uninstallDataWriter = uninstallDataWriter;
    }

    @Test
    public void testWriteUninstaller() throws Exception
    {
//        panelManager.loadPanelsInContainer().instanciatePanels();
        assertThat(uninstallDataWriter, IsNull.notNullValue());

        uninstallDataWriter.write();

        AutomatedInstallData idata = applicationContainer.getComponent(AutomatedInstallData.class);
        VariableSubstitutor variableSubstitutor = applicationContainer.getComponent(VariableSubstitutor.class);
        String dest = IoHelper.translatePath(idata.getInfo().getUninstallerPath(), variableSubstitutor);
        String jar = dest + File.separator + idata.getInfo().getUninstallerName();
        File uninstallJar = new File(jar);
        assertThat(uninstallJar.exists(), Is.is(true));
        assertThat(uninstallJar, ZipMatcher.isZipContainingFiles("com/izforge/izpack/uninstaller/Destroyer.class", "langpack.xml"));

    }
}
