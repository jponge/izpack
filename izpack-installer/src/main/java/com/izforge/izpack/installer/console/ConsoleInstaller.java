/*
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

import com.izforge.izpack.api.container.BindeableContainer;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Info;
import com.izforge.izpack.api.data.LocaleDatabase;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.api.data.ScriptParserConstant;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;
import com.izforge.izpack.installer.base.InstallerBase;
import com.izforge.izpack.installer.bootstrap.Installer;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.installer.language.ConditionCheck;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.file.FileUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Runs the console installer.
 *
 * @author Mounir el hajj
 * @author Tim Anderson
 */
public class ConsoleInstaller extends InstallerBase
{
    /**
     * The panel console factory.
     */
    private PanelConsoleFactory factory;

    /**
     * The installation data.
     */
    private AutomatedInstallData installData;

    /**
     * The rules engine.
     */
    private final RulesEngine rules;

    /**
     * Verifies the installation requirements.
     */
    private final ConditionCheck checkCondition;

    /**
     * The variable substituter.
     */
    private VariableSubstitutor variableSubstitutor;

    /**
     * The uninstallation data writer.
     */
    private UninstallDataWriter uninstallDataWriter;

    /**
     * The console.
     */
    private Console console;

    /**
     * Constructs a <tt>ConsoleInstaller</tt>
     *
     * @param container           the container
     * @param installData         the installation date
     * @param rules               the rules engine
     * @param resourceManager     the resource manager
     * @param checkCondition      the check to verify installation requirements
     * @param uninstallDataWriter the uninstallation data writer
     * @throws IzPackException for any IzPack error
     */
    public ConsoleInstaller(BindeableContainer container, AutomatedInstallData installData, RulesEngine rules,
                            ResourceManager resourceManager, ConditionCheck checkCondition,
                            UninstallDataWriter uninstallDataWriter)
    {
        super(resourceManager);
        factory = new PanelConsoleFactory(container);
        this.checkCondition = checkCondition;
        this.installData = installData;
        this.rules = rules;
        // Fallback: choose the first listed language pack if not specified via commandline
        if (installData.getLocaleISO3() == null)
        {
            installData.setLocaleISO3(resourceManager.getAvailableLangPacks().get(0));
        }

        InputStream in = resourceManager.getInputStream("langpacks/" + this.installData.getLocaleISO3() + ".xml");
        installData.setLangpack(new LocaleDatabase(in));
        installData.setVariable(ScriptParserConstant.ISO3_LANG, this.installData.getLocaleISO3());
        resourceManager.setLocale(installData.getLocaleISO3());
        if (!checkCondition.checkInstallerRequirements(this))
        {
            variableSubstitutor = new VariableSubstitutorImpl(installData.getVariables());
        }

        if (installData.getRules() == null)
        {
            installData.setRules(rules);
        }
        this.uninstallDataWriter = uninstallDataWriter;
        console = new Console();
    }

    /**
     * Determines if console installation is supported.
     *
     * @return <tt>true</tt> if there are {@link PanelConsole} implementations for each panel
     */
    public boolean canInstall()
    {
        boolean success = true;
        for (Panel panel : installData.getPanelsOrder())
        {
            if (factory.getClass(panel) == null)
            {
                success = false;
                Debug.log("No console implementation of panel: " + panel.getClassName());
            }
        }
        return success;
    }

    /**
     * Runs the installation.
     * <p/>
     * This method does not return - it invokes {@code System.exit(0)} on successful installation, or
     * {@code System.exit()} on failure.
     *
     * @param type the type of the action to perform
     * @param path the path to use for the action. May be <tt>null</tt>
     */
    public void run(int type, String path)
    {
        boolean success = false;
        ConsoleAction action = null;
        if (!canInstall())
        {
            console.println("Console installation is not supported by this installer");
            shutdown(false, false);
        }
        else
        {
            try
            {
                // TODO - InstallerBase shouldn't implement InstallerRequirementDisplay
                if (checkCondition.checkInstallerRequirements(this))
                {
                    action = createConsoleAction(type, path, console);
                    success = run(action);
                }
            }
            catch (Throwable exception)
            {
                success = false;
                Debug.error(exception);
            }
            finally
            {
                if (action != null && action.isInstall())
                {
                    shutdown(success, console);
                }
                else
                {
                    shutdown(success, false);
                }
            }
        }
    }

    public void setLangCode(String langCode)
    {
        installData.setLocaleISO3(langCode);
    }

    @Override
    public void showMissingRequirementMessage(String message)
    {
        Debug.log("Missing installer requirement: " + message);
        console.println(message);
    }

    /**
     * Shuts down the installer, rebooting if necessary.
     *
     * @param exitSuccess if <tt>true</tt>, exits with a <tt>0</tt> exit code, else exits with a <tt>1</tt> exit code
     * @param console     the console
     */
    protected void shutdown(boolean exitSuccess, Console console)
    {
        // TODO - fix reboot handling
        boolean reboot = false;
        if (installData.isRebootNecessary())
        {
            console.println("[ There are file operations pending after reboot ]");
            switch (installData.getInfo().getRebootAction())
            {
                case Info.REBOOT_ACTION_ALWAYS:
                    reboot = true;
            }
            if (reboot)
            {
                console.println("[ Rebooting now automatically ]");
            }
        }
        shutdown(exitSuccess, reboot);
    }

    /**
     * Shuts down the installer.
     *
     * @param exitSuccess if <tt>true</tt>, exits with a <tt>0</tt> exit code, else exits with a <tt>1</tt> exit code
     * @param reboot      if <tt>true</tt> perform a reboot
     */
    protected void shutdown(boolean exitSuccess, boolean reboot)
    {
        if (exitSuccess && !installData.isInstallSuccess())
        {
            Debug.error("Expected successful exit status, but installation data is reporting failure");
            exitSuccess = false;
        }
        installData.setInstallSuccess(exitSuccess);
        if (exitSuccess)
        {
            console.println("[ Console installation done ]");
        }
        else
        {
            console.println("[ Console installation FAILED! ]");
        }

        terminate(exitSuccess, reboot);
    }

    /**
     * Terminates the installation process.
     *
     * @param exitSuccess if <tt>true</tt>, exits with a <tt>0</tt> exit code, else exits with a <tt>1</tt> exit code
     * @param reboot      if <tt>true</tt> perform a reboot
     */
    protected void terminate(boolean exitSuccess, boolean reboot)
    {
        Housekeeper.getInstance().shutDown(exitSuccess ? 0 : 1, reboot);
    }

    /**
     * Runs a console action.
     *
     * @param action the action to run
     * @return <tt>true</tt> if the action was successful, otherwise <tt>false</tt>
     */
    protected boolean run(ConsoleAction action)
    {
        return action.run(console);
    }

    /**
     * Creates a new console action.
     *
     * @param type    the type of the action to perform
     * @param path    the path to use for the action. May be <tt>null</tt>
     * @param console the console
     * @return a new {@link ConsoleAction}
     * @throws IOException for any I/O error
     */
    private ConsoleAction createConsoleAction(int type, String path, Console console) throws IOException
    {
        ConsoleAction action = null;
        switch (type)
        {
            case Installer.CONSOLE_GEN_TEMPLATE:
                action = createGeneratePropertiesAction(path);
                break;

            case Installer.CONSOLE_FROM_TEMPLATE:
                action = createInstallFromPropertiesFileAction(path);
                break;

            case Installer.CONSOLE_FROM_SYSTEMPROPERTIES:
                break;

            case Installer.CONSOLE_FROM_SYSTEMPROPERTIESMERGE:
                action = createInstallFromSystemPropertiesMergeAction(path, console);
                break;

            default:
                action = createInstallAction();
        }
        return action;
    }

    /**
     * Creates a new action to perform installation.
     *
     * @return a new {@link ConsoleInstallAction}
     */
    private ConsoleAction createInstallAction()
    {
        return new ConsoleInstallAction(factory, installData, variableSubstitutor, rules, uninstallDataWriter);
    }

    /**
     * Creates a new action to generate installation properties.
     *
     * @param path the property file path
     * @return a new {@link GeneratePropertiesAction}
     * @throws IOException for any I/O error
     */
    private ConsoleAction createGeneratePropertiesAction(String path) throws IOException
    {
        return new GeneratePropertiesAction(factory, installData, variableSubstitutor, rules, path);
    }

    /**
     * Creates a new action to perform installation from a properties file.
     *
     * @param path the property file path
     * @return a new {@link PropertyInstallAction}
     * @throws IOException for any I/O error
     */
    private ConsoleAction createInstallFromPropertiesFileAction(String path) throws IOException
    {
        FileInputStream in = new FileInputStream(path);
        try
        {
            Properties properties = new Properties();
            properties.load(in);
            return new PropertyInstallAction(factory, installData, variableSubstitutor, rules,
                    uninstallDataWriter, properties);
        }
        finally
        {
            FileUtils.close(in);
        }
    }

    /**
     * Creates a new action to perform installation from a properties file.
     *
     * @param path    the property file path
     * @param console the console
     * @return a new {@link PropertyInstallAction}
     * @throws IOException for any I/O error
     */
    private ConsoleAction createInstallFromSystemPropertiesMergeAction(String path, Console console) throws IOException
    {
        FileInputStream in = new FileInputStream(path);
        try
        {
            Properties properties = new Properties();
            properties.load(in);
            Properties systemProperties = System.getProperties();
            Enumeration<?> e = systemProperties.propertyNames();
            while (e.hasMoreElements())
            {
                String key = (String) e.nextElement();
                String newValue = systemProperties.getProperty(key);
                String oldValue = (String) properties.setProperty(key, newValue);
                if (oldValue != null)
                {
                    console.println("Warning: Property " + key + " overwritten: '"
                            + oldValue + "' --> '" + newValue + "'");
                }
            }
            return new PropertyInstallAction(factory, installData, variableSubstitutor, rules,
                    uninstallDataWriter, properties);
        }
        finally
        {
            FileUtils.close(in);
        }
    }

}
