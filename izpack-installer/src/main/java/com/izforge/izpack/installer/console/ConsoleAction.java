package com.izforge.izpack.installer.console;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.DynamicInstallerRequirementValidator;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.installer.DataValidator;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;
import com.izforge.izpack.installer.base.InstallerBase;
import com.izforge.izpack.installer.manager.DataValidatorFactory;
import com.izforge.izpack.util.Console;
import com.izforge.izpack.util.OsConstraintHelper;

/**
 * Console installer action.
 *
 * @author Tim Anderson
 */
abstract class ConsoleAction
{
    private static final Logger logger = Logger.getLogger(ConsoleAction.class.getName());

    /**
     * The panel console factory.
     */
    private final PanelConsoleFactory factory;

    /**
     * The installation data.
     */
    private final AutomatedInstallData installData;

    /**
     * The variable substituter.
     */
    private final VariableSubstitutor substituter;

    /**
     * The rules engine.
     */
    private final RulesEngine rules;


    /**
     * Constructs a <tt>ConsoleAction</tt>.
     *
     * @param factory     the factory for PanelConsole instances
     * @param installData the installation data
     * @param substituter the variable substituter
     * @param rules       the rules engine
     */
    public ConsoleAction(PanelConsoleFactory factory, AutomatedInstallData installData,
                         VariableSubstitutor substituter, RulesEngine rules)
    {
        this.factory = factory;
        this.installData = installData;
        this.substituter = substituter;
        this.rules = rules;
    }

    /**
     * Runs the action for each panel.
     *
     * @param console the console
     * @return <tt>true</tt> if the action was successful, otherwise <tt>false</tt>
     */
    public boolean run(final Console console)
    {
        boolean result = false;
        try
        {
            installData.setCurPanelNumber(-1);
            for (Panel panel : installData.getPanelsOrder())
            {
                installData.setCurPanelNumber(installData.getCurPanelNumber() + 1);
                if (canShow(panel))
                {
                    result = run(panel, console);
                    if (!result)
                    {
                        break;
                    }
                }

                InstallerBase.refreshDynamicVariables(installData,
                        new VariableSubstitutorImpl(installData.getVariables()));
            }
        }
        catch (Throwable t)
        {
            result = false;
            logger.log(Level.SEVERE, t.getMessage(), t);
        }
        if (!result && isInstall())
        {
           installData.setInstallSuccess(false);
        }
        return result;
    }

    /**
     * Determines if this is an installation action.
     * <p/>
     * An installation action is any action that performs installation. Installation actions need to be distinguished
     * from other actions as they may subsequently require a reboot.
     * <p/>
     * This default implementation always returns  <tt>true</tt>.
     *
     * @return <tt>true</tt>
     */
    public boolean isInstall()
    {
        return true;
    }

    /**
     * Runs the action for the console panel associated with the specified panel.
     *
     * @param panel   the panel
     * @param console the console
     * @return <tt>true</tt> if the action was successful, otherwise <tt>false</tt>
     */
    protected boolean run(Panel panel, Console console)
    {
        boolean result;

        try
        {
            PanelConsole action = factory.create(panel);
            logger.info("Running panel " + panel.getClassName());
            result = run(panel, action, console);
        }
        catch (Exception e)
        {
            logger.log(Level.SEVERE, "Console installation failed for panel " + panel.getClassName(), e);
            result = false;
        }
        if (result)
        {
            logger.info("Panel: " + panel.getClassName() + " completed successfully");
        }
        return result;
    }


    /**
     * Runs the action for the console panel associated with the specified panel.
     *
     * @param panel        the panel
     * @param panelConsole the console implementation of the panel
     * @param console      the console
     * @return <tt>true</tt> if the action was successful, otherwise <tt>false</tt>
     * @throws InstallerException for any installer error
     */
    protected abstract boolean run(Panel panel, PanelConsole panelConsole, Console console)
            throws InstallerException;

    /**
     * Method checks whether conditions are met to show the given panel.
     *
     * @param panel the panel to check
     * @return true or false
     */
    protected boolean canShow(Panel panel)
    {
        String id = panel.getPanelid();
        boolean result = OsConstraintHelper.oneMatchesCurrentSystem(panel.getOsConstraints());
        if (result)
        {
            if (panel.hasCondition())
            {
                result = rules.isConditionTrue(panel.getCondition());
            }
            else
            {
                result = rules.canShowPanel(id, installData.getVariables());
            }
        }
        if (!result)
        {
            // skip panel, if conditions for panel aren't met
            logger.fine("Skip panel with panelid=" + id);
            // panel should be skipped, so we have to decrement panelnumber for skipping
        }
        else
        {
            logger.fine("Showing panel with panelid=" + id);
        }
        return result;
    }

    /**
     * Validates a panel.
     *
     * @param panel   the panel to validate
     * @param console the console
     * @return <tt>true</tt> if the panel is valid, otherwise <tt>false</tt>
     * @throws InstallerException if the panel cannot be validated
     */
    protected boolean validatePanel(Panel panel, Console console) throws InstallerException
    {
        try
        {
            InstallerBase.refreshDynamicVariables(installData,
                    new VariableSubstitutorImpl(installData.getVariables()));
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
                DataValidator.Status validationResult = validator.validateData(installData);
                if (validationResult != DataValidator.Status.OK)
                {
                    String errorMessage;
                    try
                    {
                        errorMessage = installData.getLangpack().getString("data.validation.error.title")
                                + ": " + substituter.substitute(installData.getLangpack().getString(validator
                                .getErrorMessageId()));
                    }
                    catch (Exception e)
                    {
                        throw new InstallerException(e);
                    }
                    // if defaultAnswer is true, result is ok
                    if (validationResult == DataValidator.Status.WARNING && validator.getDefaultAnswer())
                    {
                        console.println(errorMessage + " - ignoring");
                    }
                    // make installation fail instantly
                    return false;
                }
            }
        }

        // Evaluate panel condition
        String dataValidator = panel.getValidator();
        if (dataValidator != null)
        {
            DataValidator validator = DataValidatorFactory.createDataValidator(dataValidator);
            DataValidator.Status validationResult = validator.validateData(installData);
            if (validationResult != DataValidator.Status.OK)
            {
                // if defaultAnswer is true, result is ok
                if (validationResult == DataValidator.Status.WARNING && validator.getDefaultAnswer())
                {
                    console.println("Configuration said, it's ok to go on, if validation is not successfull");
                }
                else
                {
                    // make installation fail instantly
                    console.println("Validation failed, please verify your input");
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns the installation data.
     *
     * @return the installation data
     */
    protected AutomatedInstallData getInstallData()
    {
        return installData;
    }

}
