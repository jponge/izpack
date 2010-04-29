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

package com.izforge.izpack.core.variable;

import com.izforge.izpack.api.data.Value;
import com.izforge.izpack.api.regex.RegularExpressionFilter;
import com.izforge.izpack.api.regex.RegularExpressionProcessor;
import com.izforge.izpack.api.substitutor.SubstitutionType;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.regex.RegularExpressionProcessorImpl;

public abstract class ValueImpl implements Value
{
    public abstract void validate() throws Exception;
    public abstract String resolve() throws Exception;
    public abstract String resolve(VariableSubstitutor... substitutors) throws Exception;

    public String resolve(RegularExpressionFilter regexp, VariableSubstitutor... substitutors)
    throws Exception
    {
        String newValue = resolve(substitutors);

        if (regexp != null) {
            String replace = null, select = null, regex = null;
            for ( VariableSubstitutor substitutor : substitutors )
                replace = substitutor.substitute(regexp.getReplace(), (SubstitutionType)null);
            for ( VariableSubstitutor substitutor : substitutors )
                select = substitutor.substitute(regexp.getSelect(), (SubstitutionType)null);
            for ( VariableSubstitutor substitutor : substitutors )
                regex = substitutor.substitute(regexp.getRegexp(), (SubstitutionType)null);
            RegularExpressionProcessor processor = new RegularExpressionProcessorImpl();
            processor.setInput(newValue);
            processor.setRegexp(regex);
            processor.setCaseSensitive(regexp.getCasesensitive());
            if (select != null) {
                processor.setSelect(select);
            } else
                if (replace != null) {
                    processor.setReplace(replace);
                    processor.setGlobal(regexp.getGlobal());
                }
            newValue = processor.execute();
        }

        return newValue;
    }

}
