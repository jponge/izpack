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
import org.ini4j.sample.DwarfBean;
import org.ini4j.sample.Dwarfs;

import org.ini4j.test.DwarfsData;
import org.ini4j.test.DwarfsData.DwarfData;
import org.ini4j.test.Helper;

import static org.junit.Assert.*;

import org.junit.Test;

import java.net.URI;

public class BasicProfileTest
{
    private static final String SECTION = "section";
    private static final String NUMBER = "number";
    private static final String SINGLE = "single";
    private static final String SOLO = "solo";
    private static final String LOCATION = "location";
    private static final String LOCATION_1 = "http://www.ini4j.org";
    private static final String LOCATION_2 = "http://ini4j.org";

    @Test public void testAddPut()
    {
        Profile prof = new BasicProfile();

        prof.add(SECTION, Dwarf.PROP_AGE, DwarfsData.sneezy.age);
        prof.put(SECTION, Dwarf.PROP_HEIGHT, DwarfsData.sneezy.height);
        prof.add(SECTION, Dwarf.PROP_HOME_DIR, DwarfsData.sneezy.homeDir);
        prof.add(SECTION, Dwarf.PROP_WEIGHT, DwarfsData.sneezy.weight);
        prof.put(SECTION, Dwarf.PROP_HOME_PAGE, null);
        prof.put(SECTION, Dwarf.PROP_HOME_PAGE, DwarfsData.sneezy.homePage);
        prof.add(SECTION, Dwarf.PROP_FORTUNE_NUMBER, DwarfsData.sneezy.fortuneNumber[0]);
        prof.add(SECTION, Dwarf.PROP_FORTUNE_NUMBER, DwarfsData.sneezy.fortuneNumber[1]);
        prof.add(SECTION, Dwarf.PROP_FORTUNE_NUMBER, DwarfsData.sneezy.fortuneNumber[2]);
        prof.add(SECTION, Dwarf.PROP_FORTUNE_NUMBER, DwarfsData.sneezy.fortuneNumber[3]);
        Helper.assertEquals(DwarfsData.sneezy, prof.get(SECTION).as(Dwarf.class));
        assertNotNull(prof.remove(SECTION, Dwarf.PROP_FORTUNE_NUMBER));
        assertEquals(0, prof.get(SECTION).length(Dwarf.PROP_FORTUNE_NUMBER));
        assertNotNull(prof.remove(SECTION));
        assertNull(prof.remove(SECTION, Dwarf.PROP_FORTUNE_NUMBER));
    }

    @Test public void testFirstUpper()
    {
        BasicProfile prof = new BasicProfile(true, true);
        DwarfsRW dwarfs = prof.as(DwarfsRW.class);

        dwarfs.setBashful(DwarfsData.bashful);
        assertTrue(prof.containsKey("Bashful"));
        assertNotNull(dwarfs.getBashful());
    }

    @Test public void testFromToAs() throws Exception
    {
        BasicProfile prof = new BasicProfile();

        Helper.addDwarfs(prof);
        fromToAs(prof, DwarfsData.bashful);
        fromToAs(prof, DwarfsData.doc);
        fromToAs(prof, DwarfsData.dopey);
        fromToAs(prof, DwarfsData.grumpy);
        fromToAs(prof, DwarfsData.happy);
        fromToAs(prof, DwarfsData.sleepy);
        fromToAs(prof, DwarfsData.sneezy);

        //
        DwarfsRW dwarfs = prof.as(DwarfsRW.class);

        Helper.assertEquals(DwarfsData.bashful, dwarfs.getBashful());
        Helper.assertEquals(DwarfsData.doc, dwarfs.getDoc());
        Helper.assertEquals(DwarfsData.dopey, dwarfs.getDopey());
        Helper.assertEquals(DwarfsData.grumpy, dwarfs.getGrumpy());
        Helper.assertEquals(DwarfsData.happy, dwarfs.getHappy());
        Helper.assertEquals(DwarfsData.sleepy, dwarfs.getSleepy());
        Helper.assertEquals(DwarfsData.sneezy, dwarfs.getSneezy());

        //
        prof.remove(Dwarfs.PROP_BASHFUL);
        assertNull(prof.get(Dwarfs.PROP_BASHFUL));
        assertEquals(0, prof.length(Dwarfs.PROP_BASHFUL));
        assertNull(dwarfs.getBashful());
        dwarfs.setBashful(DwarfsData.dopey);
        Helper.assertEquals(DwarfsData.dopey, dwarfs.getBashful());
    }

    @Test public void testIniGetFetch()
    {
        Profile prof = new BasicProfile();
        Profile.Section sec = Helper.addDwarf(prof, DwarfsData.dopey);

        Helper.addDwarf(prof, DwarfsData.bashful);
        Helper.addDwarf(prof, DwarfsData.doc);
        assertEquals(sec.get(Dwarf.PROP_AGE), prof.get(Dwarfs.PROP_DOPEY, Dwarf.PROP_AGE));
        assertEquals(DwarfsData.dopey.age, (int) prof.get(Dwarfs.PROP_DOPEY, Dwarf.PROP_AGE, int.class));
        assertEquals(sec.get(Dwarf.PROP_WEIGHT), prof.get(Dwarfs.PROP_DOPEY, Dwarf.PROP_WEIGHT));
        assertEquals(DwarfsData.dopey.weight, prof.fetch(Dwarfs.PROP_DOPEY, Dwarf.PROP_WEIGHT, double.class), Helper.DELTA);
        assertEquals(sec.fetch(Dwarf.PROP_HEIGHT), prof.fetch(Dwarfs.PROP_DOPEY, Dwarf.PROP_HEIGHT));
        assertEquals(DwarfsData.dopey.weight, prof.fetch(Dwarfs.PROP_DOPEY, Dwarf.PROP_WEIGHT, double.class), Helper.DELTA);
        assertEquals(sec.fetch(Dwarf.PROP_HOME_PAGE), prof.fetch(Dwarfs.PROP_DOPEY, Dwarf.PROP_HOME_PAGE));
        assertEquals(DwarfsData.dopey.homePage, prof.fetch(Dwarfs.PROP_DOPEY, Dwarf.PROP_HOME_PAGE, URI.class));

        // nulls
        assertNull(prof.get(SECTION, Dwarf.PROP_AGE));
        assertEquals(0, (int) prof.get(SECTION, Dwarf.PROP_AGE, int.class));
        assertNull(prof.get(SECTION, Dwarf.PROP_WEIGHT));
        assertEquals(0.0, prof.fetch(SECTION, Dwarf.PROP_WEIGHT, double.class), Helper.DELTA);
        assertNull(prof.fetch(SECTION, Dwarf.PROP_HEIGHT));
        assertEquals(0.0, prof.fetch(SECTION, Dwarf.PROP_WEIGHT, double.class), Helper.DELTA);
        assertNull(prof.fetch(SECTION, Dwarf.PROP_HOME_PAGE));
        assertNull(prof.fetch(SECTION, Dwarf.PROP_HOME_PAGE, URI.class));
    }

    @Test public void testOptionArray() throws Exception
    {
        BasicProfile prof = new BasicProfile();
        Profile.Section sec = prof.add(SECTION);

        sec.add(NUMBER, 1);
        sec.add(LOCATION, LOCATION_1);
        sec.add(NUMBER, 2);
        sec.add(LOCATION, LOCATION_2);
        Section s = prof.get(SECTION).as(Section.class);

        assertNotNull(s);
        assertEquals(2, s.getNumber().length);
        assertEquals(1, s.getNumber()[0]);
        assertEquals(2, s.getNumber()[1]);
        assertEquals(2, s.getLocation().length);
        assertEquals(new URI(LOCATION_1), s.getLocation()[0]);
        assertNull(s.getMissing());
        int[] numbers = new int[] { 1, 2, 3, 4, 5 };

        s.setNumber(numbers);
        assertEquals(5, sec.length(NUMBER));
    }

    @Test public void testResolve() throws Exception
    {
        BasicProfile prof = new BasicProfile();

        Helper.addDwarf(prof, DwarfsData.happy);
        Profile.Section doc = Helper.addDwarf(prof, DwarfsData.doc);
        StringBuilder buffer;
        String input;

        // other sections's value
        input = "${happy/weight}";
        buffer = new StringBuilder(input);

        prof.resolve(buffer, doc);
        assertEquals(String.valueOf(DwarfsData.happy.weight), buffer.toString());

        // same sections's value
        input = "${height}";
        buffer = new StringBuilder(input);

        prof.resolve(buffer, doc);
        assertEquals(String.valueOf(DwarfsData.doc.height), buffer.toString());

        // system property
        input = "${@prop/user.home}";
        buffer = new StringBuilder(input);

        prof.resolve(buffer, doc);
        assertEquals(System.getProperty("user.home"), buffer.toString());

        // system environment
        input = "${@env/PATH}";
        buffer = new StringBuilder(input);
        try
        {
            prof.resolve(buffer, doc);
            assertEquals(System.getenv("PATH"), buffer.toString());
        }
        catch (Error e)
        {
            // retroweaver + JDK 1.4 throws Error on getenv
        }

        // unknown variable
        input = "${no such name}";
        buffer = new StringBuilder(input);

        prof.resolve(buffer, doc);
        assertEquals(input, buffer.toString());

        // unknown section's unknown variable
        input = "${no such section/no such name}";
        buffer = new StringBuilder(input);

        prof.resolve(buffer, doc);
        assertEquals(input, buffer.toString());

        // other section's unknown variable
        input = "${happy/no such name}";
        buffer = new StringBuilder(input);

        prof.resolve(buffer, doc);
        assertEquals(input, buffer.toString());

        // small input
        input = "${";
        buffer = new StringBuilder(input);

        prof.resolve(buffer, doc);
        assertEquals(input, buffer.toString());

        // incorrect references
        input = "${doc/weight";
        buffer = new StringBuilder(input);

        prof.resolve(buffer, doc);
        assertEquals(input, buffer.toString());

        // empty references
        input = "jim${}";
        buffer = new StringBuilder(input);

        prof.resolve(buffer, doc);
        assertEquals(input, buffer.toString());

        // escaped references
        input = "${happy/weight}";
        buffer = new StringBuilder(input);

        prof.resolve(buffer, doc);
        assertEquals("" + DwarfsData.happy.weight, buffer.toString());
        input = "\\" + input;
        buffer = new StringBuilder(input);

        prof.resolve(buffer, doc);
        assertEquals(input, buffer.toString());
    }

    @Test public void testResolveArray() throws Exception
    {
        StringBuilder buffer;
        BasicProfile prof = new BasicProfile();

        prof.add(SECTION).add(NUMBER, 1);
        prof.add(SECTION).add(NUMBER, 2);
        Profile.Section sec = prof.get(SECTION);

        //
        buffer = new StringBuilder("${section[0]/number}");
        prof.resolve(buffer, sec);
        assertEquals("1", buffer.toString());
        buffer = new StringBuilder("${section[1]/number}");
        prof.resolve(buffer, sec);
        assertEquals("2", buffer.toString());
        buffer = new StringBuilder("${section[0]/number}-${section[1]/number}");
        prof.resolve(buffer, sec);
        assertEquals("1-2", buffer.toString());

        //
        prof.clear();
        sec = prof.add(SECTION);
        sec.add(NUMBER, 1);
        sec.add(NUMBER, 2);
        sec = prof.get(SECTION);
        assertEquals(2, sec.length(NUMBER));
        buffer = new StringBuilder("${number}");
        prof.resolve(buffer, sec);
        assertEquals("2", buffer.toString());
        buffer = new StringBuilder("${number[0]}-${section/number[1]}-${section[0]/number}");
        prof.resolve(buffer, sec);
        assertEquals("1-2-2", buffer.toString());
    }

    @Test public void testSectionArray() throws Exception
    {
        BasicProfile prof = new BasicProfile();

        prof.add(SECTION).add(NUMBER, 1);
        prof.add(SECTION).add(NUMBER, 2);
        prof.add(SINGLE).add(NUMBER, 3);
        Global g = prof.as(Global.class);

        assertNotNull(g);
        assertEquals(2, g.getSection().length);
        assertEquals(1, g.getSingle().length);
        assertNull(g.getMissing());
        assertTrue(g.hasSection());
    }

    @Test public void testSetter()
    {
        BasicProfile prof = new BasicProfile();
        Global g = prof.as(Global.class);
        Section s1 = new SectionBean();
        Section s2 = new SectionBean();
        Section[] all = new Section[] { s1, s2 };

        g.setSection(all);
        assertEquals(2, prof.length("section"));
        assertNull(g.getSolo());
        g.setSolo(s1);
        assertNotNull(g.getSolo());
        g.setSolo(null);
        assertEquals(0, prof.length("solo"));
    }

    private void fromToAs(BasicProfile prof, DwarfData dwarf)
    {
        Profile.Section sec = prof.get(dwarf.name);
        Profile.Section dup = new BasicProfileSection(prof, SECTION);
        DwarfBean bean = new DwarfBean();

        sec.to(bean);
        Helper.assertEquals(dwarf, bean);
        dup.from(bean);
        bean = new DwarfBean();
        dup.to(bean);
        Helper.assertEquals(dwarf, bean);
        Dwarf proxy = dup.as(Dwarf.class);

        Helper.assertEquals(dwarf, proxy);
        dup.clear();
        sec.to(proxy);
        Helper.assertEquals(dwarf, proxy);
        prof.remove(dup);
    }

    public static interface DwarfsRW extends Dwarfs
    {
        void setBashful(Dwarf value);
    }

    public static interface Global
    {
        Section[] getMissing();

        Section[] getSection();

        void setSection(Section[] value);

        Section[] getSingle();

        Section getSolo();

        void setSolo(Section value);

        boolean hasSection();
    }

    public static interface Section
    {
        URI[] getLocation();

        void setLocation(URI[] value);

        String[] getMissing();

        void setMissing(String[] value);

        int[] getNumber();

        void setNumber(int[] value);
    }

    public static class SectionBean implements Section
    {
        private URI[] _location;
        private String[] _missing;
        private int[] _number;

        @Override public URI[] getLocation()
        {
            return _location;
        }

        @Override public void setLocation(URI[] value)
        {
            _location = value;
        }

        @Override public String[] getMissing()
        {
            return _missing;
        }

        @Override public void setMissing(String[] value)
        {
            _missing = value;
        }

        @Override public int[] getNumber()
        {
            return _number;
        }

        @Override public void setNumber(int[] value)
        {
            _number = value;
        }
    }
}
