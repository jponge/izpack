/*
 * Copyright 2005,2009 Ivan SZKIBA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ini4j;

import org.ini4j.spi.IniHandler;
import org.ini4j.spi.RegEscapeTool;
import org.ini4j.spi.TypeValuesPair;

public class BasicRegistry extends BasicProfile implements Registry
{
    private static final long serialVersionUID = -6432826330714504802L;
    private String _version;

    public BasicRegistry()
    {
        _version = VERSION;
    }

    public String getVersion()
    {
        return _version;
    }

    public void setVersion(String value)
    {
        _version = value;
    }

    public Key add(String name)
    {
        return (Key) super.add(name);
    }

    public Key get(Object key)
    {
        return (Key) super.get(key);
    }

    public Key get(Object key, int index)
    {
        return (Key) super.get(key, index);
    }

    public Key put(String key, Section value)
    {
        return (Key) super.put(key, value);
    }

    public Key put(String key, Section value, int index)
    {
        return (Key) super.put(key, value, index);
    }

    public Key remove(Section section)
    {
        return (Key) super.remove(section);
    }

    public Key remove(Object key)
    {
        return (Key) super.remove(key);
    }

    public Key remove(Object key, int index)
    {
        return (Key) super.remove(key, index);
    }

    Key newSection(String name)
    {
        return new BasicRegistryKey(this, name);
    }

    void store(IniHandler formatter, Section section, String option)
    {
        store(formatter, section.getComment(option));
        Type type = ((Key) section).getType(option, Type.REG_SZ);
        String rawName = option.equals(Key.DEFAULT_NAME) ? option : RegEscapeTool.getInstance().quote(option);
        String[] values = new String[section.length(option)];

        for (int i = 0; i < values.length; i++)
        {
            values[i] = section.get(option, i);
        }

        String rawValue = RegEscapeTool.getInstance().encode(new TypeValuesPair(type, values));

        formatter.handleOption(rawName, rawValue);
    }
}
