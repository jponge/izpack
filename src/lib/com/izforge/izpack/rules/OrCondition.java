package com.izforge.izpack.rules;

import java.util.Properties;

import net.n3.nanoxml.XMLElement;
import com.izforge.izpack.util.Debug;


/**
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 * @version $Id: OrCondition.java,v 1.1 2006/09/29 14:40:38 dennis Exp $
 */
public class OrCondition extends Condition {
    public static final String RDE_VCS_REVISION = "$Revision: 1.1 $";
    public static final String RDE_VCS_NAME = "$Name:  $";

    protected Condition leftoperand;
    protected Condition rightoperand;

    /**
     *
     */
    public OrCondition() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     *
     */
    public OrCondition(Condition operand1, Condition operand2) {
        this.leftoperand = operand1;
        this.rightoperand = operand2;
    }

    /* (non-Javadoc)
    * @see de.reddot.installer.util.Condition#isTrue()
    */
    public boolean isTrue(Properties variables) {
        return this.leftoperand.isTrue(variables) || this.rightoperand.isTrue(variables);
    }

    /* (non-Javadoc)
    * @see de.reddot.installer.rules.Condition#readFromXML(net.n3.nanoxml.XMLElement)
    */
    public void readFromXML(XMLElement xmlcondition) {
        try {
            if (xmlcondition.getChildrenCount() != 2) {
                Debug.log("or-condition needs two conditions as operands");
                return;
            }
            this.leftoperand = RulesEngine.analyzeCondition(xmlcondition.getChildAtIndex(0));
            this.rightoperand = RulesEngine.analyzeCondition(xmlcondition.getChildAtIndex(1));
        }
        catch (Exception e) {
            Debug.log("missing element in or-condition");
        }
    }
}
