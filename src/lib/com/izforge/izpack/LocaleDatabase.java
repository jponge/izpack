/*
 * IzPack Version 3.1.0 pre2 (build 2002.10.19)
 * Copyright (C) 2001,2002 Julien Ponge
 *
 * File :               LocaleDatabase.java
 * Description :        Represents a langpack database.
 * Author's email :     julien@izforge.com
 * Author's Website :   http://www.izforge.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
 
package com.izforge.izpack;

import java.io.*;
import java.util.*;

import net.n3.nanoxml.*;

public class LocaleDatabase extends TreeMap
{
    //.....................................................................
    
    // The constructor
    public LocaleDatabase(InputStream in) throws Exception
    {
        // We call the superclass default constructor
        super();
        
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
            put(e.getAttribute("id"), e.getAttribute("txt"));
        }
    }
            
    //.....................................................................    
    // The methods
    
    // Convenience method to retrieve an element
    public String getString(String key)
    {
        return (String) get(key);
    }
        
    //.....................................................................    
}
