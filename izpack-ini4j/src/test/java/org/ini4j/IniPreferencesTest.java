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

import org.ini4j.sample.Dwarf;
import org.ini4j.sample.Dwarfs;

import org.ini4j.spi.BeanAccess;
import org.ini4j.spi.BeanTool;

import org.ini4j.test.DwarfsData;
import org.ini4j.test.Helper;
import org.ini4j.test.TaleData;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.prefs.Preferences;

public class IniPreferencesTest
{
    private static final String DUMMY = "dummy";

    @Test public void testConstructor() throws Exception
    {
        Ini ini = Helper.newDwarfsIni();
        IniPreferences prefs = new IniPreferences(ini);

        assertSame(ini, prefs.getIni());
        Helper.assertEquals(DwarfsData.dwarfs, ini.as(Dwarfs.class));
        prefs = new IniPreferences(Helper.getResourceStream(Helper.DWARFS_INI));
        Helper.assertEquals(DwarfsData.doc, newDwarf(prefs.node(Dwarfs.PROP_DOC)));
        prefs = new IniPreferences(Helper.getResourceReader(Helper.DWARFS_INI));
        Helper.assertEquals(DwarfsData.happy, newDwarf(prefs.node(Dwarfs.PROP_HAPPY)));
        prefs = new IniPreferences(Helper.getResourceURL(Helper.DWARFS_INI));
        Helper.assertEquals(DwarfsData.sleepy, newDwarf(prefs.node(Dwarfs.PROP_SLEEPY)));
    }

    @Test public void testMisc() throws Exception
    {
        Ini ini = new Ini();
        IniPreferences prefs = new IniPreferences(ini);

        // do nothing, but doesn't throw exception
        prefs.sync();
        prefs.flush();

        // node & key count
        assertEquals(0, prefs.keysSpi().length);
        assertEquals(0, prefs.childrenNamesSpi().length);

        // childNode for new and for existing section
        assertNotNull(prefs.node(Dwarfs.PROP_DOC));
        assertEquals(1, prefs.childrenNamesSpi().length);
        ini.add(Dwarfs.PROP_HAPPY);
        assertNotNull(prefs.node(Dwarfs.PROP_HAPPY));
        assertEquals(2, prefs.childrenNamesSpi().length);

        // SectionPreferences
        IniPreferences.SectionPreferences sec = (IniPreferences.SectionPreferences) prefs.node(Dwarfs.PROP_DOC);

        assertEquals(0, sec.childrenNamesSpi().length);

        // do nothing, but doesn't throw exception
        sec.sync();
        sec.syncSpi();
        sec.flush();
        sec.flushSpi();

        // empty
        assertEquals(0, sec.keysSpi().length);

        // add one key
        sec.put(Dwarf.PROP_AGE, "87");
        sec.flush();
        assertEquals("87", sec.getSpi(Dwarf.PROP_AGE));

        // has one key
        assertEquals(1, sec.keysSpi().length);

        // remove key
        sec.remove(Dwarf.PROP_AGE);
        sec.flush();

        // has 0 key
        assertEquals(0, sec.keysSpi().length);
        sec.removeNode();
        prefs.flush();
        assertNull(ini.get(Dwarfs.PROP_DOC));
    }

    @Test public void testTaleTree() throws Exception
    {
        Ini ini = Helper.newTaleIni();
        IniPreferences prefs = new IniPreferences(ini);
        Preferences dwarfs = prefs.node(TaleData.PROP_DWARFS);

        Helper.assertEquals(DwarfsData.doc, newDwarf(dwarfs.node(Dwarfs.PROP_DOC)));
        assertArrayEquals(DwarfsData.dwarfNames, dwarfs.childrenNames());
        assertEquals(1, prefs.childrenNames().length);
    }

    @Test public void testTree() throws Exception
    {
        Ini ini = new Ini();
        IniPreferences prefs = new IniPreferences(ini);
        IniPreferences.SectionPreferences sec = (IniPreferences.SectionPreferences) prefs.node(Dwarfs.PROP_DOC);
        Preferences child = sec.node(DUMMY);

        assertNotNull(child);
        assertNotNull(sec.node(DUMMY));
        assertNotNull(ini.get(Dwarfs.PROP_DOC).getChild(DUMMY));
        assertEquals(1, prefs.childrenNames().length);
    }

    @SuppressWarnings("empty-statement")
    @Test public void testUnsupported() throws Exception
    {
        Ini ini = new Ini();
        IniPreferences prefs = new IniPreferences(ini);

        try
        {
            prefs.getSpi(DUMMY);
            fail();
        }
        catch (UnsupportedOperationException x)
        {
            ;
        }

        try
        {
            prefs.putSpi(DUMMY, DUMMY);
            fail();
        }
        catch (UnsupportedOperationException x)
        {
            ;
        }

        try
        {
            prefs.removeNodeSpi();
            fail();
        }
        catch (UnsupportedOperationException x)
        {
            ;
        }

        try
        {
            prefs.removeSpi(DUMMY);
            fail();
        }
        catch (UnsupportedOperationException x)
        {
            ;
        }
    }

    private Dwarf newDwarf(Preferences node)
    {
        return BeanTool.getInstance().proxy(Dwarf.class, new Access(node));
    }

    public static class Access implements BeanAccess
    {
        private final Preferences _node;

        public Access(Preferences node)
        {
            _node = node;
        }

        public void propAdd(String propertyName, String value)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String propDel(String propertyName)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String propGet(String propertyName)
        {
            return _node.get(propertyName, null);
        }

        public String propGet(String propertyName, int index)
        {
            return (index == 0) ? propGet(propertyName) : null;
        }

        public int propLength(String propertyName)
        {
            return (propGet(propertyName) == null) ? 0 : 1;
        }

        public String propSet(String propertyName, String value)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String propSet(String propertyName, String value, int index)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
