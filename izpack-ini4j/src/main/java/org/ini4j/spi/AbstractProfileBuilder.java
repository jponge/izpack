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
package org.ini4j.spi;

import org.ini4j.Config;
import org.ini4j.Ini;
import org.ini4j.Profile;

abstract class AbstractProfileBuilder implements IniHandler
{
    private Profile.Section _currentSection;
    private boolean _header;
    private String _lastComment;

    @Override public void endIni()
    {

        // comment only .ini files....
        if ((_lastComment != null) && _header)
        {
            getProfile().setComment(_lastComment);
        }
    }

    @Override public void endSection()
    {
        _currentSection = null;
    }

    @Override public void handleComment(String comment)
    {
        if ((_lastComment != null) && _header)
        {
            getProfile().setComment(_lastComment);
            _header = false;
        }

        _lastComment = comment;
    }

    @Override public void handleOption(String name, String value)
    {
        _header = false;
        if (getConfig().isMultiOption())
        {
            _currentSection.add(name, value);
        }
        else
        {
            _currentSection.put(name, value);
        }

        if (_lastComment != null)
        {
            _currentSection.putComment(name, _lastComment);
            _lastComment = null;
        }
    }

    @Override public void startIni()
    {
        _header = true;
    }

    @Override public void startSection(String sectionName)
    {
        if (getConfig().isMultiSection())
        {
            _currentSection = getProfile().add(sectionName);
        }
        else
        {
            Ini.Section s = getProfile().get(sectionName);

            _currentSection = (s == null) ? getProfile().add(sectionName) : s;
        }

        if (_lastComment != null)
        {
            if (_header)
            {
                getProfile().setComment(_lastComment);
            }
            else
            {
                getProfile().putComment(sectionName, _lastComment);
            }

            _lastComment = null;
        }

        _header = false;
    }

    abstract Config getConfig();

    abstract Profile getProfile();

    Profile.Section getCurrentSection()
    {
        return _currentSection;
    }
}
