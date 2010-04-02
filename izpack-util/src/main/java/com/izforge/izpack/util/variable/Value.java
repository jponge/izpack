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

package com.izforge.izpack.util.variable;

import com.izforge.izpack.api.substitutor.SubstitutionType;
import com.izforge.izpack.util.regex.RegularExpressionProcessor;
import com.izforge.izpack.util.substitutor.VariableSubstitutorBase;

public abstract class Value
{
    public abstract void validate() throws Exception;
    public abstract String resolve() throws Exception;
    public abstract String resolve(VariableSubstitutorBase... substitutors) throws Exception;

    public String resolve(RegularExpressionFilter regexp, VariableSubstitutorBase... substitutors)
    throws Exception
    {
        String newValue = resolve(substitutors);

        if (regexp != null) {
            String replace = null, select = null, regex = null;
            for ( VariableSubstitutorBase substitutor : substitutors )
                replace = substitutor.substitute(regexp.getReplace(), (SubstitutionType)null);
            for ( VariableSubstitutorBase substitutor : substitutors )
                select = substitutor.substitute(regexp.getSelect(), (SubstitutionType)null);
            for ( VariableSubstitutorBase substitutor : substitutors )
                regex = substitutor.substitute(regexp.getRegexp(), (SubstitutionType)null);
            RegularExpressionProcessor processor = new RegularExpressionProcessor();
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
