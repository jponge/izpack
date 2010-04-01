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
import org.ini4j.Ini;

import org.ini4j.sample.Dwarf;
import org.ini4j.sample.Dwarfs;

import org.ini4j.test.DwarfsData;
import org.ini4j.test.Helper;

import static org.junit.Assert.*;

import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

public class IniFormatterTest
{
    private static final String NL = System.getProperty("line.separator");
    private static final String DUMMY = "dummy";

    @Test public void testFormat() throws Exception
    {
        Ini ini = Helper.newDwarfsIni();
        IniHandler handler = EasyMock.createMock(IniHandler.class);
        Dwarf dwarf;

        handler.startIni();
        handler.handleComment(Helper.HEADER_COMMENT);
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
        handler.handleOption(Dwarf.PROP_HOME_PAGE, String.valueOf(dwarf.getHomePage()));
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
        handler.endIni();
        EasyMock.replay(handler);
        verify(ini, handler);
    }

    @Test public void testNewInstance() throws Exception
    {
        StringWriter stringWriter;
        PrintWriter printWriter;
        Config cfg = new Config();
        IniFormatter instance;

        stringWriter = new StringWriter();
        instance = IniFormatter.newInstance(stringWriter, cfg);

        instance.getOutput().print(DUMMY);
        instance.getOutput().flush();
        assertEquals(DUMMY, stringWriter.toString());
        assertSame(cfg, instance.getConfig());

        //
        stringWriter = new StringWriter();
        instance = IniFormatter.newInstance(stringWriter, cfg);

        instance.getOutput().print(DUMMY);
        instance.getOutput().flush();
        assertEquals(DUMMY, stringWriter.toString());

        //
        printWriter = new PrintWriter(stringWriter);
        instance = IniFormatter.newInstance(printWriter, cfg);

        assertSame(printWriter, instance.getOutput());
    }

    @Test public void testWithEmptyOption() throws Exception
    {
        Config cfg = new Config();

        cfg.setEmptyOption(true);
        Ini ini = new Ini();
        Ini.Section sec = ini.add(Dwarfs.PROP_BASHFUL);

        sec.put(Dwarf.PROP_FORTUNE_NUMBER, null);
        ini.setConfig(cfg);
        IniHandler handler = EasyMock.createMock(IniHandler.class);

        handler.startIni();
        handler.startSection(Dwarfs.PROP_BASHFUL);
        handler.handleOption(Dwarf.PROP_FORTUNE_NUMBER, "");
        handler.endSection();
        handler.endIni();
        EasyMock.replay(handler);
        verify(ini, handler);
    }

    @Test public void testWithoutConfig() throws Exception
    {
        Ini ini = new Ini();
        Ini.Section sec = ini.add(Dwarfs.PROP_BASHFUL);

        sec.put(Dwarf.PROP_FORTUNE_NUMBER, null);
        IniHandler handler = EasyMock.createMock(IniHandler.class);

        handler.startIni();
        handler.startSection(Dwarfs.PROP_BASHFUL);
        handler.endSection();
        handler.endIni();
        EasyMock.replay(handler);
        verify(ini, handler);
    }

    @Test public void testWithStrictOperator() throws Exception
    {
        Config cfg = new Config();

        cfg.setStrictOperator(true);
        Ini ini = new Ini();
        Ini.Section sec = ini.add(Dwarfs.PROP_BASHFUL);

        sec.put(Dwarf.PROP_AGE, DwarfsData.bashful.age);
        ini.setConfig(cfg);
        StringWriter writer = new StringWriter();

        ini.store(writer);
        StringBuilder exp = new StringBuilder();

        exp.append(IniParser.SECTION_BEGIN);
        exp.append(Dwarfs.PROP_BASHFUL);
        exp.append(IniParser.SECTION_END);
        exp.append(NL);
        exp.append(Dwarf.PROP_AGE);
        exp.append('=');
        exp.append(DwarfsData.bashful.age);
        exp.append(NL);
        exp.append(NL);
        assertEquals(exp.toString(), writer.toString());
    }

    private void verify(Ini ini, IniHandler mock) throws Exception
    {
        StringWriter writer = new StringWriter();

        ini.store(writer);
        IniParser parser = new IniParser();

        parser.parse(new StringReader(writer.toString()), mock);
        EasyMock.verify(mock);
    }
}
