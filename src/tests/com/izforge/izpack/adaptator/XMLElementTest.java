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

import com.izforge.izpack.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.adaptator.impl.XMLParser;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.FileNotFoundException;
import java.util.Vector;

/**
 * Test on the XMLElement
 *
 * @author Anthonin Bonnefoy
 * @author David Duponchel *
 */
public class XMLElementTest extends TestCase
{

    String lineSeparator = System.getProperty("line.separator");
    private static final String filename = "partial.xml";

    private IXMLElement root;

    public void setUp() throws FileNotFoundException
    {
        /* méthode DOM */
        IXMLParser parser = new XMLParser();
        root = parser.parse(XMLElementTest.class.getResourceAsStream(filename));
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite(XMLElementTest.class);
    }

    public void testGetName() throws NoSuchMethodException
    {
        assertEquals(root.getName(), "installation");
        assertEquals(root.getChildAtIndex(0).getName(), "info");
    }

    public void testAddChild() throws NoSuchMethodException
    {
        IXMLElement element = new XMLElementImpl("child", root);
        root.addChild(element);
        element = root.getChildAtIndex(root.getChildrenCount() - 1);
        assertEquals(element.getName(), "child");
    }

    public void testRemoveChild() throws NoSuchMethodException
    {
        IXMLElement element = new XMLElementImpl("child", root);
        root.addChild(element);
        element = root.getChildAtIndex(root.getChildrenCount() - 1);
        root.removeChild(element);
        assertEquals(root.getChildrenNamed("child").size(), 0);
    }

    public void testHasChildrenIfTrue()
    {
        assertTrue(root.hasChildren());
    }

    public void testHasChildrenIfFalse()
    {
        IXMLElement element = new XMLElementImpl("test");
        assertFalse(element.hasChildren());
    }

    public void testGetChildrenCount()
    {
        IXMLElement element = root.getChildAtIndex(0);
        assertEquals(element.getChildrenCount(), 9);
    }

    public void testGetChildAtIndex()
    {
        IXMLElement element = root.getChildAtIndex(1);
        assertEquals(element.getName(), "guiprefs");
    }

    public void testGetFirstChildNamed()
    {
        IXMLElement element = root.getFirstChildNamed("locale");
        assertEquals(element.getName(), "locale");
    }

    public void testGetChildrenNamed()
    {
        IXMLElement element = root.getChildAtIndex(1);
        Vector<IXMLElement> list = element.getChildrenNamed("modifier");
        assertEquals(list.size(), 7);
    }
}