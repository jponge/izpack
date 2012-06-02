package com.izforge.izpack.installer.container.provider;

import java.util.ArrayList;
import java.util.List;

import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.installer.container.impl.InstallerContainer;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.IzPanel;
import com.izforge.izpack.installer.gui.IzPanelView;
import com.izforge.izpack.installer.gui.IzPanels;
import com.izforge.izpack.installer.panel.PanelView;


/**
 * Provider of {@link IzPanels}.
 *
 * @author Tim Anderson
 */
public class IzPanelsProvider extends PanelsProvider
{

    /**
     * Creates the panels.
     * <p/>
     * This invokes any pre-construction actions associated with them.
     *
     * @throws IzPackException if a panel doesn't have unique identifier
     */
    public IzPanels provide(ObjectFactory factory, InstallerContainer container, GUIInstallData installData)
    {
        List<PanelView<IzPanel>> panels = new ArrayList<PanelView<IzPanel>>();

        for (Panel panel : prepare(installData))
        {
            PanelView<IzPanel> panelView = new IzPanelView(panel, factory, installData.getVariables(), installData);
            panels.add(panelView);
            panelView.setIndex(panels.size() - 1);
        }
        return new IzPanels(panels, container, installData);
    }

}
