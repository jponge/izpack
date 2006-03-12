/*
 * IzPack - Copyright 2001-2006 Julien Ponge, All Rights Reserved.
 * 
 * http://www.izforge.com/izpack/
 * http://developer.berlios.de/projects/izpack/
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.installer.ResourceManager;

/**
 * The info panel class. Displays some raw-text informations.
 * 
 * @author Julien Ponge
 */
public class InfoPanel extends IzPanel
{

    private static final long serialVersionUID = 3833748780590905399L;

    /** The layout. */
    private GridBagLayout layout;

    /** The layout constraints. */
    private GridBagConstraints gbConstraints;

    /** The info label. */
    private JLabel infoLabel;

    /** The text area. */
    private JTextArea textArea;

    /** The scrolling container. */
    private JScrollPane scroller;

    /** The info string. */
    private String info;

    /**
     * The constructor.
     * 
     * @param parent The parent window.
     * @param idata The installation data.
     */
    public InfoPanel(InstallerFrame parent, InstallData idata)
    {
        super(parent, idata);

        // We initialize our layout
        layout = new GridBagLayout();
        gbConstraints = new GridBagConstraints();
        setLayout(layout);

        // We load the text
        loadInfo();

        // We add the components

        infoLabel = LabelFactory.create(parent.langpack.getString("InfoPanel.info"), parent.icons
                .getImageIcon("edit"), JLabel.TRAILING);
        parent.buildConstraints(gbConstraints, 0, 0, 1, 1, 1.0, 0.1);
        gbConstraints.insets = new Insets(5, 5, 5, 5);
        gbConstraints.fill = GridBagConstraints.NONE;
        gbConstraints.anchor = GridBagConstraints.SOUTHWEST;
        layout.addLayoutComponent(infoLabel, gbConstraints);
        add(infoLabel);

        textArea = new JTextArea(info);
        textArea.setCaretPosition(0);
        textArea.setEditable(false);
        scroller = new JScrollPane(textArea);
        parent.buildConstraints(gbConstraints, 0, 1, 1, 1, 1.0, 0.9);
        gbConstraints.fill = GridBagConstraints.BOTH;
        gbConstraints.anchor = GridBagConstraints.CENTER;
        layout.addLayoutComponent(scroller, gbConstraints);
        add(scroller);
    }

    /** Loads the info text. */
    private void loadInfo()
    {
        try
        {
            String resNamePrifix = "InfoPanel.info";
            info = ResourceManager.getInstance().getTextResource(resNamePrifix);
        }
        catch (Exception err)
        {
            info = "Error : could not load the info text !";
        }
    }

    /**
     * Indicates wether the panel has been validated or not.
     * 
     * @return Always true.
     */
    public boolean isValidated()
    {
        return true;
    }
}
