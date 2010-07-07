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
import org.ini4j.InvalidFileFormatException;

import org.ini4j.sample.Dwarf;
import org.ini4j.sample.Dwarfs;

import org.ini4j.test.DwarfsData;
import org.ini4j.test.Helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.StringReader;

public class IniParserTest extends Ini4jCase
{
    private static final String[] BAD = { "[section\noption=value\n", "[]\noption=value", "section\noption=value", "[section]\noption\n", "[section]\n=value\n", "[section]\n\\u000d\\u000d=value\n" };
    private static final String CFG_LOWER = "[SectioN]\n\nOptioN=ValuE\n";
    private static final String CFG_UNNAMED = "[]\noption=value\n";
    private static final String CFG_EMPTY_OPTION = "[section]\noption\n";
    private static final String CFG_GLOBAL = "option=value\n";
    private static final String[] CFG_EXTRA = { CFG_EMPTY_OPTION, CFG_UNNAMED, CFG_GLOBAL };
    private static final String ANONYMOUS = "?";
    private static final String EMPTY = "";
    private static final String SECTION = "section";
    private static final String OPTION = "option";
    private static final String VALUE = "value";

    @Test public void testEmpty() throws Exception
    {
        IniParser parser = new IniParser();
        IniHandler handler = EasyMock.createMock(IniHandler.class);

        handler.startIni();
        handler.endIni();
        EasyMock.replay(handler);
        parser.parse(new StringReader(EMPTY), handler);
        EasyMock.verify(handler);
    }

    @Test public void testEmptyOption() throws Exception
    {
        IniParser parser = new IniParser();
        IniHandler handler = EasyMock.createMock(IniHandler.class);

        handler.startIni();
        handler.startSection(SECTION);
        handler.handleOption(OPTION, null);
        handler.endSection();
        handler.endIni();
        EasyMock.replay(handler);
        Config cfg = new Config();

        cfg.setEmptyOption(true);
        parser.setConfig(cfg);
        parser.parse(new StringReader(CFG_EMPTY_OPTION), handler);
        EasyMock.verify(handler);
    }

    @Test public void testGlobalSection() throws Exception
    {
        IniParser parser = new IniParser();
        IniHandler handler = EasyMock.createMock(IniHandler.class);

        handler.startIni();
        handler.startSection(ANONYMOUS);
        handler.handleOption(OPTION, VALUE);
        handler.endSection();
        handler.endIni();
        EasyMock.replay(handler);
        Config cfg = new Config();

        cfg.setGlobalSection(true);
        parser.setConfig(cfg);
        parser.parse(new StringReader(CFG_GLOBAL), handler);
        EasyMock.verify(handler);
    }

    @Test public void testLower() throws Exception
    {
        IniParser parser = new IniParser();
        IniHandler handler = EasyMock.createMock(IniHandler.class);

        handler.startIni();
        handler.startSection(SECTION);
        handler.handleOption(OPTION, "ValuE");
        handler.endSection();
        handler.endIni();
        EasyMock.replay(handler);
        Config cfg = new Config();

        cfg.setLowerCaseOption(true);
        cfg.setLowerCaseSection(true);
        parser.setConfig(cfg);
        parser.parse(new StringReader(CFG_LOWER), handler);
        EasyMock.verify(handler);
    }

    @Test public void testNewInstance() throws Exception
    {
        Config cfg = new Config();
        IniParser parser = IniParser.newInstance();

        assertEquals(IniParser.class, parser.getClass());
        parser = IniParser.newInstance(cfg);
        assertEquals(IniParser.class, parser.getClass());
        assertSame(cfg, parser.getConfig());
    }

    @Test public void testParse() throws Exception
    {
        IniParser parser = new IniParser();
        IniHandler handler = EasyMock.createMock(IniHandler.class);
        Dwarf dwarf;

        handler.startIni();
        handler.handleComment(Helper.HEADER_COMMENT);
        handler.handleComment((String) EasyMock.anyObject());
        handler.handleComment(" " + Dwarfs.PROP_BASHFUL);
        dwarf = DwarfsData.bashful;
        handler.startSection(Dwarfs.PROP_BASHFUL);
        handler.handleOption(Dwarf.PROP_WEIGHT, String.valueOf(dwarf.getWeight()));
        handler.handleOption(Dwarf.PROP_HEIGHT, String.valueOf(dwarf.getHeight()));
        handler.handleOption(Dwarf.PROP_AGE, String.valueOf(dwarf.getAge()));
        handler.handleOption(Dwarf.PROP_HOME_PAGE, String.valueOf(dwarf.getHomePage()));
        handler.handleOption(Dwarf.PROP_HOME_DIR, String.valueOf(dwarf.getHomeDir()));
        handler.endSection();
        handler.handleComment(" " + Dwarfs.PROP_DOC);
        dwarf = DwarfsData.doc;
        handler.startSection(Dwarfs.PROP_DOC);
        handler.handleOption(Dwarf.PROP_WEIGHT, String.valueOf(dwarf.getWeight()));
        handler.handleOption(Dwarf.PROP_HEIGHT, String.valueOf(dwarf.getHeight()));
        handler.handleOption(Dwarf.PROP_AGE, String.valueOf(dwarf.getAge()));
        handler.handleOption(Dwarf.PROP_HOME_PAGE, String.valueOf(dwarf.getHomePage()));
        handler.handleOption(Dwarf.PROP_HOME_DIR, String.valueOf(dwarf.getHomeDir()));
        handler.endSection();
        handler.handleComment(" " + Dwarfs.PROP_DOPEY);
        dwarf = DwarfsData.dopey;
        handler.startSection(Dwarfs.PROP_DOPEY);
        handler.handleOption(Dwarf.PROP_WEIGHT, DwarfsData.INI_DOPEY_WEIGHT);
        handler.handleOption(Dwarf.PROP_HEIGHT, DwarfsData.INI_DOPEY_HEIGHT);
        handler.handleOption(Dwarf.PROP_AGE, String.valueOf(dwarf.getAge()));
        handler.handleOption(Dwarf.PROP_HOME_PAGE, String.valueOf(dwarf.getHomePage()));
        handler.handleOption(Dwarf.PROP_HOME_DIR, String.valueOf(dwarf.getHomeDir()));
        handler.handleOption(Dwarf.PROP_FORTUNE_NUMBER, String.valueOf(dwarf.getFortuneNumber()[0]));
        handler.handleOption(Dwarf.PROP_FORTUNE_NUMBER, String.valueOf(dwarf.getFortuneNumber()[1]));
        handler.handleOption(Dwarf.PROP_FORTUNE_NUMBER, String.valueOf(dwarf.getFortuneNumber()[2]));
        handler.endSection();
        handler.handleComment(" " + Dwarfs.PROP_GRUMPY);
        dwarf = DwarfsData.grumpy;
        handler.startSection(Dwarfs.PROP_GRUMPY);
        handler.handleOption(Dwarf.PROP_WEIGHT, String.valueOf(dwarf.getWeight()));
        handler.handleOption(Dwarf.PROP_HEIGHT, DwarfsData.INI_GRUMPY_HEIGHT);
        handler.handleOption(Dwarf.PROP_AGE, String.valueOf(dwarf.getAge()));
        handler.handleOption(Dwarf.PROP_HOME_PAGE, String.valueOf(dwarf.getHomePage()));
        handler.handleOption(Dwarf.PROP_HOME_DIR, String.valueOf(dwarf.getHomeDir()));
        handler.endSection();
        handler.handleComment(" " + Dwarfs.PROP_HAPPY);
        dwarf = DwarfsData.happy;
        handler.startSection(Dwarfs.PROP_HAPPY);
        handler.handleOption(Dwarf.PROP_WEIGHT, String.valueOf(dwarf.getWeight()));
        handler.handleOption(Dwarf.PROP_HEIGHT, String.valueOf(dwarf.getHeight()));
        handler.handleOption(Dwarf.PROP_AGE, String.valueOf(dwarf.getAge()));
        handler.handleOption(EasyMock.eq(Dwarf.PROP_HOME_PAGE), (String) EasyMock.anyObject());
        handler.handleOption(Dwarf.PROP_HOME_DIR, String.valueOf(dwarf.getHomeDir()));
        handler.endSection();
        handler.handleComment(" " + Dwarfs.PROP_SLEEPY);
        dwarf = DwarfsData.sleepy;
        handler.startSection(Dwarfs.PROP_SLEEPY);
        handler.handleOption(Dwarf.PROP_WEIGHT, String.valueOf(dwarf.getWeight()));
        handler.handleOption(Dwarf.PROP_HEIGHT, DwarfsData.INI_SLEEPY_HEIGHT);
        handler.handleOption(Dwarf.PROP_AGE, String.valueOf(dwarf.getAge()));
        handler.handleOption(Dwarf.PROP_HOME_PAGE, String.valueOf(dwarf.getHomePage()));
        handler.handleOption(Dwarf.PROP_HOME_DIR, String.valueOf(dwarf.getHomeDir()));
        handler.handleOption(Dwarf.PROP_FORTUNE_NUMBER, String.valueOf(dwarf.getFortuneNumber()[0]));
        handler.endSection();
        handler.handleComment(" " + Dwarfs.PROP_SNEEZY);
        dwarf = DwarfsData.sneezy;
        handler.startSection(Dwarfs.PROP_SNEEZY);
        handler.handleOption(Dwarf.PROP_WEIGHT, String.valueOf(dwarf.getWeight()));
        handler.handleOption(Dwarf.PROP_HEIGHT, String.valueOf(dwarf.getHeight()));
        handler.handleOption(Dwarf.PROP_AGE, String.valueOf(dwarf.getAge()));
        handler.handleOption(Dwarf.PROP_HOME_PAGE, DwarfsData.INI_SNEEZY_HOME_PAGE);
        handler.handleOption(Dwarf.PROP_HOME_DIR, String.valueOf(dwarf.getHomeDir()));
        handler.handleOption(Dwarf.PROP_FORTUNE_NUMBER, String.valueOf(dwarf.getFortuneNumber()[0]));
        handler.handleOption(Dwarf.PROP_FORTUNE_NUMBER, String.valueOf(dwarf.getFortuneNumber()[1]));
        handler.handleOption(Dwarf.PROP_FORTUNE_NUMBER, String.valueOf(dwarf.getFortuneNumber()[2]));
        handler.handleOption(Dwarf.PROP_FORTUNE_NUMBER, String.valueOf(dwarf.getFortuneNumber()[3]));
        handler.endSection();
        handler.handleComment(" " + Dwarfs.PROP_HAPPY + " again");
        dwarf = DwarfsData.happy;
        handler.startSection(Dwarfs.PROP_HAPPY);
        handler.handleOption(Dwarf.PROP_HOME_PAGE, String.valueOf(dwarf.getHomePage()));
        handler.handleComment("}");
        handler.endSection();
        handler.endIni();

        //
        EasyMock.replay(handler);
        parser.parse(Helper.getResourceURL(Helper.DWARFS_INI), handler);
        EasyMock.verify(handler);
    }

    @Test public void testParseExceptions() throws Exception
    {
        assertBad(BAD);
        assertBad(CFG_EXTRA);
    }

    @Test public void testUnnamedSection() throws Exception
    {
        IniParser parser = new IniParser();
        IniHandler handler = EasyMock.createMock(IniHandler.class);

        handler.startIni();
        handler.startSection(EMPTY);
        handler.handleOption(OPTION, VALUE);
        handler.endSection();
        handler.endIni();
        EasyMock.replay(handler);
        Config cfg = new Config();

        cfg.setUnnamedSection(true);
        parser.setConfig(cfg);
        parser.parse(new StringReader(CFG_UNNAMED), handler);
        EasyMock.verify(handler);
    }

    @SuppressWarnings("empty-statement")
    private void assertBad(String[] values) throws Exception
    {
        IniParser parser = new IniParser();
        IniHandler handler = EasyMock.createNiceMock(IniHandler.class);

        for (String s : values)
        {
            try
            {
                parser.parse(new ByteArrayInputStream(s.getBytes()), handler);
                fail("expected InvalidIniFormatException: " + s);
            }
            catch (InvalidFileFormatException x)
            {
                ;
            }
        }
    }
}
