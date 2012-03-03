package com.izforge.izpack.installer.requirement;

import com.izforge.izpack.api.installer.RequirementChecker;

public class RequirementsChecker implements RequirementChecker
{
    private JavaVersionChecker versionChecker;
    private JDKChecker jdkChecker;
    private LockFileChecker lockChecker;
    private LangPackChecker langChecker;
    private InstallerRequirementChecker installerRequirementChecker;

    public RequirementsChecker(JavaVersionChecker versionChecker, JDKChecker jdkChecker, LockFileChecker lockChecker,
                               LangPackChecker langChecker, InstallerRequirementChecker installerRequirementChecker)
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
        return versionChecker.check() && jdkChecker.check() && lockChecker.check() && langChecker.check() &&
                installerRequirementChecker.check();
    }
}
