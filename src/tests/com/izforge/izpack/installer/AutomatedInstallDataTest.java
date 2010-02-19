/*
 * IzPack - Copyright 2001-2006 Julien Ponge, All Rights Reserved.
 * 
 * http://www.izforge.com/izpack/ http://izpack.codehaus.org/
 * 
 * Copyright 2007 Dennis Reil
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
package com.izforge.izpack.installer;

import com.izforge.izpack.adaptator.IXMLElement;
import junit.framework.TestCase;

public class AutomatedInstallDataTest extends TestCase
{
    public void testContruction()
    {
        AutomatedInstallData auto = new AutomatedInstallData();
        IXMLElement xmlElement = auto.xmlData;
        assertEquals("AutomatedInstallation", xmlElement.getName());
    }
}