/*
 * IzPack Version 3.0.0 rc3 (build 2002.07.28)
 * Copyright (C) 2001,2002 Julien Ponge
 *
 * File :               Info.java
 * Description :        The information class for an installation.
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

public class Info implements Serializable
{
    //.....................................................................    
    // The fields
    
    // The application name and version
    private String appName = "", appVersion = "";
    
    // The application authors
    private ArrayList authors = new ArrayList();
    
    // The application URL
    private String appURL = "";
    
    //.....................................................................    
    
    // The constructor, deliberatly void
    public Info() {}
      
    //.....................................................................    
    // The methods
    
    // Set / get for the application name & version
    public void setAppName(String appName) { this.appName = appName; }
    public String getAppName() { return appName; }
    public void setAppVersion(String appVersion) { this.appVersion = appVersion; }
    public String getAppVersion() { return appVersion; }
    
    // Accessors for the authors
    public void addAuthor(Author author) { authors.add(author); }
    public ArrayList getAuthors() { return authors; }
    
    // Set / get for the URL
    public void setAppURL(String appURL) { this.appURL = appURL; }
    public String getAppURL() { return appURL; }
    
    //.....................................................................    
    // The Author class
    public static class Author implements Serializable
    {
        //.....................................................................    
        // The fields & their accessors
    
        String name, email;
        
        public String getName() { return name; }
        public String getEmail() { return email; }
        
        //.....................................................................    
        
        // The constructor
        public Author(String name, String email)
        {
            this.name = name;
            this.email = email;
        }
        
        // To String
        public String toString()
        {
            return name + " <" + email + ">";
        }
        
        //.....................................................................    
    }
    //.....................................................................    
}


