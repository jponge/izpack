/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2002 Marcus Stursberg
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

package com.izforge.izpack.installer;

import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * With this ResourceManager you are able to get resources from the jar file.
 * <p/>
 * All resources are loaded language dependent as it's done in java.util.ResourceBundle. To set a
 * language dependent resource just append '_' and the locale to the end of the Resourcename<br>
 * <br>
 * Example:
 * <li> InfoPanel.info - for default value</li>
 * <li> InfoPanel.info_deu - for german value</li>
 * <li> InfoPanel.info_eng - for english value</li>
 * <br>
 * <p/>
 * This class is almost a singleton. It is created once using <code>create</code> by the installer
 * and later, the instance is retrieved using <code>getInstance</code>.
 *
 * @author Marcus Stursberg
 */
public class ResourceManager
{

    /**
     * Contains the current language of the installer The locale is taken from
     * InstallData#installData#getAttribute("langpack") If there is no language set, the language is
     * english.
     */
    private String locale = "";

    /**
     * The base path where to find the resources: resourceBasePathDefaultConstant = "/res/"
     */
    public final String resourceBasePathDefaultConstant = "/res/";
    
    /** Internel used resourceBasePath = "/res/" */
    protected String resourceBasePath = "/res/";

    /**
     * Contains the given InstallData
     */
    private AutomatedInstallData installData;

    /**
     * The instance of this class.
     */
    private static ResourceManager instance = null;

    /**
     * Constructor. Protected because this is a singleton.
     *
     * @param data - the current installData
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
     * <p/>
     * This method should be called only once. If it is called a second time, the already existing
     * instance is returned. The resource manager should be called <b>after</b> the language has
     * been set in {@link AutomatedInstallData#localeISO3}
     *
     * @param data the installation information
     * @return the created instance
     */
    public static ResourceManager create(AutomatedInstallData data)
    {
        if (ResourceManager.instance == null)
        {
            ResourceManager.instance = new ResourceManager(data);
        }

        return ResourceManager.instance;
    }

    /**
     * Return the resource manager.
     *
     * @return the resource manager instance, null if no instance has been created
     */
    public static ResourceManager getInstance()
    {
        if (ResourceManager.instance == null)
        {
            ResourceManager.instance = new ResourceManager( new AutomatedInstallData() );
        }
        return ResourceManager.instance;
    }
    
    /**
     * If null was given the Default BasePath "/res/" is set
     * If otherwise the Basepath is set to the given String.
     * This is useful if someone needs direct access to Reosurces in the jar. 
     * @param aDefaultBasePath If null was given the DefaultBasepath is re/set "/res/"
     */
    public void setDefaultOrResourceBasePath( String aDefaultBasePath )
    {
       // For direct access of named resources the BasePath should be empty
       if( null != aDefaultBasePath )
         this.resourceBasePath = aDefaultBasePath;
       else
         this.resourceBasePath = resourceBasePathDefaultConstant;
    }
    

    /**
     * This method is used to get the language dependent path of the given resource. If there is a
     * resource for the current language the path of the language dependen resource is returnd. If
     * there's no resource for the current lanuage the default path is returned.
     *
     * @param resource Resource to load language dependen
     * @return the language dependent path of the given resource
     * @throws ResourceNotFoundException If the resource is not found
     */
    private String getLanguageResourceString(String resource) throws ResourceNotFoundException
    {
        InputStream in;
        String resourcePath = this.resourceBasePath + resource + "_" + this.locale;        
        
        in = ResourceManager.class.getResourceAsStream(resourcePath);
        if (in != null)
        {
            
            return resourcePath;
        }
        
        else
        {
            // if there's no language dependent resource found
            resourcePath = this.resourceBasePath + resource;
            in = ResourceManager.class.getResourceAsStream(resourcePath);
            if (in != null)
            {
                return resourcePath;
            }
            else
            {
                throw new ResourceNotFoundException( "Cannot find named Resource: '" + this.resourceBasePath + resource + "' AND '" + this.resourceBasePath + resource + "_" + this.locale + "'"  );
            }
        }
        
    }

    /**
     * Returns an InputStream contains the given Resource The Resource is loaded language dependen
     * by the informations from <code>this.locale</code> If there is no Resource for the current
     * language found, the default Resource is given.
     *
     * @param resource The resource to load
     * @return an InputStream contains the requested resource
     * @throws ResourceNotFoundException Description of the Exception
     * @throws ResourceNotFoundException thrown if there is no resource found
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
     * @param resource the resource to load
     * @return A languagedependen URL spezifies the requested resource
     * @throws ResourceNotFoundException Description of the Exception
     * @throws ResourceNotFoundException thrown if there is no resource found
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
     * @param resource - a text resource to load
     * @param encoding - the encoding, which should be used to read the resource
     * @return a String contains the text of the resource
     * @throws ResourceNotFoundException if the resource can not be found
     * @throws IOException               if the resource can not be loaded
     */
    // Maybe we can add a text parser for this method
    public String getTextResource(String resource, String encoding) throws ResourceNotFoundException, IOException
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
        {
            infoData.write(buffer, 0, bytesInBuffer);
        }

        if (encoding != null)
        {
            return infoData.toString(encoding);
        }
        else
        {
            return infoData.toString();
        }
    }

    /**
     * Returns a text resource from the jar file. The resource is loaded by
     * ResourceManager#getResource and then converted into text.
     *
     * @param resource - a text resource to load
     * @return a String contains the text of the resource
     * @throws ResourceNotFoundException if the resource can not be found
     * @throws IOException               if the resource can not be loaded
     */
    // Maybe we can add a text parser for this method
    public String getTextResource(String resource) throws ResourceNotFoundException, IOException
    {
        return this.getTextResource(resource, null);
    }

    /**
     * Returns a laguage dependent ImageIcon for the given Resource
     *
     * @param resource resrouce of the Icon
     * @return a ImageIcon loaded from the given Resource
     * @throws ResourceNotFoundException thrown when the resource can not be found
     * @throws IOException               if the resource can not be loaded
     */
    public ImageIcon getImageIconResource(String resource) throws ResourceNotFoundException,
            IOException
    {
        return new ImageIcon(this.getURL(resource));
    }

    /**
     * Sets the locale for the resourcefiles. The locale is taken from
     * InstallData#installData#getAttribute("langpack") If there is no language set, the default
     * language is english.
     *
     * @param locale of the resourcefile
     */
    public void setLocale(String locale)
    {
        this.locale = locale;
    }

    /**
     * Returns the locale for the resourcefiles. The locale is taken from
     * InstallData#installData#getAttribute("langpack") If there is no language set, the default
     * language is english.
     *
     * @return the current language
     */
    public String getLocale()
    {
        return this.locale;
    }
}
