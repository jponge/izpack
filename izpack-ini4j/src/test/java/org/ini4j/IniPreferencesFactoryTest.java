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
package org.ini4j;

import org.ini4j.test.Helper;

import org.junit.AfterClass;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.prefs.Preferences;

public class IniPreferencesFactoryTest
{
    private static final String DUMMY = "dummy";

    @AfterClass public static void tearDownClass() throws Exception
    {
        Helper.resetConfig();
    }

    @Test public void testGetIniLocation() throws Exception
    {
        IniPreferencesFactory factory = new IniPreferencesFactory();

        System.setProperty(DUMMY, DUMMY);
        assertEquals(DUMMY, factory.getIniLocation(DUMMY));
        System.clearProperty(DUMMY);
        assertNull(factory.getIniLocation(DUMMY));
    }

    @SuppressWarnings("empty-statement")
    @Test public void testGetResourceAsStream() throws Exception
    {
        IniPreferencesFactory factory = new IniPreferencesFactory();

        // class path
        assertNotNull(factory.getResourceAsStream(Helper.DWARFS_INI));

        // url
        String location = Helper.getResourceURL(Helper.DWARFS_INI).toString();

        assertNotNull(factory.getResourceAsStream(location));

        // invalid url should throw IllegalArgumentException
        try
        {
            factory.getResourceAsStream("http://");
            fail();
        }
        catch (IllegalArgumentException x)
        {
            ;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNewIniPreferences()
    {
        System.setProperty(DUMMY, DUMMY);
        try
        {
            new IniPreferencesFactory().newIniPreferences(DUMMY);
        }
        finally
        {
            System.clearProperty(DUMMY);
        }
    }

    @Test public void testSystemRoot() throws Exception
    {
        Preferences prefs = Preferences.systemRoot();

        assertNotNull(prefs);
        assertEquals(IniPreferences.class, prefs.getClass());
        assertSame(prefs, Preferences.systemRoot());
    }

    @Test public void testUserRoot() throws Exception
    {
        Preferences prefs = Preferences.userRoot();

        assertNotNull(prefs);
        assertEquals(IniPreferences.class, prefs.getClass());
        assertSame(prefs, Preferences.userRoot());
    }
}
