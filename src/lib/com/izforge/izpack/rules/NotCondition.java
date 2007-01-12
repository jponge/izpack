package com.izforge.izpack.rules;

import net.n3.nanoxml.XMLElement;

import com.izforge.izpack.util.Debug;

/**
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 */
public class NotCondition extends Condition
{

    protected Condition operand;

    /**
     * 
     */
    public NotCondition()
    {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * 
     */
    public NotCondition(Condition operand)
    {
        this.operand = operand;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.reddot.installer.util.Condition#isTrue()
     */
    /*
    public boolean isTrue(Properties variables)
    {
        return !operand.isTrue(variables);
    }
    */

    /*
     * (non-Javadoc)
     * 
     * @see de.reddot.installer.rules.Condition#readFromXML(net.n3.nanoxml.XMLElement)
     */
    public void readFromXML(XMLElement xmlcondition)
    {
        try
        {
            if (xmlcondition.getChildrenCount() != 1)
            {
                Debug.log("not-condition needs one condition as operand");
                return;
            }
            this.operand = RulesEngine.analyzeCondition(xmlcondition.getChildAtIndex(0));
        }
        catch (Exception e)
        {
            Debug.log("missing element in not-condition");
        }
    }

    /*
    public boolean isTrue(Properties variables, List selectedpacks)
    {
        return !operand.isTrue(variables, selectedpacks);
    }
    */
    public boolean isTrue()
    {        
        return !operand.isTrue();
    }
}
