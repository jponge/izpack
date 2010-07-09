/*
 * IzPack - Copyright 2001-2010 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2010 Rene Krell
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

package com.izforge.izpack.event;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.DynamicVariable;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.handler.AbstractUIProgressHandler;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.data.DynamicVariableImpl;
import com.izforge.izpack.core.regex.RegularExpressionFilterImpl;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;
import com.izforge.izpack.core.variable.*;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.ExtendedUIProgressHandler;
import com.izforge.izpack.util.FileUtil;
import com.izforge.izpack.util.config.*;
import com.izforge.izpack.util.file.FileNameMapper;
import com.izforge.izpack.util.file.GlobPatternMapper;
import com.izforge.izpack.util.file.types.EnumeratedAttribute;
import com.izforge.izpack.util.file.types.FileSet;
import com.izforge.izpack.util.file.types.Mapper;
import com.izforge.izpack.util.helper.SpecHelper;

import java.io.File;
import java.util.*;


public class ConfigurationInstallerListener extends SimpleInstallerListener
{
    /**
     * Name of the specification file
     */
    public static final String SPEC_FILE_NAME = "ConfigurationActionsSpec.xml";

    private HashMap<String, HashMap<Object, ArrayList<ConfigurationAction>>> actions = null;

    private VariableSubstitutor substlocal, substglobal;

    /**
     * Default constructor
     */
    public ConfigurationInstallerListener()
    {
        super(true);
        actions = new HashMap<String, HashMap<Object, ArrayList<ConfigurationAction>>>();
    }

    /**
     * Returns the actions map.
     *
     * @return the actions map
     */
    public HashMap<String, HashMap<Object, ArrayList<ConfigurationAction>>> getActions()
    {
        return (actions);
    }

    @Override
    public void beforePacks(AutomatedInstallData idata, Integer npacks,
                            AbstractUIProgressHandler handler) throws Exception
    {
        super.beforePacks(idata, npacks, handler);

        getSpecHelper().readSpec(SPEC_FILE_NAME, new VariableSubstitutorImpl(idata.getVariables()));

        if (getSpecHelper().getSpec() == null)
        {
            return;
        }

        // Selected packs.
        Iterator<Pack> iter = idata.getSelectedPacks().iterator();
        Pack p = null;
        while (iter != null && iter.hasNext())
        {
            p = iter.next();
            Debug.trace("Entering beforepacks configuration action for pack " + p.name);

            // Resolve data for current pack.
            IXMLElement pack = getSpecHelper().getPackForName(p.name);
            if (pack == null)
            {
                continue;
            }

            Debug.trace("Found configuration action descriptor for pack " + p.name);
            // Prepare the action cache
            HashMap<Object, ArrayList<ConfigurationAction>> packActions = new HashMap<Object, ArrayList<ConfigurationAction>>();
            packActions.put(ActionBase.BEFOREPACK, new ArrayList<ConfigurationAction>());
            packActions.put(ActionBase.AFTERPACK, new ArrayList<ConfigurationAction>());
            packActions.put(ActionBase.BEFOREPACKS, new ArrayList<ConfigurationAction>());
            packActions.put(ActionBase.AFTERPACKS, new ArrayList<ConfigurationAction>());

            // Get all entries for antcalls.
            List<IXMLElement> configActionEntries = pack.getChildrenNamed(ConfigurationAction.CONFIGACTION);
            if (configActionEntries != null)
            {
                Debug.trace("Found " + configActionEntries.size() + " configuration actions");
                if (configActionEntries.size() >= 1)
                {
                    Iterator<IXMLElement> entriesIter = configActionEntries.iterator();
                    while (entriesIter != null && entriesIter.hasNext())
                    {
                        ConfigurationAction act = readConfigAction(entriesIter.next(), idata);
                        if (act != null)
                        {
                            Debug.trace("Adding " + act.getOrder() + "configuration action with "
                                    + act.getActionTasks().size() + " tasks");
                            (packActions.get(act.getOrder())).add(act);
                        }
                    }
                    // Set for progress bar interaction.
                    if ((packActions.get(ActionBase.AFTERPACKS)).size() > 0)
                    {
                        this.setProgressBarCaller();
                    }
                }
                // Set for progress bar interaction.
                if ((packActions.get(ActionBase.AFTERPACKS)).size() > 0)
                {
                    this.setProgressBarCaller();
                }
            }

            actions.put(p.name, packActions);
        }
        iter = idata.getAvailablePacks().iterator();
        while (iter.hasNext())
        {
            String currentPack = ((Pack) iter.next()).name;
            performAllActions(currentPack, ActionBase.BEFOREPACKS, null);
        }
    }

    @Override
    public void beforePack(Pack pack, Integer i, AbstractUIProgressHandler handler)
            throws Exception
    {
        performAllActions(pack.name, ActionBase.BEFOREPACK, handler);
    }

    @Override
    public void afterPack(Pack pack, Integer i, AbstractUIProgressHandler handler) throws Exception
    {
        performAllActions(pack.name, ActionBase.AFTERPACK, handler);
    }

    @Override
    public void afterPacks(AutomatedInstallData idata, AbstractUIProgressHandler handler)
            throws Exception
    {
        if (informProgressBar())
        {
            handler.nextStep(getMsg("ConfigurationAction.pack"), getProgressBarCallerId(), getActionCount(
                    idata, ActionBase.AFTERPACKS));
        }
        Iterator<Pack> iter = idata.getSelectedPacks().iterator();
        while (iter.hasNext())
        {
            String currentPack = iter.next().name;
            performAllActions(currentPack, ActionBase.AFTERPACKS, handler);
        }
    }

    private int getActionCount(AutomatedInstallData idata, String order)
    {
        int retval = 0;
        Iterator<Pack> iter = idata.getSelectedPacks().iterator();
        while (iter.hasNext())
        {
            String currentPack = iter.next().name;
            ArrayList<ConfigurationAction> actList = getActions(currentPack, order);
            if (actList != null)
            {
                retval += actList.size();
            }
        }
        return (retval);
    }

    /**
     * Returns the defined actions for the given pack in the requested order.
     *
     * @param packName name of the pack for which the actions should be returned
     * @param order    order to be used; valid are <i>beforepack</i> and <i>afterpack</i>
     * @return a list which contains all defined actions for the given pack and order
     */
    // -------------------------------------------------------
    protected ArrayList<ConfigurationAction> getActions(String packName, String order)
    {
        if (actions == null)
        {
            return null;
        }

        HashMap<Object, ArrayList<ConfigurationAction>> packActions = actions.get(packName);
        if (packActions == null || packActions.size() == 0)
        {
            return null;
        }

        return packActions.get(order);
    }

    /**
     * Performs all actions which are defined for the given pack and order.
     *
     * @param packName name of the pack for which the actions should be performed
     * @param order    order to be used; valid are <i>beforepack</i> and <i>afterpack</i>
     * @throws InstallerException
     */
    private void performAllActions(String packName, String order, AbstractUIProgressHandler handler)
            throws InstallerException
    {
        ArrayList<ConfigurationAction> actList = getActions(packName, order);
        if (actList == null || actList.size() == 0)
        {
            return;
        }

        Debug.trace("******* Executing all " + order + " configuration actions for " + packName + " ...");
        for (ConfigurationAction act : actList)
        {
            // Inform progress bar if needed. Works only on AFTER_PACKS
            if (informProgressBar() && handler != null
                    && handler instanceof ExtendedUIProgressHandler
                    && order.equals(ActionBase.AFTERPACKS))
            {
                ((ExtendedUIProgressHandler) handler)
                        .progress((act.getMessageID() != null) ? getMsg(act.getMessageID()) : "");
            }
            else
            {
                try
                {
                    act.performInstallAction();
                }
                catch (Exception e)
                {
                    throw new InstallerException(e);
                }
            }
        }
    }

    /**
     * Returns an ant call which is defined in the given XML element.
     *
     * @param el XML element which contains the description of an ant call
     * @param idata The installation data
     * @return an ant call which is defined in the given XML element
     * @throws InstallerException
     */
    private ConfigurationAction readConfigAction(IXMLElement el, AutomatedInstallData idata) throws InstallerException
    {
        if (el == null)
        {
            return null;
        }
        SpecHelper spec = getSpecHelper();
        ConfigurationAction act = new ConfigurationAction();
        try
        {
            act.setOrder(spec.getRequiredAttribute(el, ActionBase.ORDER));
        }
        catch (Exception e)
        {
            throw new InstallerException(e);
        }

        // Read specific attributes and nested elements
        substglobal = new VariableSubstitutorImpl(idata.getVariables());
        substlocal = new VariableSubstitutorImpl(readVariables(idata, el));
        act.setActionTasks(readConfigurables(idata, el));
        act.addActionTasks(readConfigurableSets(idata, el));

        return act;
    }

    private String substituteVariables(String name)
    {
        if (substglobal != null)
        {
            try
            {
                name = substglobal.substitute(name);
            }
            catch (Exception e)
            {
                // ignore
            }
        }
        if (substlocal != null)
        {
            try
            {
                name = substlocal.substitute(name);
            }
            catch (Exception e)
            {
                // ignore
            }
        }
        return name;
    }

    protected List<ConfigurationActionTask> readConfigurableSets(AutomatedInstallData idata,
            IXMLElement parent) throws InstallerException
    {
        List<ConfigurationActionTask> configtasks = new ArrayList<ConfigurationActionTask>();
        Iterator<IXMLElement> iter = parent.getChildrenNamed("configurableset").iterator();
        while (iter.hasNext())
        {
            IXMLElement el = iter.next();

            String attrib = requireAttribute(el, "type");
            ConfigType configType = null;
            if (attrib != null)
            {
                configType = ConfigType.getFromAttribute(attrib);
                if (configType == null)
                {
                    throw new InstallerException("Unknown configurableset type '"+attrib+"'");
                }
            }
            else
            {
                throw new InstallerException("Missing configurableset type");
            }

            ConfigurableTask task;
            switch (configType)
            {
            case OPTIONS:
                task = new OptionFileCopyTask();
                readConfigurableSetCommonAttributes(idata, el, (ConfigurableFileCopyTask)task);
                break;

            case INI:
                task = new IniFileCopyTask();
                readConfigurableSetCommonAttributes(idata, el, (ConfigurableFileCopyTask)task);
                break;
            default:
                throw new InstallerException("Type '"+configType.getAttribute()+"' currently not allowed for ConfigurableSet");
            }

            configtasks.add(new ConfigurationActionTask(task, getAttribute(el, "condition"),
                    getInstalldata().getRules()));
        }
        return configtasks;
    }

    private void readConfigurableSetCommonAttributes(AutomatedInstallData idata,
            IXMLElement el, ConfigurableFileCopyTask task)
    throws InstallerException
    {
        task.setToDir(FileUtil.getAbsoluteFile(getAttribute(el, "todir"), idata.getInstallPath()));
        task.setToFile(FileUtil.getAbsoluteFile(getAttribute(el, "tofile"), idata.getInstallPath()));
        task.setFile(FileUtil.getAbsoluteFile(getAttribute(el, "fromfile"), idata.getInstallPath()));
        String boolattr = getAttribute(el, "patchPreserveEntries");
        if (boolattr != null)
        {
            task.setPatchPreserveEntries(Boolean.parseBoolean(boolattr));
        }
        boolattr = getAttribute(el, "patchPreserveValues");
        if (boolattr != null)
        {
            task.setPatchPreserveValues(Boolean.parseBoolean(boolattr));
        }
        boolattr = getAttribute(el, "patchResolveVariables");
        if (boolattr != null)
        {
            task.setPatchResolveVariables(Boolean.parseBoolean(boolattr));
        }
        boolattr = getAttribute(el, "failonerror");
        if (boolattr != null)
        {
            task.setFailOnError(Boolean.parseBoolean(boolattr));
        }
        boolattr = getAttribute(el, "includeemptydirs");
        if (boolattr != null)
        {
            task.setIncludeEmptyDirs(Boolean.parseBoolean(boolattr));
        }
        boolattr = getAttribute(el, "overwrite");
        if (boolattr != null)
        {
            task.setOverwrite(Boolean.parseBoolean(boolattr));
        }
        boolattr = getAttribute(el, "preservelastmodified");
        if (boolattr != null)
        {
            task.setPreserveLastModified(Boolean.parseBoolean(boolattr));
        }
        boolattr = getAttribute(el, "enablemultiplemappings");
        if (boolattr != null)
        {
            task.setEnableMultipleMappings(Boolean.parseBoolean(boolattr));
        }
        boolattr = getAttribute(el, "cleanup");
        if (boolattr != null)
        {
            task.setCleanup(Boolean.parseBoolean(boolattr));
        }
        for (FileSet fs : readFileSets(idata, el))
        {
            task.addFileSet(fs);
        }
        try
        {
            for (FileNameMapper mapper : readMapper(idata, el))
            {
                task.add(mapper);
            }
        }
        catch (Exception e)
        {
            throw new InstallerException(e.getMessage());
        }
    }

    private void readSingleConfigurableTaskCommonAttributes(AutomatedInstallData idata,
            IXMLElement el, SingleConfigurableTask task)
    throws InstallerException
    {
        String boolattr = getAttribute(el, "create");
        if (boolattr != null)
        {
            task.setCreate(Boolean.parseBoolean(boolattr));
        }
        boolattr = getAttribute(el, "keepOldKeys");
        if (boolattr != null)
        {
            task.setPatchPreserveEntries(Boolean.parseBoolean(boolattr));
        }
        boolattr = getAttribute(el, "keepOldValues");
        if (boolattr != null)
        {
            task.setPatchPreserveValues(Boolean.parseBoolean(boolattr));
        }
        boolattr = getAttribute(el, "resolveValues");
        if (boolattr != null)
        {
            task.setPatchResolveVariables(Boolean.parseBoolean(boolattr));
        }
    }

    private void readConfigFileTaskCommonAttributes(AutomatedInstallData idata,
            IXMLElement el, ConfigFileTask task)
    throws InstallerException
    {
        File tofile = FileUtil.getAbsoluteFile(requireAttribute(el, "tofile"), idata.getInstallPath());
        task.setToFile(tofile);
        task.setOldFile(FileUtil.getAbsoluteFile(requireAttribute(el, "patchfile"), idata.getInstallPath()));
        File newfile = FileUtil.getAbsoluteFile(getAttribute(el, "originalfile"), idata.getInstallPath());
        if (newfile == null)
        {
            newfile = tofile;
        }
        task.setNewFile(newfile);
        String boolattr = getAttribute(el, "cleanup");
        if (boolattr != null)
        {
            task.setCleanup(Boolean.parseBoolean(boolattr));
        }
    }

    protected List<ConfigurationActionTask> readConfigurables(AutomatedInstallData idata,
             IXMLElement parent) throws InstallerException
    {
        List<ConfigurationActionTask> configtasks = new ArrayList<ConfigurationActionTask>();
        Iterator<IXMLElement> iter = parent.getChildrenNamed("configurable").iterator();
        while (iter.hasNext())
        {
            IXMLElement el = iter.next();

            String attrib = requireAttribute(el, "type");
            ConfigType configType = null;
            if (attrib != null)
            {
                configType = ConfigType.getFromAttribute(attrib);
                if (configType == null)
                {
                    throw new InstallerException("Unknown configurable type '"+attrib+"'");
                }
            }
            else
            {
                throw new InstallerException("Missing configurable type");
            }

            ConfigurableTask task;
            switch (configType)
            {
            case OPTIONS:
                task = new SingleOptionFileTask();
                readConfigFileTaskCommonAttributes(idata, el, (ConfigFileTask)task);
                readSingleConfigurableTaskCommonAttributes(idata, el, (SingleConfigurableTask)task);
                ((SingleConfigurableTask)task).readFromXML(el);
                break;

            case INI:
                task = new SingleIniFileTask();
                readConfigFileTaskCommonAttributes(idata, el, (ConfigFileTask)task);
                readSingleConfigurableTaskCommonAttributes(idata, el, (SingleConfigurableTask)task);
                ((SingleConfigurableTask)task).readFromXML(el);
                break;

            case XML:
                task = new SingleXmlFileMergeTask();
                File tofile = FileUtil.getAbsoluteFile(requireAttribute(el, "tofile"), idata.getInstallPath());
                ((SingleXmlFileMergeTask)task).setToFile(tofile);
                ((SingleXmlFileMergeTask)task).setPatchFile(FileUtil.getAbsoluteFile(getAttribute(el, "patchfile"), idata.getInstallPath()));
                File originalfile = FileUtil.getAbsoluteFile(getAttribute(el, "originalfile"), idata.getInstallPath());
                if (originalfile == null)
                    originalfile = tofile;
                ((SingleXmlFileMergeTask)task).setOriginalFile(originalfile);
                ((SingleXmlFileMergeTask)task).setConfigFile(FileUtil.getAbsoluteFile(getAttribute(el, "configfile"), idata.getInstallPath()));
                String boolattr = getAttribute(el, "cleanup");
                if (boolattr != null)
                    ((SingleXmlFileMergeTask)task).setCleanup(Boolean.parseBoolean(boolattr));
                List<FileSet> fslist = readFileSets(idata, el);
                for (FileSet fs : fslist)
                {
                    ((SingleXmlFileMergeTask)task).addFileSet(fs);
                }
                readAndAddXPathProperties(idata, el, (SingleXmlFileMergeTask)task);
                break;

            case REGISTRY:
                task = new RegistryTask();
                ((RegistryTask)task).setFromKey(requireAttribute(el, "fromkey"));
                ((RegistryTask)task).setKey(requireAttribute(el, "tokey"));
                readSingleConfigurableTaskCommonAttributes(idata, el, (SingleConfigurableTask)task);
                break;

            default:
                // This should never happen
                throw new InstallerException("Type '" + configType.getAttribute() + "' currently not allowed for Configurable");
            }

            configtasks.add(new ConfigurationActionTask(task, getAttribute(el, "condition"),
                    getInstalldata().getRules()));
        }
        return configtasks;
    }

    private void readAndAddXPathProperties(AutomatedInstallData idata, IXMLElement parent,
            SingleXmlFileMergeTask task)
    throws InstallerException
    {
        Iterator<IXMLElement> iter = parent.getChildrenNamed("xpathproperty").iterator();
        while (iter.hasNext())
        {
            IXMLElement f = iter.next();
            task.addProperty(requireAttribute(f, "key"), requireAttribute(f, "value"));
        }
    }

    private List<FileSet> readFileSets(AutomatedInstallData idata, IXMLElement parent)
    throws InstallerException
    {
        Iterator<IXMLElement> iter = parent.getChildrenNamed("fileset").iterator();
        List<FileSet> fslist = new ArrayList<FileSet>();
        try
        {
            while (iter.hasNext())
            {
                IXMLElement f = iter.next();


                FileSet fs = new FileSet();

                String strattr = getAttribute(f, "dir");
                if (strattr != null)
                {
                    fs.setDir(FileUtil.getAbsoluteFile(strattr, idata.getInstallPath()));
                }

                strattr = getAttribute(f, "file");
                if (strattr != null)
                {
                    fs.setFile(FileUtil.getAbsoluteFile(strattr, idata.getInstallPath()));
                }
                else
                {
                    if (fs.getDir() == null)
                    {
                        throw new InstallerException("At least one of both attributes, 'dir' or 'file' required in fileset");
                    }
                }

                strattr = getAttribute(f, "includes");
                if (strattr != null)
                {
                    fs.setIncludes(strattr);
                }

                strattr = getAttribute(f, "excludes");
                if (strattr != null)
                {
                    fs.setExcludes(strattr);
                }

                String boolval = getAttribute(f, "casesensitive");
                if (boolval != null)
                {
                    fs.setCaseSensitive(Boolean.parseBoolean(boolval));
                }

                boolval = getAttribute(f, "defaultexcludes");
                if (boolval != null)
                {
                    fs.setDefaultexcludes(Boolean.parseBoolean(boolval));
                }

                boolval = getAttribute(f, "followsymlinks");
                if (boolval != null)
                {
                    fs.setFollowSymlinks(Boolean.parseBoolean(boolval));
                }

                readAndAddIncludes(idata, f, fs);
                readAndAddExcludes(idata, f, fs);

                fslist.add(fs);
            }
        }
        catch (Exception e)
        {
            throw new InstallerException(e);
        }
        return fslist;
    }

    private List<FileNameMapper> readMapper(AutomatedInstallData idata, IXMLElement parent)
    throws InstallerException
    {
        Iterator<IXMLElement> iter = parent.getChildrenNamed("mapper").iterator();
        List<FileNameMapper> mappers = new ArrayList<FileNameMapper>();
        try
        {
            while (iter.hasNext())
            {
                IXMLElement f = iter.next();

                String attrib = requireAttribute(f, "type");
                Mapper.MapperType mappertype = null;
                if (attrib != null)
                {
                    mappertype = Mapper.MapperType.getFromAttribute(attrib);
                    if (mappertype == null)
                    {
                        throw new InstallerException("Unknown filename mapper type '"+attrib+"'");
                    }
                }
                else
                {
                    throw new InstallerException("Missing filename mapper type");
                }
                FileNameMapper mapper = (FileNameMapper) Class.forName(mappertype.getImplementation()).newInstance();
                if (mapper instanceof GlobPatternMapper)
                {
                    String boolval = getAttribute(f, "casesensitive");
                    if ( boolval != null )
                        ((GlobPatternMapper)mapper).setCaseSensitive(Boolean.parseBoolean(boolval));
                    ((GlobPatternMapper)mapper).setFrom(requireAttribute(f, "from"));
                    ((GlobPatternMapper)mapper).setTo(requireAttribute(f, "to"));
                }
                else
                {
                    throw new InstallerException("Filename mapper type \"" + "\" currently not supported");
                }

                mappers.add(mapper);
            }
        }
        catch (Exception e)
        {
            throw new InstallerException(e);
        }
        return mappers;
    }

    private void readAndAddIncludes(AutomatedInstallData idata, IXMLElement parent, FileSet fileset)
    throws InstallerException
    {
        Iterator<IXMLElement> iter = parent.getChildrenNamed("include").iterator();
        while (iter.hasNext())
        {
            IXMLElement f = iter.next();
            fileset.createInclude().setName(requireAttribute(f, "name"));
        }
    }

    private void readAndAddExcludes(AutomatedInstallData idata, IXMLElement parent, FileSet fileset)
    throws InstallerException
    {
        Iterator<IXMLElement> iter = parent.getChildrenNamed("exclude").iterator();
        while (iter.hasNext())
        {
            IXMLElement f = iter.next();
            fileset.createExclude().setName(requireAttribute(f, "name"));
        }
    }

    private int getConfigFileType(String varname, String type)
    throws InstallerException
    {
        int filetype = ConfigFileValue.CONFIGFILE_TYPE_OPTIONS;
        if (type != null)
        {
            if (type.equalsIgnoreCase("options"))
            {
                filetype = ConfigFileValue.CONFIGFILE_TYPE_OPTIONS;
            }
            else if (type.equalsIgnoreCase("xml"))
            {
                filetype = ConfigFileValue.CONFIGFILE_TYPE_XML;
            }
            else if (type.equalsIgnoreCase("ini"))
            {
                filetype = ConfigFileValue.CONFIGFILE_TYPE_INI;
            }
            else
            {
                parseError("Error in definition of dynamic variable " + varname + ": Unknown entry type " + type);
            }
        }
        return filetype;
    }

    protected Properties readVariables(AutomatedInstallData idata,
            IXMLElement parent) throws InstallerException
    {
        List<DynamicVariable> dynamicVariables = new ArrayList<DynamicVariable>();

        Iterator<IXMLElement> iter = parent.getChildrenNamed("variable").iterator();
        while (iter.hasNext())
        {
            IXMLElement var = iter.next();
            String name = requireAttribute(var, "name");

            DynamicVariable dynamicVariable = new DynamicVariableImpl();
            dynamicVariable.setName(name);

            // Check for plain value
            String value = getAttribute(var, "value");
            if (value != null)
            {
                dynamicVariable.setValue(new PlainValue(value));
            }
            else
            {
                IXMLElement valueElement = var.getFirstChildNamed("value");
                if (valueElement != null)
                {
                    value = valueElement.getContent();
                    if (value == null)
                    {
                        parseError("Empty value element for dynamic variable " + name);
                    }
                    dynamicVariable.setValue(new PlainValue(value));
                }
            }
            // Check for environment variable value
            value = getAttribute(var, "environment");
            if (value != null)
            {
                if (dynamicVariable.getValue() == null)
                {
                    dynamicVariable.setValue(new EnvironmentValue(value));
                    try
                    {
                        dynamicVariable.validate();
                    }
                    catch (Exception e)
                    {
                        parseError("Error in definition of dynamic variable " + name + ": " + e.getMessage());
                    }
                }
                else
                {
                    parseError("Ambiguous environment value definition for dynamic variable " + name);
                }
            }
            // Check for registry value
            value = getAttribute(var, "regkey");
            if (value != null)
            {
                String regroot = getAttribute(var, "regroot");
                String regvalue = getAttribute(var, "regvalue");
                if (dynamicVariable.getValue() == null)
                {
                    dynamicVariable.setValue(
                            new RegistryValue(regroot, value, regvalue));
                    try
                    {
                        dynamicVariable.validate();
                    }
                    catch (Exception e)
                    {
                        parseError("Error in definition of dynamic variable " + name + ": " + e.getMessage());
                    }
                }
                else
                {
                    parseError("Ambiguous registry value definition for dynamic variable " + name);
                }
            }
            // Check for value from plain config file
            value = var.getAttribute("file");
            if (value != null)
            {
                String stype = var.getAttribute("type");
                String filesection = var.getAttribute("section");
                String filekey = requireAttribute(var, "key");
                if (dynamicVariable.getValue() == null)
                {
                    dynamicVariable.setValue(new PlainConfigFileValue(value,
                            getConfigFileType(name, stype), filesection, filekey));
                    try
                    {
                        dynamicVariable.validate();
                    }
                    catch (Exception e)
                    {
                        parseError("Error in definition of dynamic variable " + name + ": " + e.getMessage());
                    }
                }
                else
                {
                    // unexpected combination of variable attributes
                    parseError("Ambiguous file value definition for dynamic variable " + name);
                }
            }
            // Check for value from config file entry in a zip file
            value = var.getAttribute("zipfile");
            if (value != null)
            {
                String entryname = requireAttribute(var, "entry");
                String stype = var.getAttribute("type");
                String filesection = var.getAttribute("section");
                String filekey = requireAttribute(var, "key");
                if (dynamicVariable.getValue() == null)
                {
                    dynamicVariable.setValue(new ZipEntryConfigFileValue(value, entryname,
                            getConfigFileType(name, stype), filesection, filekey));
                    try
                    {
                        dynamicVariable.validate();
                    }
                    catch (Exception e)
                    {
                        parseError("Error in definition of dynamic variable " + name + ": " + e.getMessage());
                    }
                }
                else
                {
                    // unexpected combination of variable attributes
                    parseError("Ambiguous file value definition for dynamic variable " + name);
                }
            }
            // Check for value from config file entry in a jar file
            value = var.getAttribute("jarfile");
            if (value != null)
            {
                String entryname = requireAttribute(var, "entry");
                String stype = var.getAttribute("type");
                String filesection = var.getAttribute("section");
                String filekey = requireAttribute(var, "key");
                if (dynamicVariable.getValue() == null)
                {
                    dynamicVariable.setValue(new JarEntryConfigValue(value, entryname,
                            getConfigFileType(name, stype), filesection, filekey));
                    try
                    {
                        dynamicVariable.validate();
                    }
                    catch (Exception e)
                    {
                        parseError("Error in definition of dynamic variable " + name + ": " + e.getMessage());
                    }
                }
                else
                {
                    // unexpected combination of variable attributes
                    parseError("Ambiguous file value definition for dynamic variable " + name);
                }
            }
            // Check for config file value
            value = getAttribute(var, "executable");
            if (value != null)
            {
                if (dynamicVariable.getValue()==null) {
                    String dir = var.getAttribute("dir");
                    String exectype = var.getAttribute("type");
                    String boolval = var.getAttribute("stderr");
                    boolean stderr = false;
                    if ( boolval != null )
                    {
                        stderr = Boolean.parseBoolean(boolval);
                    }

                    if (value.length() <= 0)
                    {
                        parseError("No command given in definition of dynamic variable "+name);
                    }
                    Vector<String> cmd = new Vector<String>();
                    cmd.add(value);
                    List<IXMLElement> args = var.getChildrenNamed("arg");
                    if (args != null) {
                        for (IXMLElement arg : args)
                        {
                            String content = arg.getContent();
                            if (content != null)
                            {
                                cmd.add(content);
                            }
                        }
                    }
                    String[] cmdarr = new String[cmd.size()];
                    if (exectype.equalsIgnoreCase("process") || exectype == null)
                    {
                        dynamicVariable.setValue(new ExecValue(cmd.toArray(cmdarr), dir, false, stderr));
                    }
                    else if (exectype.equalsIgnoreCase("shell"))
                    {
                        dynamicVariable.setValue(new ExecValue(cmd.toArray(cmdarr), dir, true, stderr));
                    }
                    else
                    {
                        parseError("Bad execution type "+exectype+" given for dynamic variable "+name);
                    }
                    try { dynamicVariable.validate(); }
                    catch (Exception e)
                    {
                        parseError("Error in definition of dynamic variable " + name + ": " + e.getMessage());
                    }
                }
                else
                {
                    parseError("Ambiguous execution output value definition for dynamic variable " + name);
                }
            }

            if (dynamicVariable.getValue() == null)
            {
                parseError("No value specified at all for dynamic variable " + name);
            }

            // Check whether dynamic variable has to be evaluated only once during installation
            value = getAttribute(var, "checkonce");
            if (value != null)
            {
                dynamicVariable.setCheckonce(Boolean.valueOf(value));
            }

            // Check whether evaluation failures of the dynamic variable should be ignored
            value = getAttribute(var, "ignorefailure");
            if (value != null)
            {
                dynamicVariable.setIgnoreFailure(Boolean.valueOf(value));
            }

            // Nested regular expression filter
            IXMLElement regexElement = var.getFirstChildNamed("regex");
            if (regexElement != null)
            {
                String expression = getAttribute(regexElement, "regexp");
                String selectexpr = getAttribute(regexElement, "select");
                String replaceexpr = getAttribute(regexElement, "replace");
                String defaultvalue = getAttribute(regexElement, "defaultvalue");
                String scasesensitive = getAttribute(regexElement, "casesensitive");
                String sglobal = getAttribute(regexElement, "global");
                if (dynamicVariable.getRegularExpression() == null)
                {
                    dynamicVariable.setRegularExpression(
                            new RegularExpressionFilterImpl(expression,
                                    selectexpr,
                                    replaceexpr,
                                    defaultvalue,
                                    Boolean.valueOf(scasesensitive != null ? scasesensitive : "true"),
                                    Boolean.valueOf(sglobal != null ? sglobal : "false")));
                    try
                    {
                        dynamicVariable.validate();
                    }
                    catch (Exception e)
                    {
                        parseError("Error in definition of dynamic variable " + name + ": " + e.getMessage());
                    }
                }
                else
                {
                    parseError("Ambiguous regular expression filter definition for dynamic variable " + name);
                }
            }

            for (DynamicVariable dynvar : dynamicVariables)
            {
                String conditionid = getAttribute(var, "condition");
                dynamicVariable.setConditionid(conditionid);

                if (dynvar.getName().equals(name))
                {
                    dynamicVariables.remove(dynvar);
                    parseWarn(var, "Dynamic Variable '" + name + "' overwritten");
                }
                dynamicVariables.add(dynamicVariable);
            }
        }

        return evaluateDynamicVariables(dynamicVariables, idata);
    }

    // FIXME put LinkedList for local variables here to keep their eval order as in xml
    private Properties evaluateDynamicVariables(List<DynamicVariable> dynamicvariables,
            AutomatedInstallData installdata)
    throws InstallerException
    {
        VariableSubstitutor subst = new VariableSubstitutorImpl(installdata.getVariables());
        // FIXME change DynamicVariableSubstitutor constructor interface
        //DynamicVariableSubstitutor dynsubst = new DynamicVariableSubstitutor((List)dynamicvariables, installdata.getRules());
        Properties props = new Properties();
        RulesEngine rules = installdata.getRules();
        Debug.log("Evaluating configuration variables");
        if (dynamicvariables != null)
        {
            for (DynamicVariable dynvar : dynamicvariables)
            {
                Debug.log("Configuration variable: " + dynvar.getName());
                boolean refresh = false;
                String conditionid = dynvar.getConditionid();
                Debug.log("condition: " + conditionid);
                if ((conditionid != null) && (conditionid.length() > 0))
                {
                    if ((rules != null) && rules.isConditionTrue(conditionid))
                    {
                        Debug.log("refresh condition");
                        // condition for this rule is true
                        refresh = true;
                    }
                }
                else
                {
                    Debug.log("refresh condition");
                    // empty condition
                    refresh = true;
                }
                if (refresh)
                {
                    try
                    {
                        String newValue = dynvar.evaluate(subst);
                        if (newValue != null)
                        {
                            Debug.log("Configuration variable " + dynvar.getName() + ": " + newValue);
                            props.setProperty(dynvar.getName(), newValue);
                        }
                        else
                        {
                            Debug.log("Configuration variable " + dynvar.getName() + " unchanged: " + dynvar.getValue());
                        }
                    }
                    catch (Exception e)
                    {
                        throw new InstallerException(e);
                    }
                }
            }
        }

        return props;
    }

    /**
     * Call getAttribute on an element, producing a meaningful error message if not present, or
     * empty. It is an error for 'element' or 'attribute' to be null.
     *
     * @param element   The element to get the attribute value of
     * @param attribute The name of the attribute to get
     */
    protected String requireAttribute(IXMLElement element, String attribute)
            throws InstallerException
    {
        String value = getAttribute(element, attribute);
        if (value == null)
        {
            parseError(element, "<" + element.getName() + "> requires attribute '" + attribute + "'");
        }
        return value;
    }

    protected String getAttribute(IXMLElement element, String attribute)
    throws InstallerException
    {
        String value = element.getAttribute(attribute);
        if (value != null)
        {
            return substituteVariables(value);
        }
        return value;
    }

    /**
     * Create parse error with consistent messages. Includes file name.
     *
     * @param message Brief message explaining error
     */
    protected void parseError(String message) throws InstallerException
    {
        throw new InstallerException(SPEC_FILE_NAME + ":" + message);
    }

    /**
     * Create parse error with consistent messages. Includes file name and line # of parent. It is
     * an error for 'parent' to be null.     *
     *
     * @param parent  The element in which the error occured
     * @param message Brief message explaining error
     */
    protected void parseError(IXMLElement parent, String message) throws InstallerException
    {
        throw new InstallerException(SPEC_FILE_NAME + ":" + parent.getLineNr() + ": " + message);
    }

    /**
     * Create a parse warning with consistent messages. Includes file name and line # of parent.     *
     *
     * @param parent  The element in which the warning occured
     * @param message Warning message
     */
    protected void parseWarn(IXMLElement parent, String message)
    {
        System.out.println("Warning: " + SPEC_FILE_NAME + ":" + parent.getLineNr() + ": " + message);
    }

    public enum ConfigType
    {
        OPTIONS("options"), INI("ini"), XML("xml"), REGISTRY("registry");

        private static Map<String, ConfigType> lookup;

        private String attribute;

        ConfigType(String attribute)
        {
            this.attribute = attribute;
        }

        static
        {
            lookup = new HashMap<String, ConfigType>();
            for (ConfigType operation : EnumSet.allOf(ConfigType.class))
            {
                lookup.put(operation.getAttribute(), operation);
            }
        }

        public String getAttribute()
        {
            return attribute;
        }

        public static ConfigType getFromAttribute(String attribute)
        {
            if (attribute != null && lookup.containsKey(attribute))
            {
                return lookup.get(attribute);
            }
            return null;
        }
    }

}
