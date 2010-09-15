package com.izforge.izpack.compiler.container;

import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.compiler.CompilerConfig;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.test.ClassUtils;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.util.FileUtil;
import org.apache.commons.io.FileUtils;
import org.junit.runners.model.FrameworkMethod;
import org.picocontainer.MutablePicoContainer;

import java.io.File;
import java.io.IOException;

/**
 * Container for compilation test
 *
 * @author Anthonin Bonnefoy
 */
public class TestCompilationContainer extends AbstractContainer
{

    public static final String APPNAME = "Test Installation";

    private Class klass;
    private FrameworkMethod frameworkMethod;

    public TestCompilationContainer(Class klass, FrameworkMethod frameworkMethod)
    {
        this.klass = klass;
        this.frameworkMethod = frameworkMethod;
    }

    public void fillContainer(MutablePicoContainer pico)
    {
        try
        {
            deleteLock();
            CompilerContainer compilerContainer = new CompilerContainer();
            compilerContainer.fillContainer(pico);
            InstallFile installFile = frameworkMethod.getAnnotation(InstallFile.class);
            if (installFile == null)
            {
                installFile = (InstallFile) klass.getAnnotation(InstallFile.class);
            }
            String installFileName = installFile.value();

            File installerFile = FileUtil.convertUrlToFile(getClass().getClassLoader().getResource(installFileName));
            File baseDir = installerFile.getParentFile();

            File out = new File(baseDir, "out" + Math.random() + ".jar");
            out.deleteOnExit();
            CompilerData data = new CompilerData(installerFile.getAbsolutePath(), baseDir.getAbsolutePath(), out.getAbsolutePath());
            pico.addConfig("installFile", installerFile.getAbsolutePath());
            pico.addComponent(CompilerData.class, data);
            pico.addComponent(File.class, out);
            pico.addComponent(this);
        }
        catch (Exception e)
        {
            throw new IzPackException(e);
        }
    }

    public void launchCompilation()
    {
        try
        {
            CompilerConfig compilerConfig = pico.getComponent(CompilerConfig.class);
            File out = pico.getComponent(File.class);
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
