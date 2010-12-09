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

import org.easymock.EasyMock;

import org.ini4j.Config;
import org.ini4j.Ini4jCase;

import org.ini4j.test.Helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class IniSourceTest extends Ini4jCase
{
    private static final String COMMENTS = ";#";
    private static final String NESTED_TXT = "nested.txt";
    private static final String NESTED = ":" + NESTED_TXT;
    private static final String NESTED_PATH = "org/ini4j/spi/" + NESTED_TXT;
    private static final String INCLUDE = ":include.txt";
    private static final String PART1 = ":part1.txt";
    private static final String PART2 = ":part2.txt";
    private static final String OUTER = ":outer";

    @Test public void testWithInclude() throws Exception
    {
        HandlerBase handler = EasyMock.createMock(HandlerBase.class);

        handler.handleComment("-1" + OUTER);
        handler.handleComment("-1" + NESTED);
        handler.handleComment("-2" + NESTED);
        handler.handleComment("-1" + INCLUDE);
        handler.handleComment("-2" + INCLUDE);
        handler.handleComment("-1" + PART1);
        handler.handleComment("-2" + PART1);
        handler.handleComment("-3" + INCLUDE);
        handler.handleComment("-4" + INCLUDE);
        handler.handleComment("-5" + INCLUDE);
        handler.handleComment("-6" + INCLUDE);
        handler.handleComment("-1" + PART2);
        handler.handleComment("-2" + PART2);
        handler.handleComment("-7" + INCLUDE);
        handler.handleComment("-8" + INCLUDE);
        handler.handleComment("-3" + NESTED);
        handler.handleComment("-4" + NESTED);
        handler.handleComment("-2" + OUTER);
        EasyMock.replay(handler);
        StringBuilder outer = new StringBuilder();

        outer.append(";-1" + OUTER + '\n');
        outer.append("1" + OUTER + '\n');
        outer.append('<');
        outer.append(Helper.getResourceURL(NESTED_PATH).toExternalForm());
        outer.append(">\n");
        outer.append("2" + OUTER + '\n');
        outer.append(";-2" + OUTER + '\n');
        InputStream in = new ByteArrayInputStream(outer.toString().getBytes());
        Config cfg = new Config();

        cfg.setInclude(true);
        System.err.println("*********************** " + cfg.isEscapeNewline());
        IniSource src = new IniSource(in, handler, COMMENTS, cfg);

        assertEquals("1" + OUTER, src.readLine());
        assertEquals(2, src.getLineNumber());
        assertEquals("1" + NESTED, src.readLine());
        assertEquals(2, src.getLineNumber());
        assertEquals("1" + INCLUDE, src.readLine());
        assertEquals(2, src.getLineNumber());
        assertEquals("1" + PART1, src.readLine());
        assertEquals(2, src.getLineNumber());
        assertEquals("2" + PART1, src.readLine());
        assertEquals(4, src.getLineNumber());
        assertEquals("3" + PART1 + "\\\\", src.readLine());
        assertEquals(5, src.getLineNumber());
        assertEquals("4:\\\\part1.txt", src.readLine());
        assertEquals(7, src.getLineNumber());
        assertEquals("5" + PART1 + "\\\\\\\\", src.readLine());
        assertEquals(8, src.getLineNumber());
        assertEquals("6" + PART1 + ";", src.readLine());
        assertEquals(10, src.getLineNumber());
        assertEquals("2" + INCLUDE, src.readLine());
        assertEquals(6, src.getLineNumber());
        assertEquals("3" + INCLUDE, src.readLine());
        assertEquals(10, src.getLineNumber());
        assertEquals("1" + PART2, src.readLine());
        assertEquals(3, src.getLineNumber());
        assertEquals("4" + INCLUDE, src.readLine());
        assertEquals(14, src.getLineNumber());
        assertEquals("2" + NESTED, src.readLine());
        assertEquals(6, src.getLineNumber());
        assertEquals("2" + OUTER, src.readLine());
        assertEquals(4, src.getLineNumber());
        assertNull(src.readLine());
        EasyMock.verify(handler);
    }

    @Test public void testWithoutInclude() throws Exception
    {
        HandlerBase handler = EasyMock.createMock(HandlerBase.class);

        handler.handleComment("-1" + NESTED);
        handler.handleComment("-2" + NESTED);
        handler.handleComment("-3" + NESTED);
        handler.handleComment("-4" + NESTED);
        EasyMock.replay(handler);
        Config cfg = new Config();

        cfg.setInclude(false);
        IniSource src = new IniSource(Helper.getResourceURL(NESTED_PATH), handler, COMMENTS, cfg);

        assertEquals("1" + NESTED, src.readLine());
        assertEquals("<include.txt>", src.readLine());
        assertEquals("2" + NESTED, src.readLine());
        assertNull(src.readLine());
        EasyMock.verify(handler);
    }
}
