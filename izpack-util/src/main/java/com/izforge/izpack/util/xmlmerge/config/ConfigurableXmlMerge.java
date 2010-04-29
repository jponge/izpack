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

package com.izforge.izpack.util.xmlmerge.config;

import java.io.*;

import org.w3c.dom.Document;

import com.izforge.izpack.util.xmlmerge.*;
import com.izforge.izpack.util.xmlmerge.merge.DefaultXmlMerge;

/**
 * XmlMerge wrapper applying a configurer on the wrapped instance.
 *
 * @author Laurent Bovet (LBO)
 * @author Alex Mathey (AMA)
 */
public class ConfigurableXmlMerge implements XmlMerge
{

    /**
     * Wrapped XmlMerge instance.
     */
    XmlMerge m_wrappedXmlMerge;

    /**
     * Creates a default XmlMerge instance and configures it with the given configurer.
     *
     * @param configurer The configurer used to configure the XmlMerge instance
     * @throws ConfigurationException If an error occurred during configuration
     */
    public ConfigurableXmlMerge(Configurer configurer) throws ConfigurationException
    {
        this(new DefaultXmlMerge(), configurer);
    }

    /**
     * Applies a configurer on a wrapped XmlMerge instance.
     *
     * @param wrappedXmlMerge The wrapped XmlMerge instance to configure
     * @param configurer The configurer to apply
     * @throws ConfigurationException If an error occurred during configuration
     */
    public ConfigurableXmlMerge(XmlMerge wrappedXmlMerge, Configurer configurer)
            throws ConfigurationException
    {
        this.m_wrappedXmlMerge = wrappedXmlMerge;
        configurer.configure(wrappedXmlMerge);
    }

    /**
     * {@inheritDoc}
     */
    public InputStream merge(InputStream[] sources) throws AbstractXmlMergeException
    {
        return m_wrappedXmlMerge.merge(sources);
    }

    /**
     * {@inheritDoc}
     */
    public void merge(File[] sources, File target) throws AbstractXmlMergeException
    {
        m_wrappedXmlMerge.merge(sources, target);
    }

    /**
     * {@inheritDoc}
     */
    public Document merge(Document[] sources) throws AbstractXmlMergeException
    {
        return m_wrappedXmlMerge.merge(sources);
    }

    /**
     * {@inheritDoc}
     */
    public String merge(String[] sources) throws AbstractXmlMergeException
    {
        return m_wrappedXmlMerge.merge(sources);
    }

    /**
     * {@inheritDoc}
     */
    public void setRootMapper(Mapper rootMapper)
    {
        m_wrappedXmlMerge.setRootMapper(rootMapper);
    }

    /**
     * {@inheritDoc}
     */
    public void setRootMergeAction(MergeAction rootMergeAction)
    {
        m_wrappedXmlMerge.setRootMergeAction(rootMergeAction);
    }

}
