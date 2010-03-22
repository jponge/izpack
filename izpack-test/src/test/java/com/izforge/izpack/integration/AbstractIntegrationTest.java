package com.izforge.izpack.integration;

import com.izforge.izpack.api.container.BindeableContainer;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.compiler.CompilerConfig;
import com.izforge.izpack.compiler.container.CompilerContainer;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.installer.base.InstallerFrame;
import com.izforge.izpack.installer.container.impl.InstallerContainer;
import com.izforge.izpack.installer.language.LanguageDialog;
import com.izforge.izpack.test.ClassUtils;
import org.apache.commons.io.FileUtils;
import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.fixture.FrameFixture;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.io.IOException;

/**
 * Abstract test for integration test
 */
public class AbstractIntegrationTest
{

    public static final String APPNAME = "Test Installation";

    protected BindeableContainer applicationContainer;
    protected CompilerContainer compilerContainer;
    protected ResourceManager resourceManager;

    protected FrameFixture installerFrameFixture;
    protected DialogFixture dialogFrameFixture;
    protected File out;

    @BeforeMethod
    public void initBinding() throws Throwable
    {
        out = File.createTempFile("izpack", ".jar");

        compilerContainer = new CompilerContainer();
        compilerContainer.initBindings();
        deleteLock();
    }

    public void initInstallerContainer() throws Exception
    {
        applicationContainer = new InstallerContainer();
        applicationContainer.initBindings();
        resourceManager = applicationContainer.getComponent(ResourceManager.class);
    }

    private void deleteLock() throws IOException
    {
        File file = new File(System.getProperty("java.io.tmpdir"), "iz-" + APPNAME + ".tmp");
        FileUtils.deleteQuietly(file);
    }

    /**
     * Prepare fest fixture for installer frame
     *
     * @throws Exception
     */
    protected FrameFixture prepareFrameFixture() throws Exception
    {
        InstallerFrame installerFrame = applicationContainer.getComponent(InstallerFrame.class);
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
    protected DialogFixture prepareDialogFixture()
    {
        LanguageDialog languageDialog = applicationContainer.getComponent(LanguageDialog.class);
        DialogFixture dialogFixture = new DialogFixture(languageDialog);
        dialogFixture.show();
        return dialogFixture;
    }

    /**
     * Compile an installer jar.
     *
     * @param installationFile The izpack installation file
     * @param workingDirectory
     * @throws Exception
     */
    protected void compileInstallJar(String installationFile, File workingDirectory) throws Exception
    {
        File installerFile = new File(workingDirectory, installationFile);
        compileInstallJar(new CompilerData(installerFile.getAbsolutePath(), workingDirectory.getAbsolutePath(), out.getAbsolutePath()));
    }

    protected File getWorkingDirectory(String workingDirectoryName)
    {
        return new File(ClassLoader.getSystemClassLoader().getResource(workingDirectoryName).getFile());
    }

    /**
     * Compile an installer
     *
     * @param compilerData
     * @throws Exception
     */
    public void compileInstallJar(CompilerData compilerData) throws Exception
    {
        CompilerData data = compilerData;
        compilerContainer.addConfig("installFile", compilerData.getInstallFile());
        compilerContainer.addComponent(CompilerData.class, data);
        CompilerConfig compilerConfig = compilerContainer.getComponent(CompilerConfig.class);
        compilerConfig.executeCompiler();
        ClassUtils.loadJarInSystemClassLoader(out);
        initInstallerContainer();
    }

    @AfterMethod
    public void unloadJarInSystemClassLoader() throws Exception
    {
        ClassUtils.unloadLastJar();
    }

}
