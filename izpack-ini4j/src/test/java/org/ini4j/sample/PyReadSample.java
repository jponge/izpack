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
//|                                 ----------------------------------
//|                                 Using Python like ConfigParser API
//|
//|Using Python like ConfigParser API
//|
//| For Python programmers the simlest API to use is ConfigParser.
//| This sample presents accessing .ini files as Python ConfigParser objects.
//|
//| This sample program expect the .ini file as a command line argument.
//| If there is no such argument, it use the {{{dwarfs-py.ini.html}dwarfs-py.ini}}
//| file.
//|
//</editor-fold>
//{
import org.ini4j.ConfigParser;

public class PyReadSample
{
    public static final String FILENAME = "dwarfs-py.ini";

    public static void main(String[] args) throws Exception
    {
        String filename = (args.length > 0) ? args[0] : FILENAME;
        ConfigParser config = new ConfigParser();

        config.read(filename);
        for (String key : config.options("sleepy"))
        {
            System.out.println("sleepy/" + key + " = " + config.get("sleepy", key));
        }
    }
}
//}
