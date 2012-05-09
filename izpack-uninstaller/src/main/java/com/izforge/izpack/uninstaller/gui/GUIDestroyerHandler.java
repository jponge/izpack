package com.izforge.izpack.uninstaller.gui;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.izforge.izpack.api.data.LocaleDatabase;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.api.handler.AbstractUIProgressHandler;

/**
 * The destroyer handler.
 *
 * @author Julien Ponge
 * @author Tino Schwarze
 */
public class GUIDestroyerHandler implements AbstractUIProgressHandler
{

    private UninstallerFrame uninstallerFrame;

    private final LocaleDatabase messages;

    public GUIDestroyerHandler(UninstallerFrame uninstallerFrame, LocaleDatabase messages)
    {
        this.uninstallerFrame = uninstallerFrame;
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
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                uninstallerFrame.progressBar.setMinimum(0);
                uninstallerFrame.progressBar.setMaximum(max);
                uninstallerFrame.blockGUI();
            }
        });
    }

    /**
     * The destroyer stops.
     */
    public void stopAction()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                uninstallerFrame.progressBar.setString(messages.get("InstallPanel.finished"));
                uninstallerFrame.targetDestroyCheckbox.setEnabled(false);
                uninstallerFrame.destroyButton.setEnabled(false);
                uninstallerFrame.releaseGUI();
            }
        });
    }

    /**
     * The destroyer progresses.
     *
     * @param pos     The actual position.
     * @param message The message.
     */
    public void progress(final int pos, final String message)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                uninstallerFrame.progressBar.setValue(pos);
                uninstallerFrame.progressBar.setString(message);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void nextStep(String step_name, int step_no, int no_of_substeps)
    {
        // not used
    }

    /**
     * {@inheritDoc}
     */
    public void setSubStepNo(int no_of_substeps)
    {
        // not used
    }

    /**
     * Output a notification.
     * <p/>
     * Does nothing here.
     *
     * @param text
     */
    public void emitNotification(String text)
    {
    }

    /**
     * Output a warning.
     *
     * @param text
     */
    public boolean emitWarning(String title, String text)
    {
        return (JOptionPane.showConfirmDialog(null, text, title, JOptionPane.OK_CANCEL_OPTION,
                                              JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION);
    }

    /**
     * The destroyer encountered an error.
     *
     * @param error The error message.
     */
    public void emitError(String title, String error)
    {
        uninstallerFrame.progressBar.setString(error);
        JOptionPane.showMessageDialog(null, error, title, JOptionPane.OK_CANCEL_OPTION);
    }

    /**
     * The destroyer encountered an error.
     *
     * @param error The error message.
     */
    public void emitErrorAndBlockNext(String title, String error)
    {
        emitError(title, error);
    }

    /**
     * Ask the user a question.
     *
     * @param title    Message title.
     * @param question The question.
     * @param choices  The set of choices to present.
     * @return The user's choice.
     * @see com.izforge.izpack.api.handler.AbstractUIHandler#askQuestion(String, String, int)
     */
    public int askQuestion(String title, String question, int choices)
    {
        return askQuestion(title, question, choices, -1);
    }

    /**
     * Ask the user a question.
     *
     * @param title          Message title.
     * @param question       The question.
     * @param choices        The set of choices to present.
     * @param default_choice The default choice. (-1 = no default choice)
     * @return The user's choice.
     * @see com.izforge.izpack.api.handler.AbstractUIHandler#askQuestion(String, String, int, int)
     */
    public int askQuestion(String title, String question, int choices, int default_choice)
    {
        int jo_choices = 0;

        if (choices == AbstractUIHandler.CHOICES_YES_NO)
        {
            jo_choices = JOptionPane.YES_NO_OPTION;
        }
        else if (choices == AbstractUIHandler.CHOICES_YES_NO_CANCEL)
        {
            jo_choices = JOptionPane.YES_NO_CANCEL_OPTION;
        }

        int user_choice = JOptionPane.showConfirmDialog(null, question, title,
                                                        jo_choices, JOptionPane.QUESTION_MESSAGE);

        if (user_choice == JOptionPane.CANCEL_OPTION)
        {
            return AbstractUIHandler.ANSWER_CANCEL;
        }

        if (user_choice == JOptionPane.YES_OPTION)
        {
            return AbstractUIHandler.ANSWER_YES;
        }

        if (user_choice == JOptionPane.NO_OPTION)
        {
            return AbstractUIHandler.ANSWER_NO;
        }

        return default_choice;
    }

}
