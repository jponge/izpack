/*
 * IzPack Version 3.1.0 pre2 (build 2002.10.19)
 * Copyright (C) 2001,2002 Julien Ponge
 *
 * File :               Frontend.java
 * Description :        The Frontend class.
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

package com.izforge.izpack.frontend;

import com.izforge.izpack.*;
import com.izforge.izpack.gui.*;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;

import net.n3.nanoxml.*;

import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;

import com.incors.plaf.kunststoff.*;

public class Frontend
{
    //.....................................................................
    
    // The fields
    private LocaleDatabase langpack;
    
    public static final int MAX_SPLASHES_PICS = 8;
    
    public static XMLElement config;
    public static String curLocale;
    public static String lastDir = ".";
    public static ArrayList bookmarks;
    public static Random random = new Random();
    public static FrontendSplashWindow splashWindow;
    public static String IZPACK_HOME = ".";
    
    //.....................................................................
    
    // The constructor
    public Frontend() throws Exception
    {
        super();
        
        // Sets the Kunststoff L&F as the right one
        KunststoffLookAndFeel klnf = new KunststoffLookAndFeel();
        UIManager.setLookAndFeel(klnf);
        klnf.setCurrentTheme(new IzPackKMetalTheme());
        
        // Shows the splash window
        splashWindow = new FrontendSplashWindow();
        
        // We load the configuration
        Frontend.splashWindow.update(1, "Loading the configuration ...");
        loadConfig();
        
        // We load the localisation
        Frontend.splashWindow.update(2, "Loading the localisation ...");
        loadLocalisation();
        String title = langpack.getString("frontend.title");

        // We launch the GUI
        FrontendFrame fef = new FrontendFrame(title, langpack);
    }
    
    // Loads the localisation
    private void loadLocalisation() throws Exception
    {
        curLocale = config.getFirstChildNamed("langpack").getContent();
        String lp = IZPACK_HOME + "bin" + File.separator + "langpacks" + File.separator +
                "frontend" + File.separator + curLocale + ".xml";
        FileInputStream inLangpack = new FileInputStream(lp);
        langpack = new LocaleDatabase(inLangpack);
    }
    
    // Loads the configuration
    private void loadConfig() throws Exception
    {
        // We determine wether there is a configuration file
        String homePath = System.getProperty("user.home");
        File configFile = new File(homePath + File.separator + ".izpack-fe" +
                                   File.separator + "config.xml");
        if (configFile.exists())
        {
            // We load the configuration from the file
            StdXMLParser parser = new StdXMLParser();
            parser.setBuilder(new StdXMLBuilder());
            parser.setReader(new StdXMLReader(new FileInputStream(configFile)));
            parser.setValidator(new NonValidator());
            config = (XMLElement) parser.parse();
            
            if (config.getAttribute("spec").equalsIgnoreCase("1"))
            {
                // Upgrades from 1 to 2
                config.setAttribute("spec", "2");
                XMLElement bookmarks = new XMLElement("bookmarks");
                config.addChild(bookmarks);
            }
        }
        else
        {
            // We make a default configuration
            config = new XMLElement("configuration");
            config.setAttribute("spec", "2");
            
            XMLElement locale = new XMLElement("langpack");
            locale.setContent("eng");
            config.addChild(locale);
            
            XMLElement lastdir = new XMLElement("lastdir");
            lastdir.setContent(".");
            config.addChild(lastdir);
            
            XMLElement bookmarks = new XMLElement("bookmarks");
            config.addChild(bookmarks);
        }
        
        lastDir = config.getFirstChildNamed("lastdir").getContent();
        bookmarks = new ArrayList(config.getFirstChildNamed("bookmarks").getChildren());
    }
    
    //.....................................................................
    
    // The main method (program entry-point)
    public static void main(String[] args)
    {
      // We get the IzPack home directory
      if (args.length == 2)
          if (args[0].equalsIgnoreCase("-HOME"))
        IZPACK_HOME = args[1];
      if (!IZPACK_HOME.endsWith(File.separator))
          IZPACK_HOME = IZPACK_HOME + File.separator;
    
        // Launches the GUI
        try
        {
          Frontend fe = new Frontend();
        }
        catch (Exception err)
        {
            System.err.println("-> Error :");
            System.err.println(err.toString());
            err.printStackTrace();
        }
    }
    
    // Creates a blank installation XML tree
    public static XMLElement createBlankInstallation()
    {
        // Creates the root element
        XMLElement root = new XMLElement("installation");
        root.setAttribute("version", "1.0");
        
        // Creates the info section
        XMLElement info = new XMLElement("info");
            XMLElement appname = new XMLElement("appname");
            info.addChild(appname);
            XMLElement appversion = new XMLElement("appversion");
            info.addChild(appversion);
            XMLElement authors = new XMLElement("authors");
            info.addChild(authors);
            XMLElement url = new XMLElement("url");
            info.addChild(url);
        root.addChild(info);
        
        // Creates the guiprefs section
        XMLElement guiprefs = new XMLElement("guiprefs");
        guiprefs.setAttribute("resizable", "no");
        guiprefs.setAttribute("width", "640");
        guiprefs.setAttribute("height", "480");
        root.addChild(guiprefs);
        
        // Creates the locale section
        XMLElement locale = new XMLElement("locale");
        root.addChild(locale);
        
        // Creates the resources section
        XMLElement resources = new XMLElement("resources");
        root.addChild(resources);
        
        // Creates the panels section
        XMLElement panels = new XMLElement("panels");
        root.addChild(panels);
        
        // Creates the packs section
        XMLElement packs = new XMLElement("packs");
        root.addChild(packs);
        
        // Returns
        return root;
    }
    
    // Saves the configuration
    public static void saveConfig()
    {
        try
        {
            // Inits
            config.getFirstChildNamed("lastdir").setContent(lastDir);
            config.getFirstChildNamed("bookmarks").getChildren().clear();
            config.getFirstChildNamed("bookmarks").getChildren().addAll(bookmarks);
            
            // We create the file
            String homePath = System.getProperty("user.home");
            String izPath = homePath + File.separator + ".izpack-fe";
            File path = new File(izPath);
            path.mkdirs();
            File configFile = new File(izPath + File.separator + "config.xml");
            
            // We write the file
            FileOutputStream out = new FileOutputStream(configFile);
            XMLWriter writer = new XMLWriter(out);
            writer.write(config);
            out.flush();
            out.close();
        }
        catch (Exception err)
        {
            err.printStackTrace();
        }
    }
    
    //.....................................................................
}
