/*
 * IzPack Version 3.0.0 (build 2002.08.13)
 * Copyright (C) 2001,2002 Julien Ponge
 *
 * File :               Pack.java
 * Description :        Contains informations about a pack.
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

public class Pack implements Serializable
{
    //.....................................................................
    
    // The fields (self-explicit names ...)
    public String name;
    public String description;
    public boolean required;
    
    // The constructor
    public Pack(String name, String description, boolean required)
    {
        this.name = name;
        this.description = description;
        this.required = required;
    }
    
    // To a String (usefull for JLists)
    public String toString()
    {
        return name + " (" + description + ")";
    }
    
    //.....................................................................
}
