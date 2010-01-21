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

package com.izforge.izpack.api.adaptator;

import com.izforge.izpack.api.adaptator.impl.XMLParser;
import com.izforge.izpack.util.substitutor.SubstitutionType;
import com.izforge.izpack.util.substitutor.VariableSubstitutor;
import com.izforge.izpack.util.substitutor.VariableSubstitutorImpl;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import static junit.framework.Assert.assertEquals;

/**
 * Test on the XMLElement
 *
 * @author Anthonin Bonnefoy
 * @author David Duponchel
 */
public class XMLParserTest {

    private static final String filename = "shortcutSpec.xml";
    private static final String shortFilename = "short.xml";
    private static final String lnFilename = "linenumber/linenumber.xml";
    private static final String xlnFilename = "linenumber/xinclude-linenumber.xml";
    private static final String parseErrorFilename = "notvalid.xml";
    private static final String parseErrorXincludeFilename = "xinclude-notvalid.xml";


    @org.junit.Test
    public void testParseFile() throws Exception {
        InputStream input;
        IXMLElement spec;
        input = XMLParserTest.class.getResourceAsStream(shortFilename);

        IXMLParser parser = new XMLParser();
        spec = parser.parse(input);
        Assert.assertEquals("shortcuts", spec.getName());
    }

    @Test
    public void testParseString() throws Exception {
        InputStream input;
        IXMLElement spec;
        input = XMLParserTest.class.getResourceAsStream(filename);
        VariableSubstitutor substitutor = new VariableSubstitutorImpl(new Properties(System.getProperties()));
        String substitutedSpec = substitutor.substitute(input, SubstitutionType.TYPE_XML);

        IXMLParser parser = new XMLParser();
        spec = parser.parse(substitutedSpec);
        Assert.assertEquals("shortcuts", spec.getName());
    }

    private void checkEltLN(IXMLElement elt) {
        assertEquals(Integer.parseInt(elt.getAttribute("ln")), elt.getLineNr());
        for (IXMLElement child : elt.getChildren()) {
            checkEltLN(child);
        }
    }

    @org.junit.Test
    public void testLineNumber() throws SAXException, ParserConfigurationException, IOException, TransformerException {
        InputStream input = XMLParserTest.class.getResourceAsStream(lnFilename);
        IXMLElement elt;

        IXMLParser parser = new XMLParser();
        elt = parser.parse(input);

        checkEltLN(elt);
    }

    @Test
    public void testXincludeLineNumber() throws SAXException, ParserConfigurationException, IOException, TransformerException {
        URL url = XMLParserTest.class.getResource(xlnFilename);

        IXMLParser parser = new XMLParser();
        IXMLElement elt = parser.parse(url);

        checkEltLN(elt);
    }

    @Test(expected = XMLException.class)
    public void testXMLExceptionThrown() {
        InputStream input = XMLParserTest.class.getResourceAsStream(parseErrorFilename);
        IXMLParser parser = new XMLParser();
        parser.parse(input, parseErrorFilename);
    }

    @Test(expected = XMLException.class)
    public void testXMLExceptionThrownXInclude() {
        InputStream input = XMLParserTest.class.getResourceAsStream(parseErrorXincludeFilename);
        IXMLParser parser = new XMLParser();
        parser.parse(input, parseErrorXincludeFilename);
    }

}