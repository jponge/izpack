package com.izforge.izpack.installer.multiunpacker;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.handler.AbstractUIProgressHandler;
import com.izforge.izpack.installer.unpacker.IMultiVolumeUnpackerHelper;


public class MultiVolumeUnpackerAutomationHelper implements IMultiVolumeUnpackerHelper
{
    /**
     * The installation data.
     */
    private final AutomatedInstallData installData;

    /**
     * The progress handler.
     */
    private final AbstractUIProgressHandler handler;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(MultiVolumeUnpackerAutomationHelper.class.getName());

    /**
     * Constructs a <tt>MultiVolumeUnpackerAutomationHelper</tt>.
     *
     * @param installData the installation data
     * @param handler     the progress handler
     */
    public MultiVolumeUnpackerAutomationHelper(AutomatedInstallData installData, AbstractUIProgressHandler handler)
    {
        this.installData = installData;
        this.handler = handler;
    }


    public File enterNextMediaMessage(String volumename, boolean lastcorrupt)
    {
        if (lastcorrupt)
        {
            System.err.println(" [ " + installData.getLangpack().getString("nextmedia.corruptmedia.title") + " ] ");
            System.err.println(installData.getLangpack().getString("nextmedia.corruptmedia"));
            System.err.println(installData.getLangpack().getString("nextmedia.corruptmedia"));
        }
        logger.fine("Enter next media: " + volumename);

        File nextvolume = new File(volumename);

        while (!nextvolume.exists() || lastcorrupt)
        {
            System.out.println(" [ " + installData.getLangpack().getString("nextmedia.title") + " ] ");
            System.out.println(installData.getLangpack().getString("nextmedia.msg"));
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            String nextmediainput = null;
            try
            {
                nextmediainput = reader.readLine();
            }
            catch (IOException e)
            {
                logger.log(Level.WARNING, "Error reading next media path: " + e.getMessage(), e);
                e.printStackTrace();
            }

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
