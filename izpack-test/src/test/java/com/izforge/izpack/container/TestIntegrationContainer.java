package com.izforge.izpack.container;

import com.izforge.izpack.compiler.container.TestCompilerContainer;
import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.installer.container.impl.InstallerContainer;
import org.picocontainer.MutablePicoContainer;

/**
 * Container for integration testing
 *
 * @author Anthonin Bonnefoy
 */
public class TestIntegrationContainer extends AbstractContainer
{
    private Class klass;

    public TestIntegrationContainer(Class klass)
    {
        this.klass = klass;
    }

    public void fillContainer(MutablePicoContainer picoContainer) throws Exception
    {
        TestCompilerContainer compilerContainer = new TestCompilerContainer(klass);
        compilerContainer.fillContainer(picoContainer);

        InstallerContainer installerContainer = new InstallerContainer();
        installerContainer.fillContainer(picoContainer);
    }

}
