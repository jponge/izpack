package com.izforge.izpack.installer.data;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.compiler.container.TestInstallationContainer;
import com.izforge.izpack.matcher.ZipMatcher;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.junit.PicoRunner;
import com.izforge.izpack.util.IoHelper;
import org.hamcrest.core.Is;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test of unpacker
 */
@RunWith(PicoRunner.class)
@Container(TestInstallationContainer.class)
public class UninstallWriteTest
{
    private UninstallDataWriter uninstallDataWriter;
    private AutomatedInstallData idata;
    private VariableSubstitutor variableSubstitutor;

    public UninstallWriteTest(UninstallDataWriter uninstallDataWriter, VariableSubstitutor variableSubstitutor, AutomatedInstallData idata)
    {
        this.uninstallDataWriter = uninstallDataWriter;
        this.variableSubstitutor = variableSubstitutor;
        this.idata = idata;
    }

    @Test
    @InstallFile("samples/basicInstall/basicInstall.xml")
    public void testWriteUninstaller() throws Exception
    {
        uninstallDataWriter.write();

        String dest = IoHelper.translatePath(idata.getInfo().getUninstallerPath(), variableSubstitutor);
        String jar = dest + File.separator + idata.getInfo().getUninstallerName();
        File uninstallJar = new File(jar);
        assertThat(uninstallJar.exists(), Is.is(true));
        assertThat(uninstallJar, ZipMatcher.isZipContainingFiles("com/izforge/izpack/uninstaller/Destroyer.class",
                "langpack.xml", "META-INF/MANIFEST.MF", " com/izforge/izpack/gui/IconsDatabase.class"));

    }
}
