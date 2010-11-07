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

package com.izforge.izpack.core.rules.process;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.rules.Condition;
import com.izforge.izpack.api.rules.RulesEngine;

/**
 * References an already defined condition
 *
 * @author Dennis Reil, <izpack@reil-online.de>
 */
public class RefCondition extends Condition
{
    private static final long serialVersionUID = -2880915036530702269L;
    Condition referencedcondition;
    private String referencedConditionId;
    private RulesEngine rules;

    public RefCondition(RulesEngine rules)
    {
        this.rules = rules;
        this.referencedcondition = null;
        this.referencedConditionId = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readFromXML(IXMLElement xmlcondition) throws Exception
    {
        this.referencedConditionId = xmlcondition.getAttribute("refid");
        if (this.referencedConditionId == null)
        {
            throw new Exception("Missing attribute \"refid\" in condition \"" + getId() + "\"");
        }
        this.referencedcondition = rules.getCondition(this.referencedConditionId);
        if (this.referencedcondition == null)
        {
            throw new Exception("Condition \"" + referencedConditionId
                    + "\" referenced from condition \"" + getId() + "\" does not exist");
        }
        this.setId("ref." + this.referencedConditionId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTrue()
    {
        if (this.referencedConditionId == null)
        {
            return false;
        }
        else
        {
            if (this.referencedcondition == null)
            {
                this.referencedcondition = rules.getCondition(this.referencedConditionId);
            }
            if (this.referencedcondition != null)
            {
                this.referencedcondition.setInstalldata(this.getInstallData());
            }
            return (this.referencedcondition != null) ? this.referencedcondition.isTrue() : false;
        }
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
        details.append(referencedcondition.getDependenciesDetails());
        details.append("</li></ul>");
        return details.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void makeXMLData(IXMLElement conditionRoot)
    {
        conditionRoot.setAttribute("refid", this.referencedConditionId);
    }
}
