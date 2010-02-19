/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2007-2009 Dennis Reil
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
import com.izforge.izpack.adaptator.IXMLElement;

/**
 * @author Dennis Reil, <izpack@reil-online.de>
 * @version $Id: OrCondition.java,v 1.1 2006/09/29 14:40:38 dennis Exp $
 */
public class OrCondition extends Condition
{
    private static final long serialVersionUID = 8341350377205144199L;

    protected Condition leftoperand;

    protected Condition rightoperand;

    /**
     *
     */
    public OrCondition()
    {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     *
     */
    public OrCondition(Condition operand1, Condition operand2)
    {
        this.leftoperand = operand1;
        this.leftoperand.setInstalldata(this.installdata);
        this.rightoperand = operand2;
        this.rightoperand.setInstalldata(this.installdata);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.reddot.installer.util.Condition#isTrue()
     */
    /*
     * public boolean isTrue(Properties variables) { return this.leftoperand.isTrue(variables) ||
     * this.rightoperand.isTrue(variables); }
     */
    /*
     * (non-Javadoc)
     * 
     * @see de.reddot.installer.rules.Condition#readFromXML(com.izforge.izpack.adaptator.IXMLElement)
     */
    public void readFromXML(IXMLElement xmlcondition)
    {
        try
        {
            if (xmlcondition.getChildrenCount() != 2)
            {
                Debug.log("or-condition needs two conditions as operands");
                return;
            }
            this.leftoperand = RulesEngine.analyzeCondition(xmlcondition.getChildAtIndex(0));
            this.rightoperand = RulesEngine.analyzeCondition(xmlcondition.getChildAtIndex(1));
        }
        catch (Exception e)
        {
            Debug.log("missing element in or-condition");
        }
    }

    /*
     * public boolean isTrue(Properties variables, List selectedpacks) { return
     * this.leftoperand.isTrue(variables, selectedpacks) || this.rightoperand.isTrue(variables,
     * selectedpacks); }
     */
    public boolean isTrue()
    {
        if ((this.leftoperand == null) || (this.rightoperand == null)){
            Debug.trace("Operands of condition " + this.id + " not initialized correctly.");
            return false;
        }
        this.leftoperand.setInstalldata(this.installdata);
        this.rightoperand.setInstalldata(this.installdata);
        return this.leftoperand.isTrue() || this.rightoperand.isTrue();
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.rules.Condition#getDependenciesDetails()
     */
    public String getDependenciesDetails()
    {
        StringBuffer details = new StringBuffer();
        details.append(this.id);
        details.append(" depends on:<ul><li>");
        details.append(leftoperand.getDependenciesDetails());
        details.append("</li> OR <li>");
        details.append(rightoperand.getDependenciesDetails());
        details.append("</li></ul>");
        return details.toString();
    }

    @Override
    public void makeXMLData(IXMLElement conditionRoot)
    {
        IXMLElement left = RulesEngine.createConditionElement(this.leftoperand, conditionRoot);
        this.leftoperand.makeXMLData(left);
        conditionRoot.addChild(left);        
        IXMLElement right = RulesEngine.createConditionElement(this.rightoperand, conditionRoot);
        this.rightoperand.makeXMLData(right);
        conditionRoot.addChild(right);     
    }
}
