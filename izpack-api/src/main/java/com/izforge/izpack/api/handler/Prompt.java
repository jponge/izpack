package com.izforge.izpack.api.handler;

/**
 * Prompts users for a value or informs them of something.
 *
 * @author Tim Anderson
 */
public interface Prompt
{
    /**
     * The prompt type.
     */
    enum Type
    {
        WARNING, ERROR
    }

    /**
     * A prompt option.
     */
    enum Option
    {
        YES, NO, CANCEL
    }

    /**
     * Predefined options.
     */
    enum Options
    {
        YES_NO, YES_NO_CANCEL
    }

    /**
     * Displays a message.
     *
     * @param type    the type of the message
     * @param message the message to display
     */
    void message(Type type, String message);

    /**
     * Displays a confirmation message.
     *
     * @param type    the type of the message
     * @param message the message
     * @param options the options which may be selected
     * @return the selected option
     */
    Option confirm(Type type, String message, Options options);

}