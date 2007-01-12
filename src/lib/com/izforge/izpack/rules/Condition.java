package com.izforge.izpack.rules;

import net.n3.nanoxml.XMLElement;

import com.izforge.izpack.installer.AutomatedInstallData;

/**
 * Abstract base class for all conditions
 *
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 */
public abstract class Condition {

    protected String id;
    protected AutomatedInstallData installdata;

    public Condition() {
        this.id = "UNKNOWN";
        this.installdata = null;
    }

    
    /**
     * checks if this condition is met.
     *
     * @return true if condition is fulfilled
     *         false if condition is not fulfilled
     */
    /*
    public abstract boolean isTrue(Properties variables);
     */
    /**
     * checks if this condition is met.
     *
     * @param variables
     * @param selectedpacks
     * @return true if condition is fulfilled
     *         false if condition is not fulfilled
     */
    /*
    public boolean isTrue(Properties variables, List selectedpacks) {
        // default implementation is to ignore the selected packs
        return this.isTrue(variables);
    }
    */

    /**
     * @return the id
     */
    public String getId() {
        return this.id;
    }


    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    public abstract void readFromXML(XMLElement xmlcondition);

    public abstract boolean isTrue();
    
    public AutomatedInstallData getInstalldata()
    {
        return installdata;
    }


    
    public void setInstalldata(AutomatedInstallData installdata)
    {
        this.installdata = installdata;
    }
}
