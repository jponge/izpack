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
package net.n3.nanoxml;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;

/**
 * Extend the XMLBuilder to add XInclude functionality
 */
public class XIncludeXMLBuilder extends StdXMLBuilder
{
    /**
     * Namespace for XInclude  (NOTE that this is not used
     * at the moment). The specification can be found
     * <a href="http://www.w3.org/TR/xinclude/">here</a>.
     */
    public static final String INCLUDE_NS = "http://www.w3.org/2001/XInclude";
    /**
     * The name of the include element (this should be "include" using the
     * {@link #INCLUDE_NS} but namespaces are not supported
     */
    public static final String INCLUDE_ELEMENT = "xinclude";
    /**
     * The location of the included data
     */
    public static final String HREF_ATTRIB = "href";

    /**
     * The xpointer attribute. This must not be used when "parse='text'"
     */
    public static final String XPOINTER_ATTRIB = "xpointer";

    /**
     * The attribute to decribe the encoding of the text include (no effect when
     * parse='xml')
     */
    public static final String ENCODING_ATTRIB = "encoding";

    /**
     * The attribute describing the accept header that will be used with
     * http based includes.
     */
    public static final String ACCEPT_ENCODING = "accept";

    /**
     * The element for handling fallbacks. This should be called "fallback" and
     * be in the {@link #INCLUDE_NS} but namespaces are not supported
     */
    public static final String FALLBACK_ELEMENT = "xfallback";

    /**
     * Parse attribute. If missing this implies "xml" its other valid value
     * is "text"
     */
    public static final String PARSE_ATTRIB = "parse";

    /**
     * Namespace for the "fragment" element used to include xml documents with
     * no explicit root node.
     */
    public static final String FRAGMENT_NS = "http://izpack.org/izpack/fragment";

    /**
     * The fragment element is a root node element that can be
     * used to wrap xml fragments for inclusion. It is removed during the
     * include operation. This should be called "fragment" and be in the
     * {@link #FRAGMENT_NS} but namespaces are not supported.
     */
    public static final String FRAGMENT = "xfragment";

    // Javadoc inherited
    public void endElement(String name, String nsPrefix, String nsSystemID)
    {
        // get the current element before it gets popped from the stack
        XMLElement element = getCurrentElement();
        // let normal processing occur
        super.endElement(name, nsPrefix, nsSystemID);
        // now process the "include" element
        processXInclude(element);
    }

    /**
     * This method handles XInclude elements in the code
     *
     * @param element the node currently being procesed. In this case it should
     *                be the {@link #INCLUDE_ELEMENT}
     */
    private void processXInclude(final XMLElement element)
    {
        if (INCLUDE_ELEMENT.equals(element.getName()))
        {

            Vector<XMLElement> fallbackChildren = element.getChildrenNamed(FALLBACK_ELEMENT);
            if (element.getChildrenCount() != fallbackChildren.size() ||
                    fallbackChildren.size() > 1)
            {
                throw new RuntimeException(new XMLParseException(
                        element.getSystemID(),
                        element.getLineNr(),
                        INCLUDE_ELEMENT + " can optionally have a single " +
                                FRAGMENT + " as a child"));
            }
            boolean usingFallback = false;

            String href = element.getAttribute(HREF_ATTRIB, "");
            if (!href.equals(""))
            { // including an external file.

                IXMLReader reader = null;
                try
                {
                    reader = getReader(element);
                }
                catch (Exception e)
                { // yes really catch all exceptions
                    // ok failed to read from the location for some reason.
                    // see if we have a fallback
                    reader = handleFallback(element);
                    usingFallback = true;
                }
                String parse = element.getAttribute(PARSE_ATTRIB, "xml");
                // process as text if we are not using our fallback and the parse
                // type is "text"
                if ("text".equals(parse) && !usingFallback)
                {
                    includeText(element, reader);
                }
                else if ("xml".equals(parse))
                {
                    includeXML(element, reader);
                }
                else
                {
                    throw new RuntimeException(
                            new XMLParseException(
                                    element.getSystemID(),
                                    element.getLineNr(),
                                    PARSE_ATTRIB + " attribute of " + INCLUDE_ELEMENT +
                                            " must be \"xml\" or \"text\" but was " +
                                            parse));
                }
            }
            else
            { // including part of this file rather then an external one
                if (!element.hasAttribute(XPOINTER_ATTRIB))
                {
                    throw new RuntimeException(
                            new XMLParseException(
                                    element.getSystemID(),
                                    element.getLineNr(),
                                    XPOINTER_ATTRIB + "must be specified if href is " +
                                            "empty or missing"));
                }
            }
        }
    }

    /**
     * Handle the fallback if one exists. If one does not exist then throw
     * a runtime exception as this is a fatal error
     *
     * @param include the include element
     * @return a reader for the fallback
     */
    private IXMLReader handleFallback(XMLElement include)
    {
        Vector<XMLElement> fallbackChildren = include.getChildrenNamed(FALLBACK_ELEMENT);
        if (fallbackChildren.size() == 1)
        {
            // process fallback

            XMLElement fallback = fallbackChildren.get(0);
            // fallback element can only contain a CDATA so it will not have
            // its content in un-named children
            String content = fallback.getContent();
            if (content != null)
            {
                content = content.trim();
            }

            if ("".equals(content) || content == null)
            {
                // an empty fragment requires us to just remove the "include"
                // element. A nasty hack follows:
                // a "fragment" with no children will just be removed along with
                // the "include" element.
                content = "<?xml version=\"1.0\" encoding=\"iso-8859-1\" standalone=\"yes\" ?><" + FRAGMENT + "/>";
            }
            return StdXMLReader.stringReader(content);
        }
        else
        {
            throw new RuntimeException(new XMLParseException(
                    include.getSystemID(),
                    include.getLineNr(),
                    "could not load content"));
        }
    }

    /**
     * Include the xml contained in the specified reader. This content will be
     * parsed and attached to the parent of the <param>element</param> node
     *
     * @param element the include element
     * @param reader  the reader containing the xml to parse and include.
     */
    private void includeXML(final XMLElement element, IXMLReader reader)
    {

        try
        {
            Stack<XMLElement> stack = getStack();
            // set up a new parser to parse the include file.
            StdXMLParser parser = new StdXMLParser();
            parser.setBuilder(XMLBuilderFactory.createXMLBuilder());
            parser.setReader(reader);
            parser.setValidator(new NonValidator());

            XMLElement childroot = (XMLElement) parser.parse();
            // if the include element was the root element in the original
            // document then keep the element as-is (i.e.
            // don't remove the "fragment" element from the included content
            if (stack.isEmpty())
            {
                setRootElement(childroot);
            }
            else
            {
                XMLElement parent = stack.peek();
                // remove the include element from its parent
                parent.removeChild(element);

                // if there was a "fragment" included remove the fragment
                // element and attach its children in place of this include
                // element.
                if (FRAGMENT.equals(childroot.getName()))
                {
                    Vector grandchildren = childroot.getChildren();
                    Iterator it = grandchildren.iterator();
                    while (it.hasNext())
                    {
                        XMLElement grandchild = (XMLElement) it.next();
                        parent.addChild(grandchild);
                    }
                }
                else
                {
                    // if it was a complete document included then
                    // just add it in place of the include element
                    parent.addChild(childroot);
                }
            }
        }
        catch (XMLException e)
        {
            throw new RuntimeException(new XMLParseException(
                    element.getSystemID(), element.getLineNr(), e.getMessage()));
        }
    }


    /**
     * Include plain text. The reader contains the content in the appropriate
     * encoding as determined by the {@link #ENCODING_ATTRIB} if one was
     * present.
     *
     * @param element the include element
     * @param reader  the reader containing the include text
     */
    private void includeText(XMLElement element, IXMLReader reader)
    {

        if (element.getAttribute("xpointer") != null)
        {
            throw new RuntimeException(new XMLParseException(
                    "xpointer cannot be used with parse='text'"));
        }

        Stack<XMLElement> stack = getStack();
        if (stack.isEmpty())
        {
            throw new RuntimeException(new XMLParseException(
                    element.getSystemID(),
                    element.getLineNr(),
                    "cannot include text as the root node"));
        }

        // remove the include element from the parent
        XMLElement parent = stack.peek();
        parent.removeChild(element);
        StringBuffer buffer = new StringBuffer();
        try
        {
            while (!reader.atEOF())
            {
                buffer.append(reader.read());
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(new XMLParseException(
                    element.getSystemID(), element.getLineNr(), e.getMessage()));
        }

        if (parent.getChildrenCount() == 0)
        {
            // no children so just set the content as there cannot have been
            // any content there already
            parent.setContent(buffer.toString());
        }
        else
        {
            // nanoxml also claims to store #PCDATA in unnamed children
            // if there was a combination of #PCDATA and child elements.
            // This should put it in the correct place as we haven't finihshed
            // parsing the children of includes parent yet.
            XMLElement content = new XMLElement();
            content.setContent(buffer.toString());
            parent.addChild(content);
        }
    }

    /**
     * Return a reader for the specified {@link #INCLUDE_ELEMENT}. The caller
     * is responsible for closing the reader produced.
     *
     * @param element the include element to obtain a reader for
     * @return a reader for the include element
     * @throws XMLParseException if a problem occurs parsing the
     *                           {@link #INCLUDE_ELEMENT}
     * @throws IOException       if the href cannot be read
     */
    private IXMLReader getReader(XMLElement element) throws XMLParseException, IOException
    {
        String href = element.getAttribute(HREF_ATTRIB);
        // This is a bit nasty but is a simple way of handling files that are
        // not fully qualified urls
        URL url = null;
        try
        {
            // standard URL
            url = new URL(href);
        }
        catch (MalformedURLException e)
        {
            try
            {
                // absolute file without a protocol
                if (href.charAt(0) == '/')
                {
                    url = new URL("file://" + href);
                }
                else
                {
                    // relative file
                    url = new URL(new URL(element.getSystemID()), href);
                }
            }
            catch (MalformedURLException e1)
            {
                new XMLParseException(element.getSystemID(),
                        element.getLineNr(), "malformed url '" + href + "'");
            }
        }

        URLConnection connection = url.openConnection();
        // special handling for http and https
        if (connection instanceof HttpURLConnection &&
                element.hasAttribute(ENCODING_ATTRIB))
        {
            connection.setRequestProperty(
                    "accept", element.getAttribute(ENCODING_ATTRIB));
        }

        InputStream is = connection.getInputStream();

        InputStreamReader reader = null;
        // Only pay attention to the {@link #ENCODING_ATTRIB} if parse='text'  
        if (element.getAttribute(PARSE_ATTRIB, "xml").equals("text") &&
                element.hasAttribute(ENCODING_ATTRIB))
        {
            reader = new InputStreamReader(
                    is, element.getAttribute(ENCODING_ATTRIB, ""));
        }
        else
        {
            reader = new InputStreamReader(is);
        }

        IXMLReader ireader = new StdXMLReader(reader);
        ireader.setSystemID(url.toExternalForm());
        return ireader;
    }

    /**
     * used to record the system id for this document.
     *
     * @param systemID the system id of the document being built
     * @param lineNr   the line number
     */
    public void startBuilding(String systemID, int lineNr)
    {
        super.startBuilding(systemID, lineNr);
    }
}
