/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001,2002 Julien Ponge
 *
 *  File :               TargetPanel.java
 *  Description :        A panel to select the installation path.
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

import com.izforge.izpack.*;
import com.izforge.izpack.gui.*;
import com.izforge.izpack.installer.*;

import java.awt.*;
import java.awt.event.*;

import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;

import net.n3.nanoxml.*;

/**
 *  The taget directory selection panel.
 *
 * @author     Julien Ponge
 * @created    November 1, 2002
 */
public class TargetPanel extends IzPanel implements ActionListener
{
  /**  The default directory. */
  private String defaultDir;

  /**  The info label. */
  private JLabel infoLabel;

  /**  The text field. */
  private JTextField textField;

  /**  The 'browse' button. */
  private JButton browseButton;

  /**  The layout . */
  private GridBagLayout layout;

  /**  The layout constraints. */
  private GridBagConstraints gbConstraints;


  /**
   *  The constructor.
   *
   * @param  parent  The parent window.
   * @param  idata   The installation data.
   */
  public TargetPanel(InstallerFrame parent, InstallData idata)
  {
    super(parent, idata);

    // We initialize our layout
    layout = new GridBagLayout();
    gbConstraints = new GridBagConstraints();
    setLayout(layout);

    // load the default directory info (if present)
    loadDefaultDir();
    if (defaultDir != null)
      // override the system default that uses app name (which is set in the Installer class)
      idata.setInstallPath(defaultDir);

    // We create and put the components

    infoLabel = new JLabel(parent.langpack.getString("TargetPanel.info"),
      parent.icons.getImageIcon("home"), JLabel.TRAILING);
    parent.buildConstraints(gbConstraints, 0, 0, 2, 1, 3.0, 0.0);
    gbConstraints.insets = new Insets(5, 5, 5, 5);
    gbConstraints.fill = GridBagConstraints.NONE;
    gbConstraints.anchor = GridBagConstraints.SOUTHWEST;
    layout.addLayoutComponent(infoLabel, gbConstraints);
    add(infoLabel);

    textField = new JTextField(idata.getInstallPath(), 40);
    textField.addActionListener(this);
    parent.buildConstraints(gbConstraints, 0, 1, 1, 1, 3.0, 0.0);
    gbConstraints.fill = GridBagConstraints.HORIZONTAL;
    gbConstraints.anchor = GridBagConstraints.WEST;
    layout.addLayoutComponent(textField, gbConstraints);
    add(textField);

    browseButton = ButtonFactory.createButton(parent.langpack.getString("TargetPanel.browse"),
      parent.icons.getImageIcon("open"),
      idata.buttonsHColor);
    browseButton.addActionListener(this);
    parent.buildConstraints(gbConstraints, 1, 1, 1, 1, 1.0, 0.0);
    gbConstraints.fill = GridBagConstraints.HORIZONTAL;
    gbConstraints.anchor = GridBagConstraints.EAST;
    layout.addLayoutComponent(browseButton, gbConstraints);
    add(browseButton);
  }


  /**
   *  Loads up the "dir" resource associated with TargetPanel. Acceptable dir
   *  resource names: <code>
   *   TargetPanel.dir.macosx
   *   TargetPanel.dir.mac
   *   TargetPanel.dir.windows
   *   TargetPanel.dir.unix
   *   TargetPanel.dir.xxx,
   *     where xxx is the lower case version of System.getProperty("os.name"),
   *     with any spaces replace with underscores
   *   TargetPanel.dir (generic that will be applied if none of above is found)
   *   </code> As with all IzPack resources, each the above ids should be
   *  associated with a separate filename, which is set in the install.xml file
   *  at compile time.
   */
  public void loadDefaultDir()
  {
    BufferedReader br = null;
    try
    {
      String os = System.getProperty("os.name");
      InputStream in = null;

      if (os.regionMatches(true, 0, "windows", 0, 7))
        in = parent.getResource("TargetPanel.dir.windows");

      else if (os.regionMatches(true, 0, "macosx", 0, 6))
        in = parent.getResource("TargetPanel.dir.macosx");

      else if (os.regionMatches(true, 0, "mac", 0, 3))
        in = parent.getResource("TargetPanel.dir.mac");

      else
      {
        // first try to look up by specific os name
        os.replace(' ', '_');// avoid spaces in file names
        os = os.toLowerCase();// for consistency among TargetPanel res files
        in = parent.getResource("TargetPanel.dir.".concat(os));
        // if not specific os, try getting generic 'unix' resource file
        if (in == null)
          in = parent.getResource("TargetPanel.dir.unix");

        // if all those failed, try to look up a generic dir file
        if (in == null)
          in = parent.getResource("TargetPanel.dir");

      }

      // if all above tests failed, there is no resource file,
      // so use system default
      if (in == null)
        return;

      // now read the file, once we've identified which one to read
      InputStreamReader isr = new InputStreamReader(in);
      br = new BufferedReader(isr);
      String line = null;
      while ((line = br.readLine()) != null)
      {
        line = line.trim();
        // use the first non-blank line
        if (!line.equals(""))
          break;
      }
      defaultDir = line;
    }
    catch (Exception e)
    {
      defaultDir = null;// leave unset to take the system default set by Installer class
    }
    finally
    {
      try
      {
        if (br != null)
          br.close();
      }
      catch (IOException ignored)
      {}
    }
  }


  /**
   *  Indicates wether the panel has been validated or not.
   *
   * @return    Wether the panel has been validated or not.
   */
  public boolean isValidated()
  {
    String installPath = textField.getText();
    boolean ok = true;

    // We put a warning if the specified target is nameless
    if (installPath.length() == 0)
    {
      int res = JOptionPane.showConfirmDialog(this,
        parent.langpack.getString("TargetPanel.empty_target"),
        parent.langpack.getString("installer.warning"),
        JOptionPane.YES_NO_OPTION);
      ok = (res == JOptionPane.YES_OPTION);
    }

    // Normalize the path
    File path = new File(installPath);
    installPath = path.toString();

    // We put a warning if the directory exists else we warn that it will be created
    if (path.exists())
    {
      int res = JOptionPane.showConfirmDialog(this,
        parent.langpack.getString("TargetPanel.warn"),
        parent.langpack.getString("installer.warning"),
        JOptionPane.YES_NO_OPTION);
      ok = (res == JOptionPane.YES_OPTION);
    }
    else
      JOptionPane.showMessageDialog(this, parent.langpack.getString("TargetPanel.createdir") +
        "\n" + installPath);

    idata.setInstallPath(installPath);
    return ok;
  }


  /**
   *  Actions-handling method.
   *
   * @param  e  The event.
   */
  public void actionPerformed(ActionEvent e)
  {
    Object source = e.getSource();

    if (source != textField)
    {
      // The user wants to browse its filesystem

      // Prepares the file chooser
      JFileChooser fc = new JFileChooser();
      fc.setCurrentDirectory(new File(textField.getText()));
      fc.setMultiSelectionEnabled(false);
      fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      fc.addChoosableFileFilter(fc.getAcceptAllFileFilter());

      // Shows it
      if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
        textField.setText(fc.getSelectedFile().getAbsolutePath());

    }
  }


  /**
   *  Asks to make the XML panel data.
   *
   * @param  panelRoot  The tree to put the data in.
   */
  public void makeXMLData(XMLElement panelRoot)
  {
    // Installation path markup
    XMLElement ipath = new XMLElement("installpath");
    ipath.setContent(idata.getInstallPath());
    panelRoot.addChild(ipath);
  }


  /**
   *  Asks to run in the automated mode.
   *
   * @param  panelRoot  The XML tree to read the data from.
   */
  public void runAutomated(XMLElement panelRoot)
  {
    // We set the installation path
    XMLElement ipath = panelRoot.getFirstChildNamed("installpath");
    idata.setInstallPath(ipath.getContent());
  }
}

