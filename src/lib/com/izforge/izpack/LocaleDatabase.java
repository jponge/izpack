/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
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

package com.izforge.izpack;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.TreeMap;
import java.util.Vector;

import net.n3.nanoxml.NonValidator;
import net.n3.nanoxml.StdXMLParser;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLElement;
import net.n3.nanoxml.XMLBuilderFactory;

/**
 * Represents a database of a locale.
 * 
 * @author Julien Ponge
 */
public class LocaleDatabase extends TreeMap
{

    static final long serialVersionUID = 4941525634108401848L;

    /**
     * The constructor.
     * 
     * @param in An InputStream to read the translation from.
     * @exception Exception Description of the Exception
     */
    public LocaleDatabase(InputStream in) throws Exception
    {
        // We call the superclass default constructor
        super();
        add(in);
    }

    /**
     * Adds the contents of the given stream to the data base. The stream have to contain key value
     * pairs as declared by the DTD langpack.dtd.
     * 
     * @param in an InputStream to read the translation from.
     * @throws Exception
     */
    public void add(InputStream in) throws Exception
    {
        // Initialises the parser
        StdXMLParser parser = new StdXMLParser();
        parser.setBuilder(XMLBuilderFactory.createXMLBuilder());
        parser.setReader(new StdXMLReader(in));
        parser.setValidator(new NonValidator());

        // We get the data
        XMLElement data = (XMLElement) parser.parse();

        // We check the data
        if (!"langpack".equalsIgnoreCase(data.getName()))
            throw new Exception("this is not an IzPack XML langpack file");

        // We fill the Hashtable
        Vector children = data.getChildren();
        int size = children.size();
        for (int i = 0; i < size; i++)
        {
            XMLElement e = (XMLElement) children.get(i);
            String text = e.getContent();
            if (text != null && !"".equals(text))
            {
                put(e.getAttribute("id"), text.trim());
            }
            else
            {
                put(e.getAttribute("id"), e.getAttribute("txt"));
            }
        }

    }

    /**
     * Convenience method to retrieve an element.
     * 
     * @param key The key of the element to retrieve.
     * @return The element value or the key if not found.
     */
    public String getString(String key)
    {
        String val = (String) get(key);
        // At a change of the return value at val == null the method
        // com.izforge.izpack.installer.IzPanel.getI18nStringForClass
        // should be also addapted.
        if (val == null)
            val = key;
        return val;
    }

    /**
     * Convenience method to retrieve an element and simultainiously insert variables into the
     * string. A placeholder have to be build with the substring {n} where n is the parameter
     * argument beginning with 0. The first argument is therefore {0}. If a parameter starts with a
     * dollar sign the value will be used as key into the LocalDatabase. The key can be written as
     * $MYKEY or ${MYKEY}. For all placeholder an argument should be exist and vis a versa.
     * 
     * @param key The key of the element to retrieve.
     * @param variables the variables to insert
     * @return The element value with the variables inserted or the key if not found.
     */
    public String getString(String key, String[] variables)
    {
        for (int i = 0; i < variables.length; ++i)
        {
            if (variables[i].startsWith("$"))
            { // Argument is also a key into the LocaleDatabase.
                String curArg = variables[i];
                if (curArg.startsWith("${"))
                    curArg = curArg.substring(2, curArg.length() - 1);
                else
                    curArg = curArg.substring(1);
                variables[i] = getString(curArg);
            }
        }

        return MessageFormat.format(getString(key), variables);
    }

}
