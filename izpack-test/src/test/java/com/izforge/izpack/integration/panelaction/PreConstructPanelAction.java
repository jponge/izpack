package com.izforge.izpack.integration.panelaction;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Panel;


/**
 * Pre-construction panel action.
 *
 * @author Tim Anderson
 */
public class PreConstructPanelAction extends TestPanelAction
{
    /**
     * Constructs a <tt>PreConstructPanelAction</tt>.
     *
     * @param panel       the panel
     * @param installData the installation data
     */
    public PreConstructPanelAction(Panel panel, AutomatedInstallData installData)
    {
        super(panel, ActionStage.preconstruct, installData);
    }

}
