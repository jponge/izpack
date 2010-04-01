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

public class EscapeToolTest
{
    private static final String VALUE1 = "simple";
    private static final String ESCAPE1 = "simple";
    private static final String VALUE2 = "Iv\ufffdn";
    private static final String ESCAPE2 = "Iv\\ufffdn";
    private static final String VALUE3 = "1\t2\n3\f4\b5\r6";
    private static final String ESCAPE3 = "1\\t2\\n3\\f4\\b5\\r6";
    private static final String VALUE4 = "Iv\u0017n";
    private static final String ESCAPE4 = "Iv\\u0017n";
    private static final String INVALID_UNICODE = "\\u98x";
    private static final String UNQUOTED1 = "simple";
    private static final String QUOTED1 = "\"simple\"";
    private static final String UNQUOTED2 = "no\\csak\"";
    private static final String QUOTED2 = "\"no\\\\csak\\\"\"";
    private static final String UNQUOTED3 = "";
    private static final String QUOTED3 = "";
    protected EscapeTool instance;

    @Before public void setUp() throws Exception
    {
        instance = EscapeTool.getInstance();
    }

    @Test public void testEscape() throws Exception
    {
        assertEquals(ESCAPE1, instance.escape(VALUE1));
        assertEquals(ESCAPE2, instance.escape(VALUE2));
        assertEquals(ESCAPE3, instance.escape(VALUE3));
        assertEquals(ESCAPE4, instance.escape(VALUE4));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidUnicode()
    {
        instance.unescape(INVALID_UNICODE);
    }

    @Test public void testQuote() throws Exception
    {
        assertEquals(QUOTED1, instance.quote(UNQUOTED1));
        assertEquals(QUOTED2, instance.quote(UNQUOTED2));
        assertEquals(QUOTED3, instance.quote(UNQUOTED3));
        assertNull(instance.quote(null));
    }

    @Test public void testSingleton() throws Exception
    {
        assertEquals(EscapeTool.class, EscapeTool.getInstance().getClass());
    }

    @SuppressWarnings("empty-statement")
    @Test public void testUnescape() throws Exception
    {
        assertEquals(VALUE1, instance.unescape(ESCAPE1));
        assertEquals(VALUE2, instance.unescape(ESCAPE2));
        assertEquals(VALUE3, instance.unescape(ESCAPE3));
        assertEquals(VALUE4, instance.unescape(ESCAPE4));
        assertEquals("=", instance.unescape("\\="));
    }
}
