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

import org.ini4j.Config;
import org.ini4j.Ini;
import org.ini4j.Ini4jCase;
import org.ini4j.InvalidFileFormatException;

import org.junit.Test;

import java.nio.charset.Charset;

public class UnicodeInputStreamReaderTest extends Ini4jCase
{
    @Test public void _testUTF32BE() throws Exception
    {
        test("UTF-32BE.ini", "UTF-32BE");
    }

    @Test public void _testUTF32BE_BOM() throws Exception
    {
        test("UTF-32BE-BOM.ini", null);
        test("UTF-32BE-BOM.ini", "UTF-8");
        test("UTF-32BE-BOM.ini", "UTF-16");
    }

    @Test public void _testUTF32BE_fail() throws Exception
    {
        try
        {
            test("UTF-32BE.ini", "ISO-8859-1");
            missing(IllegalStateException.class);
        }
        catch (IllegalStateException x)
        {
            //
        }
    }

    @Test public void _testUTF32LE() throws Exception
    {
        test("UTF-32LE.ini", "UTF-32LE");
    }

    @Test public void _testUTF32LE_BOM() throws Exception
    {
        test("UTF-32LE-BOM.ini", null);
        test("UTF-32LE-BOM.ini", "UTF-8");
        test("UTF-32LE-BOM.ini", "UTF-16");
    }

    @Test public void _testUTF32LE_fail() throws Exception
    {
        try
        {
            test("UTF-32LE.ini", "ISO-8859-1");
            missing(IllegalStateException.class);
        }
        catch (IllegalStateException x)
        {
            //
        }
    }

    @Test public void t_e_s_tUTF16BE_fail() throws Exception
    {
        try
        {
            test("UTF-16BE.ini", "ISO-8859-1");
            missing(IllegalStateException.class);
        }
        catch (IllegalStateException x)
        {
            //
        }
    }

    @Test public void t_e_s_tUTF16LE_fail() throws Exception
    {
        try
        {
            test("UTF-16LE.ini", "ISO-8859-1");
            missing(IllegalStateException.class);
        }
        catch (IllegalStateException x)
        {
            //
        }
    }

    @Test public void testUTF16BE() throws Exception
    {
        test("UTF-16BE.ini", "UTF-16BE");
    }

    @Test public void testUTF16BE_BOM() throws Exception
    {
        test("UTF-16BE-BOM.ini", null);
        test("UTF-16BE-BOM.ini", "UTF-8");
        test("UTF-16BE-BOM.ini", "UTF-16");
    }

    @Test public void testUTF16LE() throws Exception
    {
        test("UTF-16LE.ini", "UTF-16LE");
    }

    @Test public void testUTF16LE_BOM() throws Exception
    {
        test("UTF-16LE-BOM.ini", null);
        test("UTF-16LE-BOM.ini", "UTF-8");
        test("UTF-16LE-BOM.ini", "UTF-16");
    }

    @Test public void testUTF8() throws Exception
    {
        test("UTF-8.ini", null);
        test("UTF-8.ini", "UTF-8");
    }

    @Test public void testUTF8_BOM() throws Exception
    {
        test("UTF-8-BOM.ini", null);
        test("UTF-8-BOM.ini", "UTF-8");
        test("UTF-8-BOM.ini", "UTF-16");
    }

    @Test public void testUTF8_fail() throws Exception
    {
        try
        {
            test("UTF-8.ini", "UTF-16");
            missing(InvalidFileFormatException.class);
        }
        catch (InvalidFileFormatException x)
        {
            //
        }
    }

    private UnicodeInputStreamReader instantiate(String filename, String defaultEncoding)
    {
        Charset charset = (defaultEncoding == null) ? Charset.defaultCharset() : Charset.forName(defaultEncoding);

        return new UnicodeInputStreamReader(getClass().getResourceAsStream(filename), charset);
    }

    private void test(String filename, String defaultEncoding) throws Exception
    {
        Charset charset = (defaultEncoding == null) ? Config.DEFAULT_FILE_ENCODING : Charset.forName(defaultEncoding);
        UnicodeInputStreamReader reader = new UnicodeInputStreamReader(getClass().getResourceAsStream(filename), charset);
        Ini ini = new Ini();

        ini.setConfig(Config.getGlobal().clone());
        ini.getConfig().setFileEncoding(charset);
        ini.load(reader);
        Ini.Section sec = ini.get("section");

        if (sec == null)
        {
            throw new IllegalStateException("Missing section: section");
        }

        if (!"value".equals(sec.get("option")))
        {
            throw new IllegalStateException("Missing option: option");
        }
    }
}
