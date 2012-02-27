package com.izforge.izpack.installer.console;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.util.Debug;

import java.io.IOException;


/**
 * Abstract implementation of the {@link PanelConsole} interface.
 *
 * @author Tim Anderson
 */
public abstract class AbstractPanelConsole implements PanelConsole
{

    /**
     * Runs the panel in interactive console mode.
     *
     * @param installData the installation data
     */
    @Override
    public boolean runConsole(AutomatedInstallData installData)
    {
        return runConsole(installData, new Console());
    }

    /**
     * Displays a prompt and waits for numeric input.
     *
     * @param console the console to use
     * @param prompt  the prompt to display
     * @param min     the minimum allowed value
     * @param max     the maximum allowed value
     * @param eof     the value to return if end of stream is reached
     * @return a value in the range of <tt>from..to</tt>, or <tt>eof</tt> if the end of stream is reached
     */
    protected int prompt(Console console, String prompt, int min, int max, int eof)
    {
        int result = 0;
        try
        {
            do
            {
                console.println(prompt);
                String value = console.readLine();
                if (value != null)
                {
                    try
                    {
                        result = Integer.valueOf(value);
                    }
                    catch (NumberFormatException ignore)
                    {
                        // loop round to try again
                    }
                }
                else
                {
                    // end of stream
                    result = eof;
                    break;
                }
            }
            while (result < min || result > max);
        }
        catch (IOException exception)
        {
            Debug.log(exception);
            result = eof;
        }
        return result;
    }

    /**
     * Displays a prompt and waits for input.
     *
     * @param console the console to use
     * @param prompt  the prompt to display
     * @param eof     the value to return if end of stream is reached
     * @return the input value or <tt>eof</tt> if the end of stream is reached
     */
    protected String prompt(Console console, String prompt, String eof)
    {
        String result;
        try
        {
            console.print(prompt);
            result = console.readLine();
            if (result == null)
            {
                result = eof;
            }
        }
        catch (IOException exception)
        {
            result = eof;
            Debug.log(exception);
        }
        return result;
    }

    /**
     * Prompts to end the console panel.
     * <p/>
     * This displays a prompt to continue, quit, or redisplay. On redisplay, it invokes
     * {@link #runConsole(AutomatedInstallData, Console)}.
     *
     * @param installData the installation date
     * @param console     the console to use
     * @return <tt>true</tt> to continue, <tt>false</tt> to quit. If redisplaying the panel, the result of
     *         {@link #runConsole(AutomatedInstallData, Console)} is returned
     */
    protected boolean promptEndPanel(AutomatedInstallData installData, Console console)
    {
        boolean result;
        int value = prompt(console, "press 1 to continue, 2 to quit, 3 to redisplay", 1, 3, 2);
        result = value == 1 || value != 2 && runConsole(installData, console);
        return result;
    }

}
