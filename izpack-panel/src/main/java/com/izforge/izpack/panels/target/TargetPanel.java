/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2004 Klaus Bartz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.panels.target;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.installer.base.InstallerFrame;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.panels.path.PathInputPanel;

/**
 * The taget directory selection panel.
 *
 * @author Julien Ponge
 */
public class TargetPanel extends PathInputPanel
{

    /**
     *
     */
    private static final long serialVersionUID = 3256443616359429170L;

    /**
     * The constructor.
     *
     * @param parent The parent window.
     * @param idata  The installation installDataGUI.
     */
    public TargetPanel(InstallerFrame parent, GUIInstallData idata, ResourceManager resourceManager)
    {
        super(parent, idata, resourceManager);
    }

    /**
     * Called when the panel becomes active.
     */
    public void panelActivate()
    {
        // Resolve the default for chosenPath
        super.panelActivate();

        loadDefaultInstallDir();
        if (getDefaultInstallDir() != null)
        {
            pathSelectionPanel.setPath(getDefaultInstallDir());
        }
    }

    /**
     * Indicates wether the panel has been validated or not.
     *
     * @return Wether the panel has been validated or not.
     */
    public boolean isValidated()
    {
        // Standard behavior of PathInputPanel.
        if (!super.isValidated())
        {
            return (false);
        }
        this.installData.setInstallPath(pathSelectionPanel.getPath());
        return (true);
    }

    /**
     * Returns the default install directory. This is equal to
     * <code>PathInputPanel.getDefaultInstallDir</code>
     *
     * @return the default install directory
     */
    public String getDefaultDir()
    {
        return getDefaultInstallDir();
    }

    /**
     * Sets the default install directory to the given String. This is equal to
     * <code>PathInputPanel.setDefaultInstallDir</code>
     *
     * @param defaultDir path to be used for the install directory
     */
    public void setDefaultDir(String defaultDir)
    {
        setDefaultInstallDir(defaultDir);
    }

    /**
     * Asks to make the XML panel installDataGUI.
     *
     * @param panelRoot The tree to put the installDataGUI in.
     */
    public void makeXMLData(IXMLElement panelRoot)
    {
        new TargetPanelAutomationHelper(variableSubstitutor).makeXMLData(this.installData, panelRoot);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.izforge.izpack.installer.IzPanel#getSummaryBody()
     */

    public String getSummaryBody()
    {
        return (this.installData.getInstallPath());
    }

}
