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
import java.text.DecimalFormat;

public class Pack implements Serializable
{
    //.....................................................................
    
    // The fields (self-explicit names ...)
    public String name;
    public String description;
    public boolean required;
    public long nbytes;
    
    // The constructor
    public Pack(String name, String description, boolean required)
    {
        this.name = name;
        this.description = description;
        this.required = required;
        nbytes = 0;
    }
    
    // To a String (usefull for JLists)
    public String toString()
    {
        return name + " (" + description + ")";
    }
    
    //values used for converting byte units
     private static final double KILOBYTES = 1024.0;
     private static final double MEGABYTES = 1024.0 * 1024.0;
     private static final double GIGABYTES = 1024.0 * 1024.0 * 1024.0;
     private static final DecimalFormat formatter = new DecimalFormat("#,###.##"); 
     
     //convert bytes into appropiate mesaurements
     public static String toByteUnitsString(int bytes) 
     {
         if (bytes < KILOBYTES)
         {    
             return String.valueOf(bytes) + " bytes";  
         } else if (bytes < (MEGABYTES)) 
         {  
             double value = bytes / KILOBYTES;  
             return formatter.format(value) + " KB";  
         } else if (bytes < (GIGABYTES)) 
         {
             double value = bytes / MEGABYTES; 
             return formatter.format(value) + " MB";   
         } else 
         { 
            double value = bytes / GIGABYTES;   
            return formatter.format(value) + " GB";   
         }
     }

    
    //.....................................................................
}
