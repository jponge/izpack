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

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class BasicMultiMapTest
{
    private static final String KEY1 = "key1";
    private static final String KEY2 = "key2";
    private static final String KEY3 = "key3";
    private static final String VALUE1 = "value1";
    private static final String VALUE2 = "value2";
    private static final String VALUE3 = "value3";
    private static final String[] VALUES = { VALUE1, VALUE2, VALUE3 };
    private MultiMap<String, String> _map;

    @Before public void setUp()
    {
        _map = new BasicMultiMap<String, String>();
    }

    @Test public void testAdd()
    {
        _map.add(KEY1, VALUE1);
        _map.add(KEY1, VALUE2);
        _map.add(KEY1, VALUE3);
        assertEquals(3, _map.length(KEY1));
        _map.add(KEY1, VALUE3, 0);
        assertEquals(4, _map.length(KEY1));
        assertEquals(VALUE3, _map.get(KEY1, 0));
        assertEquals(VALUE3, _map.get(KEY1, 3));
        _map.clear();
        assertTrue(_map.isEmpty());
    }

    @Test public void testAll()
    {
        _map.putAll(KEY1, Arrays.asList(VALUES));
        assertEquals(VALUES.length, _map.length(KEY1));
        String[] values = _map.getAll(KEY1).toArray(new String[] {});

        assertArrayEquals(VALUES, values);
    }

    @Test public void testContainsValue()
    {
        _map.putAll(KEY1, Arrays.asList(VALUES));
        assertTrue(_map.containsValue(VALUE1));
        assertTrue(_map.containsValue(VALUE2));
        assertTrue(_map.containsValue(VALUE3));
        _map.clear();
        _map.put(KEY2, VALUE1);
        assertFalse(_map.containsValue(VALUE3));
    }

    @Test public void testEntrySet()
    {
        _map.putAll(KEY1, Arrays.asList(VALUES));
        _map.put(KEY2, VALUE2);
        _map.put(KEY3, VALUE3);
        Set<Entry<String, String>> set = _map.entrySet();

        assertNotNull(set);
        assertEquals(3, set.size());
        for (Entry<String, String> e : set)
        {
            if (e.getKey().equals(KEY1))
            {
                assertEquals(VALUES[2], e.getValue());
                e.setValue(VALUES[1]);
            }
            else if (e.getKey().equals(KEY2))
            {
                assertEquals(VALUE2, e.getValue());
                e.setValue(VALUE3);
            }
            else if (e.getKey().equals(KEY3))
            {
                assertEquals(VALUE3, e.getValue());
                e.setValue(VALUE2);
            }
        }

        assertEquals(VALUES[1], _map.get(KEY1));
        assertEquals(VALUES.length, _map.length(KEY1));
        assertEquals(VALUE3, _map.get(KEY2));
        assertEquals(VALUE2, _map.get(KEY3));
    }

    @Test public void testGetEmpty()
    {
        assertNull(_map.get(KEY1));
        assertNull(_map.get(KEY1, 1));
    }

    @Test public void testPut()
    {
        _map.put(KEY1, VALUE1);
        _map.add(KEY1, VALUE2);
        assertEquals(VALUE2, _map.get(KEY1, 1));
        _map.put(KEY1, VALUE3, 1);
        assertEquals(VALUE3, _map.get(KEY1, 1));
        assertEquals(VALUE3, _map.get(KEY1));
    }

    @Test public void testPutAll()
    {
        _map.put(KEY1, VALUE1);
        _map.put(KEY2, VALUE1);
        _map.add(KEY2, VALUE2);
        MultiMap<String, String> other = new BasicMultiMap<String, String>();

        other.putAll(_map);
        assertEquals(2, other.size());
        assertEquals(2, other.length(KEY2));
        assertEquals(1, other.length(KEY1));
        assertEquals(VALUE1, _map.get(KEY1));
        assertEquals(VALUE1, _map.get(KEY2, 0));
        assertEquals(VALUE2, _map.get(KEY2, 1));
        Map<String, String> regular = new HashMap<String, String>(_map);

        _map.clear();
        _map.putAll(regular);
        assertEquals(regular.keySet(), _map.keySet());
    }

    @Test public void testRemove()
    {
        _map.add(KEY1, VALUE1);
        _map.add(KEY2, VALUE1);
        _map.add(KEY2, VALUE2);
        _map.add(KEY3, VALUE1);
        _map.add(KEY3, VALUE2);
        _map.add(KEY3, VALUE3);
        assertEquals(VALUE2, _map.get(KEY3, 1));
        _map.remove(KEY3, 1);
        assertEquals(VALUE3, _map.get(KEY3, 1));
        _map.remove(KEY3, 1);
        assertEquals(VALUE1, _map.get(KEY3));
        _map.remove(KEY3, 0);
        assertEquals(0, _map.length(KEY3));
        assertFalse(_map.containsKey(KEY3));
        _map.remove(KEY2);
        assertFalse(_map.containsKey(KEY2));
        _map.remove(KEY1);
        assertFalse(_map.containsKey(KEY1));
        assertEquals(0, _map.size());
        assertTrue(_map.isEmpty());
        assertNull(_map.remove(KEY1));
        assertNull(_map.remove(KEY1, 1));
    }

    @Test public void testValues()
    {
        _map.put(KEY1, VALUE1);
        _map.put(KEY2, VALUE2);
        _map.add(KEY2, VALUE3);
        String[] values = _map.values().toArray(new String[] {});

        Arrays.sort(values);
        assertArrayEquals(values, VALUES);
    }
}
