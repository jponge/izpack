package com.izforge.izpack.installer.gui;

import java.awt.Component;
import java.awt.Cursor;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.installer.panel.PanelView;

/**
 * Implementation of {@link PanelView} for {@link IzPanel}s.
 *
 * @author Tim Anderson
 */
public class IzPanelView extends PanelView<IzPanel>
{
    /**
     * Constructs a {@code IzPanelView}.
     *
     * @param panel       the panel
     * @param factory     the factory for creating the view
     * @param installData the installation data
     */
    public IzPanelView(Panel panel, ObjectFactory factory, AutomatedInstallData installData)
    {
        super(panel, IzPanel.class, factory, installData);
    }

    /**
     * Determines if the panel is valid.
     *
     * @return {@code true} if the panel is valid
     */
    @Override
    public boolean isValid()
    {
        return getView().panelValidated() && super.isValid();
    }

    /**
     * Returns a handler to prompt the user.
     *
     * @return the handler
     */
    @Override
    protected AbstractUIHandler getHandler()
    {
        return getView();
    }

    /**
     * Initialises the view.
     *
     * @param view        the view to initialise
     * @param panel       the panel the view represents
     * @param installData the installation data
     */
    @Override
    protected void initialise(IzPanel view, Panel panel, AutomatedInstallData installData)
    {
        setVisible(!view.isHidden());
        view.setHelpUrl(panel.getHelpUrl(installData.getLocaleISO3()));
    }

    /**
     * Validates dynamic conditions.
     * <br/>
     * This implementation sets a busy cursor while evaluating conditions.
     *
     * @return {@code true} if there are no conditions, or conditions validate successfully
     */
    @Override
    protected boolean validateDynamicConditions()
    {
        Component component = getView().getTopLevelAncestor();
        Cursor current = component.getCursor();
        Cursor wait = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
        try
        {
            component.setCursor(wait);
            return super.validateDynamicConditions();
        }
        finally
        {
            component.setCursor(current);
        }
    }

    /**
     * Evaluates the panel data validator.
     * <br/>
     * This implementation sets a busy cursor while evaluating conditions.
     *
     * @return {@code true} if the validator evaluated successfully, or with a warning that the user chose to skip;
     *         otherwise {@code false}
     */
    @Override
    protected boolean validateData()
    {
        Component component = getView().getTopLevelAncestor();
        Cursor current = component.getCursor();
        Cursor wait = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
        try
        {
            component.setCursor(wait);
            return super.validateData();
        }
        finally
        {
            component.setCursor(current);
        }
    }
}
