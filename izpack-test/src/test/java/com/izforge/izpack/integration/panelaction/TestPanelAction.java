package com.izforge.izpack.integration.panelaction;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.PanelActionConfiguration;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.api.installer.DataValidator;
import com.izforge.izpack.data.PanelAction;
import com.izforge.izpack.integration.datavalidator.TestDataValidator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * Test {@link PanelAction} implementation.
 * <p/>
 * This also implements {@link DataValidator} to verify the sequence of events.
 *
 * @author Tim Anderson
 */
public abstract class TestPanelAction extends TestDataValidator implements PanelAction
{

    /**
     * The action stage.
     */
    private final ActionStage stage;


    /**
     * Constructs a <tt>TestPanelAction</tt>.
     *
     * @param stage       the action stage
     * @param installData the installation data
     */
    public TestPanelAction(ActionStage stage, AutomatedInstallData installData)
    {
        super(installData);
        this.stage = stage;
    }

    /**
     * Returns the no. of times the pre-construction action has been invoked for the specified panel.
     *
     * @param panelId     the panel identifier
     * @param installData the installation data
     * @return the no. of times the pre-construction action has been invoked
     */
    public static int getPreConstruct(String panelId, AutomatedInstallData installData)
    {
        return getValue(panelId + "." + ActionStage.preconstruct, installData);
    }

    /**
     * Returns the no. of times the pre-activation action has been invoked for the specified panel.
     *
     * @param panelId     the panel identifier
     * @param installData the installation data
     * @return the no. of times the pre-activation action has been invoked
     */
    public static int getPreActivate(String panelId, AutomatedInstallData installData)
    {
        return getValue(panelId + "." + ActionStage.preactivate, installData);
    }

    /**
     * Returns the no. of times the pre-validation action has been invoked for the specified panel.
     *
     * @param panelId     the panel identifier
     * @param installData the installation data
     * @return the no. of times the pre-validation action has been invoked
     */
    public static int getPreValidate(String panelId, AutomatedInstallData installData)
    {
        return getValue(panelId + "." + ActionStage.prevalidate, installData);
    }

    /**
     * Returns the no. of times the post-validation action has been invoked for the specified panel.
     *
     * @param panelId     the panel identifier
     * @param installData the installation data
     * @return the no. of times the post-validation action has been invoked
     */
    public static int getPostValidate(String panelId, AutomatedInstallData installData)
    {
        return getValue(panelId + "." + ActionStage.postvalidate, installData);
    }

    /**
     * Executes the action.
     * <p/>
     * This verifies that actions are invoked, and in the correct order.
     *
     * @param installData the installation data
     * @param handler     the UI handler. On a {@link ActionStage#preconstruct} action the handler is null because it
     *                    is not available until it is constructed. During an automated installation the handler is
     *                    null on each action because we have no GUI to handle.
     */
    @Override
    public void executeAction(AutomatedInstallData installData, AbstractUIHandler handler)
    {
        String id = getPanelId();
        if (stage == ActionStage.preconstruct)
        {
            assertNull(handler);
        }
        else
        {
            assertNotNull(handler);
        }
        String variable = id + "." + stage;
        int value = increment(variable);
        System.err.println("Incremented: " + variable + "=" + value + ", thread=" + Thread.currentThread().getName());

        int preConstruct = getPreConstruct();
        int preActivate = getPreActivate();
        int preValidate = getPreValidate();
        int validate = getValidate();
        int postValidate = getPostValidate();

        switch (stage)
        {
            case preconstruct:
                assertEquals(1, preConstruct);
                assertEquals(0, preActivate);
                assertEquals(0, preValidate);
                assertEquals(0, validate);
                assertEquals(0, postValidate);
                break;
            case preactivate:
                assertEquals(1, preConstruct);
                assertEquals(1, preActivate);
                assertEquals(0, preValidate);
                assertEquals(0, validate);
                assertEquals(0, postValidate);
                break;
            case prevalidate:
                assertEquals(1, preConstruct);
                assertEquals(1, preActivate);
                assertTrue(preValidate >= 1);
                assertEquals(preValidate - 1, validate);
                assertEquals(preValidate - 1, postValidate);
                break;
            case postvalidate:
                assertEquals(1, preConstruct);
                assertEquals(1, preActivate);
                assertTrue(preValidate >= 1);
                assertEquals(preValidate, validate);
                assertEquals(preValidate, postValidate);
                break;
            default:
                fail("Unsupported stage: " + stage);
        }
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
        Status status = super.validateData(installData);

        int preConstruct = getPreConstruct();
        int preActivate = getPreActivate();
        int preValidate = getPreValidate();
        int validate = getValidate();
        int postValidate = getPostValidate();

        assertEquals(1, preConstruct);
        assertEquals(1, preActivate);
        assertTrue(preValidate >= 1);
        assertEquals(preValidate, validate);
        assertEquals(preValidate - 1, postValidate);

        return status;
    }

    /**
     * Initializes the PanelAction with the given configuration.
     *
     * @param configuration the configuration. May be <tt>null</tt>
     */
    @Override
    public void initialize(PanelActionConfiguration configuration)
    {
    }

    /**
     * Returns the number of times the pre-construct action has been invoked.
     * \
     *
     * @return the number of times the action has been invoked
     */
    private int getPreConstruct()
    {
        return getValue(getPanelId() + "." + ActionStage.preconstruct);
    }

    /**
     * Returns the number of times the pre-activation action has been invoked.
     * \
     *
     * @return the number of times the action has been invoked
     */
    private int getPreActivate()
    {
        return getValue(getPanelId() + "." + ActionStage.preactivate);
    }

    /**
     * Returns the number of times the pre-validation action has been invoked.
     *
     * @return the number of times the action has been invoked
     */
    private int getPreValidate()
    {
        return getValue(getPanelId() + "." + ActionStage.prevalidate);
    }

    /**
     * Returns the number of times the post-validation action has been invoked.
     * \
     *
     * @return the number of times the action has been invoked
     */
    private int getPostValidate()
    {
        return getValue(getPanelId() + "." + ActionStage.postvalidate);
    }

}
