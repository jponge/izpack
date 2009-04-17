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

import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.util.Debug;

/**
 * Defines a condition where both operands have to be true
 *
 * @author Dennis Reil, <izpack@reil-online.de>
 */
public class AndCondition extends Condition
{
    private static final long serialVersionUID = -5854944262991488370L;

    protected Condition leftoperand;

    protected Condition rightoperand;

    /**
     *
     */
    public AndCondition()
    {
        super();
    }

    /**
     *
     */
    public AndCondition(Condition operand1, Condition operand2)
    {
        this.leftoperand = operand1;
        if (this.leftoperand != null){
            this.leftoperand.setInstalldata(this.installdata);    
        }
        
        this.rightoperand = operand2;
        if (this.rightoperand != null){
            this.rightoperand.setInstalldata(this.installdata);    
        }        
    }


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
                Debug.log("and-condition needs two conditions as operands");
                return;
            }
            this.leftoperand = RulesEngine.analyzeCondition(xmlcondition.getChildAtIndex(0));
            this.rightoperand = RulesEngine.analyzeCondition(xmlcondition.getChildAtIndex(1));
        }
        catch (Exception e)
        {
            Debug.log("missing element in and-condition");
        }
    }


    public boolean isTrue()
    {
        if ((this.leftoperand == null) || (this.rightoperand == null)){
            Debug.trace("Operands of condition " + this.id + " not initialized correctly.");
            return false;
        }
        this.leftoperand.setInstalldata(this.installdata);
        this.rightoperand.setInstalldata(this.installdata);
        return leftoperand.isTrue() && rightoperand.isTrue();
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
        details.append("</li> AND <li>");
        details.append(rightoperand.getDependenciesDetails());
        details.append("</li></ul>");
        return details.toString();
    }

    @Override
    public void makeXMLData(IXMLElement root)
    {
        IXMLElement left = RulesEngine.createConditionElement(this.leftoperand, root);
        this.leftoperand.makeXMLData(left);
        root.addChild(left);        
        IXMLElement right = RulesEngine.createConditionElement(this.rightoperand, root);
        this.rightoperand.makeXMLData(right);
        root.addChild(right);                
    }
}
