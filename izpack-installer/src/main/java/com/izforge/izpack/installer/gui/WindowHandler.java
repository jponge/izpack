package com.izforge.izpack.installer.gui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * The window events handler.
 *
 * @author julien created October 27, 2002
 */
class WindowHandler extends WindowAdapter
{
    private InstallerFrame installerFrame;

    public WindowHandler(InstallerFrame installerFrame)
    {
        this.installerFrame = installerFrame;
    }

    /**
     * Window close is pressed,
     *
     * @param e The event.
     */
    public void windowClosing(WindowEvent e)
    {
        // We ask for confirmation
        installerFrame.exit();
    }
}
