package com.izforge.izpack.compiler.container;

import org.picocontainer.MutablePicoContainer;

import com.izforge.izpack.installer.console.ConsoleInstaller;
import com.izforge.izpack.installer.console.TestConsoleInstaller;
import com.izforge.izpack.installer.container.impl.ConsoleInstallerContainer;
import com.izforge.izpack.test.util.TestConsole;
import com.izforge.izpack.test.util.TestHousekeeper;
import com.izforge.izpack.util.Console;
import com.izforge.izpack.util.Housekeeper;


/**
 * Test installer container for console based installers.
 * <p/>
 * This returns:
 * <ul>
 * <li>a {@link TestConsoleInstaller} instead of a {@link ConsoleInstaller}</li>
 * <li>a {@link TestConsole} instead of a {@link Console}</li>
 * <li>a {@link TestHousekeeper} instead of a {@link Housekeeper}</li>
 * </ul>
 *
 * @author Tim Anderson
 */
public class TestConsoleInstallerContainer extends ConsoleInstallerContainer
{

    public TestConsoleInstallerContainer()
    {
    }

    public TestConsoleInstallerContainer(MutablePicoContainer container)
    {
        super(container);
    }

    /**
     * Registers components with the container.
     *
     * @param container the container
     */
    @Override
    protected void registerComponents(MutablePicoContainer container)
    {
        super.registerComponents(container);
        container.removeComponent(ConsoleInstaller.class);
        container.addComponent(TestConsoleInstaller.class);
        container.removeComponent(Console.class);
        container.addComponent(TestConsole.class);
        container.removeComponent(Housekeeper.class);
        container.addComponent(TestHousekeeper.class);
    }
}
