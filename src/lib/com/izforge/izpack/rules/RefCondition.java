package com.izforge.izpack.rules;

import java.util.List;
import java.util.Properties;

import net.n3.nanoxml.XMLElement;

/**
 * References an already defined condition
 * 
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 */
public class RefCondition extends Condition
{

    Condition referencedcondition;

    public RefCondition()
    {
        this.referencedcondition = null;
    }

    public boolean isTrue(Properties variables)
    {
        if (referencedcondition == null)
        {
            return false;
        }
        else
        {
            return referencedcondition.isTrue(variables);
        }
    }

    public void readFromXML(XMLElement xmlcondition)
    {
        String refid = xmlcondition.getAttribute("refid");
        this.referencedcondition = RulesEngine.getCondition(refid);
    }

    public boolean isTrue(Properties variables, List selectedpacks)
    {
        return referencedcondition.isTrue(variables, selectedpacks);
    }

}
