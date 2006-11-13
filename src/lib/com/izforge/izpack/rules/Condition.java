package com.izforge.izpack.rules;

import java.util.List;
import java.util.Properties;

import net.n3.nanoxml.XMLElement;

/**
 * Abstract base class for all conditions
 *
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 */
public abstract class Condition {

    protected String id;

    public Condition() {
        this.id = "UNKNOWN";
    }

    /**
     * checks if this condition is met.
     *
     * @return true if condition is fulfilled
     *         false if condition is not fulfilled
     */
    public abstract boolean isTrue(Properties variables);

    /**
     * checks if this condition is met.
     *
     * @param variables
     * @param selectedpacks
     * @return true if condition is fulfilled
     *         false if condition is not fulfilled
     */
    public boolean isTrue(Properties variables, List selectedpacks) {
        // default implementation is to ignore the selected packs
        return this.isTrue(variables);
    }

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
}
