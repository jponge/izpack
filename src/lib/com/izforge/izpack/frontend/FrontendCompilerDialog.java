/*
 * IzPack Version 3.0.0 (build 2002.08.13)
 * Copyright (C) 2001,2002 Julien Ponge
 *
 * File :               FrontendCompilerDialog.java
 * Description :        The Frontend compiler frame class.
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
import com.izforge.izpack.compiler.*;

import java.io.*;
import java.net.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;

import net.n3.nanoxml.*;

import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;

import com.incors.plaf.kunststoff.*;

public class FrontendCompilerDialog extends JDialog implements ActionListener, PackagerListener
{
    //.....................................................................
    
    // The fields
    private LocaleDatabase langpack;
    private IconsDatabase icons;
    private GridBagLayout layout;
    private GridBagConstraints gbConstraints;
    private HighlightJButton okButton;
    private JTextArea textArea;
    private JScrollBar vertScrollBar;
    
    // The constructor
    public FrontendCompilerDialog(Frame owner, LocaleDatabase langpack, IconsDatabase icons)
    {
        super(owner, langpack.getString("frontend.comp_dlg.title"), false);
        
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
        // Prepares the glass pane to block gui interaction when needed
        JPanel glassPane = (JPanel) getGlassPane();
        glassPane.addMouseListener(new MouseAdapter() {} );
        glassPane.addMouseMotionListener(new MouseMotionAdapter() {} );
        glassPane.addKeyListener(new KeyAdapter() {} );
        
        // We initialize our layout
        layout = new GridBagLayout();
        gbConstraints = new GridBagConstraints();
        gbConstraints.insets = new Insets(5, 5, 5, 5);
        JPanel contentPane = (JPanel) getContentPane();
        contentPane.setLayout(layout);
        
        // We put our components
        
        JLabel label = new JLabel(langpack.getString("frontend.comp_dlg.msg"));
        FrontendFrame.buildConstraints(gbConstraints, 0, 0, 1, 1, 1.0, 0.0);
        gbConstraints.fill = GridBagConstraints.NONE;
        gbConstraints.anchor = GridBagConstraints.SOUTHWEST;
        layout.addLayoutComponent(label, gbConstraints);
        contentPane.add(label);
        
        textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scroller = new JScrollPane(textArea);
        FrontendFrame.buildConstraints(gbConstraints, 0, 1, 1, 1, 1.0, 1.0);
        scroller.setPreferredSize(new Dimension(500, 250));
        gbConstraints.fill = GridBagConstraints.BOTH;
        gbConstraints.anchor = GridBagConstraints.CENTER;
        layout.addLayoutComponent(scroller, gbConstraints);
        contentPane.add(scroller);
        vertScrollBar = scroller.getVerticalScrollBar();
        
        okButton = new HighlightJButton(langpack.getString("frontend.comp_dlg.ok"),
                                         icons.getImageIcon("forward"),
                                         FrontendFrame.buttonsHColor);
        okButton.addActionListener(this);
        FrontendFrame.buildConstraints(gbConstraints, 0, 2, 1, 1, 1.0, 0.0);
        gbConstraints.fill = GridBagConstraints.NONE;
        gbConstraints.anchor = GridBagConstraints.SOUTH;
        layout.addLayoutComponent(okButton, gbConstraints);
        contentPane.add(okButton);
    }
    
    // Blocks GUI interaction
    public void blockGUI()
    {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        getGlassPane().setVisible(true);
        getGlassPane().setEnabled(true);
    }
    
    // Releases GUI interaction
    public void releaseGUI()
    {
        getGlassPane().setEnabled(false);
        getGlassPane().setVisible(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
    
    // Action events handler
    public void actionPerformed(ActionEvent e)
    {
        dispose();
    }
    
    // Called as the packager sends messages
    public void packagerMsg(String info)
    {
        textArea.append("\n" + info);
        vertScrollBar.setValue(vertScrollBar.getMaximum());
    }
    
    // Called when the packager starts
    public void packagerStart()
    {
        blockGUI();
        textArea.setText("[ Begin ]\n");
    }
    
    // Called when the packager stops
    public void packagerStop()
    {
        releaseGUI();
        textArea.append("\n\n[ End ]");
        vertScrollBar.setValue(vertScrollBar.getMaximum());
    }
    
    //.....................................................................
}
