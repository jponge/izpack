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
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.DynamicInstallerRequirementValidator;
import com.izforge.izpack.api.data.Info;
import com.izforge.izpack.api.data.LocaleDatabase;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.api.data.ScriptParserConstant;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.api.installer.DataValidator;
import com.izforge.izpack.api.installer.DataValidator.Status;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;
import com.izforge.izpack.data.PanelAction;
import com.izforge.izpack.installer.base.InstallerBase;
import com.izforge.izpack.installer.console.ConsolePanelAutomationHelper;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.installer.manager.DataValidatorFactory;
import com.izforge.izpack.installer.manager.PanelActionFactory;
import com.izforge.izpack.installer.requirement.RequirementsChecker;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.OsConstraintHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Runs the install process in text only (no GUI) mode.
 *
 * @author Jonathan Halliday <jonathan.halliday@arjuna.com>
 * @author Julien Ponge <julien@izforge.com>
 * @author Johannes Lehtinen <johannes.lehtinen@iki.fi>
 */
public class AutomatedInstaller extends InstallerBase
{
    private static final Logger logger = Logger.getLogger(AutomatedInstaller.class.getName());

    // there are panels which can be instantiated multiple times
    // we therefore need to select the right XML section for each
    // instance
    private TreeMap<String, Integer> panelInstanceCount;

    /**
     * The automated installation data.
     */
    private final AutomatedInstallData installData;

    /**
     * The result of the installation.
     */
    private boolean result = false;

    /**
     * Installation requirements.
     */
    private RequirementsChecker requirements;

    /**
     * Manager for writing uninstall data
     */
    private UninstallDataWriter uninstallDataWriter;

    private VariableSubstitutor variableSubstitutor;

    /**
     * The house-keeper.
     */
    private final Housekeeper housekeeper;

    /**
     * Constructs an <tt>AutomatedInstaller</tt>.
     *
     * @param installData         the installation data
     * @param resourceManager     the resource manager
     * @param requirements        the installation requirements checker
     * @param uninstallDataWriter the uninstallation data writer
     * @param variableSubstitutor the variable substituter
     * @param housekeeper         the house-keeper
     */
    public AutomatedInstaller(AutomatedInstallData installData, ResourceManager resourceManager,
                              RequirementsChecker requirements, UninstallDataWriter uninstallDataWriter,
                              VariableSubstitutor variableSubstitutor, Housekeeper housekeeper)
    {
        super(resourceManager);
        this.installData = installData;
        this.requirements = requirements;
        this.uninstallDataWriter = uninstallDataWriter;

        this.panelInstanceCount = new TreeMap<String, Integer>();
        this.variableSubstitutor = variableSubstitutor;
        this.housekeeper = housekeeper;
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
        this.installData.setXmlData(getXMLData(input));

        // Loads the langpack
        this.installData.setLocaleISO3(this.installData.getXmlData().getAttribute("langpack", "eng"));
        InputStream in = resourceManager.getLangPack(this.installData.getLocaleISO3());
        this.installData.setLangpack(new LocaleDatabase(in));
        this.installData.setVariable(ScriptParserConstant.ISO3_LANG, this.installData.getLocaleISO3());

        // create the resource manager singleton
        resourceManager.setLocale(this.installData.getLocaleISO3());
//        ResourceManager.create(this.installData);
    }

    /**
     * Runs the automated installation logic for each panel in turn.
     *
     * @throws Exception
     */
    public void doInstall() throws Exception
    {
        VariableSubstitutor subst = new VariableSubstitutorImpl(this.installData.getVariables());

        // Get dynamic variables immediately for being able to use them as
        // variable condition in installerrequirements
        InstallerBase.refreshDynamicVariables(this.installData, subst);

        // check installer conditions
        if (!requirements.check())
        {
            System.out.println("[ Automated installation FAILED! ]");
            System.exit(-1);
            return;
        }

        // TODO: i18n
        System.out.println("[ Starting automated installation ]");
        logger.info("[ Starting automated installation ]");

        ConsolePanelAutomationHelper uihelper = new ConsolePanelAutomationHelper(housekeeper);

        try
        {
            // assume that installation will succeed
            this.result = true;

            // walk the panels in order
            for (Panel p : this.installData.getPanelsOrder())
            {
                RulesEngine rules = this.installData.getRules();
                if (p.hasCondition()
                        && !rules.isConditionTrue(p.getCondition(), this.installData))
                {
                    logger.fine("Condition for panel " + p.getPanelid() + "is not fulfilled, skipping panel!");
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

                if (!OsConstraintHelper.oneMatchesCurrentSystem(p.getOsConstraints()))
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

                refreshDynamicVariables(this.installData, subst);
            }

            if (uninstallDataWriter.isUninstallRequired())
            {
                result = uninstallDataWriter.write();
            }
        }
        catch (Exception e)
        {
            result = false;
            System.err.println(e.toString());
            e.printStackTrace();
        }
        finally
        {
            if (result)
            {
                System.out.println("[ Automated installation done ]");
            }
            else
            {
                System.out.println("[ Automated installation FAILED! ]");
            }

            // Bye
            // FIXME !!! Reboot handling
            boolean reboot = false;
            if (installData.isRebootNecessary())
            {
                System.out.println("[ There are file operations pending after reboot ]");
                switch (installData.getInfo().getRebootAction())
                {
                    case Info.REBOOT_ACTION_ALWAYS:
                        reboot = true;
                }
                if (reboot)
                {
                    System.out.println("[ Rebooting now automatically ]");
                }
            }
            housekeeper.shutDown(this.result ? 0 : 1, reboot);
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

        logger.fine("automationHelperInstance.runAutomated: "
                + automationHelper.getClass().getName() + " entered.");

        automationHelper.runAutomated(this.installData, panelRoot);

        logger.fine("automationHelperInstance.runAutomated: "
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
        List<IXMLElement> panelRoots = this.installData.getXmlData().getChildrenNamed(panelClassName);
        int panelRootNo = 0;

        if (this.panelInstanceCount.containsKey(panelClassName))
        {
            // get number of panel instance to process
            panelRootNo = this.panelInstanceCount.get(panelClassName);
        }

        IXMLElement panelRoot = panelRoots.get(panelRootNo);

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
        if (p.className.contains("."))
        // Full qualified class name
        {
            praefix = "";
        }

        String automationHelperClassName = praefix + p.className + "AutomationHelper";

        try
        {
            logger.fine("AutomationHelper: " + automationHelperClassName);
            // determine if the panel supports automated install
            automationHelperClass = (Class<PanelAutomation>) Class.forName(automationHelperClassName);
        }
        catch (ClassNotFoundException e)
        {
            // this is OK - not all panels have/need automation support.
            logger.log(Level.WARNING, "AutomationHelper class not found: " + automationHelperClassName, e);
        }

        executePreConstructActions(p, null);

        if (automationHelperClass != null)
        {
            try
            {
                // instantiate the automation logic for the panel
                logger.fine("Instantiate :" + automationHelperClassName);
                automationHelperInstance = automationHelperClass.newInstance();
            }
            catch (IllegalAccessException e)
            {
                logger.log(Level.WARNING, "no default constructor for " + automationHelperClassName + ", skipping...", e);
            }
            catch (InstantiationException e)
            {
                logger.log(Level.WARNING, "no default constructor for " + automationHelperClassName + ", skipping...", e);
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
        try
        {
            InstallerBase.refreshDynamicVariables(this.installData,
                    new VariableSubstitutorImpl(this.installData.getVariables()));
        }
        catch (Exception e)
        {
            throw new InstallerException(e);
        }

        // Evaluate all global dynamic conditions
        List<DynamicInstallerRequirementValidator> dynConds = installData.getDynamicinstallerrequirements();
        if (dynConds != null)
        {
            for (DynamicInstallerRequirementValidator validator : dynConds)
            {
                Status validationResult = validator.validateData(installData);
                if (validationResult != DataValidator.Status.OK)
                {
                    String errorMessage;
                    try
                    {
                        errorMessage = installData.getLangpack().getString("data.validation.error.title")
                                + ": " + variableSubstitutor.substitute(installData.getLangpack().getString(validator
                                .getErrorMessageId()));
                    }
                    catch (Exception e)
                    {
                        throw new InstallerException(e);
                    }
                    // if defaultAnswer is true, result is ok
                    if (validationResult == Status.WARNING && validator.getDefaultAnswer())
                    {
                        System.out.println(errorMessage + " - ignoring");
                        return;
                    }
                    // make installation fail instantly
                    this.result = false;
                    logger.warning("Dynamic installer requirement validation (" + validator.getClass().getName() + ") failed");
                    throw new InstallerException(errorMessage);
                }
            }
        }

        // Evaluate panel condition
        String dataValidator = p.getValidator();
        if (dataValidator != null)
        {
            DataValidator validator = DataValidatorFactory.createDataValidator(dataValidator);
            Status validationResult = validator.validateData(installData);
            if (validationResult != DataValidator.Status.OK)
            {
                String errorMessage;
                try
                {
                    errorMessage = installData.getLangpack().getString("data.validation.error.title")
                            + ": " + variableSubstitutor.substitute(installData.getLangpack().getString(validator
                            .getErrorMessageId()));
                }
                catch (Exception e)
                {
                    throw new InstallerException(e);
                }
                // if defaultAnswer is true, result is ok
                // if defaultAnswer is true, result is ok
                if (validationResult == Status.WARNING && validator.getDefaultAnswer())
                {
                    System.out.println(errorMessage + " - ignoring");
                    return;
                }
                // make installation fail instantly
                this.result = false;
                logger.warning("Data validation (" + validator.getClass().getName() + ") failed");
                throw new InstallerException(errorMessage);
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
                preConstructAction.executeAction(installData, handler);
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
                preActivateAction.executeAction(installData, handler);
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
                preValidateAction.executeAction(installData, handler);
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
                postValidateAction.executeAction(installData, handler);
            }
        }
    }
}
