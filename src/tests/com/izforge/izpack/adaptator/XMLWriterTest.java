/*
* IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
*
* http://izpack.org/
* http://izpack.codehaus.org/
*
* Copyright (c) 2008, 2009 Anthonin Bonnefoy
* Copyright (c) 2008, 2009 David Duponchel
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.izforge.izpack.adaptator;

import com.izforge.izpack.adaptator.impl.XMLParser;
import com.izforge.izpack.adaptator.impl.XMLWriter;
import com.izforge.izpack.adaptator.impl.XMLElementImpl;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Test the writer implementation
 *
 * @author Anthonin Bonnefoy
 */
public class XMLWriterTest extends TestCase
{

    String lineSeparator = System.getProperty("line.separator");
    private static final String filename = "partial.xml";
    private static final String output = "src/tests/com/izforge/izpack/adaptator/output.xml";
    private IXMLParser parser;
    private IXMLElement root;

    public void setUp() throws FileNotFoundException
    {
        parser = new XMLParser();
        root = parser.parse(XMLWriterTest.class.getResourceAsStream(filename));
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite(XMLWriterTest.class);
    }


    /**
     * Try to write a file with an outputStream
     *
     * @throws FileNotFoundException
     */
    public void testWriteFile() throws FileNotFoundException
    {
        IXMLWriter writer = new XMLWriter();
        File file = new File(output);
        FileOutputStream out = new FileOutputStream(file);
        writer.setOutput(out);
        writer.write(root);
        root = parser.parse(XMLWriterTest.class.getResourceAsStream(filename));
        IXMLElement element = parser.parse(new FileInputStream(file));
        assertEquals(root.getName(), element.getName());
    }


    /**
     * Try to write a file with an Url to a resource
     *
     * @throws FileNotFoundException
     */
    public void testWriteURL() throws FileNotFoundException
    {
        IXMLWriter writer = new XMLWriter();
        File file = new File(output);
        FileOutputStream out = new FileOutputStream(file);
        writer.setOutput(out);
        writer.write(root);
        root = parser.parse(XMLWriterTest.class.getResourceAsStream(filename));
        IXMLElement element = parser.parse(new FileInputStream(file));
        assertEquals(root.getName(), element.getName());
    }

    public void testFail()
    {
        // TODO : don't use XMLElementImpl !
        IXMLElement elt = new XMLElementImpl("root");
        IXMLWriter writer = new XMLWriter();
        writer.setOutput(""); // will take the current directory, which is not a file !
        try
        {
            writer.write(elt);
            fail("No exception were thrown will writing on an invalid file !");
        }
        catch (XMLException e)
        {
        }
    }
}