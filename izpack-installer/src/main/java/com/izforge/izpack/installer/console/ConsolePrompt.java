package com.izforge.izpack.installer.console;

import com.izforge.izpack.installer.console.Console;
import com.izforge.izpack.installer.requirement.Prompt;

public class ConsolePrompt implements Prompt
{
    private final Console console;

    public ConsolePrompt(Console console)
    {
        this.console = console;
    }

    @Override
    public void message(Type type, String message)
    {
        console.println(message);
    }

    @Override
    public Option confirm(Type type, String message, Options options)
    {
        Option result;
        console.println(message);
        if (options == Options.YES_NO_CANCEL)
        {
            String selected = console.prompt("Enter Y for Yes, N for No, or C to Cancel", new String[]{"Y", "N", "C"}, "C");
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
        else if (options == Options.YES_NO)
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
        else
        {
            String selected = console.prompt("Enter O for OK or C for Cancel", new String[]{"O", "C"}, "C");
            if ("O".equals(selected))
            {
                result = Option.YES;
            }
            else
            {
                result = Option.CANCEL;
            }
        }
        return result;
    }
}
