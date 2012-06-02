package com.izforge.izpack.installer.panel;

import java.util.ArrayList;
import java.util.List;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.api.installer.DataValidator;
import com.izforge.izpack.data.PanelAction;


/**
 * Encapsulates a {@link Panel} and its user-interface representation.
 *
 * @author Tim Anderson
 */
public class PanelView<T>
{

    /**
     * The panel.
     */
    private final Panel panel;

    /**
     * The panel user interface class.
     */
    private final Class<T> viewClass;

    /**
     * The factory for creating the view.
     */
    private final ObjectFactory factory;

    /**
     * Variables used to determine if the view can be displayed.
     */
    private final Variables variables;

    /**
     * The panel index.
     */
    private int index;

    /**
     * The panel user interface.
     */
    private T view;

    /**
     * Determines if the user interface is visible.
     */
    private boolean visible = true;

    /**
     * The data validator. May be {@code null}.
     */
    private DataValidator validator;

    /**
     * The installation data.
     */
    private final AutomatedInstallData installData;

    /**
     * Actions to invoke prior to the panel being displayed.
     */
    private final List<PanelAction> preActivationActions = new ArrayList<PanelAction>();

    /**
     * Actions to invoke prior to the panel being validated.
     */
    private final List<PanelAction> preValidationActions = new ArrayList<PanelAction>();

    /**
     * Actions to invoke after the panel being validated.
     */
    private final List<PanelAction> postValidationActions = new ArrayList<PanelAction>();

    /**
     * Constructs a {@code PanelView}.
     *
     * @param panel       the panel
     * @param viewClass   the panel user interface class
     * @param factory     the factory for creating the view
     * @param variables   variables used to determine if the view can be displayed
     * @param installData the installation data
     */
    public PanelView(Panel panel, Class<T> viewClass, ObjectFactory factory, Variables variables,
                     AutomatedInstallData installData)
    {
        this.panel = panel;
        this.viewClass = viewClass;
        this.factory = factory;
        this.variables = variables;
        this.installData = installData;
    }

    /**
     * Returns the panel identifier.
     *
     * @return the panel identifier
     */
    public String getPanelId()
    {
        return panel.getPanelid();
    }

    /**
     * Returns the panel.
     *
     * @return the panel
     */
    public Panel getPanel()
    {
        return panel;
    }

    /**
     * Returns the panel index.
     * <br/>
     * This is the offset of the panel relative to the other panels, visible or not.
     *
     * @return the panel index.
     */
    public int getIndex()
    {
        return index;
    }

    /**
     * Sets the panel index.
     *
     * @param index the index
     */
    public void setIndex(int index)
    {
        this.index = index;
    }

    /**
     * Returns the panel user interface.
     *
     * @return the panel user interface
     */
    public T getView()
    {
        if (view == null)
        {
            executePreConstructionActions();
            view = factory.create(panel.getClassName(), viewClass, panel);
            String dataValidator = panel.getValidator();
            if (dataValidator != null)
            {
                validator = factory.create(dataValidator, DataValidator.class);
            }

            addActions(panel.getPreActivationActions(), preActivationActions);
            addActions(panel.getPreValidationActions(), preValidationActions);
            addActions(panel.getPostValidationActions(), postValidationActions);

            initialise(view, panel, installData);
        }
        return view;
    }


    /**
     * Returns the panel validator.
     *
     * @return the panel validator, or {@code null} if there is none
     */
    public DataValidator getValidator()
    {
        return validator;
    }

    /**
     * Sets the visibility of the panel.
     *
     * @param visible if {@code true} the panel is visible, otherwise it is hidden
     */
    public void setVisible(boolean visible)
    {
        this.visible = visible;
    }

    /**
     * Determines the visibility of the panel.
     *
     * @return {@code true} if the panel is visible, {@code false} if it is hidden
     */
    public boolean isVisible()
    {
        return visible;
    }

    /**
     * Determines if the panel can be shown.
     *
     * @return {@code true} if the panel can be shown
     */
    public boolean canShow()
    {
        boolean result;
        String panelId = panel.getPanelid();
        variables.refresh();
        if (panel.hasCondition())
        {
            result = installData.getRules().isConditionTrue(panel.getCondition());
        }
        else
        {
            result = installData.getRules().canShowPanel(panelId, installData.getVariables());
        }
        return result;
    }

    /**
     * Executes actions prior to activating the panel.
     *
     * @param handler the handler to notify
     */
    public void executePreActivationActions(AbstractUIHandler handler)
    {
        execute(preActivationActions, handler);
    }

    /**
     * Executes actions prior to validating the panel.
     *
     * @param handler the handler to notify
     */
    public void executePreValidationActions(AbstractUIHandler handler)
    {
        execute(preValidationActions, handler);
    }

    /**
     * Executes actions after validating the panel.
     *
     * @param handler the handler to notify
     */
    public void executePostValidationActions(AbstractUIHandler handler)
    {
        execute(postValidationActions, handler);
    }


    /**
     * Initialises the view.
     * <br/>
     * This implementation is a no-op
     *
     * @param view        the view to initialise
     * @param panel       the panel the view represents
     * @param installData the installation data
     */
    protected void initialise(T view, Panel panel, AutomatedInstallData installData)
    {

    }

    /**
     * Executes actions.
     *
     * @param actions the actions to execute
     * @param handler the handler to notify
     */
    private void execute(List<PanelAction> actions, AbstractUIHandler handler)
    {
        for (PanelAction action : actions)
        {
            action.executeAction(installData, handler);
        }
    }

    /**
     * Executes actions prior to creating the panel.
     */
    private void executePreConstructionActions()
    {
        List<String> classNames = panel.getPreConstructionActions();
        if (classNames != null)
        {
            for (String className : classNames)
            {
                PanelAction action = factory.create(className, PanelAction.class);
                action.initialize(panel.getPanelActionConfiguration(className));
                action.executeAction(installData, null);
            }
        }
    }

    /**
     * Creates {@link PanelAction}s, adding them to the supplied list.
     *
     * @param classNames the action class names. May be {@code null}
     * @param actions    the actions to add to
     */
    private void addActions(List<String> classNames, List<PanelAction> actions)
    {
        if (classNames != null)
        {
            for (String className : classNames)
            {
                PanelAction action = factory.create(className, PanelAction.class);
                action.initialize(panel.getPanelActionConfiguration(className));
                actions.add(action);
            }
        }
    }

}