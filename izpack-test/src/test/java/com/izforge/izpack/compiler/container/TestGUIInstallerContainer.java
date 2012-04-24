package com.izforge.izpack.compiler.container;

import org.picocontainer.MutablePicoContainer;

import com.izforge.izpack.installer.container.impl.GUIInstallerContainer;
import com.izforge.izpack.test.util.TestHousekeeper;
import com.izforge.izpack.util.Housekeeper;


/**
 * Test installer container for GUI based installers.
 * <p/>
 * This returns a {@link TestHousekeeper} instead of an {@link Housekeeper}.
 *
 * @author Tim Anderson
 */
public class TestGUIInstallerContainer extends GUIInstallerContainer
{

    /**
     * Default constructor.
     */
    public TestGUIInstallerContainer()
    {
        super();
    }

    /**
     * Constructs a <tt>TestGUIInstallerContainer</tt>.
     *
     * @param container the container to use
     */
    public TestGUIInstallerContainer(MutablePicoContainer container)
    {
        super(container);
    }

    /**
     * Registers components with the container.
     *
     * @param pico the container
     */
    @Override
    protected void registerComponents(MutablePicoContainer pico)
    {
        super.registerComponents(pico);
        super.getContainer().removeComponent(Housekeeper.class);
        addComponent(TestHousekeeper.class);
    }
}
