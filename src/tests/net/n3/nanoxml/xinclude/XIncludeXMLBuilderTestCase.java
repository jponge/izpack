/*
 * Copyright 2007 Volantis Systems Ltd., All Rights Reserved.
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
package net.n3.nanoxml.xinclude;

import junit.framework.TestCase;
import net.n3.nanoxml.*;

import java.util.Enumeration;

/**
 * Test the XInclude style functionality added to the XMLBuilder
 */
public class XIncludeXMLBuilderTestCase extends TestCase
{


    /**
     * This method takes the fileBase name and attempts to find two files
     * called &lt;fileBase&gt;-input.xml and &lt;fileBase&gt;-expected.xml
     *
     * @param fileBase the base of the test file names
     * @throws Exception
     */
    public void doTest(String fileBase) throws Exception
    {

        String baseURL = getClass().getResource(fileBase + "-input.xml").toExternalForm();
        // set up a new parser to parse the input xml (with includes)
        StdXMLParser inputParser = new StdXMLParser();
        inputParser.setBuilder(XMLBuilderFactory.createXMLBuilder());
        IXMLReader inputReader = new StdXMLReader(
                getClass().getResourceAsStream(fileBase + "-input.xml"));
        inputReader.setSystemID(baseURL);
        inputParser.setReader(inputReader);
        inputParser.setValidator(new NonValidator());

        // set up a new parser to parse the expected xml (without includes)
        StdXMLParser expectedParser = new StdXMLParser();
        expectedParser.setBuilder(XMLBuilderFactory.createXMLBuilder());
        IXMLReader expectedReader = new StdXMLReader(
                getClass().getResourceAsStream(fileBase + "-expect.xml"));
        expectedReader.setSystemID(baseURL);
        expectedParser.setReader(expectedReader);
        expectedParser.setValidator(new NonValidator());

        XMLElement inputElement = (XMLElement) inputParser.parse();
        XMLElement expectedElement = (XMLElement) expectedParser.parse();

        deepEqual(expectedElement, inputElement);

    }

    /**
     * This method is used to ensure that the contents of the specified file
     * (when having "-input.xml" appended) cause the parser to fail
     *
     * @param fileBase the base name of the input file.
     * @throws Exception
     */
    public void ensureFailure(String fileBase) throws Exception
    {
        try
        {
            String baseURL = getClass().getResource(fileBase + "-input.xml").toExternalForm();
            // set up a new parser to parse the input xml (with includes)
            StdXMLParser inputParser = new StdXMLParser();
            inputParser.setBuilder(XMLBuilderFactory.createXMLBuilder());
            IXMLReader inputReader = new StdXMLReader(
                    getClass().getResourceAsStream(fileBase + "-input.xml"));
            inputReader.setSystemID(baseURL);
            inputParser.setReader(inputReader);
            inputParser.setValidator(new NonValidator());

            inputParser.parse();
            fail("an exception should have been thrown");
        }
        catch (Throwable t)
        {
            // success
        }
    }

    /**
     * Perform a deep equality check on the two nodes.
     */
    public void deepEqual(XMLElement a, XMLElement b)
    {

        assertEquals("element names", a.getName(), b.getName());
        assertEquals("element attributes for " + a.getName(),
                a.getAttributes(), b.getAttributes());
        assertEquals("content for " + a.getName(),
                a.getContent(), b.getContent());
        assertEquals("equal number of children" + a.getName(),
                a.getChildrenCount(), b.getChildrenCount());

        Enumeration aChildren = a.enumerateChildren();
        Enumeration bChildren = b.enumerateChildren();
        while (aChildren.hasMoreElements())
        {
            XMLElement aChild = (XMLElement) aChildren.nextElement();
            XMLElement bChild = (XMLElement) bChildren.nextElement();
            deepEqual(aChild, bChild);
        }
    }

    /**
     * Test Empty document with include
     *
     * @throws Exception
     */
    public void testIncludeOnly() throws Exception
    {
        doTest("include-only");
    }

    /**
     * Test that a fragment included as the root node does not have the
     * "fragment" element removed
     *
     * @throws Exception
     */
    public void testIncludeFragmentOnly() throws Exception
    {
        doTest("include-fragment-only");
    }

    /**
     * Test to ensure that content is correctly included when the include
     * element is not the root element
     *
     * @throws Exception
     */
    public void testIncludeInElement() throws Exception
    {
        doTest("include-in-element");
    }

    /**
     * Test to ensure that content is correctly included when the include
     * element is not the root element
     *
     * @throws Exception
     */
    public void testIncludeFragmentInElement() throws Exception
    {
        doTest("include-fragment-in-element");
    }

    /**
     * Test text inclusion
     *
     * @throws Exception
     */
    public void testIncludeTextInElement() throws Exception
    {
        doTest("include-fragment-in-element");
    }

    /**
     * Ensure that the parse attribute accepts "text" and treats it like text
     *
     * @throws Exception
     */
    public void testParseAttributeText() throws Exception
    {
        doTest("include-xml-as-text");
    }

    /**
     * Ensure that the parse attribute accepts "xml" and treats like xml
     * (most other tests do not explicitly set the parse parameter and let it
     * default to "xml"
     *
     * @throws Exception
     */
    public void testParseAttributeXML() throws Exception
    {
        doTest("include-xml-as-xml");
    }

    /**
     * Make sure that a failure occurs for a parse valid that is not "xml"
     * or "text"
     *
     * @throws Exception
     */
    public void testParseInvalidAttribute() throws Exception
    {
        ensureFailure("invalid-parse-attrib");
    }

    /**
     * Ensure fallbacks work correctly
     *
     * @throws Exception
     */
    public void testFallback() throws Exception
    {
        doTest("fallback");
    }

    /**
     * Test that an empty fallback just removes the include and fallback
     * elements
     *
     * @throws Exception
     */
    public void testEmptyFallback() throws Exception
    {
        doTest("empty-fallback");
    }

    /**
     * Ensure that two includes in the same element both get included
     *
     * @throws Exception
     */
    public void testMultipleIncludes() throws Exception
    {
        doTest("multiple-include");
    }


}
