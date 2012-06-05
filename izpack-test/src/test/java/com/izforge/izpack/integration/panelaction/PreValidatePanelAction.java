package com.izforge.izpack.integration.panelaction;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Panel;


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
     * @param panel       the panel
     * @param installData the installation data
     */
    public PreValidatePanelAction(Panel panel, AutomatedInstallData installData)
    {
        super(panel, ActionStage.prevalidate, installData);
    }

}
