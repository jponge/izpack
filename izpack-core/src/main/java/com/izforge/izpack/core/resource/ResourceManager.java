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

package com.izforge.izpack.core.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import javax.swing.ImageIcon;

import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.exception.ResourceNotFoundException;

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
public class ResourceManager extends AbstractResources
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
     * Constructs a <tt>ResourceManager</tt>.
     *
     * @param properties the properties
     */
    public ResourceManager(Properties properties)
    {
        this(properties, ClassLoader.getSystemClassLoader());
    }

    /**
     * Constructs a <tt>ResourceManager</tt>.
     *
     * @param properties the properties
     * @param loader     the class loader to use to load resources
     */
    public ResourceManager(Properties properties, ClassLoader loader)
    {
        super(loader);
        this.locale = "eng";
        final String systemPropertyBundleName = properties.getProperty("resource.bundle.system.property");
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
     * @throws ResourceNotFoundException If the resource is not
     *                                   found
     */
    private String getLanguageResourceString(String resource)
    {
        String resourcePath = resource + "_" + this.locale;
        if (getResource(resourcePath) != null)
        {
            return resourcePath;
        }
        else if (getResource(resource) != null)
        {
            return resource;
        }

        throw new ResourceNotFoundException("Cannot find named Resource: '" + resource
                                                    + "' AND '" + resourcePath + "'");
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
     * @throws ResourceNotFoundException thrown if there is no resource found
     */
    public InputStream getInputStream(String resource) throws ResourceNotFoundException
    {
        resource = getLanguageResourceString(resource);
        return super.getInputStream(resource);
    }

    /**
     * Returns the URL to a resource.
     *
     * @param name the resource name
     * @return the URL to the resource
     * @throws ResourceNotFoundException if the resource cannot be found
     */
    @Override
    public URL getURL(String name)
    {
        return getLocalizedURL(name);
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
        return getInputStream(resourcepath);
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
        return getResource(getLanguageResourceString(resource));
    }

    /**
     * Returns a text resource from the jar file. The resource is loaded by
     * ResourceManager#getResource and then converted into text.
     *
     * @param resource - a text resource to load
     * @param encoding - the encoding, which should be used to read the resource
     * @return a String contains the text of the resource
     * @throws ResourceNotFoundException if the resource can not be
     *                                   found
     * @throws IOException               if the resource can not be loaded
     * @deprecated use {@link com.izforge.izpack.api.resource.Resources#getString(String, String, String)}
     */
    @Deprecated
    public String getTextResource(String resource, String encoding) throws IOException
    {
        return readString(resource, encoding);
    }

    /**
     * Returns a text resource from the jar file. The resource is loaded by
     * ResourceManager#getResource and then converted into text.
     *
     * @param resource - a text resource to load
     * @return a String contains the text of the resource
     * @throws ResourceNotFoundException if the resource can not be found
     * @throws IOException               if the resource can not be loaded
     * @deprecated use {@link com.izforge.izpack.api.resource.Resources#getString(String)}
     */
    @Deprecated
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
     * @deprecated use {@link #getImageIcon(String, String...)}
     */
    @Deprecated
    public ImageIcon getImageIconResource(String resource, String... fallback)
    {
        return getImageIcon(resource, fallback);
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

    /**
     * Resolves relative resource names.
     * <p/>
     * This implementation prefixes relative resource names with {@link #getResourceBasePath()}.
     *
     * @param name the resource name
     * @return the absolute resource name
     */
    @Override
    protected String resolveName(String name)
    {
        name = (name.charAt(0) == '/') ? name : getResourceBasePath() + name;
        return super.resolveName(name);
    }

    /**
     * Returns an input stream for reading the specified resource.
     *
     * @param name the resource name
     * @return the resource, or <tt>null</tt> if it is not found
     */
    protected InputStream getResourceAsStream(String name)
    {
        return getLoader().getResourceAsStream(name);
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
