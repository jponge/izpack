package com.izforge.izpack.uninstaller.console;

import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.uninstaller.event.DestroyerHandler;
import com.izforge.izpack.util.Console;

/**
 * The console destroyer handler.
 */
public class ConsoleDestroyerHandler extends DestroyerHandler
{

    /**
     * The console.
     */
    private final Console console;

    /**
     * The locale-specific messages.
     */
    private final Messages messages;


    /**
     * Constructs a {@code ConsoleDestroyerHandler}.
     *
     * @param console  the console
     * @param prompt   the prompt
     * @param messages the locale-specific messages
     */
    public ConsoleDestroyerHandler(Console console, Prompt prompt, Messages messages)
    {
        super(prompt);
        this.console = console;
        this.messages = messages;
    }

    /**
     * The destroyer starts.
     *
     * @param name The name of the overall action. Not used here.
     * @param max  The maximum value of the progress.
     */
    public void startAction(final String name, final int max)
    {
        console.print("Processing " + name);
    }

    /**
     * The destroyer stops.
     */
    public void stopAction()
    {
        console.print(messages.get("InstallPanel.finished"));
    }

    /**
     * The destroyer progresses.
     *
     * @param subStepNo The actual position.
     * @param message   The message.
     */
    public void progress(final int subStepNo, final String message)
    {
        console.print(message);
    }

    /**
     * Notify the user about something.
     * <p/>
     * The difference between notification and warning is that a notification should not need user
     * interaction and can safely be ignored.
     *
     * @param message the notification
     */
    @Override
    public void emitNotification(String message)
    {
        console.println(message);
    }

}
