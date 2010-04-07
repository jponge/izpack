/*
 * $Id: Compiler.java 1918 2007-11-29 14:02:17Z dreil $
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2007 Dennis Reil, 2010 René Krell
 *
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

package com.izforge.izpack.core.data;

import com.izforge.izpack.api.data.DynamicVariable;
import com.izforge.izpack.api.data.Value;
import com.izforge.izpack.api.regex.RegularExpressionFilter;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;


public class DynamicVariableImpl implements DynamicVariable
{
    private static final long serialVersionUID = -7985397187206803090L;

    private String name;
    private Value value;
    private String conditionid;
    private RegularExpressionFilter regexp;
    private boolean checkonce = false;
    private boolean ignorefailure = true;

    private transient String currentValue;

    public void validate() throws Exception
    {
        if (name == null)
            throw new Exception("No dynamic variable name defined");

        if (value == null)
            throw new Exception("No dynamic variable value defined for variable "+name);

        value.validate();

        if (regexp != null)
            regexp.validate();
    }

    public String evaluate(VariableSubstitutor... substitutors) throws Exception
    {
        String newValue = this.currentValue;

        if (this.value == null)
            return null;

        if (this.checkonce && this.currentValue != null)
            return this.currentValue;

        try {
            newValue = value.resolve(regexp, substitutors);
            if (this.checkonce)
                this.currentValue = newValue;
        }
        catch (Exception e) {
            if (!this.ignorefailure)
                throw e;
            if (regexp != null)
            {
                newValue = regexp.getDefaultValue();
                if (this.checkonce)
                    this.currentValue = newValue;
            }
        }

        return newValue;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        if (name != null)
        {
            this.name = name;
        }
    }

    /**
     * @return the value
     */
    public Value getValue()
    {
        return this.value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(Value value)
    {
        if (value != null)
        {
            this.value = value;
        }
    }

    /**
     * @return the non-mandatory regular expression
     */
    public RegularExpressionFilter getRegularExpression()
    {
        return this.regexp;
    }

    /**
     * @param expression the non-mandatory regular expression
     */
    public void setRegularExpression(RegularExpressionFilter expression)
    {
        if (expression != null)
        {
            this.regexp = expression;
        }
    }

    /**
     * @return the conditionid
     */
    public String getConditionid()
    {
        return this.conditionid;
    }

    /**
     * @param conditionid the conditionid to set
     */
    public void setConditionid(String conditionid)
    {
        if (conditionid != null)
        {
            this.conditionid = conditionid;
        }
    }

    public boolean isCheckonce()
    {
        return checkonce;
    }

    public void setCheckonce(boolean checkonce)
    {
        this.checkonce = checkonce;
    }

    public boolean isIgnoreFailure()
    {
        return ignorefailure;
    }

    public void setIgnoreFailure(boolean ignore)
    {
        this.ignorefailure = ignore;
    }

    @Override
    public boolean equals(Object obj)
    {
        if ((obj == null) || !(obj instanceof DynamicVariable))
        {
            return false;
        }
        DynamicVariable compareObj = (DynamicVariable) obj;
        return (name.equals(compareObj.getName()) && conditionid.equals(compareObj.getConditionid()));
    }

    @Override
    public int hashCode()
    {
        // TODO: check if this always correct
        return name.hashCode() ^ conditionid.hashCode();
    }

}

