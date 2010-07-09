/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
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

import java.util.Comparator;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.api.rules.Condition;
import com.izforge.izpack.core.substitutor.VariableSubstitutorBase;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;
import com.izforge.izpack.util.Debug;

public class CompareversionsCondition extends Condition
{
    private static final long serialVersionUID = 5631805710151645907L;

    protected String operand1;
    protected String operand2;
    protected ComparisonOperator operator = ComparisonOperator.EQUAL;

    public CompareversionsCondition(String op1, String op2)
    {
        super();
        operand1 = op1;
        operand2 = op2;
    }

    public CompareversionsCondition()
    {
        super();
    }

    public String getLeftOperand()
    {
        return operand1;
    }

    public void setLeftOperand(String value)
    {
        operand1 = value;
    }

    public String getRightOperand()
    {
        return operand2;
    }

    public void setRightOperand(String value)
    {
        operand2 = value;
    }

    public ComparisonOperator getOperator()
    {
        return this.operator;
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
            this.operand1 = xmlcondition.getFirstChildNamed("arg1").getContent();
            this.operand2 = xmlcondition.getFirstChildNamed("arg2").getContent();
            String operatorAttr = xmlcondition.getFirstChildNamed("operator").getContent();
            if (operatorAttr != null)
            {
                operator = ComparisonOperator.getComparisonOperatorFromAttribute(operatorAttr);
            }
        }
        catch (Exception e)
        {
            Debug.log("Missing element in <condition type=\"compareversions\"/>");
        }

    }

    @Override
    public boolean isTrue()
    {
        boolean result = false;
        if (this.getInstallData() != null && operand1 != null && operand2 != null)
        {
            VariableSubstitutorBase subst = new VariableSubstitutorImpl(this.getInstallData().getVariables());
            String arg1 = subst.substitute(operand1);
            String arg2 = subst.substitute(operand2);
            if (operator == null)
            {
                operator = ComparisonOperator.EQUAL;
            }
            int res = new VersionStringComparator().compare(arg1, arg2);

            switch (operator)
            {
            case EQUAL:
                result = (res == 0);
                break;
            case NOTEQUAL:
                result = (res != 0);
                break;
            case GREATER:
                result = (res > 0);
                break;
            case GREATEREQUAL:
                result = (res >= 0);
                break;
            case LESS:
                result = (res < 0);
                break;
            case LESSEQUAL:
                result = (res <= 0);
                break;
            default:
                break;
            }
        }
        return result;
    }

    @Override
    public String getDependenciesDetails()
    {
        StringBuffer details = new StringBuffer();
        details.append(this.getId());
        details.append(" depends on the values <b>");
        details.append(this.operand1);
        details.append("</b> and <b>");
        details.append(this.operand2);
        details.append("</b>");
        details.append("This value has to be <b>" + this.operator);
        details.append("</b><br/>");
        return details.toString();
    }


    @Override
    public void makeXMLData(IXMLElement conditionRoot)
    {
        XMLElementImpl nameXml = new XMLElementImpl("arg1", conditionRoot);
        nameXml.setContent(this.operand1);
        conditionRoot.addChild(nameXml);
        XMLElementImpl valueXml = new XMLElementImpl("arg2", conditionRoot);
        valueXml.setContent(this.operand2);
        conditionRoot.addChild(valueXml);
        XMLElementImpl opXml = new XMLElementImpl("operator", conditionRoot);
        opXml.setContent(this.operator.getAttribute());
        conditionRoot.addChild(opXml);
    }

    private static class VersionStringComparator implements Comparator<String>
    {
        public int compare(String s1, String s2){
            if( s1 == null && s2 == null )
                return 0;
            else if( s1 == null )
                return -1;
            else if( s2 == null )
                return 1;

            String[]
                arr1 = s1.split("[^a-zA-Z0-9_]+"),
                arr2 = s2.split("[^a-zA-Z0-9_]+")
            ;

            int i1, i2, i3;

            for(int ii = 0, max = Math.min(arr1.length, arr2.length); ii <= max; ii++){
                if( ii == arr1.length )
                    return ii == arr2.length ? 0 : -1;
                else if( ii == arr2.length )
                    return 1;

                try{
                    i1 = Integer.parseInt(arr1[ii]);
                }
                catch (Exception x){
                    i1 = Integer.MAX_VALUE;
                }

                try{
                    i2 = Integer.parseInt(arr2[ii]);
                }
                catch (Exception x){
                    i2 = Integer.MAX_VALUE;
                }

                if( i1 != i2 ){
                    return i1 - i2;
                }

                i3 = arr1[ii].compareTo(arr2[ii]);

                if( i3 != 0 )
                    return i3;
            }

            return 0;
        }
    }
}