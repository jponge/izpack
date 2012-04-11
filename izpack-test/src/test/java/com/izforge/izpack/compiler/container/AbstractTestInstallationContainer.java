package com.izforge.izpack.compiler.container;

import java.util.jar.JarFile;

import org.junit.Rule;
import org.junit.runners.model.FrameworkMethod;
import org.picocontainer.MutablePicoContainer;

import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.installer.container.impl.InstallerContainer;
import com.izforge.izpack.test.junit.UnloadJarRule;

/**
 * Abstract implementation of a container for testing purposes.
 *
 * @author Anthonin Bonnefoy
 * @author Tim Anderson
 */
public abstract class AbstractTestInstallationContainer extends AbstractContainer
{
    protected Class klass;
    protected FrameworkMethod frameworkMethod;
    @Rule
    public UnloadJarRule unloadJarRule = new UnloadJarRule();

    public AbstractTestInstallationContainer(Class klass, FrameworkMethod frameworkMethod)
    {
        this.klass = klass;
        this.frameworkMethod = frameworkMethod;
    }

    @Override
    protected void fillContainer(MutablePicoContainer picoContainer)
    {
        TestCompilationContainer compiler = new TestCompilationContainer(klass, frameworkMethod);
        compiler.launchCompilation();

        // propagate compilation objects to the installer container so the installation test can use them
        CompilerData data = compiler.getComponent(CompilerData.class);
        JarFile installer = compiler.getComponent(JarFile.class);
        picoContainer.addComponent(data);
        picoContainer.addComponent(installer);

        fillInstallerContainer(picoContainer);
    }

    protected abstract InstallerContainer fillInstallerContainer(MutablePicoContainer picoContainer);
}
