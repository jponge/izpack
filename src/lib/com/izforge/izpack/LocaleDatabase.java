/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2004 Julien Ponge
 *
 *  File :               LocaleDatabase.java
 *  Description :        Represents a langpack database.
 *  Author's email :     julien@izforge.com
 *  Author's Website :   http://www.izforge.com
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.izforge.izpack;

import java.io.InputStream;
import java.util.TreeMap;
import java.util.Vector;

import net.n3.nanoxml.NonValidator;
import net.n3.nanoxml.StdXMLBuilder;
import net.n3.nanoxml.StdXMLParser;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLElement;

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
     * @param in
     *            An InputStream to read the translation from.
     * @exception Exception
     *                Description of the Exception
     */
    public LocaleDatabase(InputStream in) throws Exception
    {
        // We call the superclass default constructor
        super();
        add(in);
    }

    public void add(InputStream in) throws Exception
    {
        // Initialises the parser
        StdXMLParser parser = new StdXMLParser();
        parser.setBuilder(new StdXMLBuilder());
        parser.setReader(new StdXMLReader(in));
        parser.setValidator(new NonValidator());

        // We get the data
        XMLElement data = (XMLElement) parser.parse();

        // We check the data
        if (!data.getName().equalsIgnoreCase("langpack"))
            throw new Exception("this is not an IzPack XML langpack file");

        // We fill the Hashtable
        Vector children = data.getChildren();
        int size = children.size();
        for (int i = 0; i < size; i++)
        {
            XMLElement e = (XMLElement) children.get(i);
            String text = e.getContent();
            if (text != null && !text.equals(""))
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
     * @param key
     *            The key of the element to retrieve.
     * @return The element value or the key if not found.
     */
    public String getString(String key)
    {
        String val = (String) get(key);
        if (val == null) val = key;
        return val;
    }
}
