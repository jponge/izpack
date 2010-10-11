package com.izforge.izpack.installer.base;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Info;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.util.FileUtil;
import com.izforge.izpack.util.PrivilegedRunner;

import javax.swing.*;

/**
 * Configure rules engine and install data after initialization
 *
 * @author Anthonin Bonnefoy
 */
public class InstallDataConfiguratorWithRules
{

    private AutomatedInstallData installData;

    private RulesEngine rules;

    public InstallDataConfiguratorWithRules(AutomatedInstallData installData, RulesEngine rules)
    {
        this.installData = installData;
        this.rules = rules;
    }


    public void configureInstallData()
    {
        checkForPrivilegedExecution(installData.getInfo());
        checkForRebootAction(installData.getInfo());
    }


    private void checkForPrivilegedExecution(Info info)
    {
        if (info.isPrivilegedExecutionRequired())
        {
            boolean shouldElevate = true;
            final String conditionId = info.getPrivilegedExecutionConditionID();
            if (conditionId != null)
            {
                shouldElevate = rules.getCondition(conditionId).isTrue();
            }
            PrivilegedRunner runner = new PrivilegedRunner(!shouldElevate);
            if (runner.isPlatformSupported() && runner.isElevationNeeded())
            {
                try
                {
                    FileUtil.getLockFile(installData.getInfo().getAppName()).delete();
                    if (runner.relaunchWithElevatedRights() == 0)
                    {
                        System.exit(0);
                    }
                    else
                    {
                        throw new IzPackException("Launching an installer with elevated permissions failed.");
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "The installer could not launch itself with administrator permissions.\n" +
                            "The installation will still continue but you may encounter problems due to insufficient permissions.");
                }
            }
            else if (!runner.isPlatformSupported())
            {
                JOptionPane.showMessageDialog(null, "This installer should be run by an administrator.\n" +
                        "The installation will still continue but you may encounter problems due to insufficient permissions.");
            }
        }

    }


    private void checkForRebootAction(Info info)
    {
        final String conditionId = info.getRebootActionConditionID();
        if (conditionId != null)
        {
            if (!rules.getCondition(conditionId).isTrue())
            {
                info.setRebootAction(Info.REBOOT_ACTION_IGNORE);
            }
        }
    }

}
