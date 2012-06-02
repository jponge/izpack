package com.izforge.izpack.installer.container.provider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.picocontainer.injectors.Provider;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.installer.panel.Panels;
import com.izforge.izpack.util.OsConstraintHelper;


/**
 * Base class for {@link Panels} providers.
 *
 * @author Tim Anderson
 */
public abstract class PanelsProvider implements Provider
{

    /**
     * Prepares panels for the current platform.
     * <br/>
     * This adds XML to the {@link AutomatedInstallData#getXmlData() XML data} for each panel.
     *
     * @param installData the installation data
     * @return the panels for the current platform
     * @throws IzPackException if a panel doesn't have unique identifier
     */
    protected List<Panel> prepare(AutomatedInstallData installData)
    {
        List<Panel> result = new ArrayList<Panel>();
        Set<String> ids = new HashSet<String>();
        for (Panel panel : installData.getPanelsOrder())
        {
            if (OsConstraintHelper.oneMatchesCurrentSystem(panel.getOsConstraints()))
            {
                String panelId = panel.getPanelid();
                String key = (panelId != null) ? panelId : panel.getClassName();
                if (!ids.add(key))
                {
                    throw new IzPackException("Duplicate panel: " + key);
                }

                addPanelXml(panel, installData);

                result.add(panel);
            }
        }
        return result;
    }

    /**
     * Adds XML to the {@link AutomatedInstallData#getXmlData() XML data} for the supplied panel.
     *
     * @param panel       the panel
     * @param installData the installation data
     */
    protected void addPanelXml(Panel panel, AutomatedInstallData installData)
    {
        IXMLElement panelRoot = new XMLElementImpl(panel.getClassName(), installData.getXmlData());
        String panelId = panel.getPanelid();
        if (panelId != null)
        {
            panelRoot.setAttribute("id", panelId);
        }
        installData.getXmlData().addChild(panelRoot);
    }
}
