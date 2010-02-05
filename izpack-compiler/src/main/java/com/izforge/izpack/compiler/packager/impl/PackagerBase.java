/*
 * $Id:$
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2007 Klaus Bartz
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

package com.izforge.izpack.compiler.packager.impl;

import com.izforge.izpack.api.data.DynamicVariable;
import com.izforge.izpack.api.data.Info;
import com.izforge.izpack.api.data.InstallerRequirement;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.rules.Condition;
import com.izforge.izpack.compiler.compressor.PackCompressor;
import com.izforge.izpack.compiler.container.CompilerContainer;
import com.izforge.izpack.compiler.listener.PackagerListener;
import com.izforge.izpack.compiler.packager.IPackager;
import com.izforge.izpack.data.CustomData;
import com.izforge.izpack.data.GUIPrefs;
import com.izforge.izpack.data.PackInfo;
import com.izforge.izpack.merge.JarMerge;
import com.izforge.izpack.merge.MergeManager;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;


/**
 * The packager base class. The packager interface <code>IPackager</code> is used by the compiler to put files into an installer, and
 * create the actual installer files. The packager implementation depends on different requirements (e.g. normal packager versus multi volume packager).
 * This class implements the common used method which can also be overload as needed.
 *
 * @author Klaus Bartz
 */
public abstract class PackagerBase implements IPackager {

    /**
     * Path to resources in jar
     */
    public static final String RESOURCES_PATH = "resources/";
    private Properties properties;

    protected CompilerContainer compilerContainer;

    public PackagerBase(Properties properties, CompilerContainer compilerContainer, PackagerListener listener, MergeManager mergeManager) {
        this.properties = properties;
        this.compilerContainer = compilerContainer;
        this.listener = listener;
        this.mergeManager = mergeManager;
    }

    /**
     * Basic installer info.
     */
    protected Info info = null;

    /**
     * Gui preferences of instatller.
     */
    protected GUIPrefs guiPrefs = null;

    /**
     * The ordered panels informations.
     */
    protected List<Panel> panelList = new ArrayList<Panel>();

    /**
     * The ordered packs informations (as PackInfo objects).
     */
    protected List<PackInfo> packsList = new ArrayList<PackInfo>();

    /**
     * The ordered langpack locale names.
     */
    protected List<String> langpackNameList = new ArrayList<String>();

    /**
     * The ordered custom actions informations.
     */
    protected List<CustomData> customDataList = new ArrayList<CustomData>();

    /**
     * The langpack URLs keyed by locale name (e.g. de_CH).
     */
    protected Map<String, URL> installerResourceURLMap = new HashMap<String, URL>();

    /**
     * the conditions
     */
    protected Map<String, Condition> rules = new HashMap<String, Condition>();

    /**
     * dynamic variables
     */
    protected Map<String, List<DynamicVariable>> dynamicvariables = new HashMap<String, List<DynamicVariable>>();

    /**
     * Jar file URLs who's contents will be copied into the installer.
     */
    protected Set<Object[]> includedJarURLs = new HashSet<Object[]>();

    /**
     * Each pack is created in a separte jar if webDirURL is non-null.
     */
    protected boolean packJarsSeparate = false;

    /**
     * The listeners.
     */
    protected PackagerListener listener;
    private MergeManager mergeManager;

    /**
     * The compression format to be used for pack compression
     */
    protected PackCompressor compressor;

    /**
     * Files which are always written into the container file
     */
    protected HashMap<FilterOutputStream, HashSet<String>> alreadyWrittenFiles = new HashMap<FilterOutputStream, HashSet<String>>();

    private List<InstallerRequirement> installerrequirements;

    /**
     * Dispatches a message to the listeners.
     *
     * @param job The job description.
     */
    protected void sendMsg(String job) {
        sendMsg(job, PackagerListener.MSG_INFO);
    }

    /**
     * Dispatches a message to the listeners at specified priority.
     *
     * @param job      The job description.
     * @param priority The message priority.
     */
    protected void sendMsg(String job, int priority) {
        if (listener != null) {
            listener.packagerMsg(job, priority);
        }
    }

    /**
     * Dispatches a start event to the listeners.
     */
    protected void sendStart() {
        if (listener != null) {
            listener.packagerStart();
        }
    }

    /**
     * Dispatches a stop event to the listeners.
     */
    protected void sendStop() {
        if (listener != null) {
            listener.packagerStop();
        }
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.compiler.packager.IPackager#addCustomJar(com.izforge.izpack.CustomData, java.net.URL)
     */

    public void addCustomJar(CustomData ca, URL url) {
        customDataList.add(ca); // serialized to keep order/variables correct
        if (url != null) {
            addJarContent(url); // each included once, no matter how many times added
        }
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.compiler.packager.IPackager#addJarContent(java.net.URL)
     */

    public void addJarContent(URL jarURL) {
        sendMsg("Adding content of jar: " + jarURL.getFile(), PackagerListener.MSG_VERBOSE);
        mergeManager.addResourceToMerge(new JarMerge(jarURL));
    }

    public void addLangPack(String iso3, URL xmlURL, URL flagURL) {
        sendMsg("Adding langpack: " + iso3, PackagerListener.MSG_VERBOSE);
        // put data & flag as entries in installer, and keep array of iso3's
        // names
        langpackNameList.add(iso3);
        addResource("flag." + iso3, flagURL);
        installerResourceURLMap.put("langpacks/" + iso3 + ".xml", xmlURL);
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.compiler.packager.IPackager#addNativeLibrary(java.lang.String, java.net.URL)
     */

    public void addNativeLibrary(String name, URL url) {
        sendMsg("Adding native library: " + name, PackagerListener.MSG_VERBOSE);
        installerResourceURLMap.put("native/" + name, url);
    }


    /* (non-Javadoc)
     * @see com.izforge.izpack.compiler.packager.IPackager#addNativeUninstallerLibrary(com.izforge.izpack.CustomData)
     */

    public void addNativeUninstallerLibrary(CustomData data) {
        customDataList.add(data); // serialized to keep order/variables
        // correct

    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.compiler.packager.IPackager#addPack(com.izforge.izpack.compiler.PackInfo)
     */

    public void addPack(PackInfo pack) {
        packsList.add(pack);
    }

    public void addPanel(Panel panel) {
        panelList.add(panel); // serialized to keep order/variables correct
        mergeManager.addPanelToMerge(panel.getClassName());
    }

    /* (non-Javadoc)
    * @see com.izforge.izpack.compiler.packager.IPackager#addPanelJar(com.izforge.izpack.Panel, java.net.URL)
    */

    /* (non-Javadoc)
     * @see com.izforge.izpack.compiler.packager.IPackager#addResource(java.lang.String, java.net.URL)
     */

    public void addResource(String resId, URL url) {
        sendMsg("Adding resource: " + resId, PackagerListener.MSG_VERBOSE);
        installerResourceURLMap.put(resId, url);
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.compiler.packager.IPackager#getCompressor()
     */

    public PackCompressor getCompressor() {
        return compressor;
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.compiler.packager.IPackager#getPacksList()
     */

    public List<PackInfo> getPacksList() {
        return packsList;
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.compiler.packager.IPackager#getVariables()
     */

    public Properties getVariables() {
        return properties;
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.compiler.packager.IPackager#setGUIPrefs(com.izforge.izpack.GUIPrefs)
     */

    public void setGUIPrefs(GUIPrefs prefs) {
        sendMsg("Setting the GUI preferences", PackagerListener.MSG_VERBOSE);
        guiPrefs = prefs;
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.compiler.packager.IPackager#setInfo(com.izforge.izpack.Info)
     */

    public void setInfo(Info info) {
        sendMsg("Setting the installer information", PackagerListener.MSG_VERBOSE);
        this.info = info;

        if (!getCompressor().useStandardCompression() && getCompressor().getDecoderMapperName() != null) {
            this.info.setPackDecoderClassName(getCompressor().getDecoderMapperName());
        }
    }

    /**
     * @return the rules
     */
    public Map<String, Condition> getRules() {
        return this.rules;
    }


    protected void writeInstaller() throws Exception {
        // write the primary jar. MUST be first so manifest is not overwritten
        // by
        // an included jar
        writeSkeletonInstaller();

        writeInstallerObject("info", info);
        writeInstallerObject("vars", properties);
        writeInstallerObject("GUIPrefs", guiPrefs);
        writeInstallerObject("panelsOrder", panelList);
        writeInstallerObject("customData", customDataList);
        writeInstallerObject("langpacks.info", langpackNameList);
        writeInstallerObject("rules", rules);
        writeInstallerObject("dynvariables", dynamicvariables);
        writeInstallerObject("installerrequirements", installerrequirements);

        writeInstallerResources();
        writeIncludedJars();

        // Pack File Data may be written to separate jars
        writePacks();
    }

    protected abstract void writeInstallerObject(String entryName, Object object) throws IOException;

    protected abstract void writeSkeletonInstaller() throws IOException;

    protected abstract void writeInstallerResources() throws IOException;

    protected abstract void writeIncludedJars() throws IOException;

    protected abstract void writePacks() throws Exception;


    /**
     * @return the dynamicvariables
     */
    public Map<String, List<DynamicVariable>> getDynamicVariables() {
        return this.dynamicvariables;
    }


    public void addInstallerRequirements(List<InstallerRequirement> conditions) {
        this.installerrequirements = conditions;
    }
}
