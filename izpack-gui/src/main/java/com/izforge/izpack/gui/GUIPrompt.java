package com.izforge.izpack.gui;

import com.izforge.izpack.api.handler.Prompt;

import javax.swing.*;


/**
 * Displays a dialog prompting users for a value or informs them of something.
 *
 * @author Tim Anderson
 */
public class GUIPrompt implements Prompt
{

    /**
     * Displays a message.
     *
     * @param type    the type of the message
     * @param message the message to display
     */
    @Override
    public void message(Type type, String message)
    {
        String title = getTitle(type);
        JOptionPane.showMessageDialog(null, message, title, getMessageType(type));
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
        int messageType = getMessageType(type);
        int optionType;
        switch (options)
        {
            case YES_NO_CANCEL:
                optionType = JOptionPane.YES_NO_CANCEL_OPTION;
                break;
            default:
                optionType = JOptionPane.YES_NO_OPTION;
                break;
        }
        String title = getTitle(type);
        int selected = JOptionPane.showConfirmDialog(null, message, title, optionType, messageType);
        switch (selected)
        {
            case JOptionPane.YES_OPTION:
                result = Option.YES;
                break;
            case JOptionPane.NO_OPTION:
                result = Option.NO;
                break;
            case JOptionPane.CANCEL_OPTION:
                result = Option.CANCEL;
                break;
            default:
                result = (options == Options.YES_NO_CANCEL) ? Option.CANCEL : Option.NO;
        }
        return result;
    }

    /**
     * Returns the dialog title based on the type of the message.
     *
     * @param type the message type
     * @return the title
     */
    private String getTitle(Type type)
    {
        return (type == Type.ERROR) ? "Error" : "Warning";
    }

    /**
     * Maps a {@link Type} to a JOptionPane message type.
     *
     * @param type the message type
     * @return the JOptionPane equivalent
     */
    private int getMessageType(Type type)
    {
        return (type == Type.ERROR) ? JOptionPane.ERROR_MESSAGE : JOptionPane.WARNING_MESSAGE;
    }
}
