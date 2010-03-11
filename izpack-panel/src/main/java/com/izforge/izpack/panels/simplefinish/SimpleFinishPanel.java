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

package com.izforge.izpack.panels.simplefinish;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.base.InstallerFrame;
import com.izforge.izpack.installer.base.IzPanel;
import com.izforge.izpack.installer.data.GUIInstallData;

import javax.swing.*;
import java.io.File;

/**
 * The simple finish panel class.
 *
 * @author Julien Ponge
 */
public class SimpleFinishPanel extends IzPanel
{

    /**
     *
     */
    private static final long serialVersionUID = 3689911781942572085L;

    /**
     * The variables substitutor.
     */
    private VariableSubstitutor vs;

    /**
     * The constructor.
     *
     * @param parent The parent.
     * @param idata  The installation installDataGUI.
     */
    public SimpleFinishPanel(InstallerFrame parent, GUIInstallData idata, ResourceManager resourceManager)
    {
        super(parent, idata, new IzPanelLayout(), resourceManager);
    }

    /**
     * Indicates wether the panel has been validated or not.
     *
     * @return true if the panel has been validated.
     */
    public boolean isValidated()
    {
        return true;
    }

    /**
     * Called when the panel becomes active.
     */
    public void panelActivate()
    {
        parent.lockNextButton();
        parent.lockPrevButton();
        parent.setQuitButtonText(installData.getLangpack().getString("FinishPanel.done"));
        parent.setQuitButtonIcon("done");
        if (this.installData.isInstallSuccess())
        {

            // We set the information
            add(LabelFactory.create(parent.icons.getImageIcon("check")));
            add(IzPanelLayout.createVerticalStrut(5));
            JLabel jLabel = LabelFactory.create(installData.getLangpack().getString("FinishPanel.success"),
                    parent.icons.getImageIcon("preferences"), LEADING);
            jLabel.setName(GuiId.SIMPLE_FINISH_LABEL.id);
            add(jLabel, NEXT_LINE);
            add(IzPanelLayout.createVerticalStrut(5));
            if (this.installData.getUninstallOutJar() != null)
            {
                // We prepare a message for the uninstaller feature
                String path = translatePath("$INSTALL_PATH") + File.separator + "Uninstaller";

                add(LabelFactory.create(installData.getLangpack()
                        .getString("FinishPanel.uninst.info"), parent.icons
                        .getImageIcon("preferences"), LEADING), NEXT_LINE);
                add(LabelFactory.create(path, parent.icons.getImageIcon("empty"),
                        LEADING), NEXT_LINE);
            }
        }
        else
        {
            add(LabelFactory.create(installData.getLangpack().getString("FinishPanel.fail"),
                    parent.icons.getImageIcon("stop"), LEADING));
        }
        getLayoutHelper().completeLayout(); // Call, or call not?
        Log.getInstance().informUser();
    }

    /**
     * Translates a relative path to a local system path.
     *
     * @param destination The path to translate.
     * @return The translated path.
     */
    private String translatePath(String destination)
    {
        // Parse for variables
        destination = variableSubstitutor.substitute(destination);

        // Convert the file separator characters
        return destination.replace('/', File.separatorChar);
    }
}
