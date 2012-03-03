package com.izforge.izpack.installer.console;

import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.util.Console;

/**
 * Console implementation of {@link Prompt}.
 *
 * @author Tim Anderson
 */
public class ConsolePrompt implements Prompt
{
    /**
     * The console.
     */
    private final Console console;

    /**
     * Cosntructs a <tt>ConsolePrompt</tt>.
     *
     * @param console the console
     */
    public ConsolePrompt(Console console)
    {
        this.console = console;
    }

    /**
     * Displays a message.
     *
     * @param type    the type of the message
     * @param message the message to display
     */
    @Override
    public void message(Type type, String message)
    {
        console.println(message);
    }

    /**
     * Displays a confirmation message.
     *
     * @param type    the type of the message
     * @param message the message
     * @param options the options which may be selected
     * @return the selected option
     */
    @Override
    public Option confirm(Type type, String message, Options options)
    {
        Option result;
        console.println(message);
        if (options == Options.YES_NO_CANCEL)
        {
            String selected = console.prompt("Enter Y for Yes, N for No, or C to Cancel",
                    new String[]{"Y", "N", "C"}, "C");
            if ("Y".equals(selected))
            {
                result = Option.YES;
            }
            else if ("N".equals(selected))
            {
                result = Option.NO;
            }
            else
            {
                result = Option.CANCEL;
            }
        }
        else
        {
            String selected = console.prompt("Enter Y for Yes or N for No", new String[]{"Y", "N"}, "N");
            if ("Y".equals(selected))
            {
                result = Option.YES;
            }
            else
            {
                result = Option.NO;
            }
        }
        return result;
    }
}
