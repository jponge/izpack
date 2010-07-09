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
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.rules.Condition;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.util.Debug;

/**
 * @author Dennis Reil, <izpack@reil-online.de>
 */
public class NotCondition extends Condition
{

    private static final long serialVersionUID = 3194843222487006309L;
    protected Condition operand;
    private RulesEngine rulesEngineImpl;

    public NotCondition(RulesEngine rulesEngineImpl)
    {
        this.rulesEngineImpl = rulesEngineImpl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readFromXML(IXMLElement xmlcondition)
    {
        try
        {
            if (xmlcondition.getChildrenCount() != 1)
            {
                Debug.log("not-condition needs one condition as operand");
                return;
            }
            this.operand = rulesEngineImpl.instanciateCondition(xmlcondition.getChildAtIndex(0));
        }
        catch (Exception e)
        {
            Debug.log("missing element in not-condition");
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTrue()
    {
        if ((this.operand == null))
        {
            Debug.trace("Operand of condition " + this.getId() + " not initialized correctly.");
            return false;
        }
        this.operand.setInstalldata(this.getInstallData());
        return !operand.isTrue();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getDependenciesDetails()
    {
        StringBuffer details = new StringBuffer();
        details.append(this.getId());
        details.append(" depends on:<ul><li>NOT ");
        details.append(operand.getDependenciesDetails());
        details.append("</li></ul>");
        return details.toString();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void makeXMLData(IXMLElement conditionRoot)
    {
        IXMLElement conditionElement = rulesEngineImpl.createConditionElement(this.operand, conditionRoot);
        this.operand.makeXMLData(conditionElement);
        conditionRoot.addChild(conditionElement);
    }

    public static Condition createFromCondition(Condition conditionByExpr, RulesEngine rulesEngine, AutomatedInstallData installData)
    {
        NotCondition notCondition = new NotCondition(rulesEngine);
        notCondition.setInstalldata(installData);
        notCondition.operand = conditionByExpr;
        if (conditionByExpr != null)
        {
            notCondition.operand.setInstalldata(installData);
        }
        return notCondition;
    }
}
