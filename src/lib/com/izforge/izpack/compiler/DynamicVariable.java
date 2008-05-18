/*
 * $Id: Compiler.java 1918 2007-11-29 14:02:17Z dreil $
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2007 Dennis Reil
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
package com.izforge.izpack.compiler;

import java.io.Serializable;


/**
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 * @version $Id: $
 */
public class DynamicVariable implements Serializable
{
    private static final long serialVersionUID = -7985397187206803090L;
    private String name;
    private String value;
    private String conditionid;

    public DynamicVariable()
    {
        name = "";
        value = "";
        conditionid = "";
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
    public String getValue()
    {
        return this.value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value)
    {
        if (value != null)
        {
            this.value = value;
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

