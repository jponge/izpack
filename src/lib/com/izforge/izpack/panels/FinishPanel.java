/*
 * IzPack Version 3.0.0 rc1 (build 2002.07.03)
 * Copyright (C) 2001,2002 Julien Ponge
 *
 * File :               FinishPanel.java
 * Description :        A panel to end with the installation.
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

public class FinishPanel extends IzPanel implements ActionListener
{
    //.....................................................................
    
    // The fields
    private BoxLayout layout;
    private JLabel infoLabel;
    private HighlightJButton autoButton;
    private JPanel centerPanel;
    
    // The constructor
    public FinishPanel(InstallerFrame parent, InstallData idata)
    {
        super(parent, idata);
        
        // The 'super' layout
        GridBagLayout superLayout = new GridBagLayout();
        setLayout(superLayout);
        GridBagConstraints gbConstraints = new GridBagConstraints();
        gbConstraints.insets = new Insets(0, 0, 0, 0);
        gbConstraints.fill = GridBagConstraints.NONE;
        gbConstraints.anchor = GridBagConstraints.CENTER;
        
        // We initialize our 'real' layout
        centerPanel = new JPanel();
        layout = new BoxLayout(centerPanel, BoxLayout.Y_AXIS);
        centerPanel.setLayout(layout);
        superLayout.addLayoutComponent(centerPanel, gbConstraints);
        add(centerPanel);
        
        // We create and put the labels
        
        centerPanel.add(Box.createVerticalStrut(20));
        
        infoLabel = new JLabel("", parent.icons.getImageIcon("information"), JLabel.TRAILING);
    }
    
    //.....................................................................
    // The methods
    
    // Indicates wether the panel has been validated or not
    public boolean isValidated()
    {
        return true;
    }
    
    // Called when the panel becomes active
    public void panelActivate()
    {
        parent.lockNextButton();
        parent.lockPrevButton();
        
        if (idata.installSuccess)
        {
            // We prepare a message for the uninstaller feature
            String home = "";
            home = System.getProperty("user.home");
            String path = home + File.separator +  "." + idata.info.getAppName() + "-uninstaller" +
                          File.separator + "uninstaller.jar";
            
            // We set the information
            infoLabel.setText(parent.langpack.getString("FinishPanel.success"));
            centerPanel.add(Box.createVerticalStrut(20));
            centerPanel.add(new JLabel(parent.langpack.getString("FinishPanel.uninst.info"),
                            parent.icons.getImageIcon("information"), JLabel.TRAILING));
            centerPanel.add(new JLabel(path, parent.icons.getImageIcon("empty"), JLabel.TRAILING));
            
            // We add the autoButton
            centerPanel.add(Box.createVerticalStrut(20));
            autoButton = new HighlightJButton(parent.langpack.getString("FinishPanel.auto"),
                                              parent.icons.getImageIcon("edit"),
                                              idata.buttonsHColor);
            autoButton.setToolTipText(parent.langpack.getString("FinishPanel.auto.tip"));
            autoButton.addActionListener(this);
            centerPanel.add(autoButton);
        }
        else
            infoLabel.setText(parent.langpack.getString("FinishPanel.fail"));
    }
    
    // Actions-handling method
    public void actionPerformed(ActionEvent e)
    {
        // Prepares the file chooser
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new File(idata.getInstallPath()));
        fc.setMultiSelectionEnabled(false);
        fc.addChoosableFileFilter(fc.getAcceptAllFileFilter());
        fc.setDialogType(JFileChooser.SAVE_DIALOG);
        fc.setCurrentDirectory(new File("."));

        // Shows it
        try
        {
            if(fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
            {
                // We handle the xml data writing
                File file = fc.getSelectedFile();
                FileOutputStream out = new FileOutputStream(file);
                BufferedOutputStream outBuff = new BufferedOutputStream(out, 5120);
                parent.writeXMLTree(idata.xmlData, outBuff);
                outBuff.flush();
                outBuff.close();
                
                autoButton.setEnabled(false);
            }
        }
        catch (Exception err) 
        {
            err.printStackTrace();
            JOptionPane.showMessageDialog(this, err.toString(),
                                          parent.langpack.getString("installer.error"),
                                          JOptionPane.ERROR_MESSAGE);
        }
    }
    
    //.....................................................................
}
