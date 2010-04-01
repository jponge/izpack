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

import org.ini4j.sample.Dwarfs;

import org.ini4j.test.DwarfsData;
import org.ini4j.test.Helper;

import static org.junit.Assert.*;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

public class RegTest
{
    private static final String DWARFS_PATH = Helper.DWARFS_REG_PATH + "\\dwarfs\\";

    @Test public void proba() throws Exception
    {
    }

    @Test public void testDwarfs() throws Exception
    {
        Reg reg = Helper.loadDwarfsReg();
        Dwarfs dwarfs = reg.as(Dwarfs.class, DWARFS_PATH);

        assertNotNull(dwarfs);
        Helper.assertEquals(DwarfsData.dwarfs, dwarfs);
    }

    @Test(expected = InvalidFileFormatException.class)
    public void testInvalidFileFormatException() throws Exception
    {
        new Reg(Helper.getResourceReader(Helper.DWARFS_INI));
    }

    @Test public void testIsWindwos()
    {
        assertEquals(isWindows(), Reg.isWindows());
    }

    @Test public void testLoad() throws Exception
    {
        Reg r1 = new Reg(new InputStreamReader(Helper.getResourceStream(Helper.DWARFS_REG), "UnicodeLittle"));
        Reg r2 = new Reg(Helper.getResourceStream(Helper.DWARFS_REG));
        Reg r3 = new Reg(Helper.getResourceURL(Helper.DWARFS_REG));
        File f = Helper.getSourceFile(Helper.DWARFS_REG);
        Reg r4 = new Reg(f);
        Reg r5 = new Reg();

        r5.setFile(f);
        r5.load();
        Helper.assertEquals(DwarfsData.dwarfs, r1.as(Dwarfs.class, DWARFS_PATH));
        Helper.assertEquals(DwarfsData.dwarfs, r2.as(Dwarfs.class, DWARFS_PATH));
        Helper.assertEquals(DwarfsData.dwarfs, r3.as(Dwarfs.class, DWARFS_PATH));
        Helper.assertEquals(DwarfsData.dwarfs, r4.as(Dwarfs.class, DWARFS_PATH));
        Helper.assertEquals(DwarfsData.dwarfs, r5.as(Dwarfs.class, DWARFS_PATH));
        assertSame(f, r4.getFile());
    }

    @Test(expected = FileNotFoundException.class)
    public void testLoadFileNotFoundException() throws Exception
    {
        Reg reg = new Reg();

        reg.load();
    }

    @Test public void testLoadSave() throws Exception
    {
        Reg reg = new Reg(Helper.getResourceURL(Helper.TEST_REG));

        checkLoadSave(Helper.TEST_REG, reg);
    }

    @Test(expected = InvalidFileFormatException.class)
    public void testMissingVersion() throws Exception
    {
        new Reg(new StringReader("\r\n\r\n[section]\r\n\"option\"=\"value\""));
    }

    @Test public void testNonWindwosExec() throws Exception
    {
        if (isSkip(isWindows(), "testNonWindwosExec"))
        {
            return;
        }

        Reg reg = new Reg();

        reg.exec(new String[] { "/bin/true" });
        try
        {
            reg.exec(new String[] { "/bin/ls", "no such file" });
            fail("IOException expected");
        }
        catch (IOException x)
        {
            assert true;
        }
    }

    @Test public void testReadException() throws Exception
    {
        if (!isWindows())
        {
            try
            {
                new Reg(Reg.Hive.HKEY_CURRENT_USER.toString());
                fail("missing UnsupportedOperationException");
            }
            catch (UnsupportedOperationException x)
            {
                assert true;
            }
        }
        else
        {
            try
            {
                new Reg("no such key");
                fail("missing IOException");
            }
            catch (IOException x)
            {
                assert true;
            }
        }
    }

    @Test public void testReadWrite() throws Exception
    {
        if (isSkip(!isWindows(), "testReadWrite"))
        {
            return;
        }

        Reg reg = Helper.loadDwarfsReg();

        reg.write();
        Reg dup = new Reg(Helper.DWARFS_REG_PATH);

        Helper.assertEquals(reg.get(Helper.DWARFS_REG_PATH), dup.get(Helper.DWARFS_REG_PATH));
        Dwarfs dwarfs = dup.as(Dwarfs.class, DWARFS_PATH);

        assertNotNull(dwarfs);
        Helper.assertEquals(DwarfsData.dwarfs, dwarfs);
    }

    @Test public void testStore() throws Exception
    {
        Reg reg = Helper.loadDwarfsReg();
        File tmp = File.createTempFile(Reg.TMP_PREFIX, Reg.DEFAULT_SUFFIX);

        tmp.deleteOnExit();
        reg.setFile(tmp);
        reg.store();
        reg = new Reg(tmp);
        Helper.assertEquals(DwarfsData.dwarfs, reg.as(Dwarfs.class, DWARFS_PATH));
        tmp.delete();
    }

    @Test(expected = FileNotFoundException.class)
    public void testStoreFileNotFoundException() throws Exception
    {
        new Reg().store();
    }

    @Test public void testUnsupportedOperatingSystem() throws Exception
    {
        if (isSkip(isWindows(), "testUnsupportedOperatingSystem"))
        {
            return;
        }

        Reg reg = new Reg();

        try
        {
            reg.read(Helper.DWARFS_REG_PATH);
            fail("UnsupportedOperationException expected");
        }
        catch (UnsupportedOperationException x)
        {
            assert true;
        }

        try
        {
            reg.write();
            fail("UnsupportedOperationException expected");
        }
        catch (UnsupportedOperationException x)
        {
            assert true;
        }
    }

    private boolean isSkip(boolean flag, String testName)
    {
        if (!flag)
        {
            System.out.println("Skipping " + getClass().getName() + '#' + testName);
        }

        return flag;
    }

    private boolean isWindows()
    {
        String family = System.getProperty("os.family");

        return (family != null) && family.equals("windows");
    }

    private void checkLoadSave(String path, Reg reg) throws Exception
    {
        File tmp = File.createTempFile(Reg.TMP_PREFIX, Reg.DEFAULT_SUFFIX);

        tmp.deleteOnExit();
        reg.store(new FileOutputStream(tmp));
        assertArrayEquals(read(Helper.getResourceStream(path)), read(new FileInputStream(tmp)));
    }

    private byte[] read(InputStream input) throws Exception
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buff = new byte[81912];
        int n;

        while ((n = input.read(buff)) >= 0)
        {
            out.write(buff, 0, n);
        }

        return out.toByteArray();
    }
}
