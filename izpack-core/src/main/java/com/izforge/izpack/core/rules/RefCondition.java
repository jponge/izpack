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

package com.izforge.izpack.core.rules;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.rules.Condition;

/**
 * References an already defined condition
 *
 * @author Dennis Reil, <izpack@reil-online.de>
 */
public class RefCondition extends Condition
{

    /**
     *
     */
    private static final long serialVersionUID = -2880915036530702269L;
    Condition referencedcondition;
    private String referencedConditionId;

    public RefCondition()
    {
        this.referencedcondition = null;
        this.referencedConditionId = null;
    }

    /*
     * public boolean isTrue(Properties variables) { if (referencedcondition == null) { return
     * false; } else { return referencedcondition.isTrue(variables); } }
     */

    public void readFromXML(IXMLElement xmlcondition)
    {
        this.referencedConditionId = xmlcondition.getAttribute("refid");
        this.referencedcondition = RulesEngineImpl.getCondition(this.referencedConditionId);
        this.setId("ref." + this.referencedConditionId);
    }

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
                this.referencedcondition = RulesEngineImpl.getCondition(this.referencedConditionId);
            }
            if (this.referencedcondition != null)
            {
                this.referencedcondition.setInstalldata(this.getInstalldata());
            }
            return (this.referencedcondition != null) ? this.referencedcondition.isTrue() : false;
        }
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.api.rules.Condition#getDependenciesDetails()
     */

    public String getDependenciesDetails()
    {
        StringBuffer details = new StringBuffer();
        details.append(this.getId());
        details.append(" depends on:<ul><li>");
        details.append(referencedcondition.getDependenciesDetails());
        details.append("</li></ul>");
        return details.toString();
    }

    @Override
    public void makeXMLData(IXMLElement conditionRoot)
    {
        conditionRoot.setAttribute("refid", this.referencedConditionId);
    }
}
