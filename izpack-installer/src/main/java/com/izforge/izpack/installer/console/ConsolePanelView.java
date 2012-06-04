package com.izforge.izpack.installer.console;

import java.util.logging.Logger;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.core.handler.ConsolePrompt;
import com.izforge.izpack.core.handler.PromptUIHandler;
import com.izforge.izpack.installer.panel.PanelView;
import com.izforge.izpack.util.Console;


/**
 * Implementation of {@link PanelView} for {@link PanelConsole}s.
 *
 * @author Tim Anderson
 */
public class ConsolePanelView extends PanelView<PanelConsole>
{

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(ConsolePanelView.class.getName());

    /**
     * The console.
     */
    private final Console console;

    /**
     * The prompt.
     */
    private final ConsolePrompt prompt;


    /**
     * Constructs a {@code ConsolePanelView}.
     *
     * @param panel       the panel
     * @param factory     the factory for creating the view
     * @param installData the installation data
     */
    public ConsolePanelView(Panel panel, ObjectFactory factory, AutomatedInstallData installData, Console console)
    {
        super(panel, PanelConsole.class, factory, installData);
        this.console = console;
        this.prompt = new ConsolePrompt(console);
    }

    /**
     * Returns a handler to prompt the user.
     *
     * @return the handler
     */
    @Override
    protected AbstractUIHandler getHandler()
    {
        return new PromptUIHandler(prompt)
        {
            @Override
            public void emitNotification(String message)
            {
                console.println(message);
            }
        };
    }

    /**
     * Returns the PanelConsole class corresponding to the panel's class name
     *
     * @return the corresponding {@link PanelConsole} implementation class, or {@code null} if none is found
     */
    public Class<PanelConsole> getViewClass()
    {
        Panel panel = getPanel();
        Class<PanelConsole> result = getClass(panel.getClassName() + "Console");
        if (result == null)
        {
            // use the old ConsoleHelper suffix convention
            result = getClass(panel.getClassName() + "ConsoleHelper");
        }
        return result;
    }

    /**
     * Creates a new view.
     *
     * @param panel     the panel to create the view for
     * @param viewClass the view base class
     * @return the new view
     */
    @Override
    protected PanelConsole createView(Panel panel, Class<PanelConsole> viewClass)
    {
        Class<PanelConsole> impl = getViewClass();
        if (impl == null)
        {
            throw new IzPackException("Console implementation not found for panel: " + panel.getClassName());
        }
        return getFactory().create(impl);
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
