package com.izforge.izpack.integration.panelaction;

import com.izforge.izpack.api.data.AutomatedInstallData;


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
     * @param installData the installation data
     */
    public PostValidatePanelAction(AutomatedInstallData installData)
    {
        super(ActionStage.postvalidate, installData);
    }

}
