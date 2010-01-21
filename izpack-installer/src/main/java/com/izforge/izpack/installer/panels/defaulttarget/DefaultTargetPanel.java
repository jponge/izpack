/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
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

package com.izforge.izpack.installer.panels.defaulttarget;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.installer.PanelAutomation;
import com.izforge.izpack.installer.base.InstallerFrame;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.panels.PathInputPanel;

/**
 * The taget directory selection panel.
 *
 * @author Julien Ponge
 * @author Jeff Gordon
 */
public class DefaultTargetPanel extends PathInputPanel {

    private static final long serialVersionUID = 3256443616359329170L;
    private PanelAutomation defaultTargetPanelAutomationHelper;

    /**
     * The constructor.
     *
     * @param defaultTargetPanelAutomationHelper
     *
     * @param parent The parent window.
     * @param idata  The installation installDataGUI.
     */
    public DefaultTargetPanel(InstallerFrame parent, GUIInstallData idata, ResourceManager resourceManager, DefaultTargetPanelAutomationHelper defaultTargetPanelAutomationHelper) {
        super(parent, idata, resourceManager);
        // load the default directory info (if present)
        loadDefaultInstallDir(parent);
        if (getDefaultInstallDir() != null) {
            // override the system default that uses app name (which is set in
            // the Installer class)
            idata.setInstallPath(getDefaultInstallDir());
        }
        this.defaultTargetPanelAutomationHelper = defaultTargetPanelAutomationHelper;
    }

    /**
     * Called when the panel becomes active.
     */
    public void panelActivate() {
        // Resolve the default for chosenPath
//        super.panelActivate();
        // Set the default or old value to the path selection panel.
        pathSelectionPanel.setPath(this.installData.getInstallPath());
        parent.skipPanel();
    }

    /**
     * This method simple delegates to <code>PathInputPanel.loadDefaultInstallDir</code> with the
     * current parent as installer frame.
     */
    public void loadDefaultDir() {
        super.loadDefaultInstallDir(parent);
    }

    /**
     * Indicates wether the panel has been validated or not.
     *
     * @return Whether the panel has been validated or not.
     */
    public boolean isValidated() {
        // Standard behavior of PathInputPanel.
        //if (!super.isValidated()) return (false);
        //installData.setInstallPath(pathSelectionPanel.getPath());
        return (true);
    }

    /**
     * Returns the default install directory. This is equal to
     * <code>PathInputPanel.getDefaultInstallDir</code>
     *
     * @return the default install directory
     */
    public String getDefaultDir() {
        return getDefaultInstallDir();
    }

    /**
     * Sets the default install directory to the given String. This is equal to
     * <code>PathInputPanel.setDefaultInstallDir</code>
     *
     * @param defaultDir path to be used for the install directory
     */
    public void setDefaultDir(String defaultDir) {
        setDefaultInstallDir(defaultDir);
    }

    /**
     * Asks to make the XML panel installDataGUI.
     *
     * @param panelRoot The tree to put the installDataGUI in.
     */
    public void makeXMLData(IXMLElement panelRoot) {
        defaultTargetPanelAutomationHelper.makeXMLData(this.installData, panelRoot);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.izforge.izpack.installer.IzPanel#getSummaryBody()
     */

    public String getSummaryBody() {
        return (this.installData.getInstallPath());
    }

}
