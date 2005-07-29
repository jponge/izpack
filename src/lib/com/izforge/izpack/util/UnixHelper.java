/*
 * IzPack - Copyright 2001-2005 Julien Ponge, All Rights Reserved.
 * 
 * http://www.izforge.com/izpack/ http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2004 Marc Eppelmann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.izforge.izpack.util;

/**
 * Help Methods for unix-systems and derived.
 * 
 * @author marc.eppelmann&#064;reddot.de
 */
public class UnixHelper
{

    /**
     * Testmain
     * 
     * @param args commandline args
     */
    public static void main(String[] args)
    {
        kdeIsInstalled();
    }

    /**
     * Test if KDE is installed.
     * This is done by $>/usr/bin/env konqueror --version
     * 
     * This assumes that the konqueror as a main-app of kde is already installed. 
     * 
     * If this returns with 0 konqeror and resp. kde means to be installed, 
     * 
     * @return true if kde is installed otherwise false.
     */
    public static boolean kdeIsInstalled()
    {
        FileExecutor fe = new FileExecutor();

        String[] execOut = new String[2];

        int execResult = fe.executeCommand(
                new String[] { "/usr/bin/env", "konqueror", "--version"}, execOut);

        return execResult == 0;
    }
}
