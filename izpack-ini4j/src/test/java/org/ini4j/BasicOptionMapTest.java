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
import org.ini4j.sample.DwarfBean;

import org.ini4j.test.DwarfsData;
import org.ini4j.test.DwarfsData.DwarfData;
import org.ini4j.test.Helper;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URI;

public class BasicOptionMapTest
{
    private static BasicOptionMap _map;

    @BeforeClass public static void setUpClass()
    {
        _map = new BasicOptionMap();
        _map.putAll(Helper.newDwarfsOpt());
    }

    @Test public void testAddPutNullAndString()
    {
        OptionMap map = new BasicOptionMap();
        Object o;

        // null
        o = null;
        map.add(Dwarf.PROP_AGE, o);
        assertNull(map.get(Dwarf.PROP_AGE));
        map.put(Dwarf.PROP_AGE, DwarfsData.doc.age);
        assertNotNull(map.get(Dwarf.PROP_AGE));
        map.add(Dwarf.PROP_AGE, o, 0);
        assertNull(map.get(Dwarf.PROP_AGE, 0));
        map.put(Dwarf.PROP_AGE, DwarfsData.doc.age, 0);
        assertNotNull(map.get(Dwarf.PROP_AGE, 0));
        map.put(Dwarf.PROP_AGE, o, 0);
        assertNull(map.get(Dwarf.PROP_AGE, 0));
        map.remove(Dwarf.PROP_AGE);
        map.put(Dwarf.PROP_AGE, o);
        assertNull(map.get(Dwarf.PROP_AGE));

        // str
        map.remove(Dwarf.PROP_AGE);
        o = String.valueOf(DwarfsData.doc.age);
        map.add(Dwarf.PROP_AGE, o);
        assertEquals(o, map.get(Dwarf.PROP_AGE));
        map.remove(Dwarf.PROP_AGE);
        map.put(Dwarf.PROP_AGE, o);
        assertEquals(o, map.get(Dwarf.PROP_AGE));
        o = String.valueOf(DwarfsData.happy.age);
        map.add(Dwarf.PROP_AGE, o, 0);
        assertEquals(DwarfsData.happy.age, (int) map.get(Dwarf.PROP_AGE, 0, int.class));
        o = String.valueOf(DwarfsData.doc.age);
        map.put(Dwarf.PROP_AGE, o, 0);
        assertEquals(DwarfsData.doc.age, (int) map.get(Dwarf.PROP_AGE, 0, int.class));
    }

    @Test public void testFetch()
    {
        OptionMap map = new BasicOptionMap();

        Helper.addDwarf(map, DwarfsData.dopey, false);
        Helper.addDwarf(map, DwarfsData.bashful);
        Helper.addDwarf(map, DwarfsData.doc);

        // dopey
        assertEquals(DwarfsData.dopey.weight, map.fetch(Dwarf.PROP_WEIGHT, double.class), Helper.DELTA);
        map.add(Dwarf.PROP_HEIGHT, map.get(Dwarf.PROP_HEIGHT));
        assertEquals(DwarfsData.dopey.height, map.fetch(Dwarf.PROP_HEIGHT, 1, double.class), Helper.DELTA);

        // sneezy
        map.clear();
        Helper.addDwarf(map, DwarfsData.happy);
        Helper.addDwarf(map, DwarfsData.sneezy, false);
        assertEquals(DwarfsData.sneezy.homePage, map.fetch(Dwarf.PROP_HOME_PAGE, URI.class));

        // null
        map = new BasicOptionMap();
        map.add(Dwarf.PROP_AGE, null);
        assertNull(map.fetch(Dwarf.PROP_AGE, 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFetchAllException()
    {
        OptionMap map = new BasicOptionMap();

        map.fetchAll(Dwarf.PROP_FORTUNE_NUMBER, String.class);
    }

    @Test public void testFromToAs() throws Exception
    {
        DwarfBean bean = new DwarfBean();

        _map.to(bean);
        Helper.assertEquals(DwarfsData.dopey, bean);
        OptionMap map = new BasicOptionMap();

        map.from(bean);
        bean = new DwarfBean();
        map.to(bean);
        Helper.assertEquals(DwarfsData.dopey, bean);
        Dwarf proxy = map.as(Dwarf.class);

        Helper.assertEquals(DwarfsData.dopey, proxy);
        map.clear();
        _map.to(proxy);
        Helper.assertEquals(DwarfsData.dopey, proxy);
    }

    @Test public void testFromToAsPrefixed() throws Exception
    {
        fromToAs(DwarfsData.bashful);
        fromToAs(DwarfsData.doc);
        fromToAs(DwarfsData.dopey);
        fromToAs(DwarfsData.grumpy);
        fromToAs(DwarfsData.happy);
        fromToAs(DwarfsData.sleepy);
        fromToAs(DwarfsData.sneezy);
    }

    @Test public void testGet()
    {
        OptionMap map = new BasicOptionMap();

        // bashful
        Helper.addDwarf(map, DwarfsData.bashful, false);
        assertEquals(DwarfsData.bashful.weight, map.get(Dwarf.PROP_WEIGHT, double.class), Helper.DELTA);
        map.add(Dwarf.PROP_HEIGHT, map.get(Dwarf.PROP_HEIGHT));
        assertEquals(DwarfsData.bashful.height, map.get(Dwarf.PROP_HEIGHT, 1, double.class), Helper.DELTA);
        assertEquals(DwarfsData.bashful.homePage, map.fetch(Dwarf.PROP_HOME_PAGE, URI.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetAllException()
    {
        OptionMap map = new BasicOptionMap();

        map.getAll(Dwarf.PROP_FORTUNE_NUMBER, String.class);
    }

    @Test public void testPropertyFirstUpper()
    {
        DwarfBean bean;
        OptionMap map = new BasicOptionMap(true);

        map.from(DwarfsData.bashful);
        assertTrue(map.containsKey("Age"));
        assertTrue(map.containsKey("Height"));
        assertTrue(map.containsKey("Weight"));
        assertTrue(map.containsKey("HomePage"));
        assertTrue(map.containsKey("HomeDir"));
        bean = new DwarfBean();
        map.to(bean);
        Helper.assertEquals(DwarfsData.bashful, bean);
        Helper.assertEquals(DwarfsData.bashful, map.as(Dwarf.class));
    }

    @Test public void testPut()
    {
        OptionMap map = new BasicOptionMap();

        map.add(Dwarf.PROP_AGE, DwarfsData.sneezy.age);
        map.put(Dwarf.PROP_HEIGHT, DwarfsData.sneezy.height);
        map.add(Dwarf.PROP_HOME_DIR, DwarfsData.sneezy.homeDir);
        map.add(Dwarf.PROP_WEIGHT, DwarfsData.sneezy.weight, 0);
        map.put(Dwarf.PROP_HOME_PAGE, null);
        map.put(Dwarf.PROP_HOME_PAGE, DwarfsData.sneezy.homePage);
        map.add(Dwarf.PROP_FORTUNE_NUMBER, DwarfsData.sneezy.fortuneNumber[1]);
        map.add(Dwarf.PROP_FORTUNE_NUMBER, DwarfsData.sneezy.fortuneNumber[2]);
        map.add(Dwarf.PROP_FORTUNE_NUMBER, 0);
        map.put(Dwarf.PROP_FORTUNE_NUMBER, DwarfsData.sneezy.fortuneNumber[3], 2);
        map.add(Dwarf.PROP_FORTUNE_NUMBER, DwarfsData.sneezy.fortuneNumber[0], 0);
        Helper.assertEquals(DwarfsData.sneezy, map.as(Dwarf.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPutAllException()
    {
        OptionMap map = new BasicOptionMap();

        map.putAll(Dwarf.PROP_FORTUNE_NUMBER, 0);
    }

    @Test public void testPutGetFetchAll()
    {
        OptionMap map = new BasicOptionMap();

        map.putAll(Dwarf.PROP_FORTUNE_NUMBER, DwarfsData.sneezy.fortuneNumber);
        assertEquals(DwarfsData.sneezy.fortuneNumber.length, map.length(Dwarf.PROP_FORTUNE_NUMBER));
        assertArrayEquals(DwarfsData.sneezy.fortuneNumber, map.getAll(Dwarf.PROP_FORTUNE_NUMBER, int[].class));
        assertArrayEquals(DwarfsData.sneezy.fortuneNumber, map.fetchAll(Dwarf.PROP_FORTUNE_NUMBER, int[].class));
        map.putAll(Dwarf.PROP_FORTUNE_NUMBER, (int[]) null);
        assertEquals(0, map.length(Dwarf.PROP_FORTUNE_NUMBER));
        assertEquals(0, map.getAll(Dwarf.PROP_FORTUNE_NUMBER, int[].class).length);
        assertEquals(0, map.fetchAll(Dwarf.PROP_FORTUNE_NUMBER, int[].class).length);
    }

    @Test public void testResolve() throws Exception
    {
        StringBuilder buffer;
        String input;

        // simple value
        input = "${height}";
        buffer = new StringBuilder(input);

        _map.resolve(buffer);
        assertEquals("" + DwarfsData.dopey.getHeight(), buffer.toString());

        // system property
        input = "${@prop/user.home}";
        buffer = new StringBuilder(input);

        _map.resolve(buffer);
        assertEquals(System.getProperty("user.home"), buffer.toString());

        // system environment
        input = "${@env/PATH}";
        buffer = new StringBuilder(input);
        try
        {
            _map.resolve(buffer);
            assertEquals(System.getenv("PATH"), buffer.toString());
        }
        catch (Error e)
        {
            // retroweaver + JDK 1.4 throws Error on getenv
        }

        // unknown variable
        input = "${no such name}";
        buffer = new StringBuilder(input);

        _map.resolve(buffer);
        assertEquals(input, buffer.toString());

        // small input
        input = "${";
        buffer = new StringBuilder(input);

        _map.resolve(buffer);
        assertEquals(input, buffer.toString());

        // incorrect references
        input = "${weight";
        buffer = new StringBuilder(input);

        _map.resolve(buffer);
        assertEquals(input, buffer.toString());

        // empty references
        input = "jim${}";
        buffer = new StringBuilder(input);

        _map.resolve(buffer);
        assertEquals(input, buffer.toString());

        // escaped references
        input = "${weight}";
        buffer = new StringBuilder(input);

        _map.resolve(buffer);
        assertEquals("" + DwarfsData.dopey.getWeight(), buffer.toString());
        input = "\\" + input;
        buffer = new StringBuilder(input);

        assertEquals(input, buffer.toString());
    }

    private void fromToAs(DwarfData dwarf)
    {
        String prefix = dwarf.name + '.';
        DwarfBean bean = new DwarfBean();

        _map.to(bean, prefix);
        Helper.assertEquals(dwarf, bean);
        OptionMap map = new BasicOptionMap();

        map.from(bean, prefix);
        bean = new DwarfBean();
        map.to(bean, prefix);
        Helper.assertEquals(dwarf, bean);
        Dwarf proxy = map.as(Dwarf.class, prefix);

        Helper.assertEquals(dwarf, proxy);
        map.clear();
        _map.to(proxy, prefix);
        Helper.assertEquals(dwarf, proxy);
    }
}
