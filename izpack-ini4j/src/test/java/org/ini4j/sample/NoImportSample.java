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
//|                               --------------
//|                               NoImportSample
//|
//|NoImportSample
//|
//| Using [ini4j] without class level dependency (no Java imports). You may use
//| \[ini4j\] library as full Preferences implementation for user and system
//| root.
//|
//| This sample program expect the .ini file as a command line argument.
//| If there is no such argument, it use the {{{dwarfs.ini.html}dwarfs.ini}} file.
//|
//</editor-fold>
//{
import java.util.prefs.Preferences;

public class NoImportSample
{

    static
    {
        System.setProperty("java.util.prefs.PreferencesFactory", "org.ini4j.IniPreferencesFactory");

        // you should set file:///... like URL as property value to work
        System.setProperty("org.ini4j.prefs.user", "org/ini4j/sample/dwarfs.ini");
    }

    public static void main(String[] args) throws Exception
    {
        Preferences prefs = Preferences.userRoot();

        System.out.println("grumpy/homePage: " + prefs.node("grumpy").get("homePage", null));
        System.out.println(prefs.getClass());
    }
}
//}
