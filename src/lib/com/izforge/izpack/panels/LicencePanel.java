/*
 * IzPack - Copyright 2001-2005 Julien Ponge, All Rights Reserved.
 * 
 * http://www.izforge.com/izpack/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2002 Jan Blok
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

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.installer.ResourceManager;

/**
 * The license panel.
 * 
 * @author Julien Ponge
 */
public class LicencePanel extends IzPanel implements ActionListener
{

    /**
     * 
     */
    private static final long serialVersionUID = 3691043187997552948L;

    /** The license text. */
    private String licence;

    /** The text area. */
    private JTextArea textArea;

    /** The radio buttons. */
    private JRadioButton yesRadio, noRadio;

    /** The scrolling container. */
    private JScrollPane scroller;

    /**
     * The constructor.
     * 
     * @param parent The parent window.
     * @param idata The installation data.
     */
    public LicencePanel(InstallerFrame parent, InstallData idata)
    {
        super(parent, idata);

        // We initialize our layout
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // We load the licence
        loadLicence();

        // We put our components

        JLabel infoLabel = LabelFactory.create(parent.langpack.getString("LicencePanel.info"),
                parent.icons.getImageIcon("history"), JLabel.TRAILING);
        add(infoLabel);

        add(Box.createRigidArea(new Dimension(0, 3)));

        textArea = new JTextArea(licence);
        textArea.setMargin(new Insets(2, 2, 2, 2));
        textArea.setCaretPosition(0);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        scroller = new JScrollPane(textArea);
        scroller.setAlignmentX(LEFT_ALIGNMENT);
        add(scroller);

        ButtonGroup group = new ButtonGroup();

        yesRadio = new JRadioButton(parent.langpack.getString("LicencePanel.agree"), false);
        group.add(yesRadio);
        add(yesRadio);
        yesRadio.addActionListener(this);

        noRadio = new JRadioButton(parent.langpack.getString("LicencePanel.notagree"), true);
        group.add(noRadio);
        add(noRadio);
        noRadio.addActionListener(this);
    }

    /** Loads the licence text. */
    private void loadLicence()
    {
        try
        {
            // We read it
            String resNamePrifix = "LicencePanel.licence";
            licence = ResourceManager.getInstance().getTextResource(resNamePrifix);
        }
        catch (Exception err)
        {
            licence = "Error : could not load the licence text !";
        }
    }

    /**
     * Actions-handling method (here it allows the installation).
     * 
     * @param e The event.
     */
    public void actionPerformed(ActionEvent e)
    {
        if (yesRadio.isSelected())
            parent.unlockNextButton();
        else
            parent.lockNextButton();
    }

    /**
     * Indicates wether the panel has been validated or not.
     * 
     * @return true if the user has agreed.
     */
    public boolean isValidated()
    {
        if (noRadio.isSelected())
        {
            parent.exit();
            return false;
        }
        else
            return (yesRadio.isSelected());
    }

    /** Called when the panel becomes active. */
    public void panelActivate()
    {
        if (!yesRadio.isSelected()) parent.lockNextButton();
    }
}
