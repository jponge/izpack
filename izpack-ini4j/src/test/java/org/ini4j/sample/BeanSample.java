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
//|                          ----------
//|                          BeanSample
//|
//|BeanSample
//|
//| Accessing the whole .ini file as Java Beans-style beans.
//|
//| This sample program expect the .ini file as a command line argument.
//| If there is no such argument, it use the {{{dwarfs.ini.html}dwarfs.ini}} file.
//|
//| Source code for beans: {{{Dwarf.java.html}Dwarf}},
//| {{{Dwarfs.java.html}Dwarfs}}
//|
//</editor-fold>
//{
import org.ini4j.Ini;

import java.io.FileInputStream;

public class BeanSample
{
    public static final String FILENAME = "dwarfs.ini";

    public static void main(String[] args) throws Exception
    {
        String filename = (args.length > 0) ? args[0] : FILENAME;
        Dwarfs dwarfs = new Ini(new FileInputStream(filename)).as(Dwarfs.class);
        Dwarf happy = dwarfs.getHappy();
        Dwarf doc = dwarfs.getDoc();

        System.out.println("Happy's age: " + happy.getAge());
        doc.setAge(44);
        System.out.println("Doc's age: " + doc.getAge());
    }
}
//}
