package com.izforge.izpack.installer.automation;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.installer.panel.AbstractPanels;
import com.izforge.izpack.installer.panel.Panels;


/**
 * Implementation of {@link Panels} for {@link AutomatedPanelView}.
 *
 * @author Tim Anderson
 */
public class AutomatedPanels extends AbstractPanels<AutomatedPanelView>
{

    /**
     * The installation data.
     */
    private final AutomatedInstallData installData;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(AutomatedPanels.class.getName());


    /**
     * Constructs an {@code AutomatedPanels}.
     *
     * @param panels      the panels
     * @param installData the installation data
     */
    public AutomatedPanels(List<AutomatedPanelView> panels, AutomatedInstallData installData)
    {
        super(panels, installData.getVariables());
        this.installData = installData;
    }

    /**
     * Switches panels.
     *
     * @param newPanel the panel to switch to
     * @param oldPanel the panel to switch from, or {@code null} if there was no prior panel
     * @return {@code true} if the switch was successful
     */
    @Override
    protected boolean switchPanel(AutomatedPanelView newPanel, AutomatedPanelView oldPanel)
    {
        boolean result;
        if (newPanel.getViewClass() == null)
        {
            // panel has no view. This is apparently OK - not all panels have/need automation support.
            logger.warning("AutomationHelper class not found for panel: " + newPanel.getPanel().getClassName());
            result = executeValidationActions(newPanel, true);
        }
        else
        {
            newPanel.executePreActivationActions();
            PanelAutomation view = newPanel.getView();
            IXMLElement xml = getPanelXML(newPanel);
            if (xml != null)
            {
                view.runAutomated(installData, xml);
                result = true;
            }
            else
            {
                logger.log(Level.SEVERE, "No configuration for panel: " + newPanel.getPanel().getClassName());
                result = false;
            }
        }
        return result;
    }

    /**
     * Returns the XML configuration for a panel.
     *
     * @param panel the panel
     * @return the panel's XML configuration, or {@code null} if it cannot be found
     */
    private IXMLElement getPanelXML(AutomatedPanelView panel)
    {
        IXMLElement result = null;
        String className = panel.getPanel().getClassName();
        List<IXMLElement> panelRoots = installData.getXmlData().getChildrenNamed(className);
        if (!panelRoots.isEmpty())
        {
            int index = 0;
            for (AutomatedPanelView panelView : getPanels())
            {
                Panel p = panelView.getPanel();
                if (panel.getPanel().equals(p))
                {
                    break;
                }
                if (p.getClassName().equals(className))
                {
                    ++index;
                }
            }
            if (index < panelRoots.size())
            {
                result = panelRoots.get(index);
            }
        }
        return result;
    }

}
