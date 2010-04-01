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
import org.ini4j.test.TaleData;

import static org.junit.Assert.*;

import org.junit.Test;

public class BasicProfileSectionTest
{
    @Test public void testAddChild() throws Exception
    {
        Profile prof = Helper.newTaleIni();
        Profile.Section dwarfs = prof.get(TaleData.PROP_DWARFS);
        Profile.Section doc = dwarfs.getChild(Dwarfs.PROP_DOC);
        Profile.Section dopey2 = doc.addChild(Dwarfs.PROP_DOPEY);

        assertSame(doc, dopey2.getParent());
        assertSame(dopey2, dwarfs.lookup(Dwarfs.PROP_DOC, Dwarfs.PROP_DOPEY));
        assertSame(dopey2, dwarfs.lookup(Dwarfs.PROP_DOC + '/' + Dwarfs.PROP_DOPEY));
        assertEquals(1, doc.childrenNames().length);
        doc.removeChild(Dwarfs.PROP_DOPEY);
        assertEquals(0, doc.childrenNames().length);
        assertNull(dwarfs.lookup(Dwarfs.PROP_DOC, Dwarfs.PROP_DOPEY));
        assertNull(dwarfs.lookup(Dwarfs.PROP_DOC + '/' + Dwarfs.PROP_DOPEY));
    }

    @Test public void testGetChild() throws Exception
    {
        Profile prof = Helper.newTaleIni();
        Profile.Section dwarfs = prof.get(TaleData.PROP_DWARFS);

        assertArrayEquals(DwarfsData.dwarfNames, dwarfs.childrenNames());
        assertSame(prof.get(TaleData.bashful.name), dwarfs.getChild(Dwarfs.PROP_BASHFUL));
        assertSame(prof.get(TaleData.doc.name), dwarfs.getChild(Dwarfs.PROP_DOC));
        assertSame(prof.get(TaleData.dopey.name), dwarfs.getChild(Dwarfs.PROP_DOPEY));
        assertSame(prof.get(TaleData.grumpy.name), dwarfs.getChild(Dwarfs.PROP_GRUMPY));
        assertSame(prof.get(TaleData.happy.name), dwarfs.getChild(Dwarfs.PROP_HAPPY));
        assertSame(prof.get(TaleData.sleepy.name), dwarfs.getChild(Dwarfs.PROP_SLEEPY));
        assertSame(prof.get(TaleData.sneezy.name), dwarfs.getChild(Dwarfs.PROP_SNEEZY));
    }

    @Test public void testGetParent() throws Exception
    {
        Profile prof = Helper.newTaleIni();
        Profile.Section dwarfs = prof.get(TaleData.PROP_DWARFS);

        assertNull(dwarfs.getParent());
        assertSame(dwarfs, prof.get(TaleData.bashful.name).getParent());
        assertSame(dwarfs, prof.get(TaleData.doc.name).getParent());
        assertSame(dwarfs, prof.get(TaleData.dopey.name).getParent());
        assertSame(dwarfs, prof.get(TaleData.grumpy.name).getParent());
        assertSame(dwarfs, prof.get(TaleData.happy.name).getParent());
        assertSame(dwarfs, prof.get(TaleData.sleepy.name).getParent());
        assertSame(dwarfs, prof.get(TaleData.sneezy.name).getParent());
    }

    @Test public void testLoad() throws Exception
    {
        Profile prof = Helper.loadTaleIni();
        Profile.Section dwarfs = prof.get(TaleData.PROP_DWARFS);

        Helper.assertEquals(DwarfsData.bashful, dwarfs.getChild(Dwarfs.PROP_BASHFUL).as(Dwarf.class));
        Helper.assertEquals(DwarfsData.doc, dwarfs.getChild(Dwarfs.PROP_DOC).as(Dwarf.class));
        Helper.assertEquals(DwarfsData.dopey, dwarfs.getChild(Dwarfs.PROP_DOPEY).as(Dwarf.class));
        Helper.assertEquals(DwarfsData.grumpy, dwarfs.getChild(Dwarfs.PROP_GRUMPY).as(Dwarf.class));
        Helper.assertEquals(DwarfsData.happy, dwarfs.getChild(Dwarfs.PROP_HAPPY).as(Dwarf.class));
        Helper.assertEquals(DwarfsData.sleepy, dwarfs.getChild(Dwarfs.PROP_SLEEPY).as(Dwarf.class));
        Helper.assertEquals(DwarfsData.sneezy, dwarfs.getChild(Dwarfs.PROP_SNEEZY).as(Dwarf.class));
    }
}
