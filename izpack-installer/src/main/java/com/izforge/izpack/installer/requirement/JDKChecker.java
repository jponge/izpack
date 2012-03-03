package com.izforge.izpack.installer.requirement;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.installer.RequirementChecker;
import com.izforge.izpack.util.FileExecutor;

public class JDKChecker implements RequirementChecker
{

    private final AutomatedInstallData installData;

    private final Prompt prompt;

    public JDKChecker(AutomatedInstallData installData, Prompt prompt)
    {
        this.installData = installData;
        this.prompt = prompt;
    }

    /**
     * Determines if the JDK is required, and if so, if it exists.
     *
     * @return <tt>true</tt> if JDK requirements are met, otherwise <tt>false</tt>
     */
    @Override
    public boolean check()
    {
        boolean result;
        boolean required = installData.getInfo().isJdkRequired();
        if (!required || exists())
        {
            result = true;
        }
        else
        {
            result = notFound();
        }
        return result;
    }

    /**
     * Determines if the JDK is installed, by executing attempting to execute javac.
     *
     * @return <tt>true</tt> if javac was successfully executed, otherwise <tt>false</tt>
     */
    protected boolean exists()
    {
        FileExecutor exec = new FileExecutor();
        String[] output = new String[2];
        String[] params = {"javac", "-help"};
        return (exec.executeCommand(params, output) == 0);
    }

    protected boolean notFound()
    {
        String message = "It looks like your system does not have a Java Development Kit (JDK) available.\n"
                + "The software that you plan to install requires a JDK for both its installation and execution.\n\n"
                + "Do you still want to proceed with the installation process?";
        Prompt.Option selected = prompt.confirm(Prompt.Type.WARNING, message, Prompt.Options.YES_NO);
        return selected == Prompt.Option.YES;

    }
}