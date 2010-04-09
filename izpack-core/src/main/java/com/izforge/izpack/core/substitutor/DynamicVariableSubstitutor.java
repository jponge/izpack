/*
 * IzPack - Copyright 2001-2010 Julien Ponge, All Rights Reserved.
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

package com.izforge.izpack.core.substitutor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.izforge.izpack.api.data.DynamicVariable;
import com.izforge.izpack.api.data.Value;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.util.Debug;

/**
 * Substitutes variables occurring in an input stream or a string. This implementation supports a
 * generic variable value mapping and escapes the possible special characters occurring in the
 * substituted values. The file types specifically supported are plain text files (no escaping),
 * Java properties files, and XML files. A valid variable name matches the regular expression
 * [a-zA-Z][a-zA-Z0-9_]* and names are case sensitive. Variables are referenced either by $NAME or
 * ${NAME} (the latter syntax being useful in situations like ${NAME}NOTPARTOFNAME). If a referenced
 * variable is undefined then it is not substituted but the corresponding part of the stream is
 * copied as is.
 *
 * @author Johannes Lehtinen <johannes.lehtinen@iki.fi>
 * @author René Krell <rkrell@gmx.net>
 */
public class DynamicVariableSubstitutor extends VariableSubstitutorBase implements Serializable
{
    /**
     *
     */
    private static final long serialVersionUID = 6585770687685394673L;

    /**
     * dynamic variables
     */
    protected transient Map<String, List<DynamicVariable>> dynamicvariables;

    protected RulesEngine rules;

    /**
     * Constructs a new substitutor using the specified variable value mappings. The environment
     * hashtable is copied by reference. Braces are not required by default
     *
     * @param variables the map with variable value mappings
     */
    public DynamicVariableSubstitutor(Map<String, List<DynamicVariable>> dynamicvariables,
            RulesEngine rules)
    {
        this.dynamicvariables = dynamicvariables;
        this.rules = rules;
    }


    @Override
    public Value getValue(String name) {
        if (dynamicvariables != null)
        {
            for (String dynvarname : dynamicvariables.keySet())
            {
                for (DynamicVariable dynvar : dynamicvariables.get(dynvarname))
                {
                    String conditionid = dynvar.getConditionid();
                    if ((conditionid != null) && (conditionid.length() > 0))
                    {
                        if ((rules != null) && rules.isConditionTrue(conditionid))
                        {
                            Debug.log("refresh condition");
                            // condition for this rule is true
                            return dynvar.getValue();
                        }
                    }
                    else
                    {
                        // empty condition
                        return dynvar.getValue();
                    }
                }
            }
        }
        return null;
    }
}

