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
 */
public class NotCondition extends Condition
{

    private static final long serialVersionUID = 3194843222487006309L;
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
        if (operand != null){
            this.operand.setInstalldata(this.installdata);
        }        
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
     * @see de.reddot.installer.rules.Condition#readFromXML(com.izforge.izpack.adaptator.IXMLElement)
     */

    public void readFromXML(IXMLElement xmlcondition)
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
        if ((this.operand == null)){
            Debug.trace("Operand of condition " + this.id + " not initialized correctly.");
            return false;
        }
        this.operand.setInstalldata(this.installdata);        
        return !operand.isTrue();
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.rules.Condition#getDependenciesDetails()
     */
    public String getDependenciesDetails()
    {
        StringBuffer details = new StringBuffer();
        details.append(this.id);
        details.append(" depends on:<ul><li>NOT ");
        details.append(operand.getDependenciesDetails());
        details.append("</li></ul>");
        return details.toString();
    }

    @Override
    public void makeXMLData(IXMLElement conditionRoot)
    {
        IXMLElement op = RulesEngine.createConditionElement(this.operand,conditionRoot);
        this.operand.makeXMLData(op);
        conditionRoot.addChild(op);                        
    }
}
