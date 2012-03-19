package com.izforge.izpack.integration.panelaction;

import com.izforge.izpack.api.data.AutomatedInstallData;


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
     * @param installData the installation data
     */
    public PreConstructPanelAction(AutomatedInstallData installData)
    {
        super(ActionStage.preconstruct, installData);
    }

}
