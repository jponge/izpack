package com.izforge.izpack.container;

import com.izforge.izpack.compiler.CompilerConfig;
import com.izforge.izpack.compiler.container.CompilerContainer;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.installer.container.impl.InstallerContainer;
import com.izforge.izpack.test.ClassUtils;
import com.izforge.izpack.test.InstallFile;
import org.apache.commons.io.FileUtils;
import org.junit.runners.model.FrameworkMethod;
import org.picocontainer.MutablePicoContainer;

import java.io.File;
import java.io.IOException;

/**
 * Container for integration testing
 *
 * @author Anthonin Bonnefoy
 */
public class TestIntegrationContainer extends AbstractContainer
{
    private Class klass;
    private FrameworkMethod frameworkMethod;

    public static final String APPNAME = "Test Installation";

    public TestIntegrationContainer(Class klass, FrameworkMethod frameworkMethod)
    {
        this.klass = klass;
        this.frameworkMethod = frameworkMethod;
    }

    public void fillContainer(MutablePicoContainer pico) throws Exception
    {
        launchCompilation();
        InstallerContainer installerContainer = new InstallerContainer();
        installerContainer.fillContainer(pico);
    }

    private void launchCompilation() throws Exception
    {
        deleteLock();
        CompilerContainer compilerContainer = new CompilerContainer();
        compilerContainer.initBindings();

        InstallFile installFile = (InstallFile) klass.getAnnotation(InstallFile.class);
        if (installFile == null)
        {
            installFile = ((InstallFile) frameworkMethod.getAnnotation(InstallFile.class));
        }
        String installFileName = installFile.value();

        File installerFile = new File(getClass().getClassLoader().getResource(installFileName).getFile());
        File baseDir = installerFile.getParentFile();

        File out = new File(baseDir, "out.jar");
        out.delete();
        CompilerData data = new CompilerData(installerFile.getAbsolutePath(), baseDir.getAbsolutePath(), out.getAbsolutePath());
        compilerContainer.addConfig("installFile", installerFile.getAbsolutePath());
        compilerContainer.addComponent(CompilerData.class, data);
        compilerContainer.addComponent(File.class, out);
        CompilerConfig compilerConfig = compilerContainer.getComponent(CompilerConfig.class);
        compilerConfig.executeCompiler();
        ClassUtils.loadJarInSystemClassLoader(out);
    }

    private void deleteLock() throws IOException
    {
        File file = new File(System.getProperty("java.io.tmpdir"), "iz-" + APPNAME + ".tmp");
        FileUtils.deleteQuietly(file);
    }

}
