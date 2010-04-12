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

package com.izforge.izpack.core.rules.logic;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.rules.Condition;
import com.izforge.izpack.core.rules.RulesEngineImpl;
import com.izforge.izpack.util.Debug;

/**
 * @author Dennis Reil, <izpack@reil-online.de>
 * @version $Id: OrCondition.java,v 1.1 2006/09/29 14:40:38 dennis Exp $
 */
public class OrCondition extends Condition
{
    private static final long serialVersionUID = 8341350377205144199L;

    protected Condition leftoperand;

    protected Condition rightoperand;
    protected RulesEngineImpl rulesEngineImpl;

    public OrCondition(Condition operand1, Condition operand2)
    {
        this.leftoperand = operand1;
        this.leftoperand.setInstalldata(this.getInstalldata());
        this.rightoperand = operand2;
        this.rightoperand.setInstalldata(this.getInstalldata());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readFromXML(IXMLElement xmlcondition)
    {
        try
        {
            if (xmlcondition.getChildrenCount() != 2)
            {
                Debug.log("or-condition needs two conditions as operands");
                return;
            }
            this.leftoperand = rulesEngineImpl.instanciateCondition(xmlcondition.getChildAtIndex(0));
            this.rightoperand = rulesEngineImpl.instanciateCondition(xmlcondition.getChildAtIndex(1));
        }
        catch (Exception e)
        {
            Debug.log("missing element in or-condition");
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTrue()
    {
        if ((this.leftoperand == null) || (this.rightoperand == null))
        {
            Debug.trace("Operands of condition " + this.getId() + " not initialized correctly.");
            return false;
        }
        this.leftoperand.setInstalldata(this.getInstalldata());
        this.rightoperand.setInstalldata(this.getInstalldata());
        return this.leftoperand.isTrue() || this.rightoperand.isTrue();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getDependenciesDetails()
    {
        StringBuffer details = new StringBuffer();
        details.append(this.getId());
        details.append(" depends on:<ul><li>");
        details.append(leftoperand.getDependenciesDetails());
        details.append("</li> OR <li>");
        details.append(rightoperand.getDependenciesDetails());
        details.append("</li></ul>");
        return details.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void makeXMLData(IXMLElement conditionRoot)
    {
        IXMLElement left = rulesEngineImpl.createConditionElement(this.leftoperand, conditionRoot);
        this.leftoperand.makeXMLData(left);
        conditionRoot.addChild(left);
        IXMLElement right = rulesEngineImpl.createConditionElement(this.rightoperand, conditionRoot);
        this.rightoperand.makeXMLData(right);
        conditionRoot.addChild(right);
    }
}
