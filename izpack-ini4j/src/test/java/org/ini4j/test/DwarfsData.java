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
package org.ini4j.test;

import org.ini4j.sample.Dwarf;
import org.ini4j.sample.Dwarfs;

import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;

import java.net.URI;

public final class DwarfsData implements Dwarfs
{
    public static final DwarfData bashful;
    public static final DwarfData doc;
    public static final DwarfData dopey;
    public static final DwarfData grumpy;
    public static final DwarfData happy;
    public static final DwarfData sleepy;
    public static final DwarfData sneezy;
    public static final Dwarfs dwarfs;
    public static final String[] dwarfNames;
    public static final String INI_DOPEY_WEIGHT = "${bashful/weight}";
    public static final String INI_DOPEY_HEIGHT = "${doc/height}";
    public static final String INI_GRUMPY_HEIGHT = "${dopey/height}";
    public static final String INI_SLEEPY_HEIGHT = "${doc/height}8";
    public static final String INI_SNEEZY_HOME_PAGE = "${happy/homePage}/~sneezy";
    public static final String OPT_DOPEY_WEIGHT = "${bashful.weight}";
    public static final String OPT_DOPEY_HEIGHT = "${doc.height}";
    public static final String OPT_GRUMPY_HEIGHT = "${dopey.height}";
    public static final String OPT_SLEEPY_HEIGHT = "${doc.height}8";
    public static final String OPT_SNEEZY_HOME_PAGE = "${happy.homePage}/~sneezy";

    static
    {

        // age, fortuneNumber, height, homeDir, homePage, weight
        bashful = new DwarfData(PROP_BASHFUL, 67, null, 98.8, "/home/bashful", "http://snowwhite.tale/~bashful", 45.7);
        doc = new DwarfData(PROP_DOC, 63, null, 87.7, "c:Documents and Settingsdoc", "http://doc.dwarfs", 49.5);
        dopey = new DwarfData(PROP_DOPEY, 23, new int[] { 11, 33, 55 }, doc.height, "c:\\Documents and Settings\\dopey", "http://dopey.snowwhite.tale/", bashful.weight);
        grumpy = new DwarfData(PROP_GRUMPY, 76, null, dopey.height, "/home/grumpy", "http://snowwhite.tale/~grumpy/", 65.3);
        happy = new DwarfData(PROP_HAPPY, 99, null, 77.66, "/home/happy", "http://happy.smurf", 56.4);
        sleepy = new DwarfData(PROP_SLEEPY, 121, new int[] { 99 }, doc.height + 0.08, "/home/sleepy", "http://snowwhite.tale/~sleepy", 76.11);
        sneezy = new DwarfData(PROP_SNEEZY, 64, new int[] { 11, 22, 33, 44 }, 76.88, "/home/sneezy", happy.homePage.toString() + "/~sneezy", 69.7);
        dwarfs = new DwarfsData();
        dwarfNames = new String[] { bashful.name, doc.name, dopey.name, grumpy.name, happy.name, sleepy.name, sneezy.name };
    }

    @SuppressWarnings("empty-statement")
    private DwarfsData()
    {
        ;
    }

    public Dwarf getBashful()
    {
        return bashful;
    }

    public Dwarf getDoc()
    {
        return doc;
    }

    public Dwarf getDopey()
    {
        return dopey;
    }

    public Dwarf getGrumpy()
    {
        return grumpy;
    }

    public Dwarf getHappy()
    {
        return happy;
    }

    public Dwarf getSleepy()
    {
        return sleepy;
    }

    public Dwarf getSneezy()
    {
        return sneezy;
    }

    public static class DwarfData implements Dwarf
    {
        private static final String READ_ONLY_INSTANCE = "Read only instance";
        public final int age;
        public final int[] fortuneNumber;
        public final double height;
        public final String homeDir;
        public final URI homePage;
        public final String name;
        public final double weight;

        public DwarfData(String name, int age, int[] fortuneNumber, double height, String homeDir, String homePage, double weight)
        {
            this.name = name;
            this.age = age;
            this.fortuneNumber = fortuneNumber;
            this.height = height;
            this.homeDir = homeDir;
            this.homePage = URI.create(homePage);
            this.weight = weight;
        }

        public int getAge()
        {
            return age;
        }

        public void setAge(int age)
        {
            throw new UnsupportedOperationException(READ_ONLY_INSTANCE);
        }

        public int[] getFortuneNumber()
        {
            return fortuneNumber;
        }

        public void setFortuneNumber(int[] value)
        {
            throw new UnsupportedOperationException(READ_ONLY_INSTANCE);
        }

        public double getHeight()
        {
            return height;
        }

        public void setHeight(double height) throws PropertyVetoException
        {
            throw new UnsupportedOperationException(READ_ONLY_INSTANCE);
        }

        public String getHomeDir()
        {
            return homeDir;
        }

        public void setHomeDir(String dir)
        {
            throw new UnsupportedOperationException(READ_ONLY_INSTANCE);
        }

        public URI getHomePage()
        {
            return homePage;
        }

        public void setHomePage(URI location)
        {
            throw new UnsupportedOperationException(READ_ONLY_INSTANCE);
        }

        public double getWeight()
        {
            return weight;
        }

        public void setWeight(double weight)
        {
            throw new UnsupportedOperationException(READ_ONLY_INSTANCE);
        }

        public void addPropertyChangeListener(String property, PropertyChangeListener listener)
        {
            throw new UnsupportedOperationException(READ_ONLY_INSTANCE);
        }

        public void addVetoableChangeListener(String property, VetoableChangeListener listener)
        {
            throw new UnsupportedOperationException(READ_ONLY_INSTANCE);
        }

        public boolean hasAge()
        {
            return age != 0;
        }

        public boolean hasHeight()
        {
            return height != 0.0;
        }

        public boolean hasHomePage()
        {
            return homePage != null;
        }

        public boolean hasWeight()
        {
            return weight != 0.0;
        }

        public void removePropertyChangeListener(String property, PropertyChangeListener listener)
        {
            throw new UnsupportedOperationException(READ_ONLY_INSTANCE);
        }

        public void removeVetoableChangeListener(String property, VetoableChangeListener listener)
        {
            throw new UnsupportedOperationException(READ_ONLY_INSTANCE);
        }
    }
}
