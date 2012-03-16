package com.izforge.izpack.installer.data;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.rules.Condition;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.compiler.container.TestInstallationContainer;
import com.izforge.izpack.matcher.ZipMatcher;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.junit.PicoRunner;
import com.izforge.izpack.util.IoHelper;

import org.hamcrest.core.IsNot;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link UninstallDataWriter}.
 *
 * @author Anthonin Bonnefoy
 * @author Tim Anderson
 */
@RunWith(PicoRunner.class)
@Container(TestInstallationContainer.class)
public class UninstallDataWriterTest
{
    /**
     * Temporary folder to perform installations to.
     */
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    /**
     * The uninstall jar writer.
     */
    private final UninstallDataWriter uninstallDataWriter;

    /**
     * Install data.
     */
    private final AutomatedInstallData installData;

    /**
     * Variable substitutor.
     */
    private final VariableSubstitutor variableSubstitutor;

    /**
     * The rules engine
     */
    private final RulesEngine rulesEngine;

    /**
     * Constructs an <tt>UninstallDataWriterTest</tt>.
     *
     * @param uninstallDataWriter the uninstall jar writer
     * @param variableSubstitutor the variable substitutor
     * @param installData         the install data
     * @param rulesEngine         the rules engine
     */
    public UninstallDataWriterTest(UninstallDataWriter uninstallDataWriter, VariableSubstitutor variableSubstitutor,
                                   AutomatedInstallData installData, RulesEngine rulesEngine)
    {
        this.uninstallDataWriter = uninstallDataWriter;
        this.variableSubstitutor = variableSubstitutor;
        this.installData = installData;
        this.rulesEngine = rulesEngine;
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp()
    {
        // write to temporary folder so the test doesn't need to be run with elevated permissions
        File installPath = new File(temporaryFolder.getRoot(), "izpackTest");
        installData.setInstallPath(installPath.getAbsolutePath());
    }

    /**
     * Cleans up after the test.
     */
    @After
    public void tearDown()
    {
        System.getProperties().remove("izpack.mode");
    }

    /**
     * Verifies that the uninstaller jar is written, and contains key classes and files.
     *
     * @throws IOException if the jar cannot be read
     */
    @Test
    @InstallFile("samples/basicInstall/basicInstall.xml")
    public void testWriteUninstaller() throws IOException
    {
        assertTrue(uninstallDataWriter.write());

        ZipFile uninstallJar = getUninstallerJar();

        assertThat(
                uninstallJar,
                ZipMatcher.isZipContainingFiles(
                        "com/izforge/izpack/uninstaller/Uninstaller.class",
                        "com/izforge/izpack/uninstaller/Destroyer.class",
                        "com/izforge/izpack/data/ExecutableFile.class",
                        "langpack.xml",
                        "META-INF/MANIFEST.MF",
                        "com/izforge/izpack/gui/IconsDatabase.class",
                        "com/izforge/izpack/img/trash.png"));

        // basicInstall.xml doesn't reference any listeners, so the com/izforge/izpack/event package shouldn't have
        // been written. Verify that one of the listeners in the package doesn't appear
        assertThat(uninstallJar,
                IsNot.not(ZipMatcher.isZipContainingFiles(
                        "com/izforge/izpack/event/RegistryUninstallerListener.class")));
    }

    /**
     * Verifies that standard listeners are written.
     * @throws IOException
     * @throws ZipException
     */
    @Test
    @InstallFile("samples/event/event.xml")
    public void testWriteStandardListener() throws IOException
    {
        assertTrue(uninstallDataWriter.write());

        ZipFile uninstallJar = getUninstallerJar();

        assertThat(uninstallJar,
                ZipMatcher.isZipContainingFile("com/izforge/izpack/event/RegistryUninstallerListener.class"));
    }

    /**
     * Verifies that custom listeners are written.
     */
    @Test
    @InstallFile("samples/event/customlisteners.xml")
    public void testWriteCustomListener() throws IOException
    {
        assertTrue(uninstallDataWriter.write());

        ZipFile uninstallJar = getUninstallerJar();

        assertThat(uninstallJar,
                ZipMatcher.isZipContainingFiles("com/izforge/izpack/test/listener/TestUninstallerListener.class",
                        "com/izforge/izpack/api/event/UninstallerListener.class",
                        "com/izforge/izpack/event/SimpleInstallerListener.class"));
    }

    /**
     * Verifies that native libraries are written to the uninstaller.
     */
    @Test
    @InstallFile("samples/natives/natives.xml")
    public void testWriteNatives() throws IOException
    {
        assertTrue(uninstallDataWriter.write());

        ZipFile uninstallJar = getUninstallerJar();

        assertThat(uninstallJar,
                ZipMatcher.isZipContainingFiles("com/izforge/izpack/bin/native/WinSetupAPI.dll",
                        "com/izforge/izpack/bin/native/WinSetupAPI_x64.dll",
                        "com/izforge/izpack/bin/native/COIOSHelper.dll",
                        "com/izforge/izpack/bin/native/COIOSHelper_x64.dll"));

        // verify that the native libs with stage="install" aren't in the uninstaller
        assertThat(uninstallJar,
                IsNot.not(ZipMatcher.isZipContainingFiles("com/izforge/izpack/bin/native/ShellLink.dll",
                        "com/izforge/izpack/bin/native/ShellLink.dll")));
    }

    /**
     * Verifies that the <em>com.coi.tools.os</em> packages are written if the OS is Windows.
     * <p/>
     * Strictly speaking these are only required if {@link com.izforge.izpack.event.RegistryUninstallerListener}
     * is used, but for now just right them out for all windows installations.
     */
    @Test
    @InstallFile("samples/natives/natives.xml")
    public void testWriteWindowsRegistrySupport() throws IOException
    {
        addOSCondition("izpack.windowsinstall");
        installData.getInfo().setRequirePrivilegedExecutionUninstaller(true);
        assertTrue(uninstallDataWriter.write());

        ZipFile uninstallJar = getUninstallerJar();

        assertThat(uninstallJar,
                ZipMatcher.isZipContainingFiles("com/izforge/izpack/core/os/RegistryHandler.class",
                        "com/coi/tools/os/izpack/Registry.class",
                        "com/coi/tools/os/win/RegistryImpl.class",
                        "com/izforge/izpack/installer/elevate.js"));
    }

    /**
     * Verifies that the <em>run-with-privileges-on-osx</em> script is written for mac installs.
     * @throws IOException
     * @throws
     */
    @Test
    @InstallFile("samples/basicInstall/basicInstall.xml")
    public void testRunWithPrivilegesOnOSX() throws IOException
    {
        System.setProperty("izpack.mode", "privileged");
        installData.getInfo().setRequirePrivilegedExecutionUninstaller(true);
        addOSCondition("izpack.macinstall");
        assertTrue(uninstallDataWriter.write());

        ZipFile uninstallJar = getUninstallerJar();

        assertThat(uninstallJar,
                ZipMatcher.isZipContainingFiles("com/izforge/izpack/installer/run-with-privileges-on-osx"));

    }

    private void addOSCondition(final String ruleId)
    {
        Map<String, Condition> rules = new HashMap<String, Condition>();
        rules.put(ruleId, new Condition()
        {
            {
                setId(ruleId);
            }

            public void readFromXML(IXMLElement condition)
            {
            }

            @Override
            public boolean isTrue()
            {
                return true;
            }

            @Override
            public void makeXMLData(IXMLElement conditionRoot)
            {
            }
        });
        rulesEngine.readConditionMap(rules); // use this as it doesn't check for rules being registered already
    }

    /**
     * Returns the uninstaller jar file.
     *
     * @return the uninstaller jar file
     * @throws IOException
     * @throws
     */
    private ZipFile getUninstallerJar() throws IOException
    {
        String dir = IoHelper.translatePath(installData.getInfo().getUninstallerPath(), variableSubstitutor);
        String path = dir + File.separator + installData.getInfo().getUninstallerName();
        File jar = new File(path);
        assertThat(jar.exists(), is(true));
        return new ZipFile(jar);
    }
}
