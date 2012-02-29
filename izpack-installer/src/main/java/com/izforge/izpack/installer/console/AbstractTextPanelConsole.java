package com.izforge.izpack.installer.console;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.util.Debug;

import java.util.Properties;
import java.util.StringTokenizer;

/**
 * Abstract console panel for displaying paginated text.
 *
 * @author Tim Anderson
 */
public abstract class AbstractTextPanelConsole extends AbstractPanelConsole
{
    /**
     * Runs the panel using the supplied properties.
     *
     * @param installData the installation data
     * @param properties  the properties
     * @return <tt>true</tt>
     */
    @Override
    public boolean runConsoleFromProperties(AutomatedInstallData installData, Properties properties)
    {
        return true;
    }

    /**
     * Runs the panel using the specified console.
     * <p/>
     * If there is no text to display, the panel will return <tt>false</tt>.
     *
     * @param installData the installation data
     * @param console     the console
     * @return <tt>true</tt> if the panel ran successfully, otherwise <tt>false</tt>
     */
    @Override
    public boolean runConsole(AutomatedInstallData installData, Console console)
    {
        boolean result;
        String text = getText();
        if (text != null)
        {
            result = paginateText(text, console);
        }
        else
        {
            Debug.error("No text to display");
            result = false;
        }
        return result && promptEndPanel(installData, console);
    }

    /**
     * Returns the text to display.
     *
     * @return the text. A <tt>null</tt> indicates failure
     */
    protected abstract String getText();

    /**
     * Pages through the supplied text.
     *
     * @param text    the text to display
     * @param console the console to display to
     * @return <tt>true</tt> if paginated through, <tt>false</tt> if terminated
     */
    protected boolean paginateText(String text, Console console)
    {
        boolean result = true;
        int lines = 22; // the no. of lines to display at a time
        int line = 0;

        StringTokenizer tokens = new StringTokenizer(text, "\n");
        while (tokens.hasMoreTokens())
        {
            String token = tokens.nextToken();
            console.println(token);
            line++;
            if (line >= lines && tokens.hasMoreTokens())
            {
                if (!promptContinue(console))
                {
                    result = false;
                    break;
                }
                line = 0;
            }
        }
        return result;
    }

    /**
     * Displays a prompt to continue, providing the option to terminate installation.
     *
     * @param console the console to perform I/O
     * @return <tt>true</tt> if the installation should continue, <tt>false</tt> if it should terminate
     */
    protected boolean promptContinue(Console console)
    {
        String value = prompt(console, "\nPress Enter to continue, X to exit", "x");
        console.println();
        return !value.equalsIgnoreCase("x");
    }

}
