/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2004 Klaus Bartz
 * Copyright 2004 Thomas Guenter
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.handler.AbstractUIProgressHandler;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.util.ExtendedUIProgressHandler;
import com.izforge.izpack.util.FileUtil;
import com.izforge.izpack.util.helper.SpecHelper;

/**
 * Installer listener for performing ANT actions. The definition what should be done will be made in
 * a specification file which is referenced by the resource id "AntActionsSpec.xml". There should be
 * an entry in the install.xml file in the sub ELEMENT "res" of ELEMENT "resources" which references
 * it. The specification of the xml file is done in the DTD antaction.dtd. The xml file specifies,
 * for what pack what ant call should be performed at what time of installation.
 *
 * @author Thomas Guenter
 * @author Klaus Bartz
 */
public class AntActionInstallerListener extends SimpleInstallerListener
{
    private static final Logger logger = Logger.getLogger(AntActionInstallerListener.class.getName());

    // ------------------------------------------------------------------------
    // Constant Definitions
    // ------------------------------------------------------------------------
    // --------String constants for parsing the XML specification ------------
    // -------- see class AntAction -----------------------------------------
    /**
     * Name of the specification file
     */
    public static final String SPEC_FILE_NAME = "AntActionsSpec.xml";

    private HashMap<String, HashMap<Object, ArrayList<AntAction>>> actions = null;

    private ArrayList<AntAction> uninstActions = null;
    private VariableSubstitutor variableSubstitutor;
    private UninstallData uninstallData;

    /**
     * Constructs an <tt>AntActionInstallerListener</tt>.
     *
     * @param variableSubstitutor the variable substituter
     * @param resources           the resources
     * @param uninstallData       the uninstallation data
     */
    public AntActionInstallerListener(VariableSubstitutor variableSubstitutor, Resources resources,
                                      UninstallData uninstallData)
    {
        super(resources, true);
        this.variableSubstitutor = variableSubstitutor;
        actions = new HashMap<String, HashMap<Object, ArrayList<AntAction>>>();
        uninstActions = new ArrayList<AntAction>();
        this.uninstallData = uninstallData;
    }

    /**
     * Returns the actions map.
     *
     * @return the actions map
     */
    public HashMap<String, HashMap<Object, ArrayList<AntAction>>> getActions()
    {
        return (actions);
    }

    @Override
    public void beforePacks(AutomatedInstallData idata, Integer npacks,
                            AbstractUIProgressHandler handler) throws Exception
    {
        super.beforePacks(idata, npacks, handler);

        getSpecHelper().readSpec(SPEC_FILE_NAME, variableSubstitutor);

        if (getSpecHelper().getSpec() == null)
        {
            return;
        }

        // Selected packs.
        for (Pack pack : idata.getSelectedPacks())
        {
            // Resolve data for current pack.
            IXMLElement packElement = getSpecHelper().getPackForName(pack.getName());
            if (packElement == null)
            {
                continue;
            }

            // Prepare the action cache
            HashMap<Object, ArrayList<AntAction>> packActions = new HashMap<Object, ArrayList<AntAction>>();
            packActions.put(ActionBase.BEFOREPACK, new ArrayList<AntAction>());
            packActions.put(ActionBase.AFTERPACK, new ArrayList<AntAction>());
            packActions.put(ActionBase.BEFOREPACKS, new ArrayList<AntAction>());
            packActions.put(ActionBase.AFTERPACKS, new ArrayList<AntAction>());

            // Get all entries for antcalls.
            List<IXMLElement> antCallEntries = packElement.getChildrenNamed(AntAction.ANTCALL);
            if (antCallEntries != null && antCallEntries.size() >= 1)
            {
                for (IXMLElement antCallEntry : antCallEntries)
                {
                    AntAction act = readAntCall(antCallEntry, idata);
                    if (act != null)
                    {
                        (packActions.get(act.getOrder())).add(act);
                    }
                }
                // Set for progress bar interaction.
                if ((packActions.get(ActionBase.AFTERPACKS)).size() > 0)
                {
                    this.setProgressBarCaller();
                }
            }

            actions.put(pack.getName(), packActions);
        }
        for (Pack pack : idata.getAvailablePacks())
        {
            String currentPack = pack.getName();
            performAllActions(currentPack, ActionBase.BEFOREPACKS, null);
        }
    }

    @Override
    public void beforePack(Pack pack, Integer i, AbstractUIProgressHandler handler)
            throws Exception
    {
        performAllActions(pack.getName(), ActionBase.BEFOREPACK, handler);
    }

    @Override
    public void afterPack(Pack pack, Integer i, AbstractUIProgressHandler handler) throws Exception
    {
        performAllActions(pack.getName(), ActionBase.AFTERPACK, handler);
    }

    @Override
    public void afterPacks(AutomatedInstallData idata, AbstractUIProgressHandler handler)
            throws Exception
    {
        if (informProgressBar())
        {
            handler.nextStep(getMsg("AntAction.pack"), getProgressBarCallerId(), getActionCount(
                    idata, ActionBase.AFTERPACKS));
        }
        for (Pack pack : idata.getSelectedPacks())
        {
            String currentPack = pack.getName();
            performAllActions(currentPack, ActionBase.AFTERPACKS, handler);
        }
        if (uninstActions.size() > 0)
        {
            uninstallData.addAdditionalData("antActions", uninstActions);
        }
    }

    private int getActionCount(AutomatedInstallData idata, String order)
    {
        int retval = 0;
        for (Pack pack : idata.getSelectedPacks())
        {
            String currentPack = pack.getName();
            ArrayList<AntAction> actList = getActions(currentPack, order);
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
    protected ArrayList<AntAction> getActions(String packName, String order)
    {
        if (actions == null)
        {
            return null;
        }

        HashMap<Object, ArrayList<AntAction>> packActions = actions.get(packName);
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
        ArrayList<AntAction> actList = getActions(packName, order);
        if (actList == null || actList.size() == 0)
        {
            return;
        }

        logger.fine("Executing all " + order + " Ant actions of pack " + packName + " ...");
        for (AntAction act : actList)
        {
            // Inform progress bar if needed. Works only
            // on AFTER_PACKS
            if (informProgressBar() && handler != null
                    && handler instanceof ExtendedUIProgressHandler
                    && order.equals(ActionBase.AFTERPACKS))
            {
                ((ExtendedUIProgressHandler) handler)
                        .progress((act.getMessageID() != null) ? getMsg(act.getMessageID()) : "");
            }
            try
            {
                String conditionId = act.getConditionId();
                if (conditionId == null
                        || (conditionId != null && getInstalldata().getRules().isConditionTrue(act.getConditionId())))
                {
                    act.performInstallAction();
                }
            }
            catch (Exception e)
            {
                throw new InstallerException(e);
            }
            if (act.getUninstallTargets().size() > 0)
            {
                uninstActions.add(act);
            }
        }
    }

    /**
     * Returns an ant call which is defined in the given XML element.
     *
     * @param el    XML element which contains the description of an ant call
     * @param idata The installation data
     * @return an ant call which is defined in the given XML element
     * @throws InstallerException
     */
    private AntAction readAntCall(IXMLElement el, AutomatedInstallData idata) throws InstallerException
    {
        String buildFile = null;
        String buildResource = null;

        if (el == null)
        {
            return null;
        }
        SpecHelper spec = getSpecHelper();
        VariableSubstitutor subst = new VariableSubstitutorImpl(idata.getVariables());
        AntAction act = new AntAction();
        try
        {
            act.setOrder(spec.getRequiredAttribute(el, ActionBase.ORDER));
            act.setUninstallOrder(el.getAttribute(ActionBase.UNINSTALL_ORDER,
                                                  ActionBase.BEFOREDELETION));
        }
        catch (Exception e)
        {
            throw new InstallerException(e);
        }

        act.setQuiet(spec.isAttributeYes(el, ActionBase.QUIET, false));
        act.setVerbose(spec.isAttributeYes(el, ActionBase.VERBOSE, false));
        buildFile = el.getAttribute(ActionBase.BUILDFILE);
        act.setConditionId(el.getAttribute(ActionBase.CONDITIONID));
        buildResource = processBuildfileResource(spec, idata, el);
        if (null == buildFile && null == buildResource)
        {
            throw new InstallerException(
                    "Invalid " + SPEC_FILE_NAME + ": either buildfile or buildresource must be specified");
        }
        if (null != buildFile && null != buildResource)
        {
            throw new InstallerException(
                    "Invalid " + SPEC_FILE_NAME + ": cannot specify both buildfile and buildresource");
        }
        if (null != buildFile)
        {
            try
            {
                act.setBuildFile(FileUtil.getAbsoluteFile(subst.substitute(buildFile), idata.getInstallPath()));
            }
            catch (Exception e)
            {
                act.setBuildFile(FileUtil.getAbsoluteFile(buildFile, idata.getInstallPath()));
            }
        }
        else
        {
            act.setBuildFile(new File(buildResource));
        }
        String str = el.getAttribute(ActionBase.LOGFILE);
        if (str != null)
        {
            try
            {
                act.setLogFile(FileUtil.getAbsoluteFile(subst.substitute(str), idata.getInstallPath()));
            }
            catch (Exception e)
            {
                act.setLogFile(FileUtil.getAbsoluteFile(str, idata.getInstallPath()));
            }
        }
        String msgId = el.getAttribute(ActionBase.MESSAGEID);
        if (msgId != null && msgId.length() > 0)
        {
            act.setMessageID(msgId);
        }

        // read propertyfiles
        for (IXMLElement propEl : el.getChildrenNamed(ActionBase.PROPERTYFILE))
        {
            act.addPropertyFile(spec.getRequiredAttribute(propEl, ActionBase.PATH));
        }

        // read properties
        for (IXMLElement propEl : el.getChildrenNamed(ActionBase.PROPERTY))
        {
            act.setProperty(spec.getRequiredAttribute(propEl, ActionBase.NAME), spec
                    .getRequiredAttribute(propEl, ActionBase.VALUE));
        }

        // read targets
        for (IXMLElement targEl : el.getChildrenNamed(ActionBase.TARGET))
        {
            act.addTarget(spec.getRequiredAttribute(targEl, ActionBase.NAME));
        }

        // read uninstall rules
        for (IXMLElement utargEl : el.getChildrenNamed(ActionBase.UNINSTALL_TARGET))
        {
            act.addUninstallTarget(spec.getRequiredAttribute(utargEl, ActionBase.NAME));
        }

        // see if this was an build_resource and there were uninstall actions
        if (null != buildResource && act.getUninstallTargets().size() > 0)
        {
            // We need to add the build_resource file to the uninstaller
            addBuildResourceToUninstallerData(buildResource);
        }

        return act;
    }

    private String processBuildfileResource(SpecHelper spec, AutomatedInstallData idata, IXMLElement el)
            throws InstallerException
    {
        String buildResource = null;

        // See if the build file is a resource
        String attr = el.getAttribute(ActionBase.BUILDRESOURCE);
        if (null != attr)
        {
            // Get the resource
            BufferedInputStream bis = new BufferedInputStream(spec.getResource(attr));
            BufferedOutputStream bos = null;
            try
            {
                // Write the resource to a temporary file
                File tempFile = File.createTempFile("buildfile_resource", "xml");
                tempFile.deleteOnExit();
                bos = new BufferedOutputStream(new FileOutputStream(tempFile));
                int aByte;
                while (-1 != (aByte = bis.read()))
                {
                    bos.write(aByte);
                }
                bis.close();
                bos.close();
                buildResource = tempFile.getAbsolutePath();
            }
            catch (Exception x)
            {
                throw new InstallerException("Failed to write buildfile_resource", x);
            }
            finally
            {
                if (bos != null)
                {
                    try
                    {
                        bos.close();
                    }
                    catch (Exception x)
                    {
                        // Ignore this exception
                    }
                }
            }
        }
        return buildResource;
    }

    private void addBuildResourceToUninstallerData(String buildResource) throws InstallerException
    {
        byte[] content = null;
        File buildFile = new File(buildResource);
        ByteArrayOutputStream bos = new ByteArrayOutputStream((int) buildFile.length());
        BufferedInputStream bis = null;
        try
        {
            bis = new BufferedInputStream(new FileInputStream(buildFile));
            int aByte;
            while (-1 != (aByte = bis.read()))
            {
                bos.write(aByte);
            }
            content = bos.toByteArray();
            uninstallData.addAdditionalData("build_resource", content);
        }
        catch (Exception x)
        {
            throw new InstallerException("Failed to add buildfile_resource to uninstaller", x);
        }
        finally
        {
            try
            {
                bis.close();
                bos.close();
            }
            catch (IOException iOException)
            {
                // Ignore the error
            }
        }
    }
}
