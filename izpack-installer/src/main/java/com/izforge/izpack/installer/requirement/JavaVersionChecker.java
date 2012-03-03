package com.izforge.izpack.installer.requirement;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.installer.RequirementChecker;

public class JavaVersionChecker implements RequirementChecker
{

    /**
     * The installation data.
     */
    private final AutomatedInstallData installData;

    private final Prompt prompt;

    public JavaVersionChecker(AutomatedInstallData installData, Prompt prompt)
    {
        this.installData = installData;
        this.prompt = prompt;
    }

    /**
     * Checks the Java version.
     *
     * @return <tt>true</tt> if requirements are met, otherwise <tt>false</tt>
     */
    @Override
    public boolean check()
    {
        String version = getJavaVersion();
        String required = installData.getInfo().getJavaVersion();
        boolean result = version.compareTo(required) >= 0;
        if (!result)
        {
            versionNotAvailable(version, required);
        }
        return result;
    }

    protected void versionNotAvailable(String version, String requiredVersion) {
        prompt.message(Prompt.Type.ERROR, getVersionNotAvailable(version, requiredVersion));
    }

    protected String getVersionNotAvailable(String version, String requiredVersion)
    {
        StringBuilder msg = new StringBuilder();
        msg.append("The application that you are trying to install requires a ");
        msg.append(requiredVersion);
        msg.append(" version or later of the Java platform.\n");
        msg.append("You are running a ");
        msg.append(version);
        msg.append(" version of the Java platform.\n");
        msg.append("Please upgrade to a newer version.");
        return msg.toString();
    }

    protected String getJavaVersion()
    {
        return System.getProperty("java.version");
    }
}
