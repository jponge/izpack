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

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

//<editor-fold defaultstate="collapsed" desc="apt documentation">
//|
//|                -------------------------
//|                Windows Registry Tutorial
//|
//|Windows Registry Tutorial - Read/Write windows registry
//|
//| Yes, it is possible now to read/write registry from java programs
//| without native (JNI) code !
//|
//</editor-fold>
public class WindowsRegistryTutorial extends AbstractTutorial
{
    public static final String FILENAME = "../sample/dwarfs.reg";

    public static void main(String[] args) throws Exception
    {
        if (Reg.isWindows())
        {
            new WindowsRegistryTutorial().run(filearg(args));
        }
    }

    @Override protected void run(File arg) throws Exception
    {
        sample01();
        sample02();
        sample03();
    }

//|
//|* Write
//|
//| Lets write something to registry
//{
    void sample01() throws IOException
    {
        Reg reg = new Reg();
        Reg.Key key = reg.add("HKEY_CURRENT_USER\\hello");

        key.put("world", "Hello World !");
        reg.write();
//}
//| This code will create a "hello" key in HKEY_CURRENT_USER hive, and
//| put "Hello World !" with name "world".
    }

//|
//|* Read
//|
//| Lets read something from Control Panel settings...
//{
    void sample02() throws IOException
    {
        Reg reg = new Reg("HKEY_CURRENT_USER\\Control Panel");
        Reg.Key cp = reg.get("HKEY_CURRENT_USER\\Control Panel");
        Reg.Key sound = cp.getChild("Sound");
        String beep = sound.get("Beep");

//}
//|
    }

//|
//|* Create environment variable
//|
//| Lets create a new environment variable under current users environment....
//{
    void sample03() throws IOException
    {
        Reg reg = new Reg();
        Reg.Key env = reg.add("HKEY_CURRENT_USER\\Environment");

        env.put("SAMPLE_HOME", "c:\\sample");
        reg.write();
//}
//| Thats it ! Now your environment contains variable SAMPLE_HOME ! Unfortunetly
//| you have to restart Windows to see this variable.... but hey, we crated new
//| environment variable from java without any native code !
    }
}
