/*
 * IzPack Version 3.0.0 (build 2002.08.13)
 * Copyright (C) 2001,2002 Julien Ponge
 *
 * File :               InstallPanel.java
 * Description :        A panel to launch the installation process.
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

import net.n3.nanoxml.*;

public class InstallPanel extends IzPanel implements ActionListener, InstallListener
{
    //.....................................................................
    
    // The fields
    private GridBagLayout layout;
    private GridBagConstraints gbConstraints;
    private JLabel infoLabel;
    private HighlightJButton installButton;
    private JLabel tipLabel;
    private JLabel opLabel;
    private JProgressBar progressBar;
    private volatile boolean validated = false;
    
    // The constructor
    public InstallPanel(InstallerFrame parent, InstallData idata)
    {
        super(parent, idata);
        
        // We initialize our layout
        layout = new GridBagLayout();
        gbConstraints = new GridBagConstraints();
        setLayout(layout);
        
        infoLabel = new JLabel(parent.langpack.getString("InstallPanel.info"),
                               parent.icons.getImageIcon("host"), JLabel.TRAILING);
        parent.buildConstraints(gbConstraints, 0, 0, 1, 1, 0.50, 0.0);
        gbConstraints.insets = new Insets(5, 5, 5, 5);
        gbConstraints.fill = GridBagConstraints.NONE;
        gbConstraints.anchor = GridBagConstraints.SOUTHWEST;
        layout.addLayoutComponent(infoLabel, gbConstraints);
        add(infoLabel);
        
        installButton = new HighlightJButton(parent.langpack.getString("InstallPanel.install"),
                                             parent.icons.getImageIcon("refresh_cycle"),
                                             idata.buttonsHColor);
        installButton.addActionListener(this);
        parent.buildConstraints(gbConstraints, 1, 0, 1, 1, 0.50, 0.0);
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        gbConstraints.anchor = GridBagConstraints.SOUTHEAST;
        layout.addLayoutComponent(installButton, gbConstraints);
        add(installButton);
        
        tipLabel = new JLabel(parent.langpack.getString("InstallPanel.tip"),
                              parent.icons.getImageIcon("tip"), JLabel.TRAILING);
        parent.buildConstraints(gbConstraints, 0, 1, 2, 1, 1.0, 0.0);
        gbConstraints.fill = GridBagConstraints.NONE;
        gbConstraints.anchor = GridBagConstraints.NORTHWEST;
        layout.addLayoutComponent(tipLabel, gbConstraints);
        add(tipLabel);
        
        opLabel = new JLabel(" ", JLabel.TRAILING);
        parent.buildConstraints(gbConstraints, 0, 2, 2, 1, 1.0, 0.0);
        gbConstraints.anchor = GridBagConstraints.SOUTHWEST;
        layout.addLayoutComponent(opLabel, gbConstraints);
        add(opLabel);
        
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setString(parent.langpack.getString("InstallPanel.begin"));
        progressBar.setValue(0);
        parent.buildConstraints(gbConstraints, 0, 3, 2, 1, 1.0, 0.0);
        gbConstraints.anchor = GridBagConstraints.NORTH;
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        layout.addLayoutComponent(progressBar, gbConstraints);
        add(progressBar);
    }
    
    //.....................................................................
    // The methods
    
    // Indicates wether the panel has been validated or not
    public boolean isValidated()
    {
        return validated;
    }
    
    // Actions-handling method (here it launches the installation)
    public void actionPerformed(ActionEvent e)
    {
        parent.install(this);
    }
    
    // The unpacker starts
    public void startUnpack()
    {
        parent.blockGUI();
    }
    
    // An error was encountered
    public void errorUnpack(String error)
    {
        opLabel.setText(error);
        idata.installSuccess = false;
        JOptionPane.showMessageDialog(this, error.toString(),
                                          parent.langpack.getString("installer.error"),
                                          JOptionPane.ERROR_MESSAGE);
    }
    
    // The unpacker stops
    public void stopUnpack()
    {
        parent.releaseGUI();
        parent.lockPrevButton();
        installButton.setIcon(parent.icons.getImageIcon("empty"));
        installButton.setEnabled(false);
        progressBar.setString(parent.langpack.getString("InstallPanel.finished"));
        progressBar.setEnabled(false);
        opLabel.setText(" ");
        opLabel.setEnabled(false);
        idata.installSuccess = true;
        idata.canClose = true;
        validated = true;
        if (idata.panels.indexOf(this) != (idata.panels.size() - 1))
            parent.unlockNextButton();
    }
    
    // Normal progress indicator
    public void progressUnpack(int val, String msg)
    {
        progressBar.setValue(val + 1);
        opLabel.setText(msg);
    }
    
    // Pack changing
    public void changeUnpack(int min, int max, String packName)
    {
        progressBar.setValue(0);
        progressBar.setMinimum(min);
        progressBar.setMaximum(max);
        progressBar.setString(packName);
    }
    
    // Called when the panel becomes active
    public void panelActivate()
    {
        // We clip the panel
        Dimension dim = parent.getPanelsContainerSize();
        dim.width = dim.width - (dim.width / 4);
        dim.height = 150;
        setMinimumSize(dim);
        setMaximumSize(dim);
        setPreferredSize(dim);
        parent.lockNextButton();
    }
    
    // Asks to run in the automated mode
    public void runAutomated(XMLElement panelRoot)
    {
        parent.install(this);
        while (!validated) Thread.yield();
    }
    
    //.....................................................................
}
