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


package com.izforge.izpack.adaptator.impl;

import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.adaptator.IXMLParser;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * @author Anthonin Bonnefoy
 * @author David Duponchel
 */
public class XMLParser implements IXMLParser
{

    private LineNumberFilter filter;

    public XMLParser()
    {
        try
        {
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            saxParserFactory.setNamespaceAware(true);
            saxParserFactory.setXIncludeAware(true);
            XMLReader xmlReader = saxParserFactory.newSAXParser().getXMLReader();
            filter = new LineNumberFilter(xmlReader);

        } catch (ParserConfigurationException e)
        {
            e.printStackTrace();
        } catch (SAXException e)
        {
            e.printStackTrace();
        }
    }

    private IXMLElement searchFirstElement(DOMResult domResult)
    {
        for (Node child = domResult.getNode().getFirstChild(); child != null; child = child.getNextSibling())
        {
            if (child.getNodeType() == Node.ELEMENT_NODE)
            {
                return new XMLElementImpl(child);
            }
        }
        return null;
    }

    private DOMResult parseLineNr(InputStream stream)
    {

        DOMResult result = new DOMResult();

        try
        {
            SAXSource source = new SAXSource(new InputSource(stream));
            source.setXMLReader(filter);
            Source xsltSource = new StreamSource(IXMLParser.class.getResource("styleSheet.xsl").openStream());
            Transformer xformer = TransformerFactory.newInstance().newTransformer(xsltSource);
            xformer.transform(source, result);
            filter.applyLN(result);
        } catch (TransformerException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return result;
    }

    public IXMLElement parse(InputStream inputStream)
    {
        DOMResult result = parseLineNr(inputStream);
        return searchFirstElement(result);
    }

    public IXMLElement parse(String inputString)
    {
        return parse(new ByteArrayInputStream(inputString.getBytes()));
    }

    public IXMLElement parse(URL inputURL) throws IOException
    {
        return parse(inputURL.openStream());
    }
}
