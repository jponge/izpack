package com.izforge.izpack.installer.requirement;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.installer.RequirementChecker;

/**
 * Verifies that the correct java version is available for installation to proceed.
 *
 * @author Tim Anderson
 */
public class JavaVersionChecker implements RequirementChecker
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
     * Constructs a <tt>JavaVersionChecker</tt>.
     *
     * @param installData the installation data
     * @param prompt      the prompt
     */
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
        boolean result = required == null || version == null || version.compareTo(required) >= 0;
        if (!result)
        {
            versionNotAvailable(version, required);
        }
        return result;
    }

    /**
     * Invoked when the required java version is not available.
     *
     * @param version         the current version
     * @param requiredVersion the required version
     */
    protected void versionNotAvailable(String version, String requiredVersion)
    {
        prompt.message(Prompt.Type.ERROR, getVersionNotAvailable(version, requiredVersion));
    }

    /**
     * Formats a message indicating the required java version isn't available.
     *
     * @param version         the current version
     * @param requiredVersion the required version
     * @return the formatted message
     */
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

    /**
     * Returns the java version.
     *
     * @return the java version, as determined by the <em>java.version</em> system property
     */
    protected String getJavaVersion()
    {
        return System.getProperty("java.version");
    }
}
