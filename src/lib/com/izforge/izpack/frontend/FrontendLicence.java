/*
 * IzPack Version 3.0.0 pre4 (build 2002.06.15)
 * Copyright (C) 2001,2002 Julien Ponge
 *
 * File :               FrontendLicence.java
 * Description :        The Frontend licence dialog class.
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

package com.izforge.izpack.frontend;

import com.izforge.izpack.*;
import com.izforge.izpack.gui.*;

import java.io.*;
import java.net.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class FrontendLicence extends JDialog implements ActionListener
{
    //.....................................................................
    
    // The fields
    private LocaleDatabase langpack;
    private IconsDatabase icons;
    private GridBagLayout layout;
    private GridBagConstraints gbConstraints;
    private HighlightJButton okButton;
    private JTextArea textArea;
    private JScrollPane scroller;
    
    // The constructor
    public FrontendLicence(Frame owner, LocaleDatabase langpack, IconsDatabase icons)
    {
        super(owner, langpack.getString("menu.licence"), true);
        
        this.langpack = langpack;
        this.icons = icons;

        buildGUI();
        pack();
        FrontendFrame.centerFrame(this);
        setVisible(true);
    }
    
    //.....................................................................
    
    // Builds the GUI
    private void buildGUI()
    {
        // We initialize our layout
        layout = new GridBagLayout();
        gbConstraints = new GridBagConstraints();
        gbConstraints.insets = new Insets(5, 5, 5, 5);
        JPanel contentPane = (JPanel) getContentPane();
        contentPane.setLayout(layout);
        
        // We put our components
        
        JLabel label = new JLabel(langpack.getString("frontend.licence.msg"),
                                  icons.getImageIcon("history"),
                                  JLabel.TRAILING);
        FrontendFrame.buildConstraints(gbConstraints, 0, 0, 1, 1, 1.0, 0.0);
        gbConstraints.fill = GridBagConstraints.NONE;
        gbConstraints.anchor = GridBagConstraints.SOUTHWEST;
        layout.addLayoutComponent(label, gbConstraints);
        contentPane.add(label);
        
        textArea = new JTextArea(getLicenceText());
        textArea.setEditable(false);
        textArea.setCaretPosition(1);
        scroller = new JScrollPane(textArea);
        FrontendFrame.buildConstraints(gbConstraints, 0, 1, 1, 1, 1.0, 1.0);
        scroller.setPreferredSize(new Dimension(500, 250));
        gbConstraints.fill = GridBagConstraints.BOTH;
        gbConstraints.anchor = GridBagConstraints.CENTER;
        layout.addLayoutComponent(scroller, gbConstraints);
        contentPane.add(scroller);
        
        okButton = new HighlightJButton(langpack.getString("frontend.licence.ok"),
                                         icons.getImageIcon("forward"),
                                         FrontendFrame.buttonsHColor);
        okButton.addActionListener(this);
        FrontendFrame.buildConstraints(gbConstraints, 0, 2, 1, 1, 1.0, 0.0);
        gbConstraints.fill = GridBagConstraints.NONE;
        gbConstraints.anchor = GridBagConstraints.SOUTH;
        layout.addLayoutComponent(okButton, gbConstraints);
        contentPane.add(okButton);
    }
    
    // Gets the licence text
    private String getLicenceText()
    {
        StringBuffer buffer = new StringBuffer();
        
        // We read the file
        try
        {
            FileInputStream in = new FileInputStream(Frontend.IZPACK_HOME + "legal" +
                                                     File.separator + "IzPack-Licence.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            int c = 0;
            while (c != -1)
            {
                c = reader.read();
                buffer.append( (char) c);
            }
            
            in.close();
        }
        catch (Exception err)
        {
            err.printStackTrace();
            JOptionPane.showMessageDialog(this, err.toString(),
                                          langpack.getString("frontend.error"),
                                          JOptionPane.ERROR_MESSAGE);
        }
        
        // We return it
        return buffer.toString();
    }
    
    // Action events handler
    public void actionPerformed(ActionEvent e)
    {
        dispose();
    }
    
    //.....................................................................
}
