package com.izforge.izpack.uninstaller.gui;

import javax.swing.SwingUtilities;

import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.uninstaller.event.DestroyerHandler;

/**
 * The destroyer handler.
 *
 * @author Julien Ponge
 * @author Tino Schwarze
 */
public class GUIDestroyerHandler extends DestroyerHandler
{

    /**
     * The parent frame.
     */
    private UninstallerFrame uninstallerFrame;

    /**
     * The locale specific messages.
     */
    private final Messages messages;


    /**
     * Constructs a {@code GUIDestroyerHandler}.
     *
     * @param uninstallerFrame the parent frame
     * @param prompt           the prompt
     * @param messages         the locale-specific messages
     */
    public GUIDestroyerHandler(UninstallerFrame uninstallerFrame, Prompt prompt, Messages messages)
    {
        super(prompt);
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
     * Notify the user about something.
     * <p/>
     * This implementation is a no-op
     *
     * @param message the notification
     */
    @Override
    public void emitNotification(String message)
    {
        // do nothing
    }

    /**
     * Notify the user of some error.
     *
     * @param title   the message title (used for dialog name, might not be displayed)
     * @param message the error message
     */
    @Override
    public void emitError(String title, String message)
    {
        uninstallerFrame.progressBar.setString(message);
        super.emitError(title, message);
    }

}
