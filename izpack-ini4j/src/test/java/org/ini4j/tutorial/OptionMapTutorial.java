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

import org.ini4j.sample.Dwarfs;

import org.ini4j.test.DwarfsData;

import static org.junit.Assert.*;

import java.io.File;

import java.util.Set;

//<editor-fold defaultstate="collapsed" desc="apt documentation">
//|
//|                -------------
//|                OptionMap Tutorial
//|
//|OptionMap Tutorial - more than just String,String map
//|
//| Option is a name/value pair stored in OptionMap. But OptionMap adds a lot of
//| usefull data access methods than simple get. OptionMap is base interface for
//| both Ini.Section, Registry.Key and Options classes, so this tutorial will
//| usefull for all of these.
//|
//| So in samples bellow you can use either Ini.Section, Registry.Key or Options
//| classes instead of OptionMap interface, because these classes implements
//| OptionMap.
//|
//| Code sniplets in this tutorial tested with the following files:
//| {{{../sample/dwarfs.ini.html}dwarfs.ini}}
//| {{{../sample/dwarfs.opt.html}dwarfs.opt}}
//|
//</editor-fold>
public class OptionMapTutorial extends AbstractTutorial
{
    public static void main(String[] args) throws Exception
    {
        new OptionMapTutorial().run(filearg(args));
    }

    @Override protected void run(File arg) throws Exception
    {
        Ini ini = new Ini(arg.toURI().toURL());

        sample01(ini.get(Dwarfs.PROP_HAPPY));
        sample03(ini);
        sample04(ini);
    }

//|* Data model
//|
//| OptionMap implements Map\<String,String\>, so you can access options using
//| standard collection api.
//{
    void sample01(Ini.Section section)
    {

        //
        // read some values
        //
        String age = section.get("age");
        String weight = section.get("weight");
        String homeDir = section.get("homeDir");

        // get all option names
        Set<String> optionNames = section.keySet();

//}
        assertEquals(String.valueOf(DwarfsData.happy.age), age);
        assertEquals(String.valueOf(DwarfsData.happy.weight), weight);
        assertEquals(String.valueOf(DwarfsData.happy.homeDir), homeDir);
    }

//|
//|* Macro/variable substitution
//|
//| To get a value, besides <<<get()>>> you can also
//| use <<<fetch()>>> which resolves any occurrent $\{section/option\} format
//| variable references in the needed value.
//|
//{
    void sample03(Ini ini)
    {
        Ini.Section dopey = ini.get("dopey");

        // get method doesn't resolve variable references
        String weightRaw = dopey.get("weight");  // = ${bashful/weight}
        String heightRaw = dopey.get("height");  // = ${doc/height}

        // to resolve references, you should use fetch method
        String weight = dopey.fetch("weight");  // = 45.7
        String height = dopey.fetch("height");  // = 87.7

//}
//| Assuming we have an .ini file with the following sections:
//|
//|+--------------+
//| [dopey]
//| weight = ${bashful/weight}
//| height = ${doc/height}
//|
//|[bashful]
//| weight = 45.7
//| height = 98.8
//|
//| [doc]
//| weight = 49.5
//| height = 87.7
//|+--------------+
//|
        assertEquals(DwarfsData.INI_DOPEY_WEIGHT, weightRaw);
        assertEquals(DwarfsData.INI_DOPEY_HEIGHT, heightRaw);
        assertEquals(String.valueOf(DwarfsData.dopey.weight), weight);
        assertEquals(String.valueOf(DwarfsData.dopey.height), height);
    }

//|
//|* Multi values
//|
//| \[ini4j\] library introduces MultiMap interface, which is extends normal
//| Map, but allows multiply values per keys. You can simply index values for
//| a given key, similar to indexed properties in JavaBeans api.
//|
//{
    void sample04(Ini ini)
    {
        Ini.Section sneezy = ini.get("sneezy");
        String n1 = sneezy.get("fortuneNumber", 0);  // = 11
        String n2 = sneezy.get("fortuneNumber", 1);  // = 22
        String n3 = sneezy.get("fortuneNumber", 2);  // = 33
        String n4 = sneezy.get("fortuneNumber", 3);  // = 44

        // ok, lets do in it easier...
        int[] n = sneezy.get("fortuneNumber", int[].class);
//}
    }
}
