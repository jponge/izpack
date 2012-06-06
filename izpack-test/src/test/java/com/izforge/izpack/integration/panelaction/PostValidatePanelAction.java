package com.izforge.izpack.integration.panelaction;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Panel;


/**
 * Post-validation panel action.
 *
 * @author Tim Anderson
 */
public class PostValidatePanelAction extends TestPanelAction
{
    /**
     * Constructs a <tt>PostValidatePanelAction</tt>.
     *
     * @param panel       the panel
     * @param installData the installation data
     */
    public PostValidatePanelAction(Panel panel, InstallData installData)
    {
        super(panel, ActionStage.postvalidate, installData);
    }

}
