package com.izforge.izpack.installer.requirement;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.installer.RequirementChecker;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.FileUtil;

import java.io.File;

import static com.izforge.izpack.api.handler.Prompt.Option;

/**
 * Determines if another installation is in progress, by checking for the existence of a lock file.
 *
 * @author Tim Anderson
 */
public class LockFileChecker implements RequirementChecker
{

    /**
     * The installation data.
     */
    private final AutomatedInstallData installData;

    /**
     * The prompt.
     */
    private final Prompt prompt;

    /**
     * Constructs a <tt>LockFileChecker</tt>.
     *
     * @param installData the installation data
     * @param prompt      the prompt
     */
    public LockFileChecker(AutomatedInstallData installData, Prompt prompt)
    {
        this.installData = installData;
        this.prompt = prompt;
    }

    /**
     * Determines if installation requirements are met.
     *
     * @return <tt>true</tt> if requirements are met, otherwise <tt>false</tt>
     */
    @Override
    public boolean check()
    {
        boolean result;
        String appName = installData.getInfo().getAppName();
        File file = FileUtil.getLockFile(appName);
        if (file.exists())
        {
            result = lockFileExists(file);
        }
        else
        {
            try
            {
                // Create the new lock file
                if (file.createNewFile())
                {
                    Debug.trace("Created lock file:" + file.getPath());
                    file.deleteOnExit();
                }
                else
                {
                    Debug.trace("Failed to create lock file: " + file.getPath());
                    Debug.trace("*** Multiple instances of installer will be allowed ***");
                }
            }
            catch (Exception exception)
            {
                Debug.trace("Lock file could not be created: " + exception);
                Debug.trace("*** Multiple instances of installer will be allowed ***");
            }
            result = true;
        }
        return result;
    }

    /**
     * Invoked when the lock file already exists.
     * 
     * @param file the lock file
     * @return <tt>true</tt> if the user wants to proceed with installation, <tt>false</tt> if they want to cancel
     */
    protected boolean lockFileExists(File file)
    {
        boolean result = false;
        Debug.trace("Lock File Exists, asking user for permission to proceed.");
        StringBuilder msg = new StringBuilder();
        String appName = installData.getInfo().getAppName();
        msg.append("The " + appName + " installer you are attempting to run seems to have a copy already running.\n\n");
        msg.append("This could be from a previous failed installation attempt or you may have accidentally launched\n");
        msg.append("the installer twice. The recommended action is to select 'No' and wait for the other copy of\n");
        msg.append("the installer to start. If you are sure there is no other copy of the installer running, click\n");
        msg.append("the 'Yes' button to allow this installer to run.\n\n");
        msg.append("Are you sure you want to continue with this installation?");
        Option selected = prompt.confirm(Prompt.Type.WARNING, msg.toString(), Prompt.Options.YES_NO);
        if (selected == Option.NO)
        {
            // Take control of the file so it gets deleted after this installer instance exits.
            Debug.trace("Setting temp file to delete on exit");
            file.deleteOnExit();
        }
        else
        {
            // Leave the file as it is.
            result = true;
            Debug.trace("Leaving temp file alone and exiting");
        }
        return result;
    }
}
