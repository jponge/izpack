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

import org.ini4j.spi.WinEscapeToolTest;

import static org.junit.Assert.*;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import java.net.URL;

public class WiniTest
{
    @Test public void testConstructors() throws Exception
    {
        File f = File.createTempFile("wini", "test");

        f.deleteOnExit();
        assertTrue(new WiniHelper(new FileInputStream(f)).isOK());
        assertTrue(new WiniHelper(new FileReader(f)).isOK());
        assertTrue(new WiniHelper(f).isOK());
        assertTrue(new WiniHelper(f.toURI().toURL()).isOK());
    }

    @Test public void testDefaults()
    {
        Wini wini = new Wini();
        Config cfg = wini.getConfig();

        assertTrue(cfg.isGlobalSection());
        assertTrue(cfg.isEmptyOption());
        assertFalse(cfg.isMultiOption());
        assertFalse(cfg.isEscape());
    }

    @Test public void testEscape()
    {
        Wini instance = new Wini();

        assertEquals(WinEscapeToolTest.ESCAPE1, instance.escape(WinEscapeToolTest.VALUE1));
        assertEquals(WinEscapeToolTest.ESCAPE2, instance.escape(WinEscapeToolTest.VALUE2));
        assertEquals(WinEscapeToolTest.ESCAPE3, instance.escape(WinEscapeToolTest.VALUE3));
        assertEquals(WinEscapeToolTest.ESCAPE4, instance.escape(WinEscapeToolTest.VALUE4));
        assertEquals(WinEscapeToolTest.ESCAPE5, instance.escape(WinEscapeToolTest.VALUE5));
    }

    @Test public void testUnescape() throws Exception
    {
        Wini instance = new Wini();

        assertEquals(WinEscapeToolTest.VALUE1, instance.unescape(WinEscapeToolTest.ESCAPE1));
        assertEquals(WinEscapeToolTest.VALUE2, instance.unescape(WinEscapeToolTest.ESCAPE2));
        assertEquals(WinEscapeToolTest.VALUE3, instance.unescape(WinEscapeToolTest.ESCAPE3));
        assertEquals(WinEscapeToolTest.VALUE4, instance.unescape(WinEscapeToolTest.ESCAPE4));
        assertEquals(WinEscapeToolTest.VALUE5, instance.unescape(WinEscapeToolTest.ESCAPE5));
        assertEquals("=", instance.unescape("\\="));
        assertEquals("xAx", instance.unescape("x\\o101x"));
    }

    private static class WiniHelper extends Wini
    {
        private boolean _ok;

        public WiniHelper(Reader input) throws IOException, InvalidFileFormatException
        {
            super(input);
        }

        public WiniHelper(InputStream input) throws IOException, InvalidFileFormatException
        {
            super(input);
        }

        public WiniHelper(URL input) throws IOException, InvalidFileFormatException
        {
            super(input);
        }

        public WiniHelper(File input) throws IOException, InvalidFileFormatException
        {
            super(input);
        }

        public boolean isOK()
        {
            return _ok;
        }

        @Override public void load(InputStream input) throws IOException, InvalidFileFormatException
        {
            _ok = true;
        }

        @Override public void load(Reader input) throws IOException, InvalidFileFormatException
        {
            _ok = true;
        }

        @Override public void load(File input) throws IOException, InvalidFileFormatException
        {
            _ok = true;
        }

        @Override public void load(URL input) throws IOException, InvalidFileFormatException
        {
            _ok = true;
        }
    }
}
