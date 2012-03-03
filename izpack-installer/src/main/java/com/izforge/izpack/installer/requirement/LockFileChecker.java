package com.izforge.izpack.installer.requirement;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.installer.RequirementChecker;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.FileUtil;

import java.io.File;

public class LockFileChecker implements RequirementChecker
{

    private final AutomatedInstallData installData;
    private final Prompt prompt;

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
            result = lockFileExists(appName, file);
        }
        else
        {
            try
            {
                // Create the new lock file
                if (file.createNewFile())
                {
                    Debug.trace("Temp file created");
                    file.deleteOnExit();
                }
                else
                {
                    Debug.trace("Temp file could not be created");
                    Debug.trace("*** Multiple instances of installer will be allowed ***");
                }
            }
            catch (Exception e)
            {
                Debug.trace("Temp file could not be created: " + e);
                Debug.trace("*** Multiple instances of installer will be allowed ***");
            }
            result = true;
        }
        return result;
    }

    protected boolean lockFileExists(String appName, File file)
    {
        boolean result = false;
        Debug.trace("Lock File Exists, asking user for permission to proceed.");
        StringBuilder msg = new StringBuilder();
        msg.append("The " + appName + " installer you are attempting to run seems to have a copy already running.\n\n");
        msg.append("This could be from a previous failed installation attempt or you may have accidentally launched\n");
        msg.append("the installer twice. The recommended action is to select 'No'</b> and wait for the other copy of\n");
        msg.append("the installer to start. If you are sure there is no other copy of the installer running, click\n");
        msg.append("the 'Yes' button to allow this installer to run.\n\n");
        msg.append("Are you sure you want to continue with this installation?");
        Prompt.Option selected = prompt.confirm(Prompt.Type.WARNING, msg.toString(), Prompt.Options.YES_NO);
        if (selected == Prompt.Option.NO)
        {
            // Take control of the file so it gets deleted after this installer instance exits.
            Debug.trace("Setting temp file to delete on exit");
            file.deleteOnExit();
            result = true;
        }
        else
        {
            // Leave the file as it is.
            Debug.trace("Leaving temp file alone and exiting");
        }
        return result;
    }
}
