/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2003 Jonathan Halliday
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

package com.izforge.izpack.installer.automation;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.IXMLParser;
import com.izforge.izpack.api.adaptator.impl.XMLParser;
import com.izforge.izpack.api.data.*;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.api.installer.DataValidator;
import com.izforge.izpack.api.installer.DataValidator.Status;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.data.PanelAction;
import com.izforge.izpack.installer.base.InstallerBase;
import com.izforge.izpack.installer.console.ConsolePanelAutomationHelper;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.installer.language.ConditionCheck;
import com.izforge.izpack.installer.manager.DataValidatorFactory;
import com.izforge.izpack.installer.manager.PanelActionFactory;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.OsConstraintHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

/**
 * Runs the install process in text only (no GUI) mode.
 *
 * @author Jonathan Halliday <jonathan.halliday@arjuna.com>
 * @author Julien Ponge <julien@izforge.com>
 * @author Johannes Lehtinen <johannes.lehtinen@iki.fi>
 */
public class AutomatedInstaller extends InstallerBase
{

    // there are panels which can be instantiated multiple times
    // we therefore need to select the right XML section for each
    // instance
    private TreeMap<String, Integer> panelInstanceCount;

    /**
     * The automated installation data.
     */
    private AutomatedInstallData idata;

    /**
     * The result of the installation.
     */
    private boolean result = false;

    /**
     * Manager for conditions
     */
    private ConditionCheck checkCondition;

    /**
     * Manager for writing uninstall data
     */
    private UninstallDataWriter uninstallDataWriter;
    private VariableSubstitutor variableSubstitutor;

    /**
     * Constructing an instance triggers the install.
     *
     * @param variableSubstitutor
     * @param resourceManager
     * @throws Exception Description of the Exception
     */
    public AutomatedInstaller(ResourceManager resourceManager, ConditionCheck checkCondition, UninstallDataWriter uninstallDataWriter, VariableSubstitutor variableSubstitutor)
    {
        super(resourceManager);
        this.checkCondition = checkCondition;
        this.uninstallDataWriter = uninstallDataWriter;

        this.panelInstanceCount = new TreeMap<String, Integer>();
        this.variableSubstitutor = variableSubstitutor;
    }

    /**
     * Initialize the automated installer.
     *
     * @param inputFilename Name of the file containing the installation data.
     * @throws Exception
     */
    public void init(String inputFilename) throws Exception
    {
        File input = new File(inputFilename);
        // Loads the xml data
        this.idata.setXmlData(getXMLData(input));

        // Loads the langpack
        this.idata.setLocaleISO3(this.idata.getXmlData().getAttribute("langpack", "eng"));
        InputStream in = resourceManager.getLangPack(this.idata.getLocaleISO3());
        this.idata.setLangpack(new LocaleDatabase(in));
        this.idata.setVariable(ScriptParserConstant.ISO3_LANG, this.idata.getLocaleISO3());

        // create the resource manager singleton
        resourceManager.setLocale(this.idata.getLocaleISO3());
//        ResourceManager.create(this.installData);
    }

    @Override
    public void showMissingRequirementMessage(String message)
    {
        Debug.log("Missing installer requirement: " + message);
        System.out.println(message);
    }

    /**
     * Runs the automated installation logic for each panel in turn.
     *
     * @throws Exception
     */
    public void doInstall() throws Exception
    {
        // check installer conditions
        if (!checkCondition.checkInstallerRequirements(this))
        {
            System.out.println("[ Automated installation FAILED! ]");
            System.exit(-1);
            return;
        }

        // TODO: i18n
        System.out.println("[ Starting automated installation ]");
        Debug.log("[ Starting automated installation ]");

        ConsolePanelAutomationHelper uihelper = new ConsolePanelAutomationHelper();

        try
        {
            // assume that installation will succeed
            this.result = true;

            // walk the panels in order
            for (Panel p : this.idata.getPanelsOrder())
            {
                RulesEngine rules = (RulesEngine) this.idata.getRules();
                if (p.hasCondition()
                        && !rules.isConditionTrue(p.getCondition(), this.idata.getVariables()))
                {
                    Debug.log("Condition for panel " + p.getPanelid() + "is not fulfilled, skipping panel!");
                    if (this.panelInstanceCount.containsKey(p.className))
                    {
                        // get number of panel instance to process
                        this.panelInstanceCount.put(p.className, this.panelInstanceCount.get(p.className) + 1);
                    }
                    else
                    {
                        this.panelInstanceCount.put(p.className, 1);
                    }
                    continue;
                }

                if (!OsConstraintHelper.oneMatchesCurrentSystem(p.osConstraints))
                {
                    continue;
                }

                PanelAutomation automationHelper = getPanelAutomationHelper(p);

                if (automationHelper == null)
                {
                    executePreValidateActions(p, uihelper);
                    validatePanel(p);
                    executePostValidateActions(p, uihelper);
                    continue;
                }

                IXMLElement panelRoot = updateInstanceCount(p);

                // execute the installation logic for the current panel
                installPanel(p, automationHelper, panelRoot);
                idata.refreshDynamicVariables();
            }

            // this does nothing if the uninstaller was not included
            uninstallDataWriter.write();

            if (this.result)
            {
                System.out.println("[ Automated installation done ]");
            }
            else
            {
                System.out.println("[ Automated installation FAILED! ]");
            }
        }
        catch (Exception e)
        {
            this.result = false;
            System.err.println(e.toString());
            e.printStackTrace();
            System.out.println("[ Automated installation FAILED! ]");
        }
        finally
        {
            // Bye
            // FIXME !!! Reboot handling
            boolean reboot = false;
            if (idata.isRebootNecessary())
            {
                System.out.println("[ There are file operations pending after reboot ]");
                switch (idata.getInfo().getRebootAction())
                {
                    case Info.REBOOT_ACTION_ALWAYS:
                        reboot = true;
                }
                if (reboot)
                {
                    System.out.println("[ Rebooting now automatically ]");
                }
            }
            Housekeeper.getInstance().shutDown(this.result ? 0 : 1, reboot);
        }
    }

    /**
     * Run the installation logic for a panel.
     *
     * @param p                The panel to install.
     * @param automationHelper The helper of the panel.
     * @param panelRoot        The xml element describing the panel.
     * @throws com.izforge.izpack.api.exception.InstallerException
     *          if something went wrong while installing.
     */
    private void installPanel(Panel p, PanelAutomation automationHelper, IXMLElement panelRoot) throws InstallerException
    {
        executePreActivateActions(p, null);

        Debug.log("automationHelperInstance.runAutomated :"
                + automationHelper.getClass().getName() + " entered.");

        automationHelper.runAutomated(this.idata, panelRoot);

        Debug.log("automationHelperInstance.runAutomated :"
                + automationHelper.getClass().getName() + " successfully done.");

        executePreValidateActions(p, null);
        validatePanel(p);
        executePostValidateActions(p, null);
    }

    /**
     * Update the panelInstanceCount object with a panel.
     *
     * @param p The panel.
     * @return The xml element which describe the panel.
     * @see this.panelInstanceCount
     */
    private IXMLElement updateInstanceCount(Panel p)
    {
        String panelClassName = p.className;

        // We get the panels root xml markup
        Vector<IXMLElement> panelRoots = this.idata.getXmlData().getChildrenNamed(panelClassName);
        int panelRootNo = 0;

        if (this.panelInstanceCount.containsKey(panelClassName))
        {
            // get number of panel instance to process
            panelRootNo = this.panelInstanceCount.get(panelClassName);
        }

        IXMLElement panelRoot = panelRoots.elementAt(panelRootNo);

        this.panelInstanceCount.put(panelClassName, panelRootNo + 1);

        return panelRoot;
    }

    /**
     * Try to get the automation helper for the specified panel.
     *
     * @param p The panel to handle.
     * @return The automation helper if possible, null otherwise.
     */
    private PanelAutomation getPanelAutomationHelper(Panel p)
    {
        Class<PanelAutomation> automationHelperClass = null;
        PanelAutomation automationHelperInstance = null;

        String praefix = "com.izforge.izpack.panels.";
        if (p.className.compareTo(".") > -1)
        // Full qualified class name
        {
            praefix = "";
        }

        String automationHelperClassName = praefix + p.className + "AutomationHelper";

        try
        {
            Debug.log("AutomationHelper:" + automationHelperClassName);
            // determine if the panel supports automated install
            automationHelperClass = (Class<PanelAutomation>) Class.forName(automationHelperClassName);
        }
        catch (ClassNotFoundException e)
        {
            // this is OK - not all panels have/need automation support.
            Debug.log("ClassNotFoundException-skip :" + automationHelperClassName);
        }

        executePreConstructActions(p, null);

        if (automationHelperClass != null)
        {
            try
            {
                // instantiate the automation logic for the panel
                Debug.log("Instantiate :" + automationHelperClassName);
                automationHelperInstance = automationHelperClass.newInstance();
            }
            catch (IllegalAccessException e)
            {
                Debug.log("ERROR: no default constructor for " + automationHelperClassName + ", skipping...");
            }
            catch (InstantiationException e)
            {
                Debug.log("ERROR: no default constructor for " + automationHelperClassName + ", skipping...");
            }
        }

        return automationHelperInstance;
    }

    /**
     * Validate a panel.
     *
     * @param p The panel to validate
     * @throws com.izforge.izpack.api.exception.InstallerException
     *          thrown if the validation fails.
     */
    private void validatePanel(final Panel p) throws InstallerException
    {
        String dataValidator = p.getValidator();
        if (dataValidator != null)
        {
            DataValidator validator = DataValidatorFactory.createDataValidator(dataValidator);
            Status validationResult = validator.validateData(idata);
            if (validationResult != DataValidator.Status.OK)
            {
                // if defaultAnswer is true, result is ok
                if (validationResult == Status.WARNING && validator.getDefaultAnswer())
                {
                    System.out
                            .println("Configuration said, it's ok to go on, if validation is not successfull");
                    return;
                }
                // make installation fail instantly
                this.result = false;
                throw new InstallerException("Validating data for panel " + p.getPanelid() + " was not successfull");
            }
        }
    }

    /**
     * Loads the xml data for the automated mode.
     *
     * @param input The file containing the installation data.
     * @return The root of the XML file.
     * @throws IOException thrown if there are problems reading the file.
     */
    public IXMLElement getXMLData(File input) throws IOException
    {
        FileInputStream in = new FileInputStream(input);

        // Initialises the parser
        IXMLParser parser = new XMLParser();
        IXMLElement rtn = parser.parse(in, input.getAbsolutePath());
        in.close();

        return rtn;
    }

    /**
     * Get the result of the installation.
     *
     * @return True if the installation was successful.
     */
    public boolean getResult()
    {
        return this.result;
    }

    private List<PanelAction> createPanelActionsFromStringList(Panel panel, List<String> actions)
    {
        List<PanelAction> actionList = null;
        if (actions != null)
        {
            actionList = new ArrayList<PanelAction>();
            for (String actionClassName : actions)
            {
                PanelAction action = PanelActionFactory.createPanelAction(actionClassName);
                action.initialize(panel.getPanelActionConfiguration(actionClassName));
                actionList.add(action);
            }
        }
        return actionList;
    }

    private void executePreConstructActions(Panel panel, AbstractUIHandler handler)
    {
        List<PanelAction> preConstructActions = createPanelActionsFromStringList(panel, panel
                .getPreConstructionActions());
        if (preConstructActions != null)
        {
            for (PanelAction preConstructAction : preConstructActions)
            {
                preConstructAction.executeAction(idata, handler);
            }
        }
    }

    private void executePreActivateActions(Panel panel, AbstractUIHandler handler)
    {
        List<PanelAction> preActivateActions = createPanelActionsFromStringList(panel, panel
                .getPreActivationActions());
        if (preActivateActions != null)
        {
            for (PanelAction preActivateAction : preActivateActions)
            {
                preActivateAction.executeAction(idata, handler);
            }
        }
    }

    private void executePreValidateActions(Panel panel, AbstractUIHandler handler)
    {
        List<PanelAction> preValidateActions = createPanelActionsFromStringList(panel, panel
                .getPreValidationActions());
        if (preValidateActions != null)
        {
            for (PanelAction preValidateAction : preValidateActions)
            {
                preValidateAction.executeAction(idata, handler);
            }
        }
    }

    private void executePostValidateActions(Panel panel, AbstractUIHandler handler)
    {
        List<PanelAction> postValidateActions = createPanelActionsFromStringList(panel, panel
                .getPostValidationActions());
        if (postValidateActions != null)
        {
            for (PanelAction postValidateAction : postValidateActions)
            {
                postValidateAction.executeAction(idata, handler);
            }
        }
    }
}
