/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2007 Dennis Reil
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
package com.izforge.izpack.panels;

import com.izforge.izpack.data.ResourceManager;
import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.installer.base.InstallerFrame;
import com.izforge.izpack.installer.base.IzPanel;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.util.Debug;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 * @version $Id: $
 */
public class InstallationTypePanel extends IzPanel implements ActionListener {
    private JRadioButton normalinstall;
    private JRadioButton modifyinstall;

    public InstallationTypePanel(InstallerFrame parent, GUIInstallData idata, ResourceManager resourceManager) {
        super(parent, idata, new IzPanelLayout(), resourceManager);
        buildGUI();
    }

    private void buildGUI() {
        // We put our components

        add(LabelFactory.create(installData.getLangpack().getString("InstallationTypePanel.info"),
                parent.icons.getImageIcon("history"), LEADING), NEXT_LINE);


        ButtonGroup group = new ButtonGroup();

        boolean modifyinstallation = Boolean.valueOf(this.installData.getVariable(GUIInstallData.MODIFY_INSTALLATION));

        normalinstall = new JRadioButton(installData.getLangpack().getString("InstallationTypePanel.normal"), !modifyinstallation);
        normalinstall.addActionListener(this);
        group.add(normalinstall);
        add(normalinstall, NEXT_LINE);

        modifyinstall = new JRadioButton(installData.getLangpack().getString("InstallationTypePanel.modify"), modifyinstallation);
        modifyinstall.addActionListener(this);
        group.add(modifyinstall);
        add(modifyinstall, NEXT_LINE);

        setInitialFocus(normalinstall);
        getLayoutHelper().completeLayout();
    }

    /**
     *
     */
    private static final long serialVersionUID = -8178770882900584122L;

    /* (non-Javadoc)
    * @see com.izforge.izpack.installer.IzPanel#panelActivate()
    */

    public void panelActivate() {
        boolean modifyinstallation = Boolean.valueOf(this.installData.getVariable(GUIInstallData.MODIFY_INSTALLATION));
        if (modifyinstallation) {
            modifyinstall.setSelected(true);
        } else {
            normalinstall.setSelected(true);
        }
    }

    public void actionPerformed(ActionEvent e) {
        Debug.trace("installation type changed");
        if (e.getSource() == normalinstall) {
            Debug.trace("normal installation");
            this.installData.setVariable(GUIInstallData.MODIFY_INSTALLATION, "false");
        } else {
            Debug.trace("modification installation");
            this.installData.setVariable(GUIInstallData.MODIFY_INSTALLATION, "true");
        }
        /*
        if (normalinstall.isSelected()) {
            
        }
        else {
        } */

    }
}

