/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
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

package com.izforge.izpack.panels.finish;

import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.gui.AutomatedInstallScriptFilter;
import com.izforge.izpack.gui.ButtonFactory;
import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.base.GuiId;
import com.izforge.izpack.installer.base.InstallerFrame;
import com.izforge.izpack.installer.base.IzPanel;
import com.izforge.izpack.installer.data.GUIInstallData;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

/**
 * The finish panel class.
 *
 * @author Julien Ponge
 */
public class FinishPanel extends IzPanel implements ActionListener {

    private static final long serialVersionUID = 3257282535107998009L;

    /**
     * The automated installers generation button.
     */
    protected JButton autoButton;

    /**
     * The constructor.
     *
     * @param parent The parent.
     * @param idata  The installation installDataGUI.
     */
    public FinishPanel(InstallerFrame parent, GUIInstallData idata, ResourceManager resourceManager) {
        super(parent, idata, new IzPanelLayout(), resourceManager);
    }

    /**
     * Indicates wether the panel has been validated or not.
     *
     * @return true if the panel has been validated.
     */
    public boolean isValidated() {
        return true;
    }

    /**
     * Called when the panel becomes active.
     */
    public void panelActivate() {
        parent.lockNextButton();
        parent.lockPrevButton();
        parent.setQuitButtonText(installData.getLangpack().getString("FinishPanel.done"));
        parent.setQuitButtonIcon("done");
        if (this.installData.isInstallSuccess()) {
            // We set the information
            add(LabelFactory.create(installData.getLangpack().getString("FinishPanel.success"),
                    parent.icons.getImageIcon("preferences"), LEADING), NEXT_LINE);
            add(IzPanelLayout.createVerticalStrut(5));
            if (this.installData.getUninstallOutJar() != null) {
                // We prepare a message for the uninstaller feature
                String path = translatePath("$INSTALL_PATH") + File.separator + "Uninstaller";

                add(LabelFactory.create(installData.getLangpack()
                        .getString("FinishPanel.uninst.info"), parent.icons
                        .getImageIcon("preferences"), LEADING), NEXT_LINE);
                add(LabelFactory.create(path, parent.icons.getImageIcon("empty"),
                        LEADING), NEXT_LINE);
            }

            // We add the autoButton
            add(IzPanelLayout.createVerticalStrut(5));
            autoButton = ButtonFactory.createButton(installData.getLangpack().getString("FinishPanel.auto"),
                    parent.icons.getImageIcon("edit"), this.installData.buttonsHColor);
            autoButton.setName(GuiId.FINISH_PANEL_AUTO_BUTTON.id);
            autoButton.setToolTipText(installData.getLangpack().getString("FinishPanel.auto.tip"));
            autoButton.addActionListener(this);
            add(autoButton, NEXT_LINE);
        } else {
            add(LabelFactory.create(installData.getLangpack().getString("FinishPanel.fail"),
                    parent.icons.getImageIcon("stop"), LEADING), NEXT_LINE);
        }
        getLayoutHelper().completeLayout(); // Call, or call not?
        Log.getInstance().informUser();
    }

    /**
     * Actions-handling method.
     *
     * @param e The event.
     */
    public void actionPerformed(ActionEvent e) {
        // Prepares the file chooser
        JFileChooser fc = new JFileChooser();
        fc.setName(GuiId.FINISH_PANEL_FILE_CHOOSER.id);
        fc.setCurrentDirectory(new File(this.installData.getInstallPath()));
        fc.setMultiSelectionEnabled(false);
        fc.addChoosableFileFilter(new AutomatedInstallScriptFilter());
        // fc.setCurrentDirectory(new File("."));

        // Shows it
        try {
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                // We handle the xml installDataGUI writing
                File file = fc.getSelectedFile();
                FileOutputStream out = new FileOutputStream(file);
                BufferedOutputStream outBuff = new BufferedOutputStream(out, 5120);
                parent.writeXMLTree(this.installData.getXmlData(), outBuff);
                outBuff.flush();
                outBuff.close();

                autoButton.setEnabled(false);
            }
        }
        catch (Exception err) {
            err.printStackTrace();
            JOptionPane.showMessageDialog(this, err.toString(), installData.getLangpack()
                    .getString("installer.error"), JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Translates a relative path to a local system path.
     *
     * @param destination The path to translate.
     * @return The translated path.
     */
    protected String translatePath(String destination) {
        // Parse for variables
        destination = variableSubstitutor.substitute(destination);

        // Convert the file separator characters
        return destination.replace('/', File.separatorChar);
    }
}
