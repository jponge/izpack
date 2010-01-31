/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2007 Dennis Reil
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
package com.izforge.izpack.installer.console;

import com.izforge.izpack.api.data.*;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.installer.DataValidator;
import com.izforge.izpack.api.installer.DataValidator.Status;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.installer.DataValidatorFactory;
import com.izforge.izpack.installer.base.InstallerBase;
import com.izforge.izpack.installer.bootstrap.Installer;
import com.izforge.izpack.installer.language.ConditionCheck;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.OsConstraint;
import com.izforge.izpack.util.substitutor.VariableSubstitutorImpl;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

/**
 * Runs the console installer
 *
 * @author Mounir el hajj
 */
public class ConsoleInstaller extends InstallerBase {

    private AutomatedInstallData installdata;

    private boolean result = false;

    private Properties properties;

    private PrintWriter printWriter;
    private RulesEngine rules;
    private ConditionCheck checkCondition;
    private VariableSubstitutor variableSubstitutor;

    public ConsoleInstaller(AutomatedInstallData installdata, RulesEngine rules, ResourceManager resourceManager, ConditionCheck checkCondition) throws Exception {
        super(resourceManager);
//        super(resourceManager);
        this.checkCondition = checkCondition;
        this.installdata = installdata;
        this.rules = rules;
        // Fallback: choose the first listed language pack if not specified via commandline
        if (this.installdata.getLocaleISO3() == null) {
            this.installdata.setLocaleISO3(resourceManager.getAvailableLangPacks().get(0));
        }

        InputStream in = getClass().getResourceAsStream(
                "/langpacks/" + this.installdata.getLocaleISO3() + ".xml");
        this.installdata.setLangpack(new LocaleDatabase(in));
        this.installdata.setVariable(ScriptParserConstant.ISO3_LANG, this.installdata.getLocaleISO3());
        resourceManager.setLocale(this.installdata.getLocaleISO3());
        if (!checkCondition.checkInstallerRequirements(this)) {
            Debug.log("not all installerconditions are fulfilled.");
            return;
        }
        variableSubstitutor = new VariableSubstitutorImpl(this.installdata.getVariables());
    }

    protected void iterateAndPerformAction(String strAction) throws Exception {
        if (!checkCondition.checkInstallerRequirements(this)) {
            Debug.log("not all installerconditions are fulfilled.");
            return;
        }
        Debug.log("[ Starting console installation ] " + strAction);

        try {
            this.result = true;
            Iterator<Panel> panelsIterator = this.installdata.getPanelsOrder().iterator();
            this.installdata.setCurPanelNumber(-1);
            while (panelsIterator.hasNext()) {
                Panel p = panelsIterator.next();
                this.installdata.setCurPanelNumber(this.installdata.getCurPanelNumber() + 1);
                String praefix = "com.izforge.izpack.panels.";
                if (p.className.compareTo(".") > -1) {
                    praefix = "";
                }
                if (!OsConstraint.oneMatchesCurrentSystem(p.osConstraints)) {
                    continue;
                }
                String panelClassName = p.className;
                String consoleHelperClassName = praefix + panelClassName + "ConsoleHelper";
                Class<PanelConsole> consoleHelperClass = null;

                Debug.log("ConsoleHelper:" + consoleHelperClassName);
                try {

                    consoleHelperClass = (Class<PanelConsole>) Class
                            .forName(consoleHelperClassName);

                }
                catch (ClassNotFoundException e) {
                    Debug.log("ClassNotFoundException-skip :" + consoleHelperClassName);
                    continue;
                }
                PanelConsole consoleHelperInstance = null;
                if (consoleHelperClass != null) {
                    try {
                        Debug.log("Instantiate :" + consoleHelperClassName);
                        installdata.refreshDynamicVariables();
                        consoleHelperInstance = consoleHelperClass.newInstance();
                    }
                    catch (Exception e) {
                        Debug.log("ERROR: no default constructor for " + consoleHelperClassName
                                + ", skipping...");
                        continue;
                    }
                }

                if (consoleHelperInstance != null) {
                    try {
                        Debug.log("consoleHelperInstance." + strAction + ":"
                                + consoleHelperClassName + " entered.");
                        boolean bActionResult = true;
                        boolean bIsConditionFulfilled = true;
                        String strCondition = p.getCondition();
                        if (strCondition != null) {
                            RulesEngine rules = (RulesEngine) installdata.getRules();
                            bIsConditionFulfilled = rules.isConditionTrue(
                                    strCondition);
                        }

                        if (strAction.equals("doInstall") && bIsConditionFulfilled) {
                            do {
                                bActionResult = consoleHelperInstance.runConsole(this.installdata);
                            }
                            while (!validatePanel(p));
                        } else if (strAction.equals("doGeneratePropertiesFile")) {
                            bActionResult = consoleHelperInstance.runGeneratePropertiesFile(
                                    this.installdata, this.printWriter);
                        } else if (strAction.equals("doInstallFromPropertiesFile")
                                && bIsConditionFulfilled) {
                            bActionResult = consoleHelperInstance.runConsoleFromProperties(
                                    this.installdata, this.properties);
                        }
                        if (!bActionResult) {
                            this.result = false;
                            return;
                        } else {
                            Debug.log("consoleHelperInstance." + strAction + ":"
                                    + consoleHelperClassName + " successfully done.");
                        }
                    }
                    catch (Exception e) {
                        Debug.log("ERROR: console installation failed for panel " + panelClassName);
                        e.printStackTrace();
                        this.result = false;
                    }

                }

            }

            if (this.result) {
                System.out.println("[ Console installation done ]");
            } else {
                System.out.println("[ Console installation FAILED! ]");
            }
        }
        catch (Exception e) {
            this.result = false;
            System.err.println(e.toString());
            e.printStackTrace();
            System.out.println("[ Console installation FAILED! ]");
        }

    }

    protected void doInstall() throws Exception {
        try {
            iterateAndPerformAction("doInstall");
        }
        catch (Exception e) {
            throw e;
        }
        finally {
            checkedReboot();
        }
    }

    protected void doGeneratePropertiesFile(String strFile) throws Exception {
        try {
            this.printWriter = new PrintWriter(strFile);
            iterateAndPerformAction("doGeneratePropertiesFile");
            this.printWriter.flush();
        }
        catch (Exception e) {
            throw e;
        }

        finally {
            this.printWriter.close();
            Housekeeper.getInstance().shutDown(this.result ? 0 : 1);
        }

    }

    protected void doInstallFromPropertiesFile(String strFile) throws Exception {
        FileInputStream in = new FileInputStream(strFile);
        try {
            properties = new Properties();
            properties.load(in);
            iterateAndPerformAction("doInstallFromPropertiesFile");
        }
        catch (Exception e) {
            throw e;
        }
        finally {
            in.close();
            checkedReboot();
        }
    }

    protected void doInstallFromSystemProperties() throws Exception {
        try {
            properties = System.getProperties();
            iterateAndPerformAction("doInstallFromPropertiesFile");
        }
        catch (Exception e) {
            throw e;
        }
        finally {
            checkedReboot();
        }
    }

    protected void doInstallFromSystemPropertiesMerge(String strFile) throws Exception {
        FileInputStream in = new FileInputStream(strFile);
        try {
            properties = new Properties();
            properties.load(in);
            mergeAndOverwriteFromSysProperties();
            iterateAndPerformAction("doInstallFromPropertiesFile");
        }
        catch (Exception e) {
            throw e;
        }
        finally {
            in.close();
            checkedReboot();
        }
    }

    /**
     * Validate a panel.
     *
     * @param p The panel to validate
     * @throws com.izforge.izpack.api.exception.InstallerException
     *          thrown if the validation fails.
     */
    private boolean validatePanel(final Panel p) throws InstallerException {
        boolean bValidity = true;
        String dataValidator = p.getValidator();
        if (dataValidator != null) {
            DataValidator validator = DataValidatorFactory.createDataValidator(dataValidator);
            Status validationResult = validator.validateData(installdata);
            if (validationResult != DataValidator.Status.OK) {
                // if defaultAnswer is true, result is ok
                if (validationResult == Status.WARNING && validator.getDefaultAnswer()) {
                    System.out
                            .println("Configuration said, it's ok to go on, if validation is not successfull");

                }
                // make installation fail instantly
                bValidity = false;
                System.out.println("Validation failed, please verify your input");
            }
        }
        return bValidity;
    }

    private void mergeAndOverwriteFromSysProperties() {
        Properties p = System.getProperties();
        Enumeration<?> e = p.propertyNames();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            String newval = p.getProperty(key);
            String oldval = (String) properties.setProperty(key, newval);
            if (oldval != null) {
                System.out.println(
                        "Warning: Property " + key + " overwritten: '"
                                + oldval + "' --> '" + newval + "'");
            }
        }
    }

    private void checkedReboot() {
        // FIXME !!! Reboot handling
        boolean reboot = false;
        if (installdata.isRebootNecessary()) {
            System.out.println("[ There are file operations pending after reboot ]");
            switch (installdata.getInfo().getRebootAction()) {
                case Info.REBOOT_ACTION_ALWAYS:
                    reboot = true;
            }
            if (reboot)
                System.out.println("[ Rebooting now automatically ]");
        }
        Housekeeper.getInstance().shutDown(this.result ? 0 : 1, reboot);
    }

    public void run(int type, String path) throws Exception {
        switch (type) {
            case Installer.CONSOLE_GEN_TEMPLATE:
                doGeneratePropertiesFile(path);
                break;

            case Installer.CONSOLE_FROM_TEMPLATE:
                doInstallFromPropertiesFile(path);
                break;

            case Installer.CONSOLE_FROM_SYSTEMPROPERTIES:
                doInstallFromSystemProperties();
                break;

            case Installer.CONSOLE_FROM_SYSTEMPROPERTIESMERGE:
                doInstallFromSystemPropertiesMerge(path);
                break;

            default:
                doInstall();
        }
    }

    public void setLangCode(String langCode) {
        this.installdata.setLocaleISO3(langCode);
    }
}
