package com.izforge.izpack.installer.multiunpacker;

import java.awt.Component;
import java.io.File;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.handler.AbstractUIProgressHandler;
import com.izforge.izpack.installer.base.InstallerFrame;
import com.izforge.izpack.installer.base.IzPanel;
import com.izforge.izpack.installer.unpacker.IMultiVolumeUnpackerHelper;

public class MultiVolumeUnpackerHelper implements IMultiVolumeUnpackerHelper
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

    public File enterNextMediaMessage(String volumename, boolean lastcorrupt)
    {
        if (lastcorrupt)
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
        logger.fine("Enter next media: " + volumename);

        File nextvolume = new File(volumename);
        NextMediaDialog nextMediaDialog = null;

        while (!nextvolume.exists() || lastcorrupt)
        {
            if (handler instanceof IzPanel)
            {
                InstallerFrame installframe = ((IzPanel) this.handler).getInstallerFrame();
                nextMediaDialog = new NextMediaDialog(installframe, installData, volumename);
            }
            else
            {
                nextMediaDialog = new NextMediaDialog(null, installData, volumename);
            }
            nextMediaDialog.setVisible(true);
            String nextmediainput = nextMediaDialog.getNextMedia();
            if (nextmediainput != null)
            {
                nextvolume = new File(nextmediainput);
            }
            else
            {
                logger.fine("Input from NextMediaDialog was null");
                nextvolume = new File(volumename);
            }
            // selection equal to last selected which was corrupt?
            if (!(volumename.equals(nextvolume.getAbsolutePath()) && lastcorrupt))
            {
                lastcorrupt = false;
            }
        }
        return nextvolume;
    }

    public File enterNextMediaMessage(String volumename)
    {
        return enterNextMediaMessage(volumename, false);
    }

}
