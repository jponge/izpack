/*
 * IzPack Version 3.0.0 rc1 (build 2002.07.03)
 * Copyright (C) 2001,2002 Julien Ponge
 *
 * File :               LicencePanel.java
 * Description :        A panel to prompt the user for a licence agreement.
 * Author's email :     julien@izforge.com
 * Author's Website :   http://www.izforge.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.izforge.izpack.panels;

import com.izforge.izpack.*;
import com.izforge.izpack.gui.*;
import com.izforge.izpack.installer.*;

import java.awt.*;
import java.awt.event.*;

import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

public class LicencePanel extends IzPanel implements ActionListener
{
    //.....................................................................
    // The fields

    private GridBagLayout layout;
    private GridBagConstraints gbConstraints;
    private String licence;
    private JLabel infoLabel;
    private JTextArea textArea;
    private JLabel agreeLabel;
    private JRadioButton yesRadio, noRadio;
    private JScrollPane scroller;

    // The constructor
    public LicencePanel(InstallerFrame parent, InstallData idata)
    {
        super(parent, idata);

        // We initialize our layout
        layout = new GridBagLayout();
        gbConstraints = new GridBagConstraints();
        setLayout(layout);

        // We load the licence
        loadLicence();

        // We put our components

        infoLabel = new JLabel(parent.langpack.getString("LicencePanel.info"),
                               parent.icons.getImageIcon("history"), JLabel.TRAILING);
        parent.buildConstraints(gbConstraints, 0, 0, 2, 1, 1.0, 0.0);
        gbConstraints.insets = new Insets(5, 5, 5, 5);
        gbConstraints.fill = GridBagConstraints.NONE;
        gbConstraints.anchor = GridBagConstraints.SOUTHWEST;
        layout.addLayoutComponent(infoLabel, gbConstraints);
        add(infoLabel);

        textArea = new JTextArea(licence);
        textArea.setCaretPosition(0);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        scroller = new JScrollPane(textArea);
        parent.buildConstraints(gbConstraints, 0, 1, 2, 1, 1.0, 1.0);
        gbConstraints.anchor = GridBagConstraints.CENTER;
        gbConstraints.fill = GridBagConstraints.BOTH;
        layout.addLayoutComponent(scroller, gbConstraints);
        add(scroller);

        agreeLabel = new JLabel(parent.langpack.getString("LicencePanel.agree"),
                                parent.icons.getImageIcon("help"), JLabel.TRAILING);
        parent.buildConstraints(gbConstraints, 0, 2, 2, 1, 1.0, 0.0);
        gbConstraints.fill = GridBagConstraints.NONE;
        gbConstraints.anchor = GridBagConstraints.SOUTHWEST;
        layout.addLayoutComponent(agreeLabel, gbConstraints);
        add(agreeLabel);

        ButtonGroup group = new ButtonGroup();

        yesRadio = new JRadioButton(parent.langpack.getString("LicencePanel.yes"), false);
        group.add(yesRadio);
        parent.buildConstraints(gbConstraints, 0, 3, 1, 1, 0.5, 0.0);
        gbConstraints.anchor = GridBagConstraints.NORTHWEST;
        layout.addLayoutComponent(yesRadio, gbConstraints);
        add(yesRadio);
        yesRadio.addActionListener(this);

        noRadio = new JRadioButton(parent.langpack.getString("LicencePanel.no"), false);
        group.add(noRadio);
        parent.buildConstraints(gbConstraints, 1, 3, 1, 1, 0.5, 0.0);
        gbConstraints.anchor = GridBagConstraints.NORTHEAST;
        layout.addLayoutComponent(noRadio, gbConstraints);
        add(noRadio);
        noRadio.addActionListener(this);
    }

    //.....................................................................
    // The methods

    // Loads the licence text
    private void loadLicence()
    {
        try
        {
            // We read it
            String resNamePrifix = "LicencePanel.licence";
            String resName = resNamePrifix + "_" + idata.localeISO3;
            InputStream in = parent.getResource(resName);
            if (null == in ) {
                in = parent.getResource(resNamePrifix);
            }

            ByteArrayOutputStream licenceData = new ByteArrayOutputStream();
            byte[] buffer = new byte[5120];
            int bytesInBuffer;
            while ((bytesInBuffer = in.read(buffer)) != -1)
            {
                licenceData.write(buffer, 0, bytesInBuffer);
            }
            licence = licenceData.toString();
        }
        catch (Exception err)
        {
            licence = "Error : could not load the licence text !";
        }
    }

    // Actions-handling method (here it allows the installation)
    public void actionPerformed(ActionEvent e)
    {
        if (yesRadio.isSelected())
            parent.unlockNextButton();
        else
            parent.lockNextButton();
    }

    // Indicates wether the panel has been validated or not
    public boolean isValidated()
    {
        if (noRadio.isSelected())
        {
            parent.exit();
            return false;
        }
        else return (yesRadio.isSelected());
    }

    // Called when the panel becomes active
    public void panelActivate()
    {
        if (!yesRadio.isSelected())
            parent.lockNextButton();
    }

    //.....................................................................
}
