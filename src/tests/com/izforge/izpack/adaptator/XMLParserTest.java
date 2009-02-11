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
import com.izforge.izpack.installer.ResourceNotFoundException;
import com.izforge.izpack.util.VariableSubstitutor;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Test on the XMLElement
 *
 * @author Anthonin Bonnefoy
 * @author David Duponchel
 */
public class XMLParserTest extends TestCase
{

    String lineSeparator = System.getProperty("line.separator");
    private static final String filename = "shortcutSpec.xml";
    private static final String shortFilename = "short.xml";
    private static final String lnFilename = "linenumber/linenumber.xml";
    private static final String xlnFilename = "linenumber/xinclude-linenumber.xml";

    private IXMLElement root;

    public void setUp() throws FileNotFoundException
    {
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite(XMLParserTest.class);
    }


    public void testParseFile() throws NoSuchMethodException, ResourceNotFoundException, IOException
    {
        InputStream input = null;
        IXMLElement spec;
        input = XMLParserTest.class.getResourceAsStream(shortFilename);

        IXMLParser parser = new XMLParser();
        spec = parser.parse(input);
        assertEquals("shortcuts", spec.getName());
    }

    public void testParseString() throws NoSuchMethodException, ResourceNotFoundException, IOException
    {
        InputStream input = null;
        IXMLElement spec;
        input = XMLParserTest.class.getResourceAsStream(filename);
        VariableSubstitutor substitutor = new VariableSubstitutor(new Properties(System.getProperties()));
        String substitutedSpec = substitutor.substitute(input, "xml");

        IXMLParser parser = new XMLParser();
        spec = parser.parse(substitutedSpec);
        assertEquals("shortcuts", spec.getName());
    }

    private void checkEltLN(IXMLElement elt)
    {
        assertEquals(Integer.parseInt(elt.getAttribute("ln")), elt.getLineNr());
        for (IXMLElement child : elt.getChildren())
        {
            checkEltLN(child);
        }
    }

    public void testLineNumber() throws SAXException, ParserConfigurationException, IOException, TransformerException
    {
        InputStream input = XMLParserTest.class.getResourceAsStream(lnFilename);
        IXMLElement elt;

        IXMLParser parser = new XMLParser();
        elt = parser.parse(input);

        checkEltLN(elt);
    }

    public void testXincludeLineNumber() throws SAXException, ParserConfigurationException, IOException, TransformerException
    {
        InputStream input = XMLParserTest.class.getResourceAsStream(xlnFilename);
        IXMLElement elt;

        IXMLParser parser = new XMLParser();
        elt = parser.parse(input);

        checkEltLN(elt);
    }
}