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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ButtonGroup;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.installer.ResourceManager;

/**
 * The IzPack HTML license panel.
 * 
 * @author Julien Ponge
 */
public class HTMLLicencePanel extends IzPanel implements HyperlinkListener, ActionListener
{

    /**
     * 
     */
    private static final long serialVersionUID = 3256728385458746416L;

    /** The layout. */
    private GridBagLayout layout;

    /** The layout constraints. */
    private GridBagConstraints gbConstraints;

    /** The info label. */
    private JLabel infoLabel;

    /** The text area. */
    private JEditorPane textArea;

    /** The radio buttons. */
    private JRadioButton yesRadio, noRadio;

    /**
     * The constructor.
     * 
     * @param idata The installation data.
     * @param parent Description of the Parameter
     */
    public HTMLLicencePanel(InstallerFrame parent, InstallData idata)
    {
        super(parent, idata);

        // We initialize our layout
        layout = new GridBagLayout();
        gbConstraints = new GridBagConstraints();
        setLayout(layout);

        // We load the licence
        loadLicence();

        // We put our components

        infoLabel = LabelFactory.create(parent.langpack.getString("LicencePanel.info"),
                parent.icons.getImageIcon("history"), JLabel.TRAILING);
        parent.buildConstraints(gbConstraints, 0, 0, 2, 1, 1.0, 0.0);
        gbConstraints.insets = new Insets(5, 5, 5, 5);
        gbConstraints.fill = GridBagConstraints.NONE;
        gbConstraints.anchor = GridBagConstraints.WEST;
        layout.addLayoutComponent(infoLabel, gbConstraints);
        add(infoLabel);

        try
        {
            textArea = new JEditorPane();
            textArea.setEditable(false);
            textArea.addHyperlinkListener(this);
            JScrollPane scroller = new JScrollPane(textArea);
            textArea.setPage(loadLicence());
            parent.buildConstraints(gbConstraints, 0, 1, 2, 1, 1.0, 1.0);
            gbConstraints.anchor = GridBagConstraints.CENTER;
            gbConstraints.fill = GridBagConstraints.BOTH;
            layout.addLayoutComponent(scroller, gbConstraints);
            add(scroller);
        }
        catch (Exception err)
        {
            err.printStackTrace();
        }

        ButtonGroup group = new ButtonGroup();

        yesRadio = new JRadioButton(parent.langpack.getString("LicencePanel.agree"), false);
        group.add(yesRadio);
        parent.buildConstraints(gbConstraints, 0, 2, 1, 1, 1.0, 0.0);
        gbConstraints.anchor = GridBagConstraints.WEST;
        gbConstraints.fill = GridBagConstraints.NONE;
        layout.addLayoutComponent(yesRadio, gbConstraints);
        add(yesRadio);
        yesRadio.addActionListener(this);

        noRadio = new JRadioButton(parent.langpack.getString("LicencePanel.notagree"), true);
        group.add(noRadio);
        parent.buildConstraints(gbConstraints, 0, 3, 1, 1, 1.0, 0.0);
        gbConstraints.anchor = GridBagConstraints.WEST;
        gbConstraints.fill = GridBagConstraints.NONE;
        gbConstraints.insets = new Insets(0, 5, 5, 5);
        layout.addLayoutComponent(noRadio, gbConstraints);
        add(noRadio);
        noRadio.addActionListener(this);
        setInitialFocus(textArea);
    }

    /**
     * Loads the license text.
     * 
     * @return The license text URL.
     */
    private URL loadLicence()
    {
        String resNamePrifix = "HTMLLicencePanel.licence";
        try
        {
            return ResourceManager.getInstance().getURL(resNamePrifix);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Actions-handling method (here it launches the installation).
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
     * @return true if the user agrees with the license, false otherwise.
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

    /**
     * Hyperlink events handler.
     * 
     * @param e The event.
     */
    public void hyperlinkUpdate(HyperlinkEvent e)
    {
        try
        {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
                textArea.setPage(e.getURL());
        }
        catch (Exception err)
        {}
    }

    /** Called when the panel becomes active. */
    public void panelActivate()
    {
        if (!yesRadio.isSelected()) parent.lockNextButton();
    }
}
