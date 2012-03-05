package com.izforge.izpack.compiler.container;

import com.izforge.izpack.installer.container.impl.ConsoleInstallerContainer;
import com.izforge.izpack.installer.container.impl.InstallerContainer;
import org.junit.runners.model.FrameworkMethod;
import org.picocontainer.MutablePicoContainer;

/**
 * Container for integration testing
 *
 * @author Anthonin Bonnefoy
 */
public class TestConsoleInstallationContainer extends AbstractTestInstallationContainer
{
    public TestConsoleInstallationContainer(Class klass, FrameworkMethod frameworkMethod)
    {
        super(klass, frameworkMethod);
    }

    @Override
    protected InstallerContainer createInstallerContainer()
    {
        return new ConsoleInstallerContainer();
    }
}
