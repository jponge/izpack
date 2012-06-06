package com.izforge.izpack.integration.panelaction;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Panel;


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
     * @param panel       the panel
     * @param installData the installation data
     */
    public PreActivatePanelAction(Panel panel, InstallData installData)
    {
        super(panel, ActionStage.preactivate, installData);
    }

}
