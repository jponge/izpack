/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2007-2009 Dennis Reil <izpack@reil-online.de>
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
import com.izforge.izpack.util.Debug;

/**
 * Negation of a referenced condition
 */
public class NotCondition extends Condition
{

    private static final long serialVersionUID = 3194843222487006309L;
    protected Condition operand;

    @Override
    public void readFromXML(IXMLElement xmlcondition) throws Exception
    {
        if (xmlcondition.getChildrenCount() <= 0)
        {
            throw new Exception("Missing nested element in condition \"" + getId() + "\"");
        }
        else if (xmlcondition.getChildrenCount() != 1)
        {
            throw new Exception("Condition \"" + getId() + "\" needs exactly one condition as operand");
        }
        this.operand = getInstallData().getRules().instanciateCondition(xmlcondition.getChildAtIndex(0));
    }


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


    @Override
    public void makeXMLData(IXMLElement conditionRoot)
    {
        IXMLElement conditionElement = getInstallData().getRules().createConditionElement(this.operand, conditionRoot);
        this.operand.makeXMLData(conditionElement);
        conditionRoot.addChild(conditionElement);
    }

    public static Condition createFromCondition(Condition conditionByExpr)
    {
        NotCondition notCondition = new NotCondition();
        notCondition.operand = conditionByExpr;
        if (conditionByExpr != null)
        {
            notCondition.operand.setInstalldata(notCondition.getInstallData());
        }
        return notCondition;
    }
}
