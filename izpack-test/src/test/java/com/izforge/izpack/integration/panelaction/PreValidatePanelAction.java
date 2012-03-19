package com.izforge.izpack.integration.panelaction;

import com.izforge.izpack.api.data.AutomatedInstallData;


/**
 * Pre-validation panel action.
 *
 * @author Tim Anderson
 */
public class PreValidatePanelAction extends TestPanelAction
{
    /**
     * Constructs a <tt>PreValidatePanelAction</tt>.
     *
     * @param installData the installation data
     */
    public PreValidatePanelAction(AutomatedInstallData installData)
    {
        super(ActionStage.prevalidate, installData);
    }

}
