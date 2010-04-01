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

import org.ini4j.test.DwarfsData;
import org.ini4j.test.Helper;

import org.junit.After;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigParserTest
{
    private static final String SECTION = "section";
    private static final String OPTION = "option";
    private static final String DWARFS_PATH = "org/ini4j/addon/dwarfs-py.ini";
    private static final String BAD = "[bashful\nage=3";
    private static final String TEST_DATA_PATH = "src/test/resources";
    private static final String TEST_WORK_PATH = "target";
    private static final String MISSING = "missing";
    private static final String MISSING_REF = "%(missing)";
    private static final String DUMMY = "dummy";
    protected ConfigParser instance;

    @Before public void setUp()
    {
        instance = new ConfigParser();
    }

    @After public void tearDown()
    {
    }

    @Test public void testAddHasRemove() throws Exception
    {
        assertFalse(instance.hasSection(SECTION));
        assertFalse(instance.hasOption(SECTION, OPTION));
        assertFalse(instance.getIni().containsKey(SECTION));
        instance.addSection(SECTION);
        assertTrue(instance.hasSection(SECTION));
        instance.set(SECTION, OPTION, "dummy");
        assertTrue(instance.hasOption(SECTION, OPTION));
        assertTrue(instance.getIni().get(SECTION).containsKey(OPTION));
        instance.removeOption(SECTION, OPTION);
        assertFalse(instance.hasOption(SECTION, OPTION));
        instance.removeSection(SECTION);
        assertFalse(instance.hasSection(SECTION));
    }

    @Test(expected = ConfigParser.DuplicateSectionException.class)
    public void testAddSectionDuplicate() throws Exception
    {
        instance.addSection(SECTION);
        instance.addSection(SECTION);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddSectionIllegal() throws Exception
    {
        instance.addSection(ConfigParser.PyIni.DEFAULT_SECTION_NAME);
    }

    @Test public void testDefaults() throws Exception
    {
        Map<String, String> defs = new HashMap<String, String>();

        instance = new ConfigParser(defs);

        assertSame(defs, instance.defaults());
    }

    @Test public void testDwarfs() throws Exception
    {
        readDwarfs();
        checkEquals(DwarfsData.bashful, Dwarfs.PROP_BASHFUL);
        checkEquals(DwarfsData.doc, Dwarfs.PROP_DOC);
        checkEquals(DwarfsData.dopey, Dwarfs.PROP_DOPEY);
        checkEquals(DwarfsData.happy, Dwarfs.PROP_HAPPY);
        checkEquals(DwarfsData.grumpy, Dwarfs.PROP_GRUMPY);
        checkEquals(DwarfsData.sleepy, Dwarfs.PROP_SLEEPY);
        checkEquals(DwarfsData.sneezy, Dwarfs.PROP_SNEEZY);
    }

    @Test public void testGet() throws Exception
    {
        Ini.Section section = instance.getIni().add(SECTION);

        section.put(OPTION, "on");
        assertTrue(instance.getBoolean(SECTION, OPTION));
        section.put(OPTION, "1");
        assertTrue(instance.getBoolean(SECTION, OPTION));
        section.put(OPTION, "true");
        assertTrue(instance.getBoolean(SECTION, OPTION));
        section.put(OPTION, "yes");
        assertTrue(instance.getBoolean(SECTION, OPTION));
        section.put(OPTION, "TruE");
        assertTrue(instance.getBoolean(SECTION, OPTION));

        //
        section.put(OPTION, "off");
        assertFalse(instance.getBoolean(SECTION, OPTION));
        section.put(OPTION, "0");
        assertFalse(instance.getBoolean(SECTION, OPTION));
        section.put(OPTION, "no");
        assertFalse(instance.getBoolean(SECTION, OPTION));
        section.put(OPTION, "false");
        assertFalse(instance.getBoolean(SECTION, OPTION));
        section.put(OPTION, "FalsE");
        assertFalse(instance.getBoolean(SECTION, OPTION));

        // ints
        section.put(OPTION, "12");
        assertEquals(12, instance.getInt(SECTION, OPTION));
        assertEquals(12L, instance.getLong(SECTION, OPTION));
        section.put(OPTION, "1.2");
        assertEquals(1.2f, instance.getFloat(SECTION, OPTION), Helper.DELTA);
        assertEquals(1.2, instance.getDouble(SECTION, OPTION), Helper.DELTA);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetBooleanException() throws Exception
    {
        Ini.Section section = instance.getIni().add(SECTION);

        section.put(OPTION, "joe");
        instance.getBoolean(SECTION, OPTION);
    }

    @Test(expected = ConfigParser.InterpolationMissingOptionException.class)
    public void testGetMissinOptionException() throws Exception
    {
        instance.addSection(SECTION);
        instance.set(SECTION, OPTION, MISSING_REF);
        instance.get(SECTION, OPTION);
    }

    @Test(expected = ConfigParser.NoOptionException.class)
    public void testGetNoOption() throws Exception
    {
        instance.getIni().add(SECTION);
        instance.get(SECTION, OPTION);
    }

    @Test(expected = ConfigParser.NoSectionException.class)
    public void testGetNoSection() throws Exception
    {
        instance.get(SECTION, OPTION);
    }

    @Test @SuppressWarnings("empty-statement")
    public void testGetVars() throws Exception
    {
        Map<String, String> vars = new HashMap<String, String>();

        instance = new ConfigParser(vars);

        instance.addSection(SECTION);
        instance.set(SECTION, OPTION, MISSING_REF);
        assertEquals(MISSING_REF, instance.get(SECTION, OPTION, true));
        requireMissingOptionException(SECTION, OPTION);
        vars.put(MISSING, DUMMY);
        assertEquals(DUMMY, instance.get(SECTION, OPTION));
        vars.remove(MISSING);
        requireMissingOptionException(SECTION, OPTION);
        instance.getIni().add(ConfigParser.PyIni.DEFAULT_SECTION_NAME);
        ((ConfigParser.PyIni) instance.getIni()).getDefaultSection().put(MISSING, DUMMY);
        assertEquals(DUMMY, instance.get(SECTION, OPTION));
        ((ConfigParser.PyIni) instance.getIni()).getDefaultSection().remove(MISSING);
        requireMissingOptionException(SECTION, OPTION);
        instance = new ConfigParser();
        instance.addSection(SECTION);
        instance.set(SECTION, OPTION, MISSING_REF);
        vars.put(MISSING, DUMMY);
        assertEquals(DUMMY, instance.get(SECTION, OPTION, false, vars));
    }

    @Test public void testItems() throws Exception
    {
        Ini ini = new Ini();

        ini.add(SECTION).from(DwarfsData.dopey);
        Ini.Section section = ini.get(SECTION);
        Ini.Section dopey = ini.add(Dwarfs.PROP_DOPEY);

        for (String key : section.keySet())
        {
            dopey.put(key.toLowerCase(), section.get(key));
        }

        readDwarfs();
        List<Map.Entry<String, String>> items = instance.items(Dwarfs.PROP_DOPEY);

        assertEquals(5, items.size());
        assertEquals(6, dopey.size());
        for (Map.Entry<String, String> entry : items)
        {
            assertEquals(dopey.get(entry.getKey()), entry.getValue());
        }

        // raw
        dopey = instance.getIni().get(Dwarfs.PROP_DOPEY);
        items = instance.items(Dwarfs.PROP_DOPEY, true);

        assertEquals(5, items.size());
        assertEquals("%(_weight)", dopey.get(Dwarf.PROP_WEIGHT));
        assertEquals("%(_height)", dopey.get(Dwarf.PROP_HEIGHT));
    }

    @Test public void testOptions() throws Exception
    {
        instance.addSection(SECTION);
        assertEquals(0, instance.options(SECTION).size());
        for (int i = 0; i < 10; i++)
        {
            instance.set(SECTION, OPTION + i, DUMMY);
        }

        assertEquals(10, instance.options(SECTION).size());
    }

    @Test public void testRead() throws Exception
    {
        File file = newTestFile(DWARFS_PATH);

        assertTrue(file.exists());
        instance.read(file.getCanonicalPath());
        instance.read(file);
        instance.read(new FileReader(file));
        instance.read(new FileInputStream(file));
        instance.read(file.toURI().toURL());
    }

    @Test(expected = ConfigParser.ParsingException.class)
    public void testReadFileException() throws Exception
    {
        instance.read(badFile());
    }

    @Test(expected = ConfigParser.ParsingException.class)
    public void testReadReaderException() throws Exception
    {
        instance.read(new StringReader(BAD));
    }

    @Test(expected = ConfigParser.ParsingException.class)
    public void testReadStreamException() throws Exception
    {
        instance.read(new ByteArrayInputStream(BAD.getBytes()));
    }

    @Test(expected = ConfigParser.ParsingException.class)
    public void testReadURLException() throws Exception
    {
        instance.read(badFile().toURI().toURL());
    }

    @Test public void testSections() throws Exception
    {
        instance.addSection(SECTION);
        assertEquals(1, instance.sections().size());
        for (int i = 0; i < 10; i++)
        {
            instance.addSection(SECTION + i);
        }

        assertEquals(11, instance.sections().size());
    }

    @Test public void testSet() throws Exception
    {
        instance.addSection(SECTION);
        instance.set(SECTION, OPTION, "dummy");
        assertEquals("dummy", instance.getIni().get(SECTION).get(OPTION));
        assertTrue(instance.hasOption(SECTION, OPTION));
        instance.set(SECTION, OPTION, null);
        assertFalse(instance.hasOption(SECTION, OPTION));
    }

    @Test(expected = ConfigParser.NoSectionException.class)
    public void testSetNoSection() throws Exception
    {
        instance.set(SECTION, OPTION, "dummy");
    }

    @Test public void testWrite() throws Exception
    {
        File input = newTestFile(DWARFS_PATH);
        File output = new File(TEST_WORK_PATH, input.getName());

        instance.read(input);
        instance.write(output);
        checkWrite(output);
        instance.write(new FileWriter(output));
        checkWrite(output);
        instance.write(new FileOutputStream(output));
        checkWrite(output);
    }

    protected void checkEquals(Dwarf expected, String sectionName) throws Exception
    {
        assertEquals("" + expected.getAge(), instance.get(sectionName, Dwarf.PROP_AGE));
        assertEquals("" + expected.getHeight(), instance.get(sectionName, Dwarf.PROP_HEIGHT));
        assertEquals("" + expected.getWeight(), instance.get(sectionName, Dwarf.PROP_WEIGHT));
        assertEquals("" + expected.getHomePage(), instance.get(sectionName, Dwarf.PROP_HOME_PAGE.toLowerCase()));
        assertEquals("" + expected.getHomeDir(), instance.get(sectionName, Dwarf.PROP_HOME_DIR.toLowerCase()));
    }

    protected File newTestFile(String path)
    {
        return new File(TEST_DATA_PATH, path);
    }

    protected void readDwarfs() throws Exception
    {
        instance.read(newTestFile(DWARFS_PATH));
    }

    private File badFile() throws IOException
    {
        File f = File.createTempFile("test", "ini");

        f.deleteOnExit();
        FileWriter w = new FileWriter(f);

        w.append(BAD);
        w.close();

        return f;
    }

    private void checkEquals(Map<String, String> a, Map<String, String> b) throws Exception
    {
        if (a == null)
        {
            assertNull(b);
        }
        else
        {
            assertEquals(a.size(), b.size());
            for (String key : a.keySet())
            {
                assertEquals(a.get(key), b.get(key));
            }
        }
    }

    private void checkWrite(File file) throws Exception
    {
        ConfigParser saved = new ConfigParser(instance.defaults());

        saved.read(file);
        checkEquals(((ConfigParser.PyIni) instance.getIni()).getDefaultSection(), ((ConfigParser.PyIni) saved.getIni()).getDefaultSection());
        assertEquals(instance.sections().size(), saved.sections().size());
        for (String sectionName : instance.sections())
        {
            checkEquals(instance.getIni().get(sectionName), saved.getIni().get(sectionName));
        }
    }

    @SuppressWarnings("empty-statement")
    private void requireMissingOptionException(String sectionName, String optionName) throws Exception
    {
        try
        {
            instance.get(sectionName, optionName);
            fail();
        }
        catch (ConfigParser.InterpolationMissingOptionException x)
        {
            ;
        }
    }
}
