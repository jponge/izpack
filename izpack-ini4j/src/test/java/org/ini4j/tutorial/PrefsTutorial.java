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
package org.ini4j.tutorial;

import org.ini4j.Ini;
import org.ini4j.IniPreferences;

import org.ini4j.test.DwarfsData;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import java.util.prefs.Preferences;

//<editor-fold defaultstate="collapsed" desc="apt documentation">
//|
//|                -------------
//|                Preferences Tutorial
//|
//|Preferences Tutorial
//|
//| The purpose of this document is to familiarize the reader with the usage of
//| the [ini4j] library's Preferences interface. Each chapter contains all the
//| necessary code portions and explanation for a given function.
//|
//| Code sniplets in this tutorial tested with the following .ini file:
//| {{{../sample/dwarfs.ini.html}dwarfs.ini}}
//|
//| As soon as the Preferences object is created it functions as a standard Preferences node, and should be
//| used as such. Implicitly only new nodes can be created in the root node (these will be the sections).
//| In the first level nodes (sections) only values can be created (these will be the options).
//|
//| In the case of an invalid operation an <<<UnsupportedOperationException>>> type runtime exception is generated.
//| This happens if we try to set a value at the root node or to create a node on the second level,
//| since these operations cannot be interpreted on the whole .ini file under Preferences.
//|
//</editor-fold>
public class PrefsTutorial extends AbstractTutorial
{
    public static void main(String[] args) throws Exception
    {
        new PrefsTutorial().run(filearg(args));
    }

    protected void run(File arg) throws Exception
    {
        Ini ini = new Ini(arg.toURI().toURL());

        sample01(ini);
    }

//|
//|* Reading and writing values
//|
//| Values can read and write like any other Preferences node, there is no
//| differences.
//{
    void sample01(Ini ini) throws IOException
    {
        Preferences prefs = new IniPreferences(ini);
        Preferences bashful = prefs.node("bashful");
        String home = bashful.get("homeDir", "/home");
        int age = bashful.getInt("age", -1);

        bashful.putDouble("weight", 55.6);

//}
        assertEquals(DwarfsData.bashful.homeDir, bashful.get("homeDir", null));
        assertEquals(DwarfsData.bashful.age, bashful.getInt("age", -1));
        assertEquals(55.6, bashful.getDouble("weight", -1), 0.001);
    }
}
