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

package com.izforge.izpack.core.regex;

import com.izforge.izpack.api.regex.RegularExpressionFilter;

import java.io.Serializable;

public class RegularExpressionFilterImpl implements RegularExpressionFilter, Serializable
{
    /**
     *
     */
    private static final long serialVersionUID = -1405213251817336962L;

    public String regexp;
    public String select, replace;
    public String defaultValue;
    public Boolean casesensitive;
    public Boolean global;

    public RegularExpressionFilterImpl(String regexp, String select, String replace, String defaultValue,
                                       Boolean casesensitive, Boolean global)
    {
        super();
        this.regexp = regexp;
        this.select = select;
        this.replace = replace;
        this.defaultValue = defaultValue;
        this.casesensitive = casesensitive;
        this.global = global;
    }

    public RegularExpressionFilterImpl(String regexp, String select, String defaultValue,
                                       Boolean casesensitive)
    {
        this(regexp, select, null, defaultValue, casesensitive, null);
    }

    public RegularExpressionFilterImpl(String regexp, String replace, String defaultValue,
                                       Boolean casesensitive, Boolean global)
    {
        this(regexp, null, replace, defaultValue, casesensitive, global);
    }

    public void validate() throws Exception
    {
        if (this.regexp == null || this.regexp.length() <= 0)
        {
            throw new Exception("No or empty regular expression defined");
        }
        if (this.select == null && this.replace == null)
        {
            throw new Exception("Exactly one of both select or replace expression required");
        }
        if (this.select != null && this.replace != null)
        {
            throw new Exception("Expected only one of both select or replace expression");
        }
    }

    public String getRegexp()
    {
        return regexp;
    }

    public void setRegexp(String regexp)
    {
        this.regexp = regexp;
    }

    public String getSelect()
    {
        return select;
    }

    public void setSelect(String select)
    {
        this.select = select;
    }

    public String getReplace()
    {
        return replace;
    }

    public void setReplace(String replace)
    {
        this.replace = replace;
    }

    public String getDefaultValue()
    {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    public Boolean getCasesensitive()
    {
        return casesensitive;
    }

    public void setCasesensitive(Boolean casesensitive)
    {
        this.casesensitive = casesensitive;
    }

    public Boolean getGlobal()
    {
        return global;
    }

    public void setGlobal(Boolean global)
    {
        this.global = global;
    }
}
