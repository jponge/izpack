/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2004 Julien Ponge
 *
 *  File :               FinishPanel.java
 *  Description :        A panel to end with the installation.
 *  Author's email :     julien@izforge.com
 *  Author's Website :   http://www.izforge.com
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.izforge.izpack.panels;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.izforge.izpack.gui.ButtonFactory;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.installer.VariableSubstitutor;

/**
 *  The finish panel class.
 *
 * @author     Julien Ponge
 */
public class FinishPanel extends IzPanel implements ActionListener
{
  /**  The layout. */
  private BoxLayout layout;

  /**  The automated installers generation button. */
  private JButton autoButton;

  /**  The center panel. */
  private JPanel centerPanel;

  /**  The variables substitutor. */
  private VariableSubstitutor vs;

  /**
   *  The constructor.
   *
   * @param  parent  The parent.
   * @param  idata   The installation data.
   */
  public FinishPanel(InstallerFrame parent, InstallData idata)
  {
    super(parent, idata);

    vs = new VariableSubstitutor(idata.getVariables());

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
  }

  /**
   *  Indicates wether the panel has been validated or not.
   *
   * @return    true if the panel has been validated.
   */
  public boolean isValidated()
  {
    return true;
  }

  /**  Called when the panel becomes active.  */
  public void panelActivate()
  {
    parent.lockNextButton();
    parent.lockPrevButton();
    parent.setQuitButtonText(parent.langpack.getString("FinishPanel.done"));
    if (idata.installSuccess)
    {
      // We set the information
      centerPanel.add(
        new JLabel(
          parent.langpack.getString("FinishPanel.success"),
          parent.icons.getImageIcon("information"),
          JLabel.TRAILING));
      centerPanel.add(Box.createVerticalStrut(20));

      if (idata.uninstallOutJar != null)
      {
        // We prepare a message for the uninstaller feature
        String path =
          translatePath("$INSTALL_PATH") + File.separator + "Uninstaller";

        centerPanel.add(
          new JLabel(
            parent.langpack.getString("FinishPanel.uninst.info"),
            parent.icons.getImageIcon("information"),
            JLabel.TRAILING));
        centerPanel.add(
          new JLabel(
            path,
            parent.icons.getImageIcon("empty"),
            JLabel.TRAILING));
      }

      // We add the autoButton
      centerPanel.add(Box.createVerticalStrut(20));
      autoButton =
        ButtonFactory.createButton(
          parent.langpack.getString("FinishPanel.auto"),
          parent.icons.getImageIcon("edit"),
          idata.buttonsHColor);
      autoButton.setToolTipText(
        parent.langpack.getString("FinishPanel.auto.tip"));
      autoButton.addActionListener(this);
      centerPanel.add(autoButton);
    } else
      centerPanel.add(
        new JLabel(
          parent.langpack.getString("FinishPanel.fail"),
          parent.icons.getImageIcon("information"),
          JLabel.TRAILING));
  }

  /**
   *  Actions-handling method.
   *
   * @param  e  The event.
   */
  public void actionPerformed(ActionEvent e)
  {
    // Prepares the file chooser
    JFileChooser fc = new JFileChooser();
    fc.setCurrentDirectory(new File(idata.getInstallPath()));
    fc.setMultiSelectionEnabled(false);
    fc.addChoosableFileFilter(fc.getAcceptAllFileFilter());
    //fc.setCurrentDirectory(new File("."));

    // Shows it
    try
    {
      if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
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
    } catch (Exception err)
    {
      err.printStackTrace();
      JOptionPane.showMessageDialog(
        this,
        err.toString(),
        parent.langpack.getString("installer.error"),
        JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   *  Translates a relative path to a local system path.
   *
   * @param  destination  The path to translate.
   * @return              The translated path.
   */
  private String translatePath(String destination)
  {
    // Parse for variables
    destination = vs.substitute(destination, null);

    // Convert the file separator characters
    return destination.replace('/', File.separatorChar);
  }
}
