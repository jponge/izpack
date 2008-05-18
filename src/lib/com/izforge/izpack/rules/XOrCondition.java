/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2007 Dennis Reil
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.izforge.izpack.rules;

import com.izforge.izpack.util.Debug;
import net.n3.nanoxml.XMLElement;

/**
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 * @version $Id: XOrCondition.java,v 1.1 2006/09/29 14:40:38 dennis Exp $
 */
public class XOrCondition extends OrCondition
{

    /**
     *
     */
    private static final long serialVersionUID = 3148555083095194992L;

    /**
     *
     */
    public XOrCondition()
    {
        super();
    }

    /**
     * @param operand1
     * @param operand2
     */
    public XOrCondition(Condition operand1, Condition operand2)
    {
        super(operand1, operand2);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.reddot.installer.rules.Condition#readFromXML(net.n3.nanoxml.XMLElement)
     */
    public void readFromXML(XMLElement xmlcondition)
    {
        try
        {
            if (xmlcondition.getChildrenCount() != 2)
            {
                Debug.log("xor-condition needs two conditions as operands");
                return;
            }
            this.leftoperand = RulesEngine.analyzeCondition(xmlcondition.getChildAtIndex(0));
            this.rightoperand = RulesEngine.analyzeCondition(xmlcondition.getChildAtIndex(1));
        }
        catch (Exception e)
        {
            Debug.log("missing element in xor-condition");
        }
    }

    public boolean isTrue()
    {
        boolean op1true = leftoperand.isTrue();
        boolean op2true = rightoperand.isTrue();

        if (op1true && op2true)
        {
            // in case where both are true
            return false;
        }
        return op1true || op2true;
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.rules.OrCondition#getDependenciesDetails()
     */
    public String getDependenciesDetails()
    {
        StringBuffer details = new StringBuffer();
        details.append(this.id);
        details.append(" depends on:<ul><li>");
        details.append(leftoperand.getDependenciesDetails());
        details.append("</li> XOR <li>");
        details.append(rightoperand.getDependenciesDetails());
        details.append("</li></ul>");
        return details.toString();
    }
}
