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
//|                       ---------------
//|                       BeanEventSample
//|
//|BeanEventSample
//|
//| A sample that demonstrates the handling of the bound bean properties.
//|
//| This sample program expect the .ini file as a command line argument.
//| If there is no such argument, it use the {{{dwarfs.ini.html}dwarfs.ini}} file.
//|
//| Source code for bean: {{{Dwarf.java.html}Dwarf}},
//|
//</editor-fold>
//{
import org.ini4j.Ini;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.FileInputStream;

public class BeanEventSample
{
    public static final String FILENAME = "dwarfs.ini";

    public static void main(String[] args) throws Exception
    {
        String filename = (args.length > 0) ? args[0] : FILENAME;
        Ini ini = new Ini(new FileInputStream(filename));
        Dwarf sneezy = ini.get("sneezy").as(Dwarf.class);

        sneezy.addPropertyChangeListener("age", new PropertyChangeListener()
            {
                public void propertyChange(PropertyChangeEvent event)
                {
                    System.out.println("property " + event.getPropertyName() + " change");
                    System.out.println(event.getOldValue() + " -> " + event.getNewValue());
                }
            });
        System.out.println("Sneezy's age: " + sneezy.getAge());
        sneezy.setAge(44);
        System.out.println("Sneezy's age: " + sneezy.getAge());
    }
}
//}
