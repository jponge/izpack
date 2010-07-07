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
package org.ini4j.sample;

//<editor-fold defaultstate="collapsed" desc="apt documentation">
//|
//|                             --------------
//|                             ListenerSample
//|
//|ListenerSample
//|
//| This advanced sample presents the event-controlled usage. The \[ini4j\]
//| library fully supports the events defined in Preferences API.
//|
//| This sample program expect the .ini file as a command line argument.
//| If there is no such argument, it use the {{{dwarfs.ini.html}dwarfs.ini}} file.
//|
//</editor-fold>
//{
import org.ini4j.IniPreferences;

import java.io.FileInputStream;

import java.util.prefs.*;

public class ListenerSample
{
    public static final String FILENAME = "dwarfs.ini";

    public static void main(String[] args) throws Exception
    {
        String filename = (args.length > 0) ? args[0] : FILENAME;
        Preferences prefs = new IniPreferences(new FileInputStream(filename));
        Listener listener = new Listener();

        prefs.addNodeChangeListener(listener);
        Preferences jerry = prefs.node("jerry");

        jerry.addPreferenceChangeListener(listener);
        jerry.put("color", "blue");
        jerry.put("color", "gray");
        jerry.putInt("age", 2);
    }

    static class Listener implements NodeChangeListener, PreferenceChangeListener
    {
        public void childAdded(NodeChangeEvent event)
        {
            System.out.println("node added: " + event.getChild().name());
        }

        public void childRemoved(NodeChangeEvent event)
        {
            System.out.println("node removed: " + event.getChild().name());
        }

        public void preferenceChange(PreferenceChangeEvent event)
        {
            System.out.println("preference changed: " + event.getKey() + " = " + event.getNewValue());
        }
    }
}
//}
