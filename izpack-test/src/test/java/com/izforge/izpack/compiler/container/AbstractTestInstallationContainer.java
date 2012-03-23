package com.izforge.izpack.compiler.container;

import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.installer.container.impl.InstallerContainer;
import com.izforge.izpack.test.junit.UnloadJarRule;
import org.junit.Rule;
import org.junit.runners.model.FrameworkMethod;
import org.picocontainer.MutablePicoContainer;

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
    public void fillContainer(MutablePicoContainer picoContainer)
    {
        TestCompilationContainer testInstallationContainer = new TestCompilationContainer(klass, frameworkMethod);
        testInstallationContainer.initBindings();
        testInstallationContainer.launchCompilation();

        fillInstallerContainer(picoContainer);
    }

    protected abstract InstallerContainer fillInstallerContainer(MutablePicoContainer picoContainer);
}
