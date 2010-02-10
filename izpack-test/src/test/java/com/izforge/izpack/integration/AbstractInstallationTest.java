package com.izforge.izpack.integration;

import com.izforge.izpack.AssertionHelper;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.compiler.CompilerConfig;
import com.izforge.izpack.compiler.container.CompilerContainer;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.installer.base.InstallerFrame;
import com.izforge.izpack.installer.container.ApplicationContainer;
import com.izforge.izpack.installer.container.IApplicationContainer;
import com.izforge.izpack.installer.container.IInstallerContainer;
import com.izforge.izpack.installer.language.LanguageDialog;
import org.apache.commons.io.FileUtils;
import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.fixture.FrameFixture;
import org.hamcrest.core.IsNull;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.MethodRule;
import org.junit.rules.Timeout;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Abstract test for integration test
 */
public class AbstractInstallationTest {
    private File currentDir = new File(getClass().getClassLoader().getResource(".").getFile());

    protected static final String APPNAME = "Test Installation";

    @Rule
    public MethodRule globalTimeout = new Timeout(20000);
    protected IApplicationContainer applicationContainer;
    protected IInstallerContainer installerContainer;
    protected CompilerContainer compilerContainer;
    protected ResourceManager resourceManager;

    protected FrameFixture installerFrameFixture;
    protected DialogFixture dialogFrameFixture;

    @Before
    public void initBinding() throws Throwable {
        applicationContainer = new ApplicationContainer();
        applicationContainer.initBindings();
        compilerContainer = new CompilerContainer();
        compilerContainer.initBindings();
        resourceManager = applicationContainer.getComponent(ResourceManager.class);
    }

    @Before
    public void deleteLock() {
        File file = new File(System.getProperty("java.io.tmpdir"), "iz-" + LanguageSelectionTest.APPNAME + ".tmp");
        file.delete();
    }

    /**
     * Prepare fest fixture for installer frame
     *
     * @throws Exception
     */
    protected FrameFixture prepareFrameFixture() throws Exception {
        InstallerFrame installerFrame = installerContainer.getComponent(InstallerFrame.class);
        FrameFixture installerFrameFixture = new FrameFixture(installerFrame);
        installerFrame.loadPanels();
        installerFrameFixture.show();
        installerFrame.sizeFrame();
        // wait center
        Thread.sleep(100);
        return installerFrameFixture;
    }

    /**
     * Prepare fest fixture for lang selection
     */
    protected DialogFixture prepareDialogFixture() {
        LanguageDialog languageDialog = installerContainer.getComponent(LanguageDialog.class);
        DialogFixture dialogFixture = new DialogFixture(languageDialog);
        dialogFixture.show();
        return dialogFixture;
    }

    /**
     * Compile an installer and unzip the created jar.
     *
     * @param installationFile The izpack installation file
     * @param workingDirectory
     * @throws Exception
     */
    protected void compileAndUnzip(String installationFile, File workingDirectory) throws Exception {
        File installerFile = new File(workingDirectory, installationFile);
        File out = new File(workingDirectory, "out.jar");
        compileAndUnzip(workingDirectory, out, new CompilerData(installerFile.getAbsolutePath(), workingDirectory.getAbsolutePath(), out.getAbsolutePath()));
    }

    /**
     * Compile an installer and unzip the created jar.
     *
     * @param workingDirectory
     * @param compilerData
     * @throws Exception
     */
    protected void compileAndUnzip(File workingDirectory, CompilerData compilerData) throws Exception {
        File out = new File(workingDirectory, "out.jar");
        compileAndUnzip(workingDirectory, out, compilerData);
    }

    protected File getWorkingDirectory(String workingDirectoryName) {
        File workingDirectory = new File(getClass().getClassLoader().getResource(workingDirectoryName).getFile());
        setResourcePath(workingDirectory);
        return workingDirectory;
    }

    /**
     * Compile an installer and unzip it.
     *
     * @param workingDirectory The directory containing the installer file
     * @param out              The output of the compiler
     * @param compilerData
     * @throws Exception
     */
    private void compileAndUnzip(File workingDirectory, File out, CompilerData compilerData) throws Exception {
        CompilerData data = compilerData;
        compilerContainer.addComponent(CompilerData.class, data);
        CompilerConfig compilerConfig = compilerContainer.getComponent(CompilerConfig.class);
        compilerConfig.executeCompiler();
        URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{out.toURI().toURL()}, ClassLoader.getSystemClassLoader());
        assertThat(urlClassLoader.getResource("resources/langpacks/eng.xml"), IsNull.<Object>notNullValue());

        URLClassLoader systemClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Method declaredMethod = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
        declaredMethod.setAccessible(true);
        declaredMethod.invoke(systemClassLoader, out.toURI().toURL());
        URL urlEnumeration = systemClassLoader.getResource("resources/langpacks/eng.xml");

        assertThat(urlEnumeration, IsNull.<Object>notNullValue());
        File extractedDir = new File(workingDirectory, "temp");
        // Clean before use
        FileUtils.deleteDirectory(extractedDir);
        extractedDir.mkdirs();
        AssertionHelper.unzipJar(out, extractedDir);
    }

    /**
     * Set resource path in resource manager
     *
     * @param baseDir Base path where unzip has been operated
     */
    protected void setResourcePath(File baseDir) {
        String relativePath = baseDir.getAbsolutePath().substring(currentDir.getAbsolutePath().length());
        System.out.println(relativePath);
        ResourceManager resourceManager = applicationContainer.getComponent(ResourceManager.class);
        assertThat(resourceManager, IsNull.notNullValue());
        resourceManager.setResourceBasePath(relativePath + "/temp/resources/");
    }
}
