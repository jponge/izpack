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

package com.izforge.izpack.panels;

import com.izforge.izpack.data.ResourceManager;
import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.installer.base.GuiId;
import com.izforge.izpack.installer.base.InstallerFrame;
import com.izforge.izpack.installer.base.IzPanel;
import com.izforge.izpack.installer.data.GUIInstallData;

import javax.swing.*;

/**
 * The info panel class. Displays some raw-text informations.
 *
 * @author Julien Ponge
 */
public class InfoPanel extends IzPanel {

    private static final long serialVersionUID = 3833748780590905399L;

    /**
     * The info string.
     */
    private String info;

    /**
     * The constructor.
     *
     * @param parent The parent window.
     * @param idata  The installation installDataGUI.
     */
    public InfoPanel(InstallerFrame parent, GUIInstallData idata, ResourceManager resourceManager) {
        super(parent, idata, new IzPanelLayout(), resourceManager);
        // We load the text.
        loadInfo();
        // The info label.
        add(LabelFactory.create(installData.getLangpack().getString("InfoPanel.info"), parent.icons
                .getImageIcon("edit"), LEADING), NEXT_LINE);
        // The text area which shows the info.
        JTextArea textArea = new JTextArea(info);
        textArea.setName(GuiId.INFO_PANEL_TEXT_AREA.id);
        textArea.setCaretPosition(0);
        textArea.setEditable(false);
        JScrollPane scroller = new JScrollPane(textArea);
        add(scroller, NEXT_LINE);
        // At end of layouting we should call the completeLayout method also they do nothing.
        getLayoutHelper().completeLayout();
    }

    /**
     * Loads the info text.
     */
    private void loadInfo() {
        try {
            String resNamePrifix = "InfoPanel.info";
            info = getResourceManager().getTextResource(resNamePrifix);
        }
        catch (Exception err) {
            info = "Error : could not load the info text !";
        }
    }

    /**
     * Indicates wether the panel has been validated or not.
     *
     * @return Always true.
     */
    public boolean isValidated() {
        return true;
    }
}
