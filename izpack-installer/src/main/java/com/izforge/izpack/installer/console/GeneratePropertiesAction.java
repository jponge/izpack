package com.izforge.izpack.installer.console;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.installer.DataValidator;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.util.Console;
import com.izforge.izpack.util.file.FileUtils;

/**
 * Action to generate properties for each panel.
 *
 * @author Tim Anderson
 */
class GeneratePropertiesAction extends ConsoleAction
{
    /**
     * The writer to write properties to.
     */
    private final PrintWriter writer;

    /**
     * Constructs a <tt>GeneratePropertiesAction</tt>.
     *
     * @param factory       the panel console factory
     * @param installData   the installation data
     * @param objectFactory the factory for {@link DataValidator} instances
     * @param rules         the rules engine
     * @param path          the path to write properties to
     * @throws FileNotFoundException if the file exists but is a directory rather than a regular file, does not exist
     *                               but cannot be created, or cannot be opened for any other reason
     */
    public GeneratePropertiesAction(PanelConsoleFactory factory, AutomatedInstallData installData,
                                    ObjectFactory objectFactory, RulesEngine rules, String path)
            throws FileNotFoundException
    {
        super(factory, installData, objectFactory, rules);
        writer = new PrintWriter(new FileOutputStream(path), true);
    }

    /**
     * Determines if this is an installation action.
     *
     * @return <tt>false</tt>
     */
    @Override
    public boolean isInstall()
    {
        return false;
    }

    /**
     * Runs the action for each panel.
     *
     * @param console the console
     * @return <tt>true</tt> if the action was successful, otherwise <tt>false</tt>
     */
    @Override
    public boolean run(Console console)
    {
        boolean result = super.run(console);
        FileUtils.close(writer);
        return result;
    }

    /**
     * Runs the action for the console panel associated with the specified panel.
     *
     * @param panel        the panel
     * @param panelConsole the console implementation of the panel
     * @param console      the console
     * @return <tt>true</tt> if the action was successful, otherwise <tt>false</tt>
     * @throws InstallerException for any installer error
     */
    @Override
    protected boolean run(Panel panel, PanelConsole panelConsole, Console console) throws InstallerException
    {
        boolean result = panelConsole.runGeneratePropertiesFile(getInstallData(), writer);
        if (result)
        {
            result = validatePanel(panel, console);
        }
        return result;
    }
}
