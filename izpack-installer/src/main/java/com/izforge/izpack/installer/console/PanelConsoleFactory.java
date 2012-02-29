package com.izforge.izpack.installer.console;

import com.izforge.izpack.api.container.BindeableContainer;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.util.Debug;

/**
 * Factory for {@link PanelConsole} instances.
 *
 * @author Tim Anderson
 */
class PanelConsoleFactory
{
    /**
     * The container.
     */
    private final BindeableContainer container;

    /**
     * Constructs a <tt>PanelConsoleFactory</tt>.
     *
     * @param container the container
     */
    public PanelConsoleFactory(BindeableContainer container)
    {
        this.container = container;
    }

    /**
     * Attempts to create an {@link PanelConsole} corresponding to the specified panel.
     *
     * @param panel the panel
     * @return the corresponding {@link PanelConsole}
     * @throws InstallerException if there is no {@link PanelConsole} for the panel
     */
    public PanelConsole create(Panel panel) throws InstallerException
    {
        Class<PanelConsole> impl = getClass(panel);
        if (impl == null)
        {
            throw new InstallerException("No PanelConsole implementation exists for panel: " + panel.getClassName());
        }
        container.addComponent(impl);
        return container.getComponent(impl);
    }

    /**
     * Returns the PanelConsole class corresponding to the specified panel.
     *
     * @param panel the panel
     * @return the corresponding {@link PanelConsole} implementation class, or <tt>null</tt> if none is found
     */
    public Class<PanelConsole> getClass(Panel panel)
    {
        Class<PanelConsole> result = getClass(panel.getClassName() + "Console");
        if (result == null)
        {
            // use the old ConsoleHelper suffix convention
            result = getClass(panel.getClassName() + "ConsoleHelper");
        }
        return result;
    }


    /**
     * Returns the {@link PanelConsole} class for the specified class name.
     *
     * @param name the class name
     * @return the corresponding class, or <tt>null</tt> if it cannot be found or does not implement
     *         {@link PanelConsole}.
     */
    @SuppressWarnings("unchecked")
    private Class<PanelConsole> getClass(String name)
    {
        Class<PanelConsole> result = null;
        try
        {
            Class type = Class.forName(name);
            if (!PanelConsole.class.isAssignableFrom(type))
            {
                Debug.log(name + " does not implement " + PanelConsole.class.getName() + ", ignoring");
            }
            else
            {
                result = (Class<PanelConsole>) type;
            }
        }
        catch (ClassNotFoundException ignore)
        {
            Debug.log(ignore.getMessage());
        }
        return result;
    }

}
