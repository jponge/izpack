/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2007-2009 Dennis Reil
 * Copyright 2010 Rene Krell
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
import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.api.rules.Condition;
import com.izforge.izpack.util.Debug;

/**
 * @author Dennis Reil, <izpack@reil-online.de>
 */
public class ComparenumericsCondition extends Condition
{
    private static final long serialVersionUID = 5631805710151645907L;

    protected String variablename;
    protected String value;
    protected ComparisonOperator operator = ComparisonOperator.EQUAL;

    public ComparenumericsCondition(String variablename, String value)
    {
        super();
        this.variablename = variablename;
        this.value = value;
    }

    public ComparenumericsCondition()
    {
        super();
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public String getVariablename()
    {
        return variablename;
    }

    public void setVariablename(String variablename)
    {
        this.variablename = variablename;
    }

    public ComparisonOperator getOperator()
    {
        return operator;
    }


    public void setOperator(ComparisonOperator operator)
    {
        this.operator = operator;
    }

    @Override
    public void readFromXML(IXMLElement xmlcondition)
    {
        try
        {
            this.variablename = xmlcondition.getFirstChildNamed("name").getContent();
            this.value = xmlcondition.getFirstChildNamed("value").getContent();
            String operatorAttr = xmlcondition.getFirstChildNamed("operator").getContent();
            if (operatorAttr != null)
            {
                operator = ComparisonOperator.getComparisonOperatorFromAttribute(operatorAttr);
            }
        }
        catch (Exception e)
        {
            Debug.log("missing element in <condition type=\"comparenumerics\"/>");
        }

    }

    @Override
    public boolean isTrue()
    {
        boolean result = false;
        if (this.getInstalldata() != null)
        {
            String val = this.getInstalldata().getVariable(variablename);
            if (val != null)
            {
                if (operator == null)
                {
                    operator = ComparisonOperator.EQUAL;
                }
                try
                {
                   int currentValue = new Integer(val);
                   int comparisonValue = new Integer(value);
                   switch (operator)
                    {
                    case EQUAL:
                        result = currentValue == comparisonValue;
                        break;
                    case NOTEQUAL:
                        result = currentValue != comparisonValue;
                        break;
                    case GREATER:
                        result = currentValue > comparisonValue;
                        break;
                    case GREATEREQUAL:
                        result = currentValue >= comparisonValue;
                        break;
                    case LESS:
                        result = currentValue < comparisonValue;
                        break;
                    case LESSEQUAL:
                        result = currentValue <= comparisonValue;
                        break;
                    default:
                        break;
                    }
                }
                catch (NumberFormatException nfe)
                {
                    Debug.log("The value of the associated variable is not a numeric value or the value which should be compared is not a number.");
                }
            }
        }
        return result;
    }

    @Override
    public String getDependenciesDetails()
    {
        StringBuffer details = new StringBuffer();
        details.append(this.getId());
        details.append(" depends on a value of <b>");
        details.append(this.value);
        details.append("</b> on variable <b>");
        details.append(this.variablename);
        details.append(" (current value: ");
        details.append(this.getInstalldata().getVariable(variablename));
        details.append(")");
        details.append("This value has to be " + this.operator);
        details.append("</b><br/>");
        return details.toString();
    }


    @Override
    public void makeXMLData(IXMLElement conditionRoot)
    {
        XMLElementImpl nameXml = new XMLElementImpl("name", conditionRoot);
        nameXml.setContent(this.variablename);
        conditionRoot.addChild(nameXml);
        XMLElementImpl valueXml = new XMLElementImpl("value", conditionRoot);
        valueXml.setContent(this.value);
        conditionRoot.addChild(valueXml);
        XMLElementImpl opXml = new XMLElementImpl("op", conditionRoot);
        opXml.setContent(this.operator.getAttribute());
        conditionRoot.addChild(opXml);
    }
}