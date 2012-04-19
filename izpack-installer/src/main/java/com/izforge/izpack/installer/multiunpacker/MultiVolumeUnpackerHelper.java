package com.izforge.izpack.installer.multiunpacker;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.handler.AbstractUIProgressHandler;
import com.izforge.izpack.core.io.VolumeLocator;
import com.izforge.izpack.installer.base.InstallerFrame;
import com.izforge.izpack.installer.base.IzPanel;

public class MultiVolumeUnpackerHelper implements VolumeLocator
{

    /**
     * The installation data.
     */
    private AutomatedInstallData installData;

    /**
     * The progress handler.
     */
    private AbstractUIProgressHandler handler;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(MultiVolumeUnpackerHelper.class.getName());

    /**
     * Constructs a <tt>MultiVolumeUnpackerHelper</tt>.
     *
     * @param installData the installation data
     * @param handler     the progress handler
     */
    public MultiVolumeUnpackerHelper(AutomatedInstallData installData, AbstractUIProgressHandler handler)
    {
        this.installData = installData;
        this.handler = handler;
    }

    /**
     * Returns the next volume.
     *
     * @param path    the expected volume path
     * @param corrupt if <tt>true</tt> the previous attempt detected a corrupt or invalid volume
     * @return the next volume
     * @throws java.io.IOException if the volume cannot be found
     */
    @Override
    public File getVolume(String path, boolean corrupt) throws IOException
    {
        if (corrupt)
        {
            Component parent = null;
            if ((this.handler != null) && (this.handler instanceof IzPanel))
            {
                parent = ((IzPanel) this.handler).getInstallerFrame();
            }
            JOptionPane.showMessageDialog(parent, installData.getLangpack()
                    .getString("nextmedia.corruptmedia"), installData.getLangpack()
                    .getString("nextmedia.corruptmedia.title"), JOptionPane.ERROR_MESSAGE);
        }
        logger.fine("Enter next media: " + path);

        File volume = new File(path);
        NextMediaDialog nextMediaDialog;

        while (!volume.exists() || corrupt)
        {
            if (handler instanceof IzPanel)
            {
                InstallerFrame installframe = ((IzPanel) this.handler).getInstallerFrame();
                nextMediaDialog = new NextMediaDialog(installframe, installData, path);
            }
            else
            {
                nextMediaDialog = new NextMediaDialog(null, installData, path);
            }
            nextMediaDialog.setVisible(true);
            String nextmediainput = nextMediaDialog.getNextMedia();
            if (nextmediainput != null)
            {
                volume = new File(nextmediainput);
            }
            else
            {
                logger.fine("Input from NextMediaDialog was null");
                volume = new File(path);
            }
            // selection equal to last selected which was corrupt?
            if (!(path.equals(volume.getAbsolutePath()) && corrupt))
            {
                corrupt = false;
            }
        }
        return volume;
    }

}
