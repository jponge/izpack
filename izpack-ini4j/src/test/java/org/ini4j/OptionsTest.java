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

import org.ini4j.test.DwarfsData;
import org.ini4j.test.Helper;

import static org.junit.Assert.*;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;

public class OptionsTest
{
    private static final String[] _badOptions = { "=value\n", "\\u000d\\u000d=value\n" };
    private static final String COMMENT_ONLY = "# first line\n# second line\n";
    private static final String COMMENT_ONLY_VALUE = " first line\n second line";
    private static final String OPTIONS_ONE_HEADER = COMMENT_ONLY + "\n\nkey=value\n";
    private static final String MULTI = "option=value\noption=value2\noption=value3\noption=value4\noption=value5\n";

    @Test public void testCommentOnly() throws Exception
    {
        Options opt = new Options(new StringReader(COMMENT_ONLY));

        assertEquals(COMMENT_ONLY_VALUE, opt.getComment());
    }

    @Test public void testConfig()
    {
        Options opts = new Options();
        Config conf = opts.getConfig();

        assertTrue(conf.isEmptyOption());
        assertTrue(conf.isEscape());
        assertFalse(conf.isInclude());
        assertTrue(conf.isMultiOption());
        conf = new Config();
        opts.setConfig(conf);
        assertSame(conf, opts.getConfig());
    }

    @Test public void testDwarfs() throws Exception
    {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        Options happy = new Options();

        happy.from(DwarfsData.happy);
        happy.store(buffer);
        Options dup = new Options(new ByteArrayInputStream(buffer.toByteArray()));

        Helper.assertEquals(DwarfsData.happy, dup.as(Dwarf.class));
        buffer = new ByteArrayOutputStream();
        happy.store(new OutputStreamWriter(buffer));
        dup = new Options(new ByteArrayInputStream(buffer.toByteArray()));
        Helper.assertEquals(DwarfsData.happy, dup.as(Dwarf.class));
        File file = File.createTempFile("test", ".opt");

        file.deleteOnExit();
        happy.setFile(file);
        happy.store();
        dup = new Options();
        dup.setFile(file);
        assertEquals(file, dup.getFile());
        dup.load();
        Helper.assertEquals(DwarfsData.happy, dup.as(Dwarf.class));
        file.delete();
    }

    @Test public void testLoad() throws Exception
    {
        Options o1 = new Options(Helper.getResourceURL(Helper.DWARFS_OPT));
        Options o2 = new Options(Helper.getResourceURL(Helper.DWARFS_OPT).openStream());
        Options o3 = new Options(new InputStreamReader(Helper.getResourceURL(Helper.DWARFS_OPT).openStream()));
        Options o4 = new Options(Helper.getResourceURL(Helper.DWARFS_OPT));
        Options o5 = new Options(Helper.getSourceFile(Helper.DWARFS_OPT));
        Options o6 = new Options();

        o6.setFile(Helper.getSourceFile(Helper.DWARFS_OPT));
        o6.load();
        Helper.assertEquals(DwarfsData.dopey, o1.as(Dwarf.class));
        Helper.assertEquals(DwarfsData.dopey, o2.as(Dwarf.class));
        Helper.assertEquals(DwarfsData.dopey, o3.as(Dwarf.class));
        Helper.assertEquals(DwarfsData.dopey, o4.as(Dwarf.class));
        Helper.assertEquals(DwarfsData.dopey, o5.as(Dwarf.class));
        Helper.assertEquals(DwarfsData.dopey, o6.as(Dwarf.class));
    }

    @Test(expected = FileNotFoundException.class)
    public void testLoadException() throws Exception
    {
        Options opt = new Options();

        opt.load();
    }

    @Test public void testLowerCase() throws Exception
    {
        Config cfg = new Config();
        Options opts = new Options();

        cfg.setLowerCaseOption(true);
        opts.setConfig(cfg);
        opts.load(new StringReader("OptIon=value\n"));
        assertTrue(opts.containsKey("option"));
    }

    @Test public void testMultiOption() throws Exception
    {
        Options opts = new Options(new StringReader(MULTI));

        assertEquals(5, opts.length("option"));
        opts.clear();
        Config cfg = new Config();

        cfg.setMultiOption(false);
        opts.setConfig(cfg);
        opts.load(new StringReader(MULTI));
        assertEquals(1, opts.length("option"));
    }

    @Test(expected = InvalidFileFormatException.class)
    public void testNoEmptyOption() throws Exception
    {
        Config cfg = new Config();
        Options opts = new Options();

        opts.setConfig(cfg);
        opts.load(new StringReader("dummy\n"));
        assertTrue(opts.containsKey("dummy"));
        assertNull(opts.get("dummy"));
        cfg.setEmptyOption(false);
        opts.load(new StringReader("foo\n"));
    }

    @Test public void testOneHeaderOnly() throws Exception
    {
        Options opt = new Options(new StringReader(OPTIONS_ONE_HEADER));

        assertEquals(COMMENT_ONLY_VALUE, opt.getComment());
    }

    @Test
    @SuppressWarnings("empty-statement")
    public void testParseError() throws Exception
    {
        for (String s : _badOptions)
        {
            try
            {
                new Options(new ByteArrayInputStream(s.getBytes()));
                fail("expected InvalidIniFormatException: " + s);
            }
            catch (InvalidFileFormatException x)
            {
                ;
            }
        }
    }

    @Test(expected = FileNotFoundException.class)
    public void testStoreException() throws Exception
    {
        Options opt = new Options();

        opt.store();
    }
}
