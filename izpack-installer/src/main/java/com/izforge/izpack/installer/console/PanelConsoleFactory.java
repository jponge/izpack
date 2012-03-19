package com.izforge.izpack.installer.console;

import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.factory.ObjectFactory;

import java.util.logging.Logger;


/**
 * Factory for {@link PanelConsole} instances.
 *
 * @author Tim Anderson
 */
class PanelConsoleFactory
{
    /**
     * The factory to delegate to.
     */
    private final ObjectFactory factory;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(PanelConsoleFactory.class.getName());


    /**
     * Constructs a <tt>PanelConsoleFactory</tt>.
     *
     * @param factory the factory to delegate to
     */
    public PanelConsoleFactory(ObjectFactory factory)
    {
        this.factory = factory;
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
        return factory.create(impl);
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
                logger.warning(name + " does not implement " + PanelConsole.class.getName() + ", ignoring");
            }
            else
            {
                result = (Class<PanelConsole>) type;
            }
        }
        catch (ClassNotFoundException e)
        {
            // Ignore
            logger.fine("No PanelConsole found for class " + name + ": " + e.toString());
        }
        return result;
    }

}
