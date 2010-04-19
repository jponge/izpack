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

package com.izforge.izpack.api.data;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.IXMLParser;
import com.izforge.izpack.api.adaptator.impl.XMLParser;
import com.izforge.izpack.api.exception.IzPackException;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

/**
 * Represents a database of a locale.
 *
 * @author Julien Ponge
 * @author J. Chris Folsom <jchrisfolsom@gmail.com>
 */
public class LocaleDatabase extends TreeMap<String, String>
{

    /*
     * Static cache of locale databases mapped by their iso name.
     */
    private static Map<String, LocaleDatabase> cachedLocales = new HashMap<String, LocaleDatabase>();

    /**
     * The directory where language packs are kept inside the installer jar file.
     */
    public static final String LOCALE_DATABASE_DIRECTORY = "/langpacks/";

    /**
     * The suffix for language pack definitions (.xml).
     */
    public static final String LOCALE_DATABASE_DEF_SUFFIX = ".xml";

    /*
     * static character for replacing quotes
     */
    private static final char TEMP_QUOTING_CHARACTER = '\uffff';

    static final long serialVersionUID = 4941525634108401848L;

    /**
     * The constructor.
     *
     * @param in An InputStream to read the translation from.
     * @throws Exception Description of the Exception
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
    public void add(InputStream in)
    {
        // Initialises the parser
        IXMLParser parser = new XMLParser();
        // We get the data
        IXMLElement data = parser.parse(in);

        // We check the data
        if (!"langpack".equalsIgnoreCase(data.getName()))
        {
            throw new IzPackException(
                    "this is not an IzPack XML langpack file");
        }

        // We fill the Hashtable
        for (IXMLElement child : data.getChildren())
        {
            String text = child.getContent();
            if (text != null && !"".equals(text))
            {
                put(child.getAttribute("id"), text.trim());
            }
            else
            {
                put(child.getAttribute("id"), child.getAttribute("txt"));
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
        String val = get(key);
        // At a change of the return value at val == null the method
        // com.izforge.izpack.installer.IzPanel.getI18nStringForClass
        // should be also addapted.
        if (val == null)
        {
            val = key;
        }
        return val;
    }

    /**
     * Convenience method to retrieve an element and simultaneously insert variables into the
     * string. A place holder has to be build with the substring {n} where n is the parameter
     * argument beginning with 0. The first argument is therefore {0}. If a parameter starts with a
     * dollar sign the value will be used as key into the LocalDatabase. The key can be written as
     * $MYKEY or ${MYKEY}. For all place holders an argument should be exist and vis a versa.
     *
     * @param key       The key of the element to retrieve.
     * @param variables the variables to insert
     * @return The element value with the variables inserted or the key if not found.
     */
    public String getString(String key, String[] variables)
    {
        for (int i = 0; i < variables.length; ++i)
        {
            if (variables[i] == null)
            {
                // The argument array with index is NULL! Replace it with N/A
                variables[i] = "N/A";
            }
            else if (variables[i].startsWith("$"))
            { // Argument is also a key into the LocaleDatabase.
                String curArg = variables[i];
                if (curArg.startsWith("${"))
                {
                    curArg = curArg.substring(2, curArg.length() - 1);
                }
                else
                {
                    curArg = curArg.substring(1);
                }
                variables[i] = getString(curArg);
            }
        }

        String message = getString(key);

        // replace all ' characters because MessageFormat.format()
        // don't substitute quoted place holders '{0}'
        message = message.replace('\'', TEMP_QUOTING_CHARACTER);

        message = MessageFormat.format(message, variables);

        // replace all ' characters back
        return message.replace(TEMP_QUOTING_CHARACTER, '\'');
    }

}
