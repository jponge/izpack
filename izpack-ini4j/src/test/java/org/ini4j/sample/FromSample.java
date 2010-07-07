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
//|                               ----------
//|                               FromSample
//|
//|FromSample
//|
//| Marshall Java Beans to Ini.Section.
//|
//| This sample program expect the .ini file as a command line argument.
//| If there is no such argument, it use the {{{dwarfs.ini.html}dwarfs.ini}} file.
//|
//| Source code for beans: {{{Dwarf.java.html}Dwarf}},
//| {{{DwarfBean.java.html}DwarfBean}}
//|
//</editor-fold>
//{
import org.ini4j.Ini;

import java.net.URI;

public class FromSample
{
    public static void main(String[] args) throws Exception
    {
        Ini ini = new Ini();
        DwarfBean doc = new DwarfBean();

        doc.setAge(123);
        doc.setHeight(111);
        doc.setWeight(22.1);
        doc.setHomePage(new URI("http://foo.bar"));
        ini.add("doc").from(doc);
        ini.store(System.out);
    }
}
//}
