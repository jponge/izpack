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

import org.ini4j.Wini;

import org.ini4j.sample.Dwarf;
import org.ini4j.sample.Dwarfs;

import org.ini4j.test.DwarfsData;
import org.ini4j.test.Helper;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

//<editor-fold defaultstate="collapsed" desc="apt documentation">
//|
//|                -------------------
//|                One minute Tutorial
//|
//|One minute Tutorial - First step
//|
//| First step with \[ini4j\] library. No data model, no interfaces, no design
//| patterns, simply read and write windows .ini files.
//|
//</editor-fold>
public class OneMinuteTutorial extends AbstractTutorial
{
    public static void main(String[] args) throws Exception
    {
        new OneMinuteTutorial().run(filearg(args));
    }

    protected void copy(File inputFile, File outputFile) throws IOException
    {
        FileInputStream is = new FileInputStream(inputFile);
        FileOutputStream os = new FileOutputStream(outputFile);
        byte[] buff = new byte[8192];
        int n;

        while ((n = is.read(buff)) > 0)
        {
            os.write(buff, 0, n);
        }

        is.close();
        os.close();
    }

    @Override protected void run(File arg) throws Exception
    {
        File file = File.createTempFile("tutorial", ".ini");

        file.deleteOnExit();
        copy(arg, file);
        sample01(file.getCanonicalPath());
        sample02(file.getCanonicalPath());
    }

//|
//| Lets read some value from .ini file...
//|
//{
    void sample01(String filename) throws IOException
    {
        Wini ini = new Wini(new File(filename));
        int age = ini.get("happy", "age", int.class);
        double height = ini.get("happy", "height", double.class);
        String dir = ini.get("happy", "homeDir");

//}
//| ... assuming there is a section with name <<<happy>>>, which contains at least
//| the following options: <<<age>>>, <<<height>>> and <<<homeDir>>>, something like
//| this:
//|
//|+---------+
//| [happy]
//| age = 99
//| height = 77.66
//| homeDir = /home/happy
//|+---------+
//|
//|
        assertEquals(DwarfsData.happy.age, age);
        assertEquals(DwarfsData.happy.height, height, Helper.DELTA);
        assertEquals(DwarfsData.happy.homeDir, dir);
    }

//| Now let see how to write values....
//|
//{
    void sample02(String filename) throws IOException
    {
        Wini ini = new Wini(new File(filename));

        ini.put("sleepy", "age", 55);
        ini.put("sleepy", "weight", 45.6);
        ini.store();

//}
//| ... and then file will have a section <<<sleepy>>> and this section
//| will contains at least two options: <<<age>>> with value <<<55>>> and <<<weight>>>
//| with value <<<45.6>>>, something like this:
//|
//|+---------+
//| [sleepy]
//| age = 55
//| weight = 45.6
//|+---------+
//|
        assertEquals(55, (int) ini.get(Dwarfs.PROP_SLEEPY, Dwarf.PROP_AGE, int.class));
        assertEquals(45.6, (double) ini.get(Dwarfs.PROP_SLEEPY, Dwarf.PROP_WEIGHT, double.class), Helper.DELTA);
    }

//|
//| If you want to know more about this library, read
//| {{{../tutorial/index.html}tutorials}}
}
