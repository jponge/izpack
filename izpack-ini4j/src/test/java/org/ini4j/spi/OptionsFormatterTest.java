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
import org.ini4j.Options;

import org.ini4j.sample.Dwarf;
import org.ini4j.sample.Dwarfs;

import org.ini4j.test.DwarfsData;
import org.ini4j.test.Helper;

import static org.junit.Assert.*;

import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

public class OptionsFormatterTest
{
    private static final String NL = System.getProperty("line.separator");
    private static final String DUMMY = "dummy";

    @Test public void testFormat() throws Exception
    {
        Options opts = Helper.newDwarfsOpt();
        OptionsHandler handler = EasyMock.createMock(OptionsHandler.class);
        Dwarf dwarf;
        String prefix;

        handler.startOptions();
        handler.handleComment(Helper.HEADER_COMMENT);
        handler.handleComment((String) EasyMock.anyObject());
        dwarf = DwarfsData.dopey;
        handler.handleOption(Dwarf.PROP_WEIGHT, DwarfsData.OPT_DOPEY_WEIGHT);
        handler.handleOption(Dwarf.PROP_HEIGHT, DwarfsData.OPT_DOPEY_HEIGHT);
        handler.handleOption(Dwarf.PROP_AGE, String.valueOf(dwarf.getAge()));
        handler.handleOption(Dwarf.PROP_HOME_PAGE, String.valueOf(dwarf.getHomePage()));
        handler.handleOption(Dwarf.PROP_HOME_DIR, String.valueOf(dwarf.getHomeDir()));
        handler.handleOption(Dwarf.PROP_FORTUNE_NUMBER, "11");
        handler.handleOption(Dwarf.PROP_FORTUNE_NUMBER, "33");
        handler.handleOption(Dwarf.PROP_FORTUNE_NUMBER, "55");
//

        //
        handler.handleComment(" " + Dwarfs.PROP_BASHFUL);
        dwarf = DwarfsData.bashful;
        prefix = Dwarfs.PROP_BASHFUL + ".";

        handler.handleOption(prefix + Dwarf.PROP_WEIGHT, String.valueOf(dwarf.getWeight()));
        handler.handleOption(prefix + Dwarf.PROP_HEIGHT, String.valueOf(dwarf.getHeight()));
        handler.handleOption(prefix + Dwarf.PROP_AGE, String.valueOf(dwarf.getAge()));
        handler.handleOption(prefix + Dwarf.PROP_HOME_PAGE, String.valueOf(dwarf.getHomePage()));
        handler.handleOption(prefix + Dwarf.PROP_HOME_DIR, String.valueOf(dwarf.getHomeDir()));
        handler.handleComment(" " + Dwarfs.PROP_DOC);
        dwarf = DwarfsData.doc;
        prefix = Dwarfs.PROP_DOC + ".";

        handler.handleOption(prefix + Dwarf.PROP_WEIGHT, String.valueOf(dwarf.getWeight()));
        handler.handleOption(prefix + Dwarf.PROP_HEIGHT, String.valueOf(dwarf.getHeight()));
        handler.handleOption(prefix + Dwarf.PROP_AGE, String.valueOf(dwarf.getAge()));
        handler.handleOption(prefix + Dwarf.PROP_HOME_PAGE, String.valueOf(dwarf.getHomePage()));
        handler.handleOption(prefix + Dwarf.PROP_HOME_DIR, String.valueOf(dwarf.getHomeDir()));
        handler.handleComment(" " + Dwarfs.PROP_DOPEY);
        dwarf = DwarfsData.dopey;
        prefix = Dwarfs.PROP_DOPEY + ".";

        handler.handleOption(prefix + Dwarf.PROP_WEIGHT, DwarfsData.OPT_DOPEY_WEIGHT);
        handler.handleOption(prefix + Dwarf.PROP_HEIGHT, DwarfsData.OPT_DOPEY_HEIGHT);
        handler.handleOption(prefix + Dwarf.PROP_AGE, String.valueOf(dwarf.getAge()));
        handler.handleOption(prefix + Dwarf.PROP_HOME_PAGE, String.valueOf(dwarf.getHomePage()));
        handler.handleOption(prefix + Dwarf.PROP_HOME_DIR, String.valueOf(dwarf.getHomeDir()));
        handler.handleOption(prefix + Dwarf.PROP_FORTUNE_NUMBER, String.valueOf(dwarf.getFortuneNumber()[0]));
        handler.handleOption(prefix + Dwarf.PROP_FORTUNE_NUMBER, String.valueOf(dwarf.getFortuneNumber()[1]));
        handler.handleOption(prefix + Dwarf.PROP_FORTUNE_NUMBER, String.valueOf(dwarf.getFortuneNumber()[2]));
        handler.handleComment(" " + Dwarfs.PROP_GRUMPY);
        dwarf = DwarfsData.grumpy;
        prefix = Dwarfs.PROP_GRUMPY + ".";

        handler.handleOption(prefix + Dwarf.PROP_WEIGHT, String.valueOf(dwarf.getWeight()));
        handler.handleOption(prefix + Dwarf.PROP_HEIGHT, DwarfsData.OPT_GRUMPY_HEIGHT);
        handler.handleOption(prefix + Dwarf.PROP_AGE, String.valueOf(dwarf.getAge()));
        handler.handleOption(prefix + Dwarf.PROP_HOME_PAGE, String.valueOf(dwarf.getHomePage()));
        handler.handleOption(prefix + Dwarf.PROP_HOME_DIR, String.valueOf(dwarf.getHomeDir()));
        handler.handleComment(" " + Dwarfs.PROP_HAPPY);
        dwarf = DwarfsData.happy;
        prefix = Dwarfs.PROP_HAPPY + ".";

        handler.handleOption(prefix + Dwarf.PROP_WEIGHT, String.valueOf(dwarf.getWeight()));
        handler.handleOption(prefix + Dwarf.PROP_HEIGHT, String.valueOf(dwarf.getHeight()));
        handler.handleOption(prefix + Dwarf.PROP_AGE, String.valueOf(dwarf.getAge()));
        handler.handleOption(prefix + Dwarf.PROP_HOME_PAGE, String.valueOf(dwarf.getHomePage()));
        handler.handleOption(prefix + Dwarf.PROP_HOME_DIR, String.valueOf(dwarf.getHomeDir()));
        handler.handleComment(" " + Dwarfs.PROP_SLEEPY);
        dwarf = DwarfsData.sleepy;
        prefix = Dwarfs.PROP_SLEEPY + ".";

        handler.handleOption(prefix + Dwarf.PROP_WEIGHT, String.valueOf(dwarf.getWeight()));
        handler.handleOption(prefix + Dwarf.PROP_HEIGHT, DwarfsData.OPT_SLEEPY_HEIGHT);
        handler.handleOption(prefix + Dwarf.PROP_AGE, String.valueOf(dwarf.getAge()));
        handler.handleOption(prefix + Dwarf.PROP_HOME_PAGE, String.valueOf(dwarf.getHomePage()));
        handler.handleOption(prefix + Dwarf.PROP_HOME_DIR, String.valueOf(dwarf.getHomeDir()));
        handler.handleOption(prefix + Dwarf.PROP_FORTUNE_NUMBER, String.valueOf(dwarf.getFortuneNumber()[0]));
        handler.handleComment(" " + Dwarfs.PROP_SNEEZY);
        dwarf = DwarfsData.sneezy;
        prefix = Dwarfs.PROP_SNEEZY + ".";

        handler.handleOption(prefix + Dwarf.PROP_WEIGHT, String.valueOf(dwarf.getWeight()));
        handler.handleOption(prefix + Dwarf.PROP_HEIGHT, String.valueOf(dwarf.getHeight()));
        handler.handleOption(prefix + Dwarf.PROP_AGE, String.valueOf(dwarf.getAge()));
        handler.handleOption(prefix + Dwarf.PROP_HOME_PAGE, DwarfsData.OPT_SNEEZY_HOME_PAGE);
        handler.handleOption(prefix + Dwarf.PROP_HOME_DIR, String.valueOf(dwarf.getHomeDir()));
        handler.handleOption(prefix + Dwarf.PROP_FORTUNE_NUMBER, String.valueOf(dwarf.getFortuneNumber()[0]));
        handler.handleOption(prefix + Dwarf.PROP_FORTUNE_NUMBER, String.valueOf(dwarf.getFortuneNumber()[1]));
        handler.handleOption(prefix + Dwarf.PROP_FORTUNE_NUMBER, String.valueOf(dwarf.getFortuneNumber()[2]));
        handler.handleOption(prefix + Dwarf.PROP_FORTUNE_NUMBER, String.valueOf(dwarf.getFortuneNumber()[3]));
        handler.endOptions();

        //
        EasyMock.replay(handler);
        verify(opts, handler);
    }

    @Test public void testNewInstance() throws Exception
    {
        StringWriter stringWriter;
        PrintWriter printWriter;

        ;
        Config cfg = new Config();
        OptionsFormatter instance;

        stringWriter = new StringWriter();
        instance = OptionsFormatter.newInstance(stringWriter, cfg);

        instance.getOutput().print(DUMMY);
        instance.getOutput().flush();
        assertEquals(DUMMY, stringWriter.toString());
        assertSame(cfg, instance.getConfig());

        //
        stringWriter = new StringWriter();
        instance = OptionsFormatter.newInstance(stringWriter, cfg);

        instance.getOutput().print(DUMMY);
        instance.getOutput().flush();
        assertEquals(DUMMY, stringWriter.toString());

        //
        printWriter = new PrintWriter(stringWriter);
        instance = OptionsFormatter.newInstance(printWriter, cfg);

        assertSame(printWriter, instance.getOutput());
    }

    @Test public void testWithStrictOperatorEmptyOptions() throws Exception
    {
        Config cfg = new Config();

        cfg.setStrictOperator(true);
        cfg.setEmptyOption(true);
        Options opts = new Options();

        opts.setConfig(cfg);
        opts.put(Dwarf.PROP_AGE, DwarfsData.bashful.age);
        opts.put(Dwarf.PROP_WEIGHT, null);
        StringWriter writer = new StringWriter();

        opts.store(writer);
        StringBuilder exp = new StringBuilder();

        exp.append(Dwarf.PROP_AGE);
        exp.append('=');
        exp.append(DwarfsData.bashful.age);
        exp.append(NL);
        exp.append(Dwarf.PROP_WEIGHT);
        exp.append('=');
        exp.append(NL);
        assertEquals(exp.toString(), writer.toString());
    }

    @Test public void testWithStrictOperatorNoEmptyOptions() throws Exception
    {
        Config cfg = new Config();

        cfg.setStrictOperator(true);
        cfg.setEmptyOption(false);
        Options opts = new Options();

        opts.setConfig(cfg);
        opts.put(Dwarf.PROP_AGE, DwarfsData.bashful.age);
        opts.put(Dwarf.PROP_WEIGHT, null);
        StringWriter writer = new StringWriter();

        opts.store(writer);
        StringBuilder exp = new StringBuilder();

        exp.append(Dwarf.PROP_AGE);
        exp.append('=');
        exp.append(DwarfsData.bashful.age);
        exp.append(NL);
        assertEquals(exp.toString(), writer.toString());
    }

    private void verify(Options opts, OptionsHandler mock) throws Exception
    {
        StringWriter writer = new StringWriter();

        opts.store(writer);
        OptionsParser parser = new OptionsParser();

        parser.parse(new StringReader(writer.toString()), mock);
        EasyMock.verify(mock);
    }
}
