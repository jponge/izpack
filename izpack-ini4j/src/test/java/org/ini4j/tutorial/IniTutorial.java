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

import org.ini4j.sample.Dwarf;
import org.ini4j.sample.Dwarfs;

import org.ini4j.test.DwarfsData;
import org.ini4j.test.Helper;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.Map;
import java.util.Set;

//<editor-fold defaultstate="collapsed" desc="apt documentation">
//|
//|                -------------
//|                Ini Tutorial
//|
//|Ini Tutorial - How to use \[ini4j\] api
//|
//| This tutorial familiarize the reader with the usage of
//| the [ini4j] library's natural interface.
//|
//| Code sniplets in this tutorial tested with the following .ini file:
//| {{{../sample/dwarfs.ini.html}dwarfs.ini}}
//|
//</editor-fold>
public class IniTutorial extends AbstractTutorial
{
    public static void main(String[] args) throws Exception
    {
        new IniTutorial().run(filearg(args));
    }

    @Override protected void run(File arg) throws Exception
    {
        Ini ini = new Ini(arg.toURI().toURL());

        sample01(ini);
        sample02(arg);
        sample03(ini);
        sample04(ini);
    }

//|* Data model
//|
//| Data model for .ini files is represented by org.ini4j.Ini class. This class
//| implements Map\<String,Section\>. It mean you can access sections using
//| java.util.Map collection API interface. The Section is also a map, which is
//| implements Map\<String,String\>.
//{
    void sample01(Ini ini)
    {
        Ini.Section section = ini.get("happy");

        //
        // read some values
        //
        String age = section.get("age");
        String weight = section.get("weight");
        String homeDir = section.get("homeDir");

        //
        // .. or just use java.util.Map interface...
        //
        Map<String, String> map = ini.get("happy");

        age = map.get("age");
        weight = map.get("weight");
        homeDir = map.get("homeDir");

        // get all section names
        Set<String> sectionNames = ini.keySet();

//}
        Helper.assertEquals(DwarfsData.happy, section.as(Dwarf.class));
    }

//|
//|* Loading and storing data
//|
//| There is several way to load data into Ini object. It can be done by using
//| <<<load>>> methods or overloaded constructors. Data can be load from
//| InputStream, Reader, URL or File.
//|
//| You can store data using <<<store>>> methods. Data can store to OutputStream,
//| Writer, or File.
//{
    void sample02(File file) throws IOException
    {
        Ini ini = new Ini();

        ini.load(new FileReader(file));

        //
        // or instantiate and load data:
        //
        ini = new Ini(new FileReader(file));
        File copy = File.createTempFile("sample", ".ini");

        ini.store(copy);
//}
        ini = new Ini(copy);
        Helper.assertEquals(DwarfsData.dwarfs, ini.as(Dwarfs.class));
        copy.delete();
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
        //   int[] n = sneezy.get("fortuneNumber", int[].class);
//}
    }

//|
//|* Tree model
//|
//| Beyond two level map model, Ini class provides tree model. You can access
//| Sections as tree. It means that section names becomes path names, with a
//| path separator character ('/' and '\' on Wini and Reg).
//|
//{
    void sample05()
    {
        Ini ini = new Ini();

        // lets add a section, it will create needed intermediate sections as well
        ini.add("root/child/sub");

        //
        Ini.Section root;
        Ini.Section sec;

        root = ini.get("root");
        sec = root.getChild("child").getChild("sub");

        // or...
        sec = root.lookup("child", "sub");

        // or...
        sec = root.lookup("child/sub");

        // or even...
        sec = ini.get("root/child/sub");

//}
//| If you are using Wini instead of Ini class, the path separator become '\'.
//|
        assertNotNull(root.lookup("child", "sub"));
        assertNotNull(ini.get("root/child"));
    }
}
