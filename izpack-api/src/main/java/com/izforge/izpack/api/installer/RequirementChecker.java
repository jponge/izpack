package com.izforge.izpack.api.installer;

/**
 * Checks installation requirements are met.
 */
public interface RequirementChecker
{

    /**
     * Determines if installation requirements are met.
     *
     * @return <tt>true</tt> if requirements are met, otherwise <tt>false</tt>
     */
    boolean check();

}