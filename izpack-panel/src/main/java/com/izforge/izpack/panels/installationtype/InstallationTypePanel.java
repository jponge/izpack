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

package com.izforge.izpack.panels.installationtype;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.base.InstallerFrame;
import com.izforge.izpack.installer.base.IzPanel;
import com.izforge.izpack.installer.data.GUIInstallData;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;


/**
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 * @version $Id: $
 */
public class InstallationTypePanel extends IzPanel implements ActionListener
{
    private static final long serialVersionUID = -8178770882900584122L;

    private static final transient Logger logger = Logger.getLogger(InstallationTypePanel.class.getName());

    private JRadioButton normalinstall;
    private JRadioButton modifyinstall;


    /**
     * Constructs an <tt>InstallationTypePanel</tt>.
     *
     * @param parent          the parent window
     * @param installData           the installation data
     * @param resourceManager the resource manager
     * @param log             the log
     */
    public InstallationTypePanel(InstallerFrame parent, GUIInstallData installData, ResourceManager resourceManager,
                                 Log log)
    {
        super(parent, installData, new IzPanelLayout(log), resourceManager);
        buildGUI();
    }

    private void buildGUI()
    {
        // We put our components

        add(LabelFactory.create(installData.getLangpack().getString("InstallationTypePanel.info"),
                parent.getIcons().get("history"), LEADING), NEXT_LINE);


        ButtonGroup group = new ButtonGroup();

        boolean modifyinstallation = Boolean.valueOf(this.installData.getVariable(AutomatedInstallData.MODIFY_INSTALLATION));

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

    /* (non-Javadoc)
    * @see com.izforge.izpack.installer.IzPanel#panelActivate()
    */
    @Override
    public void panelActivate()
    {
        boolean modifyinstallation = Boolean.valueOf(this.installData.getVariable(AutomatedInstallData.MODIFY_INSTALLATION));
        if (modifyinstallation)
        {
            modifyinstall.setSelected(true);
        }
        else
        {
            normalinstall.setSelected(true);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == normalinstall)
        {
            logger.fine("Installation type: Normal installation");
            this.installData.setVariable(AutomatedInstallData.MODIFY_INSTALLATION, "false");
        }
        else
        {
            logger.fine("Installation type: Modification installation");
            this.installData.setVariable(AutomatedInstallData.MODIFY_INSTALLATION, "true");
        }
    }
}

