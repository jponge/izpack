package com.izforge.izpack.rules;

import java.util.HashMap;
import java.util.Properties;

import net.n3.nanoxml.XMLElement;
import com.izforge.izpack.util.Debug;

/**
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 */
public class VariableCondition extends Condition {
    protected String variablename;
    protected String value;

    public VariableCondition(String variablename, String value, HashMap packstoremove) {
        super();
        this.variablename = variablename;
        this.value = value;
    }

    public VariableCondition(String variablename, String value) {
        super();
        this.variablename = variablename;
        this.value = value;
    }

    public VariableCondition() {
        super();
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getVariablename() {
        return variablename;
    }

    public void setVariablename(String variablename) {
        this.variablename = variablename;
    }

    /* (non-Javadoc)
    * @see de.reddot.installer.util.Condition#isTrue()
    */
    public boolean isTrue(Properties variables) {
        String val = variables.getProperty(variablename);
        if (val == null) {
            return false;
        } else {
            return val.equals(value);
        }
    }

    /* (non-Javadoc)
    * @see de.reddot.installer.rules.Condition#readFromXML(net.n3.nanoxml.XMLElement)
    */
    public void readFromXML(XMLElement xmlcondition) {
        try {
            this.variablename = xmlcondition.getFirstChildNamed("name").getContent();
            this.value = xmlcondition.getFirstChildNamed("value").getContent();
        }
        catch (Exception e) {
            Debug.log("missing element in <condition type=\"variable\"/>");
        }

    }
}