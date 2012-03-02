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

package com.izforge.izpack.core.rules.logic;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.rules.Condition;
import com.izforge.izpack.core.rules.RulesEngineImpl;

/**
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 * @version $Id: XOrCondition.java,v 1.1 2006/09/29 14:40:38 dennis Exp $
 */
public class XorCondition extends OrCondition
{
    private static final long serialVersionUID = 3148555083095194992L;

    public XorCondition(RulesEngineImpl rulesEngineImpl, Condition... operands)
    {
        super(rulesEngineImpl, operands);
    }

    @Override
    public void readFromXML(IXMLElement xmlcondition) throws Exception
    {
        if (xmlcondition.getChildrenCount() > 2)
        {
            throw new Exception("Not more than two operands allowed in XOR condition \"" + getId() + "\"");
        }
        super.readFromXML(xmlcondition);
    }

    @Override
    public boolean isTrue()
    {
        Boolean result = null;
        for (Condition condition : nestedConditions)
        {
            if (result == null)
            {
                result = condition.isTrue();
            }
            else
            {
                result = !(result && condition.isTrue()) && (result || condition.isTrue());
            }
        }
        return result;
    }

    @Override
    public String getDependenciesDetails()
    {
        StringBuffer details = new StringBuffer();
        details.append(this.getId());
        details.append(" depends on:<ul><li>");
        for (Condition condition : nestedConditions)
        {
            details.append(condition.getDependenciesDetails());
            details.append("</li> XOR <li>");
        }
        details.append("</li></ul>");
        return details.toString();
    }

}