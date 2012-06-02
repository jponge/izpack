package com.izforge.izpack.installer.gui;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.factory.ObjectFactory;
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
     * @param variables   variables used to determine if the view can be displayed
     * @param installData the installation data
     */
    public IzPanelView(Panel panel, ObjectFactory factory, Variables variables, AutomatedInstallData installData)
    {
        super(panel, IzPanel.class, factory, variables, installData);
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
        view.setValidationService(getValidator());
        view.setHelpUrl(panel.getHelpUrl(installData.getLocaleISO3()));
    }
}
