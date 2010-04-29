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

import org.ini4j.Reg;

import org.ini4j.sample.Dwarf;

import org.ini4j.test.DwarfsData;
import org.ini4j.test.Helper;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import java.net.URI;

//<editor-fold defaultstate="collapsed" desc="apt documentation">
//|
//|                -------------
//|                Reg Tutorial
//|
//|Reg Tutorial - Windows .REG file handling
//|
//| Windows regedit commands .REG file format is very close to .ini format.
//| \[ini4j\] provides org.ini4j.Reg class to model .REG format. This tutorial
//| show the differences between Ini and Reg classes.
//|
//| Code sniplets in this tutorial tested with the following .reg file:
//| {{{../sample/dwarfs.reg.html}dwarfs.reg}}
//|
//</editor-fold>
public class RegTutorial extends AbstractTutorial
{
    public static final String FILENAME = "../sample/dwarfs.reg";

    public static void main(String[] args) throws Exception
    {
        new RegTutorial().run(filearg(args));
    }

    @Override protected void run(File arg) throws Exception
    {
        Reg reg = new Reg(arg.toURI().toURL());

        sample01(arg);
        sample02();
    }

//|
//|* Loading and storing
//|
//| There is nothing special with loading and storing data, it works exactly same
//| as in Ini class. But while loading data, Reg class will strip .REG special
//| values (double qoute around strings, type data from option, etc). So after
//| loading a .REG file, you can use it exactly same way as Ini class. Ofcource
//| if you store Reg class, it will put all above meta information int file, so
//| the result will be a valid .REG file. You don't need to worry about file
//| encoding, version in first line, etc,etc.
//|
//| Assume you have a .REG file, with the following section/key:
//|
//|+---+
//|[HKEY_CURRENT_USER\Software\ini4j-test\dwarfs\bashful]
//|@="bashful"
//|"weight"=hex(2):34,00,35,00,2e,00,37,00,00,00
//|"height"="98.8"
//|"age"=dword:00000043
//|"homePage"="http://snowwhite.tale/~bashful"
//|"homeDir"="/home/bashful"
//|+---+
//|
//| As you see, "weight" and "age" is not simlpe strings. The "height" is a REG_DWORD
//| type while "weight" is REG_EXPAND_SZ. Don't worry, Reg class take care about
//| type conversion, you will access these as with regular .ini files:
//{
    void sample01(File file) throws IOException
    {
        Reg reg = new Reg(file);
        Reg.Key hive = reg.get(Reg.Hive.HKEY_CURRENT_USER.toString());
        Reg.Key bashful;

        bashful = hive.lookup("Software", "ini4j-test", "dwarfs", "bashful");

        // or ...
        bashful = hive.lookup("Software\\ini4j-test\\dwarfs\\bashful");

        // or even...
        bashful = reg.get("HKEY_CURRENT_USER\\Software\\ini4j-test\\dwarfs\\bashful");

        // read some data
        double weight = bashful.get("weight", double.class);  // = 45.7
        double height = bashful.get("height", double.class);  // = 98.8
        int age = bashful.get("age", int.class);  // = 67
        URI homePage = bashful.get("homePage", URI.class);  // = new URI("http://snowwhite.tale/~bashful");
        String homeDir = bashful.get("homeDir");  // = "/home/bashful"

//}
        assertNotNull(reg.get(Helper.DWARFS_REG_PATH + "\\dwarfs"));
        Helper.assertEquals(DwarfsData.bashful, bashful.as(Dwarf.class));
    }

//|
//|* Types
//|
//| When you load data into Reg class, it will preserve meta informations, such as
//| type informations. If you create new values, by default these will have
//| tpye REG_SZ. Ofcource you may specify type information for values.
//{
    void sample02()
    {
        Reg reg = new Reg();
        Reg.Key key = reg.add("HKEY_CURRENT_USER\\Software\\ini4j-test\\dwarfs\\sleepy");

        key.put("fortuneNumber", 99);
        key.putType("fortuneNumber", Reg.Type.REG_MULTI_SZ);

//}
//|
//| If you store reg object above, it will contains a section similar to this:
//|
//|+---+
//|[HKEY_CURRENT_USER\Software\ini4j-test\dwarfs\sleepy]
//|"fortuneNumber"=hex(7):39,00,39,00,00,00,00,00
//|+---+
//|
    }
}
