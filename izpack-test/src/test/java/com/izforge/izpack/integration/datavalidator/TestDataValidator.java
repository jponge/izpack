package com.izforge.izpack.integration.datavalidator;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.installer.DataValidator;


/**
 * Test {@link DataValidator} implementation.
 * <p/>
 * This enables the result of the validation for a panel to be determined by specifying a variable named
 * <em>&lt;panelId&gt;.status</em>. E.g.:
 * <pre>
 * installData.setVariable("HelloPanel.status", "ERROR");
 * </pre>
 * A counter <em>&lt;panelId&gt;.validate</em> is incremented each time the validator is called.
 *
 * @author Tim Anderson
 */
public class TestDataValidator implements DataValidator
{

    /**
     * The panel.
     */
    private final Panel panel;

    /**
     * The installation data.
     */
    private final AutomatedInstallData installData;


    /**
     * Constructs a <tt>TestDataValidator</tt>.
     *
     * @param panel       the panel
     * @param installData the installation data
     */
    public TestDataValidator(Panel panel, AutomatedInstallData installData)
    {
        this.installData = installData;
        this.panel = panel;
    }

    /**
     * Returns the no. of times validation has been invoked for the specified panel.
     *
     * @param panelId     the panel identifier
     * @param installData the installation data
     * @return the no. of times validation has been invoked
     */
    public static int getValidate(String panelId, AutomatedInstallData installData)
    {
        return getValue(panelId + ".validate", installData);
    }

    /**
     * Method to validate {@link AutomatedInstallData}.
     *
     * @param installData the installation data
     * @return the result of the validation
     */
    @Override
    public Status validateData(AutomatedInstallData installData)
    {
        String id = getPanelId();
        String status = installData.getVariable(id + ".status");
        increment(id + ".validate");
        return (status != null) ? Status.valueOf(status) : Status.ERROR;
    }

    /**
     * Returns the messageId for an error.
     *
     * @return the messageId
     */
    @Override
    public String getErrorMessageId()
    {
        return getPanelId() + ".error";
    }

    /**
     * Returns the messageId for a warning.
     *
     * @return the messageId
     */
    @Override
    public String getWarningMessageId()
    {
        return getPanelId() + ".warning";
    }

    /**
     * Determines how the installer responds to a warning when running automated.
     *
     * @return boolean
     */
    @Override
    public boolean getDefaultAnswer()
    {
        return false;
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

    /**
     * Returns the panel id of the current panel.
     *
     * @return the panel id
     */
    protected String getPanelId()
    {
        return panel.getPanelid();
    }

    /**
     * Returns the no. of times validation has been invoked for the current panel.
     *
     * @return the no. of invocations
     */
    protected int getValidate()
    {
        return getValue(getPanelId() + ".validate");
    }

    /**
     * Returns the value of a numeric variable.
     *
     * @param variable the variable name
     * @return the variable value
     */
    protected int getValue(String variable)
    {
        return getValue(variable, installData);
    }

    /**
     * Returns the value of a numeric variable.
     *
     * @param variable    the variable name
     * @param installData the installation data
     * @return the variable value
     */
    protected static int getValue(String variable, AutomatedInstallData installData)
    {
        String value = installData.getVariable(variable);
        if (value == null)
        {
            value = "0";
        }
        return Integer.valueOf(value);
    }

    /**
     * Increments a variable.
     *
     * @param variable the variable to increment
     * @return the new value
     */
    protected int increment(String variable)
    {
        int value = getValue(variable);
        value++;
        installData.setVariable(variable, Integer.toString(value));
        return value;
    }

}
