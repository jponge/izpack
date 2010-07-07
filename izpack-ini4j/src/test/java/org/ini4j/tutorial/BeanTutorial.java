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

import org.ini4j.Config;
import org.ini4j.Ini;
import org.ini4j.Options;

import org.ini4j.sample.Dwarf;
import org.ini4j.sample.DwarfBean;
import org.ini4j.sample.Dwarfs;

import org.ini4j.test.DwarfsData;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import java.net.URI;
import java.net.URL;

//<editor-fold defaultstate="collapsed" desc="apt documentation">
//|
//|                -------------
//|                Bean Tutorial
//|
//|Bean Tutorial - Using your own API !
//|
//| Yes, it can be done! To access the contents of sections you can use any of
//| your self-defined Java Beans compatible API.
//| In order to do this you only have to create a Java Beans-style interface or class.
//|
//| Source code for beans: {{{../sample/Dwarf.java.html}Dwarf}},
//| {{{../sample/DwarfBean.java.html}DwarfBean}}
//|
//| Code sniplets in this tutorial tested with the following .ini file:
//| {{{../sample/dwarfs.ini.html}dwarfs.ini}}
//|
//</editor-fold>
public class BeanTutorial extends AbstractTutorial
{
    public static void main(String[] args) throws Exception
    {
        new BeanTutorial().run(filearg(args));
    }

    @Override protected void run(File arg) throws Exception
    {
        Ini ini = new Ini(arg.toURI().toURL());

        sample01(ini);
        sample02(ini);
        sample03(ini);
        sample04(arg.toURI().toURL());
        Options opts = new Options();

        opts.putAll(ini.get(Dwarfs.PROP_BASHFUL));
        sample05(opts);

        //
        File optFile = new File(arg.getParentFile(), OptTutorial.FILENAME);

        sample06(optFile.toURI().toURL());
    }

//|
//|* Accessing sections as beans
//|
//| While writing a program we usually know the type of the section's values,
//| so we can define one or more java interfaces to access them. An advantage of
//| this solution is that the programmer doesn't have to convert the values
//| because they are converted automatically to the type defined in the
//| interface.
//|
//| Ofcourse you may use setters as well, not just getters. In this way you can
//| change values type safe way.
//{
    void sample01(Ini ini)
    {
        Ini.Section sec = ini.get("happy");
        Dwarf happy = sec.as(Dwarf.class);
        int age = happy.getAge();
        URI homePage = happy.getHomePage();

        happy.setWeight(45.55);

//}
//|
//| The <<<happy instanceof Dwarf>>> relation is of course fulfilled in the
//| example above.
//|
        assertEquals(DwarfsData.happy.homePage.toString(), homePage.toString());
        assertEquals(DwarfsData.happy.age, age);
        assertEquals(45.55, happy.getWeight(), 0.01);
    }

//|
//|* Marshalling beans
//|
//| Sometimes we want to store existing java beans in text file. This operation
//| usually called marshalling.
//|
//| With [ini4j] it is easy to store bean properties in a section. You simply
//| create a section, and call the sections's <<<from()>>> method. Thats it.
//{
    void sample02(Ini ini)
    {
        DwarfBean sleepy = new DwarfBean();

        sleepy.setAge(87);
        sleepy.setWeight(44.3);
        Ini.Section sec = ini.add("sleepy");

        sec.from(sleepy);

//}
//|
        assertTrue(sec.containsKey(Dwarf.PROP_AGE));
        assertTrue(sec.containsKey(Dwarf.PROP_WEIGHT));
    }

//|
//|* Unmarshalling beans
//|
//| If you have a marshalled bean in text file then you may want to read it
//| into bean. This operation usually called unmarshalling.
//|
//| With [ini4j] it is easy to load bean properties from a section. You simply
//| instantiate a bean, and call the sections's <<<to()>>> method. Thats it.
//{
    void sample03(Ini ini)
    {
        DwarfBean grumpy = new DwarfBean();

        ini.get("grumpy").to(grumpy);

//}
//|
        assertEquals(DwarfsData.grumpy.age, grumpy.getAge());
        assertEquals(DwarfsData.grumpy.homeDir, grumpy.getHomeDir());
    }

//|
//|* Indexed properties
//|
//| For handling indexed properties, you should allow mulpti option value
//| handling in configuration. After enabling this feature, option may contains
//| multiply values (multi line in file). These values can mapped to indexed
//| bean property.
//{
    void sample04(URL location) throws IOException
    {
        Config cfg = new Config();

        cfg.setMultiOption(true);
        Ini ini = new Ini();

        ini.setConfig(cfg);
        ini.load(location);
        Ini.Section sec = ini.get("sneezy");
        Dwarf sneezy = sec.as(Dwarf.class);
        int[] numbers = sneezy.getFortuneNumber();

        //
        // same as above but with unmarshalling...
        //
        DwarfBean sneezyBean = new DwarfBean();

        sec.to(sneezyBean);
        numbers = sneezyBean.getFortuneNumber();

//}
        assertArrayEquals(DwarfsData.sneezy.fortuneNumber, numbers);
        assertEquals(DwarfsData.sneezy.fortuneNumber.length, sec.length("fortuneNumber"));
        assertArrayEquals(DwarfsData.sneezy.fortuneNumber, sneezy.getFortuneNumber());
        assertArrayEquals(DwarfsData.sneezy.fortuneNumber, sneezyBean.getFortuneNumber());
    }

//|
//|* Options
//|
//| Not only Ini and Ini.Section has bean interface. There is a bean interface
//| for OptionMap class and each derived class for example for Options.
//| Options is an improved java.util.Properties replacement.
//{
    void sample05(Options opts)
    {
        Dwarf dwarf = opts.as(Dwarf.class);
        int age = dwarf.getAge();

        //
        // same as above but with unmarshalling
        //
        DwarfBean dwarfBean = new DwarfBean();

        opts.to(dwarfBean);
        age = dwarfBean.getAge();

//}
//|
//| In sample above the top level properties (like "age") mapped to bean
//| properties.
//|
        assertEquals(DwarfsData.bashful.age, dwarf.getAge());
        assertEquals(DwarfsData.bashful.age, dwarfBean.getAge());
    }

//|
//|* Prefixed mapping
//|
//| Both Ini.Section and Options has possibility to add a prefix to property
//| names while mapping from bean property name to Ini.Section or Options
//| key.
//{
    void sample06(URL optPath) throws IOException
    {
        Options opt = new Options(optPath);
        Dwarf dwarf = opt.as(Dwarf.class, "happy.");
        DwarfBean bean = new DwarfBean();

        opt.to(bean, "dopey.");

//}
//|
//| In the above example, <<<dwarf>>> bean will contain properties starts with
//| <<<happy.>>> while <<<bean>>> will contain properties starts with
//| <<<dopey.>>>
        assertEquals(DwarfsData.happy.age, dwarf.getAge());
        assertEquals(DwarfsData.dopey.age, bean.getAge());
    }
//}
}
