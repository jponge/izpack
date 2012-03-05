package com.izforge.izpack.installer.requirement;

import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.api.installer.RequirementChecker;

/**
 * Verifies that a language pack is available.
 *
 * @author Tim Anderson
 */
public class LangPackChecker implements RequirementChecker
{
    /**
     * The resources.
     */
    private final ResourceManager resources;

    /**
     * Constructs a <tt>LangPackChecker</tt>.
     *
     * @param resources the resources
     */
    public LangPackChecker(ResourceManager resources)
    {
        this.resources = resources;
    }

    /**
     * Determines if installation requirements are met.
     *
     * @return <tt>true</tt> if requirements are met, otherwise <tt>false</tt>
     */
    @Override
    public boolean check()
    {
        return !resources.getAvailableLangPacks().isEmpty();
    }
}
