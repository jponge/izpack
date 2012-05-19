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
        INFORMATION, QUESTION, WARNING, ERROR
    }

    /**
     * A prompt option.
     */
    enum Option
    {
        OK, YES, NO, CANCEL
    }

    /**
     * Predefined options.
     */
    enum Options
    {
        OK_CANCEL, YES_NO, YES_NO_CANCEL
    }

    /**
     * Displays a message.
     *
     * @param type    the type of the message
     * @param message the message to display
     */
    void message(Type type, String message);

    /**
     * Displays a message.
     *
     * @param type    the type of the message
     * @param title   the message title. If {@code null}, the title will be determined from the type
     * @param message the message to display
     */
    void message(Type type, String title, String message);

    /**
     * Displays a confirmation message.
     *
     * @param type    the type of the message
     * @param message the message
     * @param options the options which may be selected
     * @return the selected option
     */
    Option confirm(Type type, String message, Options options);

    /**
     * Displays a confirmation message.
     *
     * @param type          the type of the message
     * @param message       the message
     * @param options       the options which may be selected
     * @param defaultOption the default option to select
     * @return the selected option
     */
    Option confirm(Type type, String message, Options options, Option defaultOption);

    /**
     * Displays a confirmation message.
     *
     * @param type    the type of the message
     * @param title   the message title. May be {@code null}
     * @param message the message
     * @param options the options which may be selected
     * @return the selected option
     */
    Option confirm(Type type, String title, String message, Options options);

    /**
     * Displays a confirmation message.
     *
     * @param type          the type of the message
     * @param title         the message title. May be {@code null}
     * @param message       the message
     * @param options       the options which may be selected
     * @param defaultOption the default option to select
     * @return the selected option
     */
    Option confirm(Type type, String title, String message, Options options, Option defaultOption);

}