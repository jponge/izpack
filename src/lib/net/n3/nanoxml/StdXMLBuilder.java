/* StdXMLBuilder.java                                              NanoXML/Java
 *
 * $Revision$
 * $Date$
 * $Name$
 *
 * This file is part of NanoXML 2 for Java.
 * Copyright (C) 2001 Marc De Scheemaecker, All Rights Reserved.
 *
 * This software is provided 'as-is', without any express or implied warranty.
 * In no event will the authors be held liable for any damages arising from the
 * use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 *
 *  1. The origin of this software must not be misrepresented; you must not
 *     claim that you wrote the original software. If you use this software in
 *     a product, an acknowledgment in the product documentation would be
 *     appreciated but is not required.
 *
 *  2. Altered source versions must be plainly marked as such, and must not be
 *     misrepresented as being the original software.
 *
 *  3. This notice may not be removed or altered from any source distribution.
 */

package net.n3.nanoxml;

import java.io.Reader;
import java.util.Stack;

/**
 * StdXMLBuilder is a concrete implementation of IXMLBuilder which creates a tree of XMLElement from
 * an XML data source.
 *
 * @author Marc De Scheemaecker
 * @version $Name$, $Revision$
 * @see net.n3.nanoxml.XMLElement
 */
public class StdXMLBuilder implements IXMLBuilder
{

    /**
     * This stack contains the current element and its parents.
     */
    private Stack<XMLElement> stack;

    /**
     * The root element of the parsed XML tree.
     */
    private XMLElement root;

    /**
     * Creates the builder.
     */
    public StdXMLBuilder()
    {
        this.stack = null;
        this.root = null;
    }

    /**
     * Return the element that is currently being processed
     *
     * @return the element that is currently being processed
     */
    protected XMLElement getCurrentElement()
    {
        return stack.peek();
    }

    /**
     * Return the stack used for processing the elements.
     *
     * @return the stack used for processing the elements.
     */
    protected Stack<XMLElement> getStack()
    {
        return stack;
    }

    /**
     * Set the root element to a new element. This causes the internal stack
     * to be flushed and the supplied element to be pushed onto it
     *
     * @param element the new root element.
     */
    protected void setRootElement(XMLElement element)
    {
        stack.clear();
        stack.push(element);
        this.root = element;
    }

    /**
     * Cleans up the object when it's destroyed.
     */
    protected void finalize() throws Throwable
    {
        this.root = null;
        this.stack.clear();
        this.stack = null;
        super.finalize();
    }

    /**
     * This method is called before the parser starts processing its input.
     *
     * @param systemID the system ID of the XML data source
     * @param lineNr   the line on which the parsing starts
     */
    public void startBuilding(String systemID, int lineNr)
    {
        this.stack = new Stack<XMLElement>();
        this.root = null;
    }

    /**
     * This method is called when a processing instruction is encountered. PIs with target "xml" are
     * handled by the parser.
     *
     * @param target the PI target
     * @param reader to read the data from the PI
     */
    public void newProcessingInstruction(String target, Reader reader)
    {
        // nothing to do
    }

    /**
     * This method is called when a new XML element is encountered.
     *
     * @param name       the name of the element
     * @param nsPrefix   the prefix used to identify the namespace
     * @param nsSystemID the system ID associated with the namespace
     * @param systemID   the system ID of the XML data source
     * @param lineNr     the line in the source where the element starts
     * @see #endElement
     */
    public void startElement(String name, String nsPrefix, String nsSystemID, String systemID,
                             int lineNr)
    {
        XMLElement elt = new XMLElement(name, systemID, lineNr);

        if (this.stack.empty())
        {
            this.root = elt;
        }
        else
        {
            XMLElement top = this.stack.peek();
            top.addChild(elt);
        }

        this.stack.push(elt);
    }

    /**
     * This method is called when the attributes of an XML element have been processed.
     *
     * @param name       the name of the element
     * @param nsPrefix   the prefix used to identify the namespace
     * @param nsSystemID the system ID associated with the namespace
     * @see #startElement
     * @see #addAttribute
     */
    public void elementAttributesProcessed(String name, String nsPrefix, String nsSystemID)
    {
        // nothing to do
    }

    /**
     * This method is called when the end of an XML elemnt is encountered.
     *
     * @param name       the name of the element
     * @param nsPrefix   the prefix used to identify the namespace
     * @param nsSystemID the system ID associated with the namespace
     * @see #startElement
     */
    public void endElement(String name, String nsPrefix, String nsSystemID)
    {
        XMLElement elt = this.stack.pop();

        if (elt.getChildrenCount() == 1)
        {
            XMLElement child = elt.getChildAtIndex(0);

            if (child.getName() == null)
            {
                elt.setContent(child.getContent());
                elt.removeChildAtIndex(0);
            }
        }
    }

    /**
     * This method is called when a new attribute of an XML element is encountered.
     *
     * @param key        the key (name) of the attribute
     * @param nsPrefix   the prefix used to identify the namespace
     * @param nsSystemID the system ID associated with the namespace
     * @param value      the value of the attribute
     * @param type       the type of the attribute ("CDATA" if unknown)
     * @throws java.lang.Exception If an exception occurred while processing the event.
     */
    public void addAttribute(String key, String nsPrefix, String nsSystemID, String value,
                             String type) throws Exception
    {
        XMLElement top = this.stack.peek();

        if (top.hasAttribute(key))
        {
            throw new XMLParseException(top.getSystemID(),
                    top.getLineNr(), "Duplicate attribute: " + key);
        }

        top.setAttribute(key, value);
    }

    /**
     * This method is called when a PCDATA element is encountered. A Java reader is supplied from
     * which you can read the data. The reader will only read the data of the element. You don't
     * need to check for boundaries. If you don't read the full element, the rest of the data is
     * skipped. You also don't have to care about entities; they are resolved by the parser.
     *
     * @param reader   the Java reader from which you can retrieve the data
     * @param systemID the system ID of the XML data source
     * @param lineNr   the line in the source where the element starts
     * @throws java.lang.Exception If an exception occurred while processing the event.
     */
    public void addPCData(Reader reader, String systemID, int lineNr) throws Exception
    {
        int bufSize = 2048;
        int sizeRead = 0;
        StringBuffer str = new StringBuffer(bufSize);
        char[] buf = new char[bufSize];

        for (; ;)
        {
            if (sizeRead >= bufSize)
            {
                bufSize *= 2;
                str.ensureCapacity(bufSize);
            }

            int size = reader.read(buf);

            if (size < 0)
            {
                break;
            }

            str.append(buf, 0, size);
            sizeRead += size;
        }

        XMLElement elt = new XMLElement(null, systemID, lineNr);
        elt.setContent(str.toString());

        if (!this.stack.empty())
        {
            XMLElement top = this.stack.peek();
            top.addChild(elt);
        }
    }

    /**
     * Returns the result of the building process. This method is called just before the parse()
     * method of IXMLParser returns.
     *
     * @return the result of the building process.
     * @see net.n3.nanoxml.IXMLParser#parse
     */
    public Object getResult()
    {
        return this.root;
    }

}
