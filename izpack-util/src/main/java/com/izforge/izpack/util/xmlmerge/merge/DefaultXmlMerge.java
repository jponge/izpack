/*
 * IzPack - Copyright 2001-2010 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2009 Laurent Bovet, Alex Mathey
 * Copyright 2010 Rene Krell
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

package com.izforge.izpack.util.xmlmerge.merge;

import java.io.*;

import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;

import com.izforge.izpack.util.xmlmerge.*;
import com.izforge.izpack.util.xmlmerge.action.OrderedMergeAction;
import com.izforge.izpack.util.xmlmerge.factory.StaticOperationFactory;
import com.izforge.izpack.util.xmlmerge.mapper.IdentityMapper;
import com.izforge.izpack.util.xmlmerge.matcher.TagMatcher;

/**
 * Default implementation of XmlMerge. Create all JDOM documents, then perform the merge into a new
 * JDOM document.
 *
 * @author Laurent Bovet (LBO)
 * @author Alex Mathey (AMA)
 */
public class DefaultXmlMerge implements XmlMerge
{

    /**
     * Root merge action.
     */
    private MergeAction m_rootMergeAction = new OrderedMergeAction();

    /**
     * Root matcher.
     */
    private Matcher m_rootMatcher = new TagMatcher();

    /**
     * Creates a new DefaultXmlMerge instance.
     */
    public DefaultXmlMerge()
    {
        m_rootMergeAction.setActionFactory(new StaticOperationFactory(new OrderedMergeAction()));
        m_rootMergeAction.setMapperFactory(new StaticOperationFactory(new IdentityMapper()));
        m_rootMergeAction.setMatcherFactory(new StaticOperationFactory(new TagMatcher()));
    }

    /**
     * {@inheritDoc}
     */
    public void setRootMapper(Mapper rootMapper)
    {
    }

    /**
     * {@inheritDoc}
     */
    public void setRootMergeAction(MergeAction rootMergeAction)
    {
        this.m_rootMergeAction = rootMergeAction;
    }

    /**
     * {@inheritDoc}
     */
    public String merge(String[] sources) throws AbstractXmlMergeException
    {

        InputStream[] inputStreams = new InputStream[sources.length];

        for (int i = 0; i < sources.length; i++)
        {
            inputStreams[i] = new ByteArrayInputStream(sources[i].getBytes());
        }

        InputStream merged = merge(inputStreams);

        ByteArrayOutputStream result = new ByteArrayOutputStream();

        try
        {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = merged.read(buffer)) != -1)
            {
                result.write(buffer, 0, len);
            }
        }
        catch (IOException ioe)
        {
            // should never happen
            throw new RuntimeException(ioe);
        }

        return result.toString();
    }

    /**
     * {@inheritDoc}
     */
    public org.w3c.dom.Document merge(org.w3c.dom.Document[] sources)
            throws AbstractXmlMergeException
    {
        DOMBuilder domb = new DOMBuilder();

        // to save all XML files as JDOM objects
        Document[] docs = new Document[sources.length];

        for (int i = 0; i < sources.length; i++)
        {
            // ask JDOM to parse the given inputStream
            System.err.println("sources[i]: "+sources[i]);
            docs[i] = domb.build(sources[i]);
        }

        Document result = doMerge(docs);

        DOMOutputter outputter = new DOMOutputter();

        try
        {
            return outputter.output(result);
        }
        catch (JDOMException e)
        {
            throw new DocumentException(result, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public InputStream merge(InputStream[] sources) throws AbstractXmlMergeException
    {
        SAXBuilder sxb = new SAXBuilder();

        // to save all XML files as JDOM objects
        Document[] docs = new Document[sources.length];

        for (int i = 0; i < sources.length; i++)
        {
            try
            {
                // ask JDOM to parse the given inputStream
                docs[i] = sxb.build(sources[i]);
            }
            catch (JDOMException e)
            {
                throw new ParseException(e);
            }
            catch (IOException ioe)
            {
                ioe.printStackTrace();
                throw new ParseException(ioe);
            }
        }

        Document result = doMerge(docs);

        Format prettyFormatter = Format.getPrettyFormat();
        // Use system line seperator to avoid problems
        // with carriage return under linux
        prettyFormatter.setLineSeparator(System.getProperty("line.separator"));
        XMLOutputter sortie = new XMLOutputter(prettyFormatter);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        try
        {
            sortie.output(result, buffer);
        }
        catch (IOException ex)
        {
            throw new DocumentException(result, ex);
        }

        return new ByteArrayInputStream(buffer.toByteArray());
    }

    /**
     * {@inheritDoc}
     */
    public void merge(File[] sources, File target) throws AbstractXmlMergeException
    {
        SAXBuilder sxb = new SAXBuilder();

        // to save all XML files as JDOM objects
        Document[] docs = new Document[sources.length];

        for (int i = 0; i < sources.length; i++)
        {
            try
            {
                // ask JDOM to parse the given inputStream
                docs[i] = sxb.build(sources[i]);
            }
            catch (JDOMException e)
            {
                throw new ParseException(e);
            }
            catch (IOException ioe)
            {
                ioe.printStackTrace();
                throw new ParseException(ioe);
            }
        }

        Document result = doMerge(docs);

        Format prettyFormatter = Format.getPrettyFormat();
        // Use system line seperator to avoid problems
        // with carriage return under linux
        prettyFormatter.setLineSeparator(System.getProperty("line.separator"));
        XMLOutputter sortie = new XMLOutputter(prettyFormatter);

        try
        {
            sortie.output(result, new FileOutputStream(sources[0]));
        }
        catch (IOException ex)
        {
            throw new DocumentException(result, ex);
        }
    }

    /**
     * Performs the actual merge.
     *
     * @param docs The documents to merge
     * @return The merged result document
     * @throws AbstractXmlMergeException If an error occurred during the merge
     */
    private Document doMerge(Document[] docs) throws AbstractXmlMergeException
    {
        Document temporary = docs[0];

        for (int i = 1; i < docs.length; i++)
        {

            if (!m_rootMatcher.matches(temporary.getRootElement(), docs[i].getRootElement())) { throw new IllegalArgumentException(
                    "Root elements do not match."); }

            Document output = new Document();
            if (docs[0].getDocType() != null)
            {
                output.setDocType((DocType) docs[0].getDocType().clone());
            }
            output.setRootElement(new Element("root"));

            m_rootMergeAction.perform(temporary.getRootElement(), docs[i].getRootElement(), output
                    .getRootElement());

            Element root = (Element) output.getRootElement().getChildren().get(0);
            root.detach();

            temporary.setRootElement(root);
        }

        return temporary;
    }

}
