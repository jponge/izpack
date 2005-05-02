/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001,2002 Marcus Stursberg
 *
 *  File :               ResourceManager.java
 *  Description :        Class to get resources from the installer
 *  Author's email :     marcus@emsty.de
 *  Author's Website :   http://www.emasty.de
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
package com.izforge.izpack.installer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.swing.ImageIcon;

/**
 * With this ResourceManager you are able to get resources from the jar file.
 * 
 * All resources are loaded language dependent as it's done in
 * java.util.ResourceBundle. To set a language dependent resource just append
 * '_' and the locale to the end of the Resourcename<br>
 * <br>
 * Example:
 * <li> InfoPanel.info - for default value</li>
 * <li> InfoPanel.info_deu - for german value</li>
 * <li> InfoPanel.info_eng - for english value</li>
 * <br>
 * 
 * This class is almost a singleton. It is created once using
 * <code>create</code> by the installer and later, the instance is retrieved
 * using <code>getInstance</code>.
 * 
 * @author Marcus Stursberg
 */
public class ResourceManager
{

    /**
     * Contains the current language of the installer The locale is taken from
     * InstallData#installData#getAttribute("langpack") If there is no language
     * set, the language is english.
     */
    private String locale = "";

    /** The base path where to find the resources */
    protected final String resourceBasePath = "/res/";

    /** Contains the given InstallData */
    private AutomatedInstallData installData;

    /** The instance of this class. */
    private static ResourceManager instance = null;

    /**
     * Constructor. Protected because this is a singleton.
     * 
     * @param data -
     *            the current installData
     */
    protected ResourceManager(AutomatedInstallData data)
    {
        this.installData = data;
        if (data.localeISO3 != null)
        {
            this.locale = data.localeISO3;
        }
        else
        {
            // try to figure out ourself
            this.locale = installData.xmlData.getAttribute("langpack", "eng");
        }
    }

    /**
     * Create the resource manager.
     * 
     * This method should be called only once. If it is called a second time,
     * the already existing instance is returned. The resource manager should be
     * called <b>after</b> the language has been set in
     * {@link AutomatedInstallData#localeISO3}
     * 
     * @param data
     *            the installation information
     * @return the created instance
     */
    public static ResourceManager create(AutomatedInstallData data)
    {
        if (ResourceManager.instance == null) ResourceManager.instance = new ResourceManager(data);

        return ResourceManager.instance;
    }

    /**
     * Return the resource manager.
     * 
     * @return the resource manager instance, null if no instance has been
     *         created
     */
    public static ResourceManager getInstance()
    {
        return ResourceManager.instance;
    }

    /**
     * This method is used to get the language dependent path of the given
     * resource. If there is a resource for the current language the path of the
     * language dependen resource is returnd. If there's no resource for the
     * current lanuage the default path is returned.
     * 
     * @param resource
     *            Resource to load language dependen
     * @return the language dependent path of the given resource
     * @throws ResourceNotFoundException
     *             If the resource is not found
     */
    private String getLanguageResourceString(String resource) throws ResourceNotFoundException
    {
        InputStream in;
        String resourcePath = this.resourceBasePath + resource + "_" + this.locale;
        in = ResourceManager.class.getResourceAsStream(resourcePath);
        if (in != null)
            return resourcePath;
        else
        {
            // if there's no language dependent resource found
            resourcePath = this.resourceBasePath + resource;
            in = ResourceManager.class.getResourceAsStream(resourcePath);
            if (in != null)
                return resourcePath;
            else
                throw new ResourceNotFoundException("Can not find Resource " + resource
                        + " for language " + this.locale);
        }
    }

    /**
     * Returns an InputStream contains the given Resource The Resource is loaded
     * language dependen by the informations from <code>this.locale</code> If
     * there is no Resource for the current language found, the default Resource
     * is given.
     * 
     * @param resource
     *            The resource to load
     * @return an InputStream contains the requested resource
     * @exception ResourceNotFoundException
     *                Description of the Exception
     * @throws ResourceManager.ResourceNotFoundException
     *             thrown if there is no resource found
     */
    public InputStream getInputStream(String resource) throws ResourceNotFoundException
    {
        String resourcepath = this.getLanguageResourceString(resource);
        // System.out.println ("reading resource "+resourcepath);
        return ResourceManager.class.getResourceAsStream(resourcepath);
    }

    /**
     * Returns a URL refers to the given Resource
     * 
     * @param resource
     *            the resource to load
     * @return A languagedependen URL spezifies the requested resource
     * @exception ResourceNotFoundException
     *                Description of the Exception
     * @throws ResourceManager.ResourceNotFoundException
     *             thrown if there is no resource found
     */
    public URL getURL(String resource) throws ResourceNotFoundException
    {
        try
        {
            return this.getClass().getResource(
                    this.getLanguageResourceString(resource + "_" + installData.localeISO3));
        }
        catch (Exception ex)
        {
            return this.getClass().getResource(this.getLanguageResourceString(resource));
        }
    }

    /**
     * Returns a text resource from the jar file. The resource is loaded by
     * ResourceManager#getResource and then converted into text.
     * 
     * @param resource -
     *            a text resource to load
     * @return a String contains the text of the resource
     * @throws ResourceNotFoundException
     *             if the resource can not be found
     * @throws IOException
     *             if the resource can not be loaded
     */
    // Maybe we can add a text parser for this method
    public String getTextResource(String resource) throws ResourceNotFoundException, IOException
    {
        InputStream in = null;
        try
        {
            in = this.getInputStream(resource + "_" + this.installData.localeISO3);
        }
        catch (Exception ex)
        {
            in = this.getInputStream(resource);
        }

        ByteArrayOutputStream infoData = new ByteArrayOutputStream();
        byte[] buffer = new byte[5120];
        int bytesInBuffer;
        while ((bytesInBuffer = in.read(buffer)) != -1)
            infoData.write(buffer, 0, bytesInBuffer);

        return infoData.toString();
    }

    /**
     * Returns a laguage dependent ImageIcon for the given Resource
     * 
     * @param resource
     *            resrouce of the Icon
     * @return a ImageIcon loaded from the given Resource
     * @throws ResourceNotFoundException
     *             thrown when the resource can not be found
     * @throws IOException
     *             if the resource can not be loaded
     */
    public ImageIcon getImageIconResource(String resource) throws ResourceNotFoundException,
            IOException
    {
        return new ImageIcon(this.getURL(resource));
    }

    /**
     * Sets the locale for the resourcefiles. The locale is taken from
     * InstallData#installData#getAttribute("langpack") If there is no language
     * set, the default language is english.
     * 
     * @param locale
     *            of the resourcefile
     */
    public void setLocale(String locale)
    {
        this.locale = locale;
    }

    /**
     * Returns the locale for the resourcefiles. The locale is taken from
     * InstallData#installData#getAttribute("langpack") If there is no language
     * set, the default language is english.
     * 
     * @return the current language
     */
    public String getLocale()
    {
        return this.locale;
    }
}
