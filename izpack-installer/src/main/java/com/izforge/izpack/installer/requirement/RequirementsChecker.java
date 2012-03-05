package com.izforge.izpack.installer.requirement;

import com.izforge.izpack.api.installer.RequirementChecker;


/**
 * Verifies all installation requirements are met. This should be used prior to installation commencing.
 *
 * @author Tim Anderson
 */
public class RequirementsChecker implements RequirementChecker
{
    /**
     * The language pack checker.
     */
    private LangPackChecker langChecker;

    /**
     * The version checker.
     */
    private JavaVersionChecker versionChecker;

    /**
     * The JDK checker.
     */
    private JDKChecker jdkChecker;

    /**
     * The lock file checker.
     */
    private LockFileChecker lockChecker;

    /**
     * The installer requirement checker.
     */
    private InstallerRequirementChecker installerRequirementChecker;

    /**
     * Constructs a <tt>RequirementsChecker</tt>.
     *
     * @param langChecker the language pack checker
     * @param versionChecker the java version checker
     * @param jdkChecker the JDK checker
     * @param lockChecker the lock file checker
     * @param installerRequirementChecker the installer requirement checker
     */
    public RequirementsChecker(LangPackChecker langChecker, JavaVersionChecker versionChecker, JDKChecker jdkChecker,
                               LockFileChecker lockChecker, InstallerRequirementChecker installerRequirementChecker)
    {
        this.versionChecker = versionChecker;
        this.jdkChecker = jdkChecker;
        this.lockChecker = lockChecker;
        this.langChecker = langChecker;
        this.installerRequirementChecker = installerRequirementChecker;
    }

    /**
     * Determines if installation requirements are met.
     *
     * @return <tt>true</tt> if requirements are met, otherwise <tt>false</tt>
     */
    @Override
    public boolean check()
    {
        return langChecker.check() && versionChecker.check() && jdkChecker.check() && lockChecker.check() &&
                installerRequirementChecker.check();
    }
}
