package com.izforge.izpack.api.installer;

/**
 * Display the requirement message
 * 
 * @deprecated see {@link RequirementChecker}.
 */
@Deprecated
public interface InstallerRequirementDisplay
{
    void showMissingRequirementMessage(String message);
}
