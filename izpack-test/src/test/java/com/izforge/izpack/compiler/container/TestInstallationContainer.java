package com.izforge.izpack.compiler.container;

import com.izforge.izpack.installer.container.impl.GUIInstallerContainer;
import com.izforge.izpack.installer.container.impl.InstallerContainer;
import com.izforge.izpack.test.util.TestHousekeeper;
import com.izforge.izpack.util.Housekeeper;
import org.junit.runners.model.FrameworkMethod;
import org.picocontainer.MutablePicoContainer;

/**
 * Container for integration testing
 *
 * @author Anthonin Bonnefoy
 */
public class TestInstallationContainer extends AbstractTestInstallationContainer
{

    public TestInstallationContainer(Class klass, FrameworkMethod frameworkMethod)
    {
        super(klass, frameworkMethod);
        initialise();
    }

    @Override
    protected InstallerContainer fillInstallerContainer(MutablePicoContainer container)
    {
        return new GUIInstallerContainer(container) {
            @Override
            protected void registerComponents(MutablePicoContainer pico)
            {
                super.registerComponents(pico);
                super.getContainer().removeComponent(Housekeeper.class);
                addComponent(TestHousekeeper.class);
            }
        };
    }

}
