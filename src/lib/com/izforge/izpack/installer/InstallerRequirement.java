package com.izforge.izpack.installer;

/**
 * A requirement which has to be fulfilled to start the installer.
 * @author dennis.reil
 *
 */
public class InstallerRequirement
{
    private String condition;
    private String message;
    
    public String getCondition()
    {
        return condition;
    }
    
    public void setCondition(String condition)
    {
        this.condition = condition;
    }
    
    public String getMessage()
    {
        return message;
    }
    
    public void setMessage(String message)
    {
        this.message = message;
    }
}
