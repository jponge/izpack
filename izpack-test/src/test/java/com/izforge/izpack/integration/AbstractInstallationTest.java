package com.izforge.izpack.integration;

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
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import sun.misc.URLClassPath;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

/**
 * Abstract test for integration test
 */
public class AbstractInstallationTest {

    protected static final String APPNAME = "Test Installation";

    protected IApplicationContainer applicationContainer;
    protected IInstallerContainer installerContainer;
    protected CompilerContainer compilerContainer;
    protected ResourceManager resourceManager;

    protected FrameFixture installerFrameFixture;
    protected DialogFixture dialogFrameFixture;
    protected File out;

    @BeforeMethod
    public void initBinding() throws Throwable {
        out = File.createTempFile("izpack", "jar");
        applicationContainer = new ApplicationContainer();
        applicationContainer.initBindings();
        compilerContainer = new CompilerContainer();
        compilerContainer.initBindings();
        resourceManager = applicationContainer.getComponent(ResourceManager.class);
        deleteLock();
    }

    private void deleteLock() throws IOException {
        File file = new File(System.getProperty("java.io.tmpdir"), "iz-" + LanguageSelectionTest.APPNAME + ".tmp");
        FileUtils.deleteQuietly(file);
    }

    @AfterMethod
    public void deleteTempJar() throws IOException {
//        FileUtils.forceDelete(out);
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
     * Compile an installer jar.
     *
     * @param installationFile The izpack installation file
     * @param workingDirectory
     * @throws Exception
     */
    protected void compileInstallJar(String installationFile, File workingDirectory) throws Exception {
        File installerFile = new File(workingDirectory, installationFile);
        compileInstallJar(new CompilerData(installerFile.getAbsolutePath(), workingDirectory.getAbsolutePath(), out.getAbsolutePath()));
    }

    protected File getWorkingDirectory(String workingDirectoryName) {
        return new File(ClassLoader.getSystemClassLoader().getResource(workingDirectoryName).getFile());
    }

    /**
     * Compile an installer
     *
     * @param compilerData
     * @throws Exception
     */
    public void compileInstallJar(CompilerData compilerData) throws Exception {
        CompilerData data = compilerData;
        compilerContainer.addComponent(CompilerData.class, data);
        CompilerConfig compilerConfig = compilerContainer.getComponent(CompilerConfig.class);
        compilerConfig.executeCompiler();
        loadJarInSystemClassLoader(out);
    }

    @AfterMethod
    public void unloadJarInSystemClassLoader() throws Exception {
        URLClassLoader systemClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Field ucpField = URLClassLoader.class.getDeclaredField("ucp");
        ucpField.setAccessible(true);
        URLClassPath ucp = (URLClassPath) ucpField.get(systemClassLoader);
        Field pathField = URLClassPath.class.getDeclaredField("path");
        pathField.setAccessible(true);
        ArrayList<URL> path = (ArrayList) pathField.get(ucp);
        path.remove(path.size() - 1);

        Field loaderField = URLClassPath.class.getDeclaredField("loaders");
        loaderField.setAccessible(true);
        ArrayList loaders = (ArrayList) loaderField.get(ucp);
        loaders.remove(loaders.size() - 1);
    }

    private void loadJarInSystemClassLoader(File out) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, MalformedURLException {
        URLClassLoader systemClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();

        Method declaredMethod = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
        declaredMethod.setAccessible(true);
        declaredMethod.invoke(systemClassLoader, out.toURI().toURL());
    }

}
