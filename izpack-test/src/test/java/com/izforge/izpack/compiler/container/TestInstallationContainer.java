package com.izforge.izpack.compiler.container;

import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.installer.container.impl.InstallerContainer;
import com.izforge.izpack.test.junit.UnloadJarRule;
import org.junit.Rule;
import org.junit.runners.model.FrameworkMethod;
import org.picocontainer.MutablePicoContainer;

/**
 * Container for integration testing
 *
 * @author Anthonin Bonnefoy
 */
public class TestInstallationContainer extends AbstractContainer
{
    private Class klass;
    private FrameworkMethod frameworkMethod;

    @Rule
    public UnloadJarRule unloadJarRule = new UnloadJarRule();

    public TestInstallationContainer(Class klass, FrameworkMethod frameworkMethod)
    {
        this.klass = klass;
        this.frameworkMethod = frameworkMethod;
    }

    public void fillContainer(MutablePicoContainer pico)
    {
        try
        {
            TestCompilationContainer testInstallationContainer = new TestCompilationContainer(klass, frameworkMethod);
            testInstallationContainer.initBindings();
            testInstallationContainer.launchCompilation();
            InstallerContainer installerContainer = new InstallerContainer();
            installerContainer.fillContainer(pico);
        }
        catch (Exception e)
        {
            throw new IzPackException(e);
        }
    }

}
