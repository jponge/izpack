package com.izforge.izpack.integration;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.handler.AbstractUIProgressHandler;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.compiler.container.TestInstallationContainer;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.matcher.ZipMatcher;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.junit.PicoRunner;
import com.izforge.izpack.test.listener.TestUninstallerListener;
import com.izforge.izpack.uninstaller.Destroyer;
import com.izforge.izpack.util.IoHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Tests that {@link com.izforge.izpack.api.event.UninstallerListener}s are invoked by the {@link Destroyer}.
 *
 * @author Tim Anderson
 */
@RunWith(PicoRunner.class)
@Container(TestInstallationContainer.class)
public class UninstallerListenerTest
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
     * Uninstall data.
     */
    private final UninstallData uninstallData;

    /**
     * Variable substitutor.
     */
    private final VariableSubstitutor variableSubstitutor;


    /**
     * Constructs an <tt>UninstallerListenerTest</tt>.
     *
     * @param uninstallDataWriter the uninstall jar writer
     * @param variableSubstitutor the variable substitutor
     * @param installData         the install data
     * @param uninstallData       the uninstall data
     */
    public UninstallerListenerTest(UninstallDataWriter uninstallDataWriter, VariableSubstitutor variableSubstitutor,
                                   AutomatedInstallData installData, UninstallData uninstallData)
    {
        this.uninstallDataWriter = uninstallDataWriter;
        this.variableSubstitutor = variableSubstitutor;
        this.installData = installData;
        this.uninstallData = uninstallData;
    }

    /**
     * Sets up the test case.
     *
     * @throws IOException if the install directory cannot be created
     */
    @Before
    public void setUp() throws IOException
    {
        // write to temporary folder so the test doesn't need to be run with elevated permissions
        File installPath = new File(temporaryFolder.getRoot(), "izpackTest");
        assertTrue(installPath.mkdirs());
        installData.setInstallPath(installPath.getAbsolutePath());

        // clean up any existing state file
        removeState();
    }

    /**
     * Cleans up after the test.
     */
    @After
    public void tearDown()
    {
        removeState();
    }

    /**
     * Verifies that the uninstaller jar is written, and contains key classes and files.
     *
     * @throws java.io.IOException if the jar cannot be read
     */
    @Test
    @InstallFile("samples/event/customlisteners.xml")
    public void testUninstallerListenerInvocation() throws Exception
    {
        String installPath = installData.getInstallPath();

        // add some files to the installation.
        int files = 3;
        for (int i = 0; i < files; ++i)
        {
            File file = new File(installPath, "file" + i);
            assertTrue(file.createNewFile());
            uninstallData.addFile(file.getAbsolutePath(), true);
        }

        // write the uninstaller and verify it contains the listeners
        assertTrue(uninstallDataWriter.write());

        File uninstallJar = getUninstallerJar();

        assertThat(uninstallJar, ZipMatcher.isZipContainingFiles(
                "com/izforge/izpack/api/event/UninstallerListener.class",
                "com/izforge/izpack/test/listener/TestUninstallerListener.class"));

        // perform uninstallation.
        runDestroyer(uninstallJar);

        // verify the listener methods have been invoked the expected no. of times
        String path = TestUninstallerListener.getStatePath(installPath);
        TestUninstallerListener.State state = TestUninstallerListener.readState(path);
        assertEquals(1, state.beforeDeletionCount);
        assertEquals(files + 1, state.beforeDeleteCount); // 3 files + 1 for the uninstaller jar

        assertEquals(state.beforeDeletionCount, state.afterDeletionCount);
        assertEquals(state.beforeDeleteCount, state.afterDeleteCount);
    }

    /**
     * Runs the {@link Destroyer} in the supplied uninstall jar.
     *
     * @param uninstallJar the uninstaller jar
     * @throws Exception for any error
     */
    private void runDestroyer(File uninstallJar) throws Exception
    {
        String installPath = installData.getInstallPath();

        // create an isolated class loader, as the Destroyer locates resources using its class loader
        URLClassLoader loader = new URLClassLoader(new URL[]{uninstallJar.toURI().toURL()}, null);
        Class destroyerClass = loader.loadClass(Destroyer.class.getName());
        Class<?> handlerClass = loader.loadClass(AbstractUIProgressHandler.class.getName());
        Constructor constructor = destroyerClass.getConstructors()[0];

        // create the Destroyer
        Object destroyer = constructor.newInstance(installPath, true, Mockito.mock(handlerClass));

        // and run it
        Method method = destroyerClass.getMethod("run");
        method.invoke(destroyer);
    }

    /**
     * Returns the uninstaller jar file.
     *
     * @return the uninstaller jar file
     */
    private File getUninstallerJar()
    {
        String dir = IoHelper.translatePath(installData.getInfo().getUninstallerPath(), variableSubstitutor);
        String path = dir + File.separator + installData.getInfo().getUninstallerName();
        File jar = new File(path);
        assertThat(jar.exists(), is(true));
        return jar;
    }

    /**
     * Removes any {@link TestUninstallerListener.State} file.
     */
    private void removeState()
    {
        String path = TestUninstallerListener.getStatePath(installData.getInstallPath());
        File file = new File(path);
        if (file.exists())
        {
            assertTrue(file.delete());
        }
    }

}