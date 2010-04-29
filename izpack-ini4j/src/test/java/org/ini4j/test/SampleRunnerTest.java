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

import org.ini4j.sample.BeanEventSample;
import org.ini4j.sample.BeanSample;
import org.ini4j.sample.DumpSample;
import org.ini4j.sample.Dwarf;
import org.ini4j.sample.DwarfBean;
import org.ini4j.sample.Dwarfs;
import org.ini4j.sample.DwarfsBean;
import org.ini4j.sample.FromSample;
import org.ini4j.sample.IniSample;
import org.ini4j.sample.ListenerSample;
import org.ini4j.sample.NoImportSample;
import org.ini4j.sample.PyReadSample;
import org.ini4j.sample.ReadPrimitiveSample;
import org.ini4j.sample.ReadStringSample;
import org.ini4j.sample.StreamSample;
import org.ini4j.sample.ToSample;

import org.ini4j.tutorial.BeanTutorial;
import org.ini4j.tutorial.IniTutorial;
import org.ini4j.tutorial.OneMinuteTutorial;
import org.ini4j.tutorial.OptTutorial;
import org.ini4j.tutorial.PrefsTutorial;
import org.ini4j.tutorial.RegTutorial;
import org.ini4j.tutorial.WindowsRegistryTutorial;

import org.junit.BeforeClass;
import org.junit.Test;

import org.junit.runner.RunWith;

import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;

import java.lang.reflect.Method;

import java.nio.charset.Charset;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RunWith(Parameterized.class)
public class SampleRunnerTest
{
    private static final String DOC_PATH = "generated-site/apt";
    private static final String JAVA_SUFFIX = ".java";
    private static final String PACKAGE_INFO = "package-info" + JAVA_SUFFIX;
    private static final String APT_SUFFIX = ".apt";
    private static final String APT_INDEX = "index" + APT_SUFFIX;
    private static final String CODE_BEGIN = "\n+----+\n";
    private static final String CODE_END = "+----+\n\n";
    private static File _documentDir;
    private final Class _clazz;
    private final File _sourceFile;

    public SampleRunnerTest(Class sampleClass) throws Exception
    {
        _clazz = sampleClass;
        _sourceFile = sourceFile(_clazz);
    }

    @BeforeClass public static void setUpClass() throws Exception
    {
        System.setProperty("java.util.prefs.PreferencesFactory", "org.ini4j.IniPreferencesFactory");
        _documentDir = new File(Helper.getBuildDirectory(), DOC_PATH);
        _documentDir.mkdirs();
        document(sourceFile(Dwarf.class), "//");
        document(sourceFile(DwarfBean.class), "//");
        document(sourceFile(Dwarfs.class), "//");
        document(sourceFile(DwarfsBean.class), "//");
        document(sourceFile(IniTutorial.class.getPackage()), "//");
        document(sourceFile(IniSample.class.getPackage()), "//");
    }

    @Parameters public static Collection data()
    {
        return Arrays.asList(
                new Object[][]
                {

                    // samples
                    { ReadStringSample.class },
                    { ReadPrimitiveSample.class },
                    { IniSample.class },
                    { StreamSample.class },
                    { DumpSample.class },
                    { NoImportSample.class },
                    { ListenerSample.class },
                    { BeanSample.class },
                    { BeanEventSample.class },
                    { FromSample.class },
                    { ToSample.class },
                    { PyReadSample.class },

                    // tutorials
                    { OneMinuteTutorial.class },
                    { IniTutorial.class },
                    { RegTutorial.class },
                    { WindowsRegistryTutorial.class },
                    { OptTutorial.class },
                    { BeanTutorial.class },
                    { PrefsTutorial.class },
                });
    }

    @Test public void test() throws Exception
    {
        System.out.println("Executing " + _clazz.getName());
        PrintStream saved = System.out;
        File tmp = File.createTempFile(getClass().getSimpleName(), ".out");
        PrintStream out = new PrintStream(new FileOutputStream(tmp));

        System.setOut(out);
        try
        {
            execute();
        }
        finally
        {
            System.setOut(saved);
            out.flush();
        }

        document(_sourceFile, "//");
        index(source2document(_sourceFile), source2index(_clazz));
        if (tmp.length() > 0)
        {
            append(tmp);
        }

        tmp.delete();
    }

    private static void document(File src, String comment) throws Exception
    {
        Pattern docPattern = Pattern.compile(String.format("^\\s*%s\\|(.*)$", comment));
        Pattern beginPattern = Pattern.compile(String.format("^\\s*%s\\{.*$", comment));
        Pattern endPattern = Pattern.compile(String.format("^\\s*%s\\}.*$", comment));
        LineNumberReader reader = new LineNumberReader(openReader(src));
        PrintWriter writer = new PrintWriter(new FileWriter(source2document(src)));
        boolean in = false;

        for (String line = reader.readLine(); line != null; line = reader.readLine())
        {
            if (in)
            {
                if (endPattern.matcher(line).matches())
                {
                    in = false;
                    writer.println(CODE_END);
                }
                else
                {
                    writer.println(line);
                }
            }
            else
            {
                if (beginPattern.matcher(line).matches())
                {
                    in = true;
                    writer.println(CODE_BEGIN);
                }
                else
                {
                    Matcher m = docPattern.matcher(line);

                    if (m.matches())
                    {
                        writer.println(m.group(1));
                    }
                }
            }
        }

        reader.close();
        writer.close();
    }

    private static void index(File src, File dst) throws Exception
    {
        LineNumberReader reader = new LineNumberReader(new FileReader(src));
        PrintWriter writer = new PrintWriter(new FileWriter(dst, true));
        String name = src.getName().replace(".apt", ".html");
        boolean h1 = false;
        boolean p = false;

        for (String line = reader.readLine(); line != null; line = reader.readLine())
        {
            if (line.length() == 0)
            {
                if (p)
                {
                    writer.println();

                    break;
                }
                else if (h1)
                {
                    p = true;
                }
            }
            else
            {
                if (Character.isSpaceChar(line.charAt(0)))
                {
                    if (p)
                    {
                        writer.println(line);
                    }
                }
                else
                {
                    if (!h1)
                    {
                        h1 = true;
                        writer.print(String.format(" *{{{%s}%s}}", name, line));
                        writer.println();
                        writer.println();
                    }
                }
            }
        }

        writer.close();
        reader.close();
    }

    private static Reader openReader(File src) throws Exception
    {
        InputStream stream = new FileInputStream(src);
        byte[] head = new byte[2];
        int n = stream.read(head);

        stream.close();
        Charset charset;

        if ((n == 2) && (head[0] == -1) && (head[1] == -2))
        {
            charset = Charset.forName("UnicodeLittle");
        }
        else
        {
            charset = Charset.forName("UTF-8");
        }

        return new InputStreamReader(new FileInputStream(src), charset);
    }

    private static File source2document(File sourceFile) throws Exception
    {
        String name = sourceFile.getName();
        File dir = new File(_documentDir, sourceFile.getParentFile().getName());

        dir.mkdir();

        return new File(dir, name.equals(PACKAGE_INFO) ? APT_INDEX : (name + APT_SUFFIX));
    }

    private static File source2index(Class clazz) throws Exception
    {
        return source2document(sourceFile(clazz.getPackage()));
    }

    private static File sourceFile(Class clazz) throws Exception
    {
        return Helper.getSourceFile(clazz.getName().replaceAll("\\.", "/") + JAVA_SUFFIX);
    }

    private static File sourceFile(Package pkg) throws Exception
    {
        return Helper.getSourceFile(pkg.getName().replaceAll("\\.", "/") + '/' + PACKAGE_INFO);
    }

    private void append(File stdout) throws Exception
    {
        PrintWriter writer = new PrintWriter(new FileWriter(source2document(_sourceFile), true));

        writer.println("\n Standard output:\n");
        writer.println(CODE_BEGIN);
        LineNumberReader reader = new LineNumberReader(new FileReader(stdout));

        for (String line = reader.readLine(); line != null; line = reader.readLine())
        {
            writer.println(line);
        }

        writer.println(CODE_END);
        reader.close();
        writer.close();
    }

    @SuppressWarnings("unchecked")
    private void execute() throws Exception
    {
        Method main = _clazz.getMethod("main", String[].class);
        String[] args;

        try
        {
            File argument = new File(_sourceFile.getParentFile(), (String) _clazz.getField("FILENAME").get(null));

            document(argument, "[#;!]");
            args = new String[] { argument.getCanonicalPath() };
        }
        catch (NoSuchFieldException x)
        {
            args = new String[] {};
        }

        main.invoke(null, (Object) args);
    }
}
