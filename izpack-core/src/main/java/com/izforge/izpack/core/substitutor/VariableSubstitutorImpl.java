/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2001 Johannes Lehtinen
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

package com.izforge.izpack.core.substitutor;

import java.io.Serializable;
import java.util.Properties;

import com.izforge.izpack.api.data.Value;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.core.variable.PlainValue;

public class VariableSubstitutorImpl extends VariableSubstitutorBase implements Serializable
{
    private static final long serialVersionUID = 3907213762447685687L;

    /**
     * The variable value mappings
     */
    protected transient Properties variables;

    /**
     * Constructs a substituter with the specified variables.
     *
     * @param variables the variables
     */
    public VariableSubstitutorImpl(Variables variables)
    {
        this(variables.getProperties());
    }

    /**
     * Constructs a new substitutor using the specified variable value mappings. The environment
     * hashtable is copied by reference. Braces are not required by default
     *
     * @param properties the map with variable value mappings
     */
    public VariableSubstitutorImpl(Properties properties)
    {
        this.variables = properties;
    }

    @Override
    public Value getValue(String name)
    {
        return new PlainValue(variables.getProperty(name));
    }
}
