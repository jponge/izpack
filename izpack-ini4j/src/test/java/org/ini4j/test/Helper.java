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

import org.ini4j.Config;
import org.ini4j.Ini;
import org.ini4j.OptionMap;
import org.ini4j.Options;
import org.ini4j.Profile;
import org.ini4j.Reg;
import org.ini4j.Registry;

import org.ini4j.sample.Dwarf;
import org.ini4j.sample.Dwarfs;

import org.ini4j.spi.IniFormatter;
import org.ini4j.spi.IniParser;

import org.ini4j.test.DwarfsData.DwarfData;

import org.junit.Assert;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import java.net.URL;

public class Helper
{
    private static final String RESOURCE_PREFIX = "org/ini4j/sample/";
    private static final File _sourceDir = new File(System.getProperty("basedir") + "/src/test/java/");
    private static final File _targetDir = new File(System.getProperty("basedir") + "/target");
    public static final String DWARFS_INI = RESOURCE_PREFIX + "dwarfs.ini";
    public static final String TALE_INI = RESOURCE_PREFIX + "tale.ini";
    public static final String DWARFS_OPT = RESOURCE_PREFIX + "dwarfs.opt";
    public static final String DWARFS_REG = RESOURCE_PREFIX + "dwarfs.reg";
    public static final String TEST_REG = "org/ini4j/mozilla.reg";
    public static final String DWARFS_REG_PATH = Reg.Hive.HKEY_CURRENT_USER + "\\Software\\ini4j-test";
    public static final float DELTA = 0.00000001f;
    private static final String[] CONFIG_PROPERTIES =
        {
            Config.PROP_EMPTY_OPTION, Config.PROP_GLOBAL_SECTION, Config.PROP_GLOBAL_SECTION_NAME, Config.PROP_INCLUDE, Config.PROP_LOWER_CASE_OPTION,
            Config.PROP_LOWER_CASE_SECTION, Config.PROP_MULTI_OPTION, Config.PROP_MULTI_SECTION, Config.PROP_STRICT_OPERATOR,
            Config.PROP_UNNAMED_SECTION, Config.PROP_ESCAPE
        };
    private static final String[] FACTORY_PROPERTIES = { IniFormatter.class.getName(), IniParser.class.getName() };
    public static final String HEADER_COMMENT = " Copyright 2005,2009 Ivan SZKIBA\n" + "\n"
        + " Licensed under the Apache License, Version 2.0 (the \"License\");\n"
        + " you may not use this file except in compliance with the License.\n" + " You may obtain a copy of the License at\n" + "\n"
        + "      http://www.apache.org/licenses/LICENSE-2.0\n" + "\n" + " Unless required by applicable law or agreed to in writing, software\n"
        + " distributed under the License is distributed on an \"AS IS\" BASIS,\n"
        + " WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n"
        + " See the License for the specific language governing permissions and\n" + " limitations under the License.";

    private Helper()
    {
    }

    public static File getBuildDirectory()
    {
        return _targetDir;
    }

    public static Reader getResourceReader(String path) throws Exception
    {
        return new InputStreamReader(getResourceURL(path).openStream());
    }

    public static InputStream getResourceStream(String path) throws Exception
    {
        return getResourceURL(path).openStream();
    }

    public static URL getResourceURL(String path) throws Exception
    {
        return Helper.class.getClassLoader().getResource(path);
    }

    public static File getSourceFile(String path) throws Exception
    {
        return new File(_sourceDir, path).getCanonicalFile();
    }

    public static void addDwarf(OptionMap opts, DwarfData dwarf)
    {
        addDwarf(opts, dwarf, true);
    }

    public static Profile.Section addDwarf(Profile prof, DwarfData dwarf)
    {
        Profile.Section s = prof.add(dwarf.name);

        inject(s, dwarf, "");
        if (dwarf.name.equals(Dwarfs.PROP_DOPEY))
        {
            s.put(Dwarf.PROP_WEIGHT, DwarfsData.INI_DOPEY_WEIGHT, 0);
            s.put(Dwarf.PROP_HEIGHT, DwarfsData.INI_DOPEY_HEIGHT, 0);
        }
        else if (dwarf.name.equals(Dwarfs.PROP_GRUMPY))
        {
            s.put(Dwarf.PROP_HEIGHT, DwarfsData.INI_GRUMPY_HEIGHT, 0);
        }
        else if (dwarf.name.equals(Dwarfs.PROP_SLEEPY))
        {
            s.put(Dwarf.PROP_HEIGHT, DwarfsData.INI_SLEEPY_HEIGHT, 0);
        }
        else if (dwarf.name.equals(Dwarfs.PROP_SNEEZY))
        {
            s.put(Dwarf.PROP_HOME_PAGE, DwarfsData.INI_SNEEZY_HOME_PAGE, 0);
        }

        return s;
    }

    public static Ini.Section addDwarf(Ini ini, DwarfData dwarf)
    {
        Ini.Section s = addDwarf((Profile) ini, dwarf);

        ini.putComment(dwarf.name, " " + dwarf.name);

        return s;
    }

    public static void addDwarf(OptionMap opts, DwarfData dwarf, boolean addNamePrefix)
    {
        String prefix = addNamePrefix ? (dwarf.name + '.') : "";

        opts.putComment(prefix + Dwarf.PROP_WEIGHT, " " + dwarf.name);
        inject(opts, dwarf, prefix);
        if (dwarf.name.equals(Dwarfs.PROP_DOPEY))
        {
            opts.put(prefix + Dwarf.PROP_WEIGHT, DwarfsData.OPT_DOPEY_WEIGHT, 0);
            opts.put(prefix + Dwarf.PROP_HEIGHT, DwarfsData.OPT_DOPEY_HEIGHT, 0);
        }
        else if (dwarf.name.equals(Dwarfs.PROP_GRUMPY))
        {
            opts.put(prefix + Dwarf.PROP_HEIGHT, DwarfsData.OPT_GRUMPY_HEIGHT, 0);
        }
        else if (dwarf.name.equals(Dwarfs.PROP_SLEEPY))
        {
            opts.put(prefix + Dwarf.PROP_HEIGHT, DwarfsData.OPT_SLEEPY_HEIGHT, 0);
        }
        else if (dwarf.name.equals(Dwarfs.PROP_SNEEZY))
        {
            opts.put(prefix + Dwarf.PROP_HOME_PAGE, DwarfsData.OPT_SNEEZY_HOME_PAGE, 0);
        }
    }

    public static void addDwarfs(Profile prof)
    {
        addDwarf(prof, DwarfsData.bashful);
        addDwarf(prof, DwarfsData.doc);
        addDwarf(prof, DwarfsData.dopey);
        addDwarf(prof, DwarfsData.grumpy);
        addDwarf(prof, DwarfsData.happy);
        addDwarf(prof, DwarfsData.sleepy);
        addDwarf(prof, DwarfsData.sneezy);
    }

    public static void assertEquals(Registry.Key exp, Registry.Key act)
    {
        Assert.assertNotNull(exp);
        Assert.assertEquals(exp.size(), act.size());
        for (String child : exp.childrenNames())
        {
            assertEquals(exp.getChild(child), act.getChild(child));
        }

        for (String name : exp.keySet())
        {
            Assert.assertEquals(exp.get(name), act.get(name));
        }
    }

    public static void assertEquals(Dwarfs expected, Dwarfs actual)
    {
        assertEquals(expected.getBashful(), actual.getBashful());
        assertEquals(expected.getDoc(), actual.getDoc());
        assertEquals(expected.getDopey(), actual.getDopey());
        assertEquals(expected.getGrumpy(), actual.getGrumpy());
        assertEquals(expected.getHappy(), actual.getHappy());
        assertEquals(expected.getSleepy(), actual.getSleepy());
        assertEquals(expected.getSneezy(), actual.getSneezy());
    }

    public static void assertEquals(Dwarf expected, Dwarf actual)
    {
        Assert.assertEquals(expected.getAge(), actual.getAge());
        Assert.assertEquals(expected.getHeight(), actual.getHeight(), DELTA);
        Assert.assertEquals(expected.getWeight(), actual.getWeight(), DELTA);
        Assert.assertEquals(expected.getHomePage().toString(), actual.getHomePage().toString());
        Assert.assertEquals(expected.getHomeDir().toString(), actual.getHomeDir().toString());
        Assert.assertEquals(expected.hasAge(), actual.hasAge());
        Assert.assertEquals(expected.hasHeight(), actual.hasHeight());
        Assert.assertEquals(expected.hasWeight(), actual.hasWeight());
        Assert.assertEquals(expected.hasHomePage(), actual.hasHomePage());
    }

    public static Ini loadDwarfsIni() throws Exception
    {
        return new Ini(Helper.class.getClassLoader().getResourceAsStream(DWARFS_INI));
    }

    public static Ini loadDwarfsIni(Config config) throws Exception
    {
        Ini ini = new Ini();

        ini.setConfig(config);
        ini.load(Helper.class.getClassLoader().getResourceAsStream(DWARFS_INI));

        return ini;
    }

    public static Options loadDwarfsOpt() throws Exception
    {
        return new Options(Helper.class.getClassLoader().getResourceAsStream(DWARFS_OPT));
    }

    public static Options loadDwarfsOpt(Config config) throws Exception
    {
        Options opt = new Options();

        opt.setConfig(config);
        opt.load(Helper.class.getClassLoader().getResourceAsStream(DWARFS_OPT));

        return opt;
    }

    public static Reg loadDwarfsReg() throws Exception
    {
        return new Reg(Helper.class.getClassLoader().getResourceAsStream(DWARFS_REG));
    }

    public static Ini loadTaleIni() throws Exception
    {
        return new Ini(Helper.class.getClassLoader().getResourceAsStream(TALE_INI));
    }

    public static Ini loadTaleIni(Config config) throws Exception
    {
        Ini ini = new Ini();

        ini.setConfig(config);
        ini.load(Helper.class.getClassLoader().getResourceAsStream(TALE_INI));

        return ini;
    }

    public static Ini newDwarfsIni()
    {
        Ini ini = new Ini();

        ini.setComment(HEADER_COMMENT);
        addDwarf(ini, DwarfsData.bashful);
        addDwarf(ini, DwarfsData.doc);
        addDwarf(ini, DwarfsData.dopey);
        addDwarf(ini, DwarfsData.grumpy);
        addDwarf(ini, DwarfsData.happy);
        addDwarf(ini, DwarfsData.sleepy);
        addDwarf(ini, DwarfsData.sneezy);

        return ini;
    }

    public static Options newDwarfsOpt()
    {
        Options opts = new Options();

        opts.setComment(HEADER_COMMENT);
        addDwarf(opts, DwarfsData.dopey, false);
        addDwarf(opts, DwarfsData.bashful);
        addDwarf(opts, DwarfsData.doc);
        addDwarf(opts, DwarfsData.dopey);
        addDwarf(opts, DwarfsData.grumpy);
        addDwarf(opts, DwarfsData.happy);
        addDwarf(opts, DwarfsData.sleepy);
        addDwarf(opts, DwarfsData.sneezy);

        return opts;
    }

    public static Ini newTaleIni()
    {
        Ini ini = new Ini();

        ini.setComment(HEADER_COMMENT);
        ini.add(TaleData.PROP_DWARFS);
        addDwarf(ini, TaleData.bashful);
        addDwarf(ini, TaleData.doc);
        addDwarf(ini, TaleData.dopey);
        addDwarf(ini, TaleData.grumpy);
        addDwarf(ini, TaleData.happy);
        addDwarf(ini, TaleData.sleepy);
        addDwarf(ini, TaleData.sneezy);

        return ini;
    }

    public static void resetConfig() throws Exception
    {
        for (String name : CONFIG_PROPERTIES)
        {
            System.clearProperty(Config.KEY_PREFIX + name);
        }

        for (String name : FACTORY_PROPERTIES)
        {
            System.clearProperty(name);
        }
    }

    private static void inject(OptionMap map, Dwarf dwarf, String prefix)
    {
        map.put(prefix + Dwarf.PROP_WEIGHT, String.valueOf(dwarf.getWeight()));
        map.put(prefix + Dwarf.PROP_HEIGHT, String.valueOf(dwarf.getHeight()));
        map.put(prefix + Dwarf.PROP_AGE, String.valueOf(dwarf.getAge()));
        map.put(prefix + Dwarf.PROP_HOME_PAGE, dwarf.getHomePage().toString());
        map.put(prefix + Dwarf.PROP_HOME_DIR, dwarf.getHomeDir());
        int[] numbers = dwarf.getFortuneNumber();

        if ((numbers != null) && (numbers.length > 0))
        {
            for (int i = 0; i < numbers.length; i++)
            {
                map.add(prefix + Dwarf.PROP_FORTUNE_NUMBER, String.valueOf(numbers[i]));
            }
        }
    }
}
