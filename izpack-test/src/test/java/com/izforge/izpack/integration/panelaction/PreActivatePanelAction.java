package com.izforge.izpack.integration.panelaction;

import com.izforge.izpack.api.data.AutomatedInstallData;


/**
 * Pre-activation panel action.
 *
 * @author Tim Anderson
 */
public class PreActivatePanelAction extends TestPanelAction
{
    /**
     * Constructs a <tt>PreActivatePanelAction</tt>.
     *
     * @param installData the installation data
     */
    public PreActivatePanelAction(AutomatedInstallData installData)
    {
        super(ActionStage.preactivate, installData);
    }

}
