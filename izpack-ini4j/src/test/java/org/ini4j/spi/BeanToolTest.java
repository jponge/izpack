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

import org.ini4j.BasicOptionMapGate;

import org.ini4j.sample.Dwarf;
import org.ini4j.sample.DwarfBean;

import org.ini4j.test.DwarfsData;
import org.ini4j.test.Helper;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import java.net.URI;
import java.net.URL;

import java.util.TimeZone;

public class BeanToolTest
{
    protected BeanTool instance;

    @Before public void setUp() throws Exception
    {
        instance = BeanTool.getInstance();
    }

    @Test public void testInject() throws Exception
    {
        testInject(null);
        testInject("dummy");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInjectIllegalArgument1() throws Exception
    {
        TestMap map = new TestMap();

        instance.inject(map.newBeanAccess(), new BadBean());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInjectIllegalArgument2() throws Exception
    {
        TestMap map = new TestMap();

        map.put("name", "bad");
        instance.inject(new BadBean(), map.newBeanAccess());
    }

    @SuppressWarnings("empty-statement")
    @Test public void testParse() throws Exception
    {
        String input = "6";
        int value = 6;

        assertEquals(value, instance.parse(input, byte.class).byteValue());
        assertEquals(value, instance.parse(input, short.class).shortValue());
        assertEquals(value, instance.parse(input, int.class).intValue());
        assertEquals(value, instance.parse(input, long.class).longValue());
        assertEquals((float) value, instance.parse(input, float.class).floatValue(), Helper.DELTA);
        assertEquals((double) value, instance.parse(input, double.class).doubleValue(), Helper.DELTA);
        assertFalse(instance.parse(input, boolean.class));
        assertEquals('6', instance.parse(input, char.class).charValue());

        // parse null mean zero
        assertEquals(0, instance.parse(null, byte.class).byteValue());

        // parse to null class mean exception
        try
        {
            instance.parse(input, null);
            fail();
        }
        catch (IllegalArgumentException x)
        {
            ;
        }

        // invalid primitive value mean exception
        try
        {
            instance.parse("?", int.class);
            fail();
        }
        catch (IllegalArgumentException x)
        {
            ;
        }

        // standard, but not primitive types
        assertSame(input, instance.parse(input, String.class));
        assertEquals(new Character('6'), instance.parse(input, Character.class));
        assertEquals(new Byte(input), instance.parse(input, Byte.class));

        // special values
        input = "http://www.ini4j.org";
        assertEquals(new URL(input), instance.parse(input, URL.class));
        assertEquals(new URI(input), instance.parse(input, URI.class));
        assertEquals(new File(input), instance.parse(input, File.class));
        input = "Europe/Budapest";
        assertEquals(input, instance.parse(input, TimeZone.class).getID());
        input = "java.lang.String";
        assertEquals(String.class, instance.parse(input, Class.class));

        // invalid value should throw IllegalArgumentException
        try
        {
            instance.parse("", URL.class);
        }
        catch (IllegalArgumentException x)
        {
            ;
        }
    }

    @Test public void testSetGet() throws Exception
    {
        TestMap map = new TestMap();
        Dwarf proxy = instance.proxy(Dwarf.class, map.newBeanAccess());

        assertNull(proxy.getHomeDir());
        assertFalse(proxy.hasHomePage());
        assertNull(proxy.getFortuneNumber());
        proxy.setAge(DwarfsData.sneezy.age);
        proxy.setHeight(DwarfsData.sneezy.height);
        proxy.setWeight(DwarfsData.sneezy.weight);
        proxy.setHomePage(DwarfsData.sneezy.homePage);
        proxy.setHomeDir(DwarfsData.sneezy.homeDir);
        proxy.setFortuneNumber(DwarfsData.sneezy.fortuneNumber);
        Helper.assertEquals(DwarfsData.sneezy, proxy);
        assertArrayEquals(DwarfsData.sneezy.fortuneNumber, proxy.getFortuneNumber());
    }

    @Test public void testSingleton() throws Exception
    {
        assertEquals(BeanTool.class, BeanTool.getInstance().getClass());
    }

    @Test public void testZero() throws Exception
    {
        assertEquals(null, instance.zero(Object.class));
        assertEquals(0, instance.zero(byte.class).byteValue());
        assertEquals(0, instance.zero(short.class).shortValue());
        assertEquals(0, instance.zero(int.class).intValue());
        assertEquals(0, instance.zero(long.class).longValue());
        assertEquals(0.0f, instance.zero(float.class).floatValue(), Helper.DELTA);
        assertEquals(0.0, instance.zero(double.class).doubleValue(), Helper.DELTA);
        assertNotNull((instance.zero(boolean.class)));
        assertFalse(instance.zero(boolean.class));
        assertEquals('\0', instance.zero(char.class).charValue());
    }

    protected void testInject(String prefix) throws Exception
    {
        String p = (prefix == null) ? "" : prefix;
        Dwarf bean = new DwarfBean();

        bean.setAge(23);
        bean.setHeight(5.3);
        URI uri = new URI("http://www.ini4j.org");

        bean.setHomePage(uri);
        String dir = "/home/happy";

        bean.setHomeDir(dir);
        bean.setFortuneNumber(new int[] { 1, 2, 3 });
        TestMap map = new TestMap();

        instance.inject(map.newBeanAccess(prefix), bean);
        assertEquals(6, map.size());
        assertEquals("23", map.get(p + Dwarf.PROP_AGE));
        assertEquals("5.3", map.get(p + Dwarf.PROP_HEIGHT));
        assertEquals(uri.toString(), map.get(p + Dwarf.PROP_HOME_PAGE));
        assertEquals(dir, map.get(p + Dwarf.PROP_HOME_DIR));
        assertEquals(3, map.length(p + Dwarf.PROP_FORTUNE_NUMBER));
        assertEquals("1", map.get(p + Dwarf.PROP_FORTUNE_NUMBER, 0));
        assertEquals("2", map.get(p + Dwarf.PROP_FORTUNE_NUMBER, 1));
        assertEquals("3", map.get(p + Dwarf.PROP_FORTUNE_NUMBER, 2));
        bean.setAge(0);
        bean.setHeight(0);
        bean.setHomePage(null);
        instance.inject(bean, map.newBeanAccess(prefix));
        assertEquals(23, bean.getAge());
        assertEquals(5.3, bean.getHeight(), Helper.DELTA);
        assertEquals(uri, bean.getHomePage());
        assertEquals(dir, bean.getHomeDir());
        assertArrayEquals(new int[] { 1, 2, 3 }, bean.getFortuneNumber());

        //
        // bean interface
        //
        Dwarf proxy = instance.proxy(Dwarf.class, map.newBeanAccess(prefix));

        assertEquals(23, proxy.getAge());
        assertEquals(5.3, proxy.getHeight(), Helper.DELTA);
        assertEquals(uri, proxy.getHomePage());
        assertEquals(dir, proxy.getHomeDir());
        assertArrayEquals(new int[] { 1, 2, 3 }, proxy.getFortuneNumber());
    }

    static class TestMap extends BasicOptionMapGate
    {
        private static final long serialVersionUID = 4818386732025655044L;
    }

    private static class BadBean
    {
        public String getName() throws IOException
        {
            throw new IOException();
        }

        public void setName(String value) throws IOException
        {
            throw new IOException();
        }
    }
}
