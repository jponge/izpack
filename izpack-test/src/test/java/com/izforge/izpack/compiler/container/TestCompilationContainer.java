package com.izforge.izpack.compiler.container;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.junit.runners.model.FrameworkMethod;
import org.picocontainer.MutablePicoContainer;

import com.izforge.izpack.api.exception.ContainerException;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.compiler.CompilerConfig;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.provider.JarFileProvider;
import com.izforge.izpack.util.ClassUtils;
import com.izforge.izpack.util.FileUtil;

/**
 * Container for compilation test
 * <p/>
 * TODO - replace with TestCompilerContainer from izpack-compiler
 *
 * @author Anthonin Bonnefoy
 */
public class TestCompilationContainer extends CompilerContainer
{

    public static final String APPNAME = "Test Installation";

    private Class<?> testClass;
    private FrameworkMethod frameworkMethod;

    public TestCompilationContainer(Class<?> testClass, FrameworkMethod frameworkMethod)
    {
        super(null);
        this.testClass = testClass;
        this.frameworkMethod = frameworkMethod;
        initialise();
    }


    public void fillContainer(MutablePicoContainer pico)
    {
        super.fillContainer(pico);
        try
        {
            deleteLock();
        }
        catch (IOException exception)
        {
            throw new ContainerException(exception);
        }
        InstallFile installFile = frameworkMethod.getAnnotation(InstallFile.class);
        if (installFile == null)
        {
            installFile = testClass.getAnnotation(InstallFile.class);
        }
        String installFileName = installFile.value();

        URL resource = getClass().getClassLoader().getResource(installFileName);
        if (resource == null)
        {
            throw new IllegalStateException("Cannot find install file: " + installFileName);
        }
        File installerFile = FileUtil.convertUrlToFile(resource);
        File baseDir = installerFile.getParentFile();

        File out = new File(baseDir, "out" + Math.random() + ".jar");
        out.deleteOnExit();
        CompilerData data = new CompilerData(installerFile.getAbsolutePath(), baseDir.getAbsolutePath(),
                                             out.getAbsolutePath(), false);
        pico.addConfig("installFile", installerFile.getAbsolutePath());
        pico.addComponent(CompilerData.class, data);
        pico.addComponent(File.class, out);
        pico.addAdapter(new JarFileProvider());
    }

    public void launchCompilation()
    {
        try
        {
            CompilerConfig compilerConfig = getComponent(CompilerConfig.class);
            File out = getComponent(File.class);
            compilerConfig.executeCompiler();
            ClassUtils.loadJarInSystemClassLoader(out);
        }
        catch (Exception e)
        {
            throw new IzPackException(e);
        }
    }

    private void deleteLock() throws IOException
    {
        File file = new File(System.getProperty("java.io.tmpdir"), "iz-" + APPNAME + ".tmp");
        FileUtils.deleteQuietly(file);
    }
}
