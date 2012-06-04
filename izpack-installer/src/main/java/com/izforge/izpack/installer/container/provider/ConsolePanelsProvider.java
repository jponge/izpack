package com.izforge.izpack.installer.container.provider;

import java.util.ArrayList;
import java.util.List;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.installer.console.ConsolePanelView;
import com.izforge.izpack.installer.console.ConsolePanels;
import com.izforge.izpack.util.Console;


/**
 * Provider of {@link ConsolePanels}.
 *
 * @author Tim Anderson
 */
public class ConsolePanelsProvider extends PanelsProvider
{

    /**
     * Creates the panels.
     * <p/>
     * This invokes any pre-construction actions associated with them.
     *
     * @param factory     the factory
     * @param installData the installation data
     * @param console     the console
     * @throws IzPackException if a panel doesn't have unique identifier
     */
    public ConsolePanels provide(ObjectFactory factory, AutomatedInstallData installData, Console console)
    {
        List<ConsolePanelView> panels = new ArrayList<ConsolePanelView>();

        for (Panel panel : prepare(installData))
        {
            ConsolePanelView panelView = new ConsolePanelView(panel, factory, installData, console);
            panels.add(panelView);
            panelView.setIndex(panels.size() - 1);
        }
        return new ConsolePanels(panels, installData.getVariables());
    }

}
