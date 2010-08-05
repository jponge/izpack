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

package com.izforge.izpack.api.data;

import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.exception.ResourceNotFoundException;

import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * With this ResourceManager you are able to get resources from the jar file.
 * <p/>
 * All resources are loaded language dependent as it's done in java.util.ResourceBundle. To set a
 * language dependent resource just append '_' and the locale to the end of the Resourcename<br>
 * <br>
 * Example:
 * <li>InfoPanel.info - for default value</li>
 * <li>InfoPanel.info_deu - for german value</li>
 * <li>InfoPanel.info_eng - for english value</li> <br>
 * <p/>
 *
 * @author Marcus Stursberg
 */
public class ResourceManager
{

    private final static Logger LOGGER = Logger.getLogger(ResourceManager.class.getName());

    /**
     * Contains the current language of the installer The locale is taken from
     * InstallData#installData#getAttribute("langpack") If there is no language set, the language is
     * english.
     */
    private String locale = "";

    /**
     * The base path where to find the resources: resourceBasePathDefaultConstant = "/res/"
     */
    public final String resourceBasePathDefaultConstant = "/resources/";

    /**
     * Internally used resourceBasePath = "/resources/"
     */
    private String resourceBasePath = "/resources/";

    /**
     * Contains current bundle name. The bundleName is taken from
     * InstallData#installData#getVariable("resource.bundle.system.property") If there is no bundle
     * name set, it stays null.
     */
    private String bundleName = null;

    /**
     * The instance of this class.
     */
    private static ResourceManager instance = null;

    /**
     * Return the resource manager.
     *
     * @return the resource manager instance, null if no instance has been created
     */
    public static ResourceManager getInstance()
    {
        if (ResourceManager.instance == null)
        {
            ResourceManager.instance = new ResourceManager(new Properties());
        }
        return ResourceManager.instance;
    }

    /**
     * Constructor. Protected because this is a singleton.
     */
    public ResourceManager(Properties properties)
    {
        this.locale = "eng";
        final String systemPropertyBundleName = properties
                .getProperty("resource.bundle.system.property");
        if (systemPropertyBundleName != null)
        {
            setBundleName(System.getProperty(systemPropertyBundleName));
        }
    }

    /**
     * If null was given the Default BasePath "/res/" is set If otherwise the Basepath is set to the
     * given String. This is useful if someone needs direct access to Reosurces in the jar.
     *
     * @param aDefaultBasePath If null was given the DefaultBasepath is re/set "/res/"
     */
    public void setDefaultOrResourceBasePath(String aDefaultBasePath)
    {
        // For direct access of named resources the BasePath should be empty
        if (null != aDefaultBasePath)
        {
            this.setResourceBasePath(aDefaultBasePath);
        }
        else
        {
            this.setResourceBasePath(resourceBasePathDefaultConstant);
        }
    }

    /**
     * This method is used to get the language dependent path of the given resource. If there is a
     * resource for the current language the path of the language dependen resource is returnd. If
     * there's no resource for the current lanuage the default path is returned.
     *
     * @param resource Resource to load language dependen
     * @return the language dependent path of the given resource
     * @throws com.izforge.izpack.api.exception.ResourceNotFoundException
     *          If the resource is not
     *          found
     */
    private String getLanguageResourceString(String resource)
    {
        if (resource.charAt(0) == '/')
        {
            return getAbsoluteLanguageResourceString(resource);
        }
        else
        {
            return getAbsoluteLanguageResourceString(this.getResourceBasePath() + resource);
        }

        // String localeSuffix = "_" + this.locale;
        // String resourcePath = getBundlePath() + resource + localeSuffix;
        // if (resourceExists(resourcePath))
        // {
        // return resourcePath;
        // }
        //
        // // if there's no language dependent resource found
        // resourcePath = getBundlePath() + resource;
        // if (resourceExists(resourcePath))
        // {
        // return resourcePath;
        // }
        //
        // resourcePath = this.resourceBasePath + resource + localeSuffix;
        // if (resourceExists(resourcePath))
        // {
        // return resourcePath;
        // }
        //
        // // if there's no language dependent resource found
        // resourcePath = this.resourceBasePath + resource;
        // if (resourceExists(resourcePath))
        // {
        // return resourcePath;
        // }
        //
        // throw new ResourceNotFoundException("Cannot find named Resource: '" + getBundlePath()
        // + resource + "', '" + getBundlePath() + resource + localeSuffix + "'" + ", '"
        // + this.resourceBasePath + resource + "' AND '" + this.resourceBasePath + resource
        // + localeSuffix + "'");

    }

    private boolean resourceExists(String resourcePath)
    {
        boolean result = true;
        InputStream in = ResourceManager.class.getResourceAsStream(resourcePath);
        if (in == null)
        {
            result = false;
        }
        return result;
    }

    /**
     * Get stream on the given resource. First search if a localized resource exist then try to get
     * the given resource.
     *
     * @param resource
     * @return
     * @throws ResourceNotFoundException
     */
    private String getAbsoluteLanguageResourceString(String resource)
    {
        InputStream in;

        String resourcePath = resource + "_" + this.locale;
        in = ClassLoader.getSystemResourceAsStream(resourcePath);
        if (in != null)
        {
            return resourcePath;
        }
        else
        {
            // if there's no language dependent resource found
            in = ClassLoader.getSystemResourceAsStream(resource);
            if (in != null)
            {
                return resource;
            }
            else
            {
                if (resource.charAt(0) == '/')
                {
                    return getAbsoluteLanguageResourceString(resource
                            .substring(1));
                }
                throw new ResourceNotFoundException("Cannot find named Resource: '" + resource
                        + "' AND '" + resourcePath + "'");
            }
        }
    }

    public boolean isResourceExist(String resource)
    {
        return this.getLanguageResourceString(resource) != null;
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
        return ClassLoader.getSystemResourceAsStream(resourcepath);
    }

    /**
     * Get Input stream with a default value
     *
     * @param resource     Path of resource
     * @param defaultValue Default value if stream is not found
     * @return Stream found or default value
     */
    public InputStream getInputStream(String resource, InputStream defaultValue)
    {
        String resourcepath = this.getLanguageResourceString(resource);
        if (resourcepath == null)
        {
            return defaultValue;
        }
        return ClassLoader.getSystemResourceAsStream(resourcepath);
    }

    /**
     * Returns a URL refers to the given Resource
     *
     * @param resource the resource to load
     * @return A languagedependen URL spezifies the requested resource
     * @throws ResourceNotFoundException thrown if there is no resource found
     */
    public URL getLocalizedURL(String resource)
    {
        return ClassLoader.getSystemResource(this.getLanguageResourceString(resource));
    }


    private URL getURL(String resource)
    {
        if (resource.charAt(0) == '/')
        {
            return getClass().getResource(resource);
        }
        return getClass().getResource(getResourceBasePath() + resource);
    }

    /**
     * Returns a text resource from the jar file. The resource is loaded by
     * ResourceManager#getResource and then converted into text.
     *
     * @param resource - a text resource to load
     * @param encoding - the encoding, which should be used to read the resource
     * @return a String contains the text of the resource
     * @throws com.izforge.izpack.api.exception.ResourceNotFoundException
     *                     if the resource can not be
     *                     found
     * @throws IOException if the resource can not be loaded
     */
    // Maybe we can add a text parser for this method
    public String getTextResource(String resource, String encoding) throws IOException
    {
        InputStream in = getInputStream(resource);

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
    public String getTextResource(String resource) throws IOException
    {
        return this.getTextResource(resource, null);
    }

    /**
     * Returns a laguage dependent ImageIcon for the given Resource
     *
     * @param resource resrouce of the Icon
     * @param fallback fallback resources
     * @return a ImageIcon loaded from the given Resource
     * @throws ResourceNotFoundException thrown when the resource can not be found
     */
    public ImageIcon getImageIconResource(String resource, String... fallback)
    {
        URL location = this.getURL(resource);
        if (location != null)
        {
            return new ImageIcon(location);
        }
        for (String fallbackResource : fallback)
        {
            location = this.getURL(fallbackResource);
            if (location != null)
            {
                return new ImageIcon(location);
            }
        }
        LOGGER.info("Image icon resource not found in " + resource + " and in fallbacks " + Arrays.toString(fallback));
        return null;
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

    public String getResourceBasePath()
    {
        return resourceBasePath;
    }

    public void setResourceBasePath(String resourceBasePath)
    {
        this.resourceBasePath = resourceBasePath;
    }

    /**
     * Get langpack of the given locale
     *
     * @param localeISO3 langpack to get
     * @return InputStream on the xml
     */
    public InputStream getLangPack(String localeISO3)
    {
        return ClassLoader.getSystemResourceAsStream(getResourceBasePath() + "/langpacks/"
                + localeISO3 + ".xml");
    }

    /**
     * Get langpack of the locale present in installData
     *
     * @return InputStream on the xml
     */
    public InputStream getLangPack()
    {
        return this.getLangPack(this.locale);
    }

    /**
     * Returns an ArrayList of the available langpacks ISO3 codes.
     *
     * @return The available langpacks list.
     * @throws Exception Description of the Exception
     */
    public List<String> getAvailableLangPacks()
    {
        List<String> available;
        try
        {
            // We read from the langpacks file in the jar
            InputStream in = getInputStream("langpacks.info");
            ObjectInputStream objIn = new ObjectInputStream(in);
            available = (List<String>) objIn.readObject();
            objIn.close();
        }
        catch (Exception e)
        {
            throw new IzPackException("Could not read the langpack", e);
        }
        return available;
    }

    protected void setBundleName(String bundleName)
    {
        this.bundleName = bundleName;
    }

    protected String getBundleName()
    {
        return this.bundleName;
    }

    private String getBundlePath()
    {
        String basePath = this.resourceBasePath;
        if (this.bundleName != null)
        {
            basePath += this.bundleName + "/";
        }
        return basePath;
    }
}
