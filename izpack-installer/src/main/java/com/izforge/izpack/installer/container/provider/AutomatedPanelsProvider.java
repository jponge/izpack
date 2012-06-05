package com.izforge.izpack.installer.container.provider;

import java.util.ArrayList;
import java.util.List;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.installer.automation.AutomatedPanelView;
import com.izforge.izpack.installer.automation.AutomatedPanels;
import com.izforge.izpack.installer.automation.PanelAutomationHelper;


/**
 * Provider of {@link AutomatedPanels}.
 *
 * @author Tim Anderson
 */
public class AutomatedPanelsProvider extends PanelsProvider
{

    /**
     * Creates the panels.
     * <p/>
     * This invokes any pre-construction actions associated with them.
     *
     * @param factory     the factory
     * @param installData the installation data
     * @param helper      the helper
     * @throws IzPackException if a panel doesn't have unique identifier
     */
    public AutomatedPanels provide(ObjectFactory factory, AutomatedInstallData installData,
                                   PanelAutomationHelper helper)
    {
        List<AutomatedPanelView> panels = new ArrayList<AutomatedPanelView>();

        for (Panel panel : prepare(installData))
        {
            AutomatedPanelView panelView = new AutomatedPanelView(panel, factory, installData, helper);
            panels.add(panelView);
            panelView.setIndex(panels.size() - 1);
        }
        return new AutomatedPanels(panels, installData);
    }

}
