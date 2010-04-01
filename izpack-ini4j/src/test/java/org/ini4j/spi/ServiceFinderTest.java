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
package org.ini4j.spi;

import org.ini4j.test.Helper;

import org.junit.AfterClass;

import static org.junit.Assert.*;

import org.junit.Test;

public class ServiceFinderTest
{
    static final String DUMMY = "dummy";
    static final String DUMMY_SERVICE = "org.ini4j.Dummy";
    static final String BAD_CONFIG_SERVICE = "org.ini4j.BadConfig";
    static final String EMPTY_CONFIG_SERVICE = "org.ini4j.EmptyConfig";
    static final String DUMMY_IMPL = "DummyImpl";

    @AfterClass public static void tearDownClass() throws Exception
    {
        Helper.resetConfig();
    }

    @Test public void testFindService() throws Exception
    {
        boolean flag = false;

        System.setProperty(IniParser.class.getName(), Helper.class.getName());
        try
        {
            ServiceFinder.findService(IniParser.class);
        }
        catch (IllegalArgumentException x)
        {
            flag = true;
        }

        // System.clearProperty(IniParser.SERVICE_ID); missing in 1.4
        System.getProperties().remove(IniParser.class.getName());
        assertTrue(flag);
    }

    @Test public void testFindServiceClass() throws Exception
    {
        boolean flag = false;

        System.setProperty(IniParser.class.getName(), DUMMY);
        try
        {
            ServiceFinder.findServiceClass(IniParser.class);
        }
        catch (IllegalArgumentException x)
        {
            flag = true;
        }

        // System.clearProperty(IniParser.SERVICE_ID); missing in 1.4
        System.getProperties().remove(IniParser.class.getName());
        assertTrue(flag);
    }

    @Test public void testFindServiceClassName() throws Exception
    {
        System.setProperty(IniParser.class.getName(), DUMMY);
        assertEquals(DUMMY, ServiceFinder.findServiceClassName(IniParser.class.getName()));

        // System.clearProperty(IniParser.SERVICE_ID); missing in 1.4
        System.getProperties().remove(IniParser.class.getName());
        assertNull(ServiceFinder.findServiceClassName(IniParser.class.getName()));
        assertEquals(DUMMY_IMPL, ServiceFinder.findServiceClassName(DUMMY_SERVICE));
        assertNull(DUMMY, ServiceFinder.findServiceClassName(BAD_CONFIG_SERVICE));
        assertNull(DUMMY, ServiceFinder.findServiceClassName(EMPTY_CONFIG_SERVICE));
    }
}
