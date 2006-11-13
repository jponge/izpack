package com.izforge.izpack.rules;

import java.util.Properties;

import net.n3.nanoxml.XMLElement;
import com.izforge.izpack.util.Debug;

/**
 * Defines a condition where both operands have to be true
 *
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 */
public class AndCondition extends Condition {
    protected Condition leftoperand;
    protected Condition rightoperand;

    /**
     *
     */
    public AndCondition() {
        super();        
    }

    /**
     *
     */
    public AndCondition(Condition operand1, Condition operand2) {
        this.leftoperand = operand1;
        this.rightoperand = operand2;
    }

    /* (non-Javadoc)
    * @see de.reddot.installer.util.Condition#isTrue()
    */
    public boolean isTrue(Properties variables) {
        return leftoperand.isTrue(variables) && rightoperand.isTrue(variables);
    }

    /* (non-Javadoc)
    * @see de.reddot.installer.rules.Condition#readFromXML(net.n3.nanoxml.XMLElement)
    */
    public void readFromXML(XMLElement xmlcondition) {
        try {
            if (xmlcondition.getChildrenCount() != 2) {
                Debug.log("and-condition needs two conditions as operands");
                return;
            }
            this.leftoperand = RulesEngine.analyzeCondition(xmlcondition.getChildAtIndex(0));
            this.rightoperand = RulesEngine.analyzeCondition(xmlcondition.getChildAtIndex(1));
        }
        catch (Exception e) {
            Debug.log("missing element in and-condition");
        }
    }
}
