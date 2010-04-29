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

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class WinEscapeToolTest
{
    public static final String VALUE1 = "simple";
    public static final String ESCAPE1 = "simple";
    public static final String VALUE2 = "Iván";
    public static final String ESCAPE2 = "Iv\\xe1n";
    public static final String VALUE3 = "1\t2\n3\f4\b5\r6";
    public static final String ESCAPE3 = "1\\t2\\n3\\f4\\b5\\r6";
    public static final String VALUE4 = "Iv\u0017n";
    public static final String ESCAPE4 = "Iv\\x17n";
    public static final String VALUE5 = "Árvíztrtükörfúrógép";
    public static final String ESCAPE5 = "\\xc1rv\\xedztrt\\xfck\\xf6rf\\xfar\\xf3g\\xe9p";
    private static final String INVALID_HEX = "\\x1_";
    private static final String INVALID_OCT = "\\o19_";
    protected WinEscapeTool instance;

    @Before public void setUp() throws Exception
    {
        instance = WinEscapeTool.getInstance();
    }

    @Test public void testEscape() throws Exception
    {
        assertEquals(ESCAPE1, instance.escape(VALUE1));
        assertEquals(ESCAPE2, instance.escape(VALUE2));
        assertEquals(ESCAPE3, instance.escape(VALUE3));
        assertEquals(ESCAPE4, instance.escape(VALUE4));
        assertEquals(ESCAPE5, instance.escape(VALUE5));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidHex()
    {
        instance.unescape(INVALID_HEX);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidOctal()
    {
        instance.unescape(INVALID_OCT);
    }

    @Test public void testSingleton() throws Exception
    {
        assertEquals(WinEscapeTool.class, WinEscapeTool.getInstance().getClass());
    }

    @Test public void testUnescape() throws Exception
    {
        assertEquals(VALUE1, instance.unescape(ESCAPE1));
        assertEquals(VALUE2, instance.unescape(ESCAPE2));
        assertEquals(VALUE3, instance.unescape(ESCAPE3));
        assertEquals(VALUE4, instance.unescape(ESCAPE4));
        assertEquals(VALUE5, instance.unescape(ESCAPE5));
        assertEquals("=", instance.unescape("\\="));
        assertEquals("xAx", instance.unescape("x\\o101x"));
    }
}
