/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2003 Julien Ponge
 *
 *  File :               FrontendPacksTab.java
 *  Description :        The Frontend packs tab class.
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
package com.izforge.izpack.frontend;

import com.izforge.izpack.*;
import com.izforge.izpack.gui.*;

import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import net.n3.nanoxml.*;

/**
 *  The frontend 'packs' tab class.
 *
 * @author     Julien Ponge
 * @created    October 27, 2002
 */
public class FrontendPacksTab extends FrontendTab
   implements ActionListener, ListSelectionListener
{
  /**  The layout. */
  private GridBagLayout layout;

  /**  The layout constraints. */
  private GridBagConstraints gbConstraints;

  /**  The packs list. */
  private JList packsList;

  /**  The files list. */
  private JList filesList;

  /**  The 'add pack' button. */
  private JButton addPackButton;

  /**  The 'del pack' button. */
  private JButton delPackButton;

  /**  The 'required' checkbox. */
  private JCheckBox requiredCheckBox;

  /**  The 'add file' button . */
  private JButton addFileButton;

  /**  The 'del file' button. */
  private JButton delFileButton;

  /**  The 'script properties' button. */
  private JButton scriptPropsButton;

  /**  The 'parsable' checkbox. */
  private JCheckBox parsableCheckBox;

  /**  The packs. */
  private Vector packs;

  /**  The files. */
  private Vector files;

  /**  The current pack. */
  private int curPack = -1;

  /**  The current file. */
  private int curFile = -1;


  /**
   *  The constructor.
   *
   * @param  installation  The XML tree of the installation.
   * @param  icons         The icons database.
   * @param  langpack      The language pack.
   */
  public FrontendPacksTab(XMLElement installation, IconsDatabase icons,
                          LocaleDatabase langpack)
  {
    super(installation, icons, langpack);

    // We initialize our layout
    layout = new GridBagLayout();
    gbConstraints = new GridBagConstraints();
    gbConstraints.insets = new Insets(5, 5, 5, 5);
    setLayout(layout);

    buildGUI();
  }


  /**  Builds our GUI.  */
  private void buildGUI()
  {
    // Usefull stuffs
    JLabel label;
    JScrollPane scroller;

    label = new JLabel(langpack.getString("tabs.packs.packs"));
    FrontendFrame.buildConstraints(gbConstraints, 0, 0, 1, 1, 1.0, 0.0);
    gbConstraints.fill = GridBagConstraints.NONE;
    gbConstraints.anchor = GridBagConstraints.SOUTHWEST;
    layout.addLayoutComponent(label, gbConstraints);
    add(label);

    packsList = new JList();
    packsList.setCellRenderer(FrontendFrame.LIST_RENDERER);
    packsList.addListSelectionListener(this);
    packsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    scroller = new JScrollPane(packsList);
    scroller.setPreferredSize(new Dimension(40, 150));
    FrontendFrame.buildConstraints(gbConstraints, 0, 1, 1, 3, 1.0, 0.0);
    gbConstraints.fill = GridBagConstraints.BOTH;
    gbConstraints.anchor = GridBagConstraints.CENTER;
    layout.addLayoutComponent(scroller, gbConstraints);
    add(scroller);

    label = new JLabel(langpack.getString("tabs.packs.files"));
    FrontendFrame.buildConstraints(gbConstraints, 0, 4, 1, 1, 1.0, 0.0);
    gbConstraints.fill = GridBagConstraints.NONE;
    gbConstraints.anchor = GridBagConstraints.SOUTHWEST;
    layout.addLayoutComponent(label, gbConstraints);
    add(label);

    filesList = new JList();
    filesList.addListSelectionListener(this);
    filesList.setCellRenderer(FrontendFrame.LIST_RENDERER);
    filesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    scroller = new JScrollPane(filesList);
    scroller.setPreferredSize(new Dimension(40, 150));
    FrontendFrame.buildConstraints(gbConstraints, 0, 5, 1, 4, 1.0, 0.0);
    gbConstraints.fill = GridBagConstraints.BOTH;
    gbConstraints.anchor = GridBagConstraints.CENTER;
    layout.addLayoutComponent(scroller, gbConstraints);
    add(scroller);

    addPackButton = ButtonFactory.createButton(langpack.getString("tabs.packs.addpack"),
      icons.getImageIcon("new"),
      FrontendFrame.buttonsHColor);
    addPackButton.addActionListener(this);
    FrontendFrame.buildConstraints(gbConstraints, 1, 1, 1, 1, 0.0, 0.0);
    gbConstraints.fill = GridBagConstraints.HORIZONTAL;
    gbConstraints.anchor = GridBagConstraints.NORTH;
    layout.addLayoutComponent(addPackButton, gbConstraints);
    add(addPackButton);

    delPackButton = ButtonFactory.createButton(langpack.getString("tabs.packs.delpack"),
      icons.getImageIcon("delete"),
      FrontendFrame.buttonsHColor);
    delPackButton.addActionListener(this);
    FrontendFrame.buildConstraints(gbConstraints, 1, 2, 1, 1, 0.0, 0.0);
    gbConstraints.anchor = GridBagConstraints.CENTER;
    layout.addLayoutComponent(delPackButton, gbConstraints);
    add(delPackButton);

    requiredCheckBox = new JCheckBox(langpack.getString("tabs.packs.required"));
    requiredCheckBox.addActionListener(this);
    FrontendFrame.buildConstraints(gbConstraints, 1, 3, 1, 1, 0.0, 0.0);
    gbConstraints.anchor = GridBagConstraints.SOUTH;
    layout.addLayoutComponent(requiredCheckBox, gbConstraints);
    add(requiredCheckBox);

    addFileButton = ButtonFactory.createButton(langpack.getString("tabs.packs.addfile"),
      icons.getImageIcon("new"),
      FrontendFrame.buttonsHColor);
    addFileButton.addActionListener(this);
    FrontendFrame.buildConstraints(gbConstraints, 1, 5, 1, 1, 0.0, 0.0);
    gbConstraints.fill = GridBagConstraints.HORIZONTAL;
    gbConstraints.anchor = GridBagConstraints.NORTH;
    layout.addLayoutComponent(addFileButton, gbConstraints);
    add(addFileButton);

    delFileButton = ButtonFactory.createButton(langpack.getString("tabs.packs.delfile"),
      icons.getImageIcon("delete"),
      FrontendFrame.buttonsHColor);
    delFileButton.addActionListener(this);
    FrontendFrame.buildConstraints(gbConstraints, 1, 6, 1, 1, 0.0, 0.0);
    gbConstraints.anchor = GridBagConstraints.CENTER;
    layout.addLayoutComponent(delFileButton, gbConstraints);
    add(delFileButton);

    scriptPropsButton = ButtonFactory.createButton(langpack.getString("tabs.packs.scriptProps"),
      icons.getImageIcon("properties"),
      FrontendFrame.buttonsHColor);
    scriptPropsButton.addActionListener(this);
    FrontendFrame.buildConstraints(gbConstraints, 1, 7, 1, 1, 0.0, 0.0);
    gbConstraints.anchor = GridBagConstraints.CENTER;
    layout.addLayoutComponent(scriptPropsButton, gbConstraints);
    add(scriptPropsButton);

    parsableCheckBox = new JCheckBox(langpack.getString("tabs.packs.parsable"));
    parsableCheckBox.addActionListener(this);
    FrontendFrame.buildConstraints(gbConstraints, 1, 8, 1, 1, 0.0, 0.0);
    gbConstraints.anchor = GridBagConstraints.SOUTH;
    layout.addLayoutComponent(parsableCheckBox, gbConstraints);
    add(parsableCheckBox);
  }


  /**
   *  Action events handler.
   *
   * @param  e  The event.
   */
  public void actionPerformed(ActionEvent e)
  {
    // We get the source
    Object source = e.getSource();

    // We act depending of the source
    if (source == addPackButton)
    {
      // We make the new pack element
      XMLElement pack = new XMLElement("pack");
      pack.setAttribute("required", "yes");
      String inputValue;
      inputValue = JOptionPane.showInputDialog(langpack.getString("tabs.packs.pack_name"));
      if (inputValue == null)
        return;
      else
        pack.setAttribute("name", inputValue);
      XMLElement desc = new XMLElement("description");
      inputValue = JOptionPane.showInputDialog(langpack.getString("tabs.packs.pack_desc"));
      if (inputValue == null)
        return;
      else
        desc.setContent(inputValue);

      // We add it
      pack.addChild(desc);
      installation.getFirstChildNamed("packs").addChild(pack);

      // We select it
      curPack = installation.getFirstChildNamed("packs").getChildrenNamed("pack").size() - 1;
      curFile = -1;

      updateComponents();
    }
    else if (source == delPackButton)
    {
      // We get the selected pack index
      int index = packsList.getSelectedIndex();
      if (index == -1)
        return;

      // We remove the element
      installation.getFirstChildNamed("packs").removeChildAtIndex(index);

      // We try to keep something selected
      curPack = (index < packsList.getModel().getSize() - 2) ? index
         : packsList.getModel().getSize() - 2;
      curFile = -1;

      updateComponents();
    }
    else if (source == requiredCheckBox)
    {
      // We get the selected pack index
      int index = packsList.getSelectedIndex();
      if (index == -1)
        return;

      // We update the element
      XMLElement el = installation.getFirstChildNamed("packs").getChildAtIndex(index);
      el.setAttribute("required", (requiredCheckBox.isSelected() ? "yes" : "no"));
    }
    else if (source == addFileButton)
    {
      // We get the selected pack index
      int index = packsList.getSelectedIndex();
      if (index == -1)
        return;

      // We make a new element for the file
      XMLElement file = new XMLElement("file");
      JFileChooser fileChooser = new JFileChooser(Frontend.lastDir);
      fileChooser.addChoosableFileFilter(fileChooser.getAcceptAllFileFilter());
      fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
      if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
      {
        Frontend.lastDir = fileChooser.getSelectedFile().getParentFile().getAbsolutePath();
        file.setAttribute("src", fileChooser.getSelectedFile().getAbsolutePath());
        String inputValue;
        inputValue = JOptionPane.showInputDialog(langpack.getString("tabs.packs.target"));
        if (inputValue == null)
          return;
        else
          file.setAttribute("targetdir", inputValue);
        installation.getFirstChildNamed("packs").getChildAtIndex(index).addChild(file);

        // We select it
        curFile = filesList.getModel().getSize();
        curPack = index;
      }

      updateComponents();
    }
    else if (source == delFileButton)
    {
      // We get the selected items indices
      int pindex = packsList.getSelectedIndex();
      int findex = filesList.getSelectedIndex();
      if ((findex == -1) || (pindex == -1))
        return;

      // We remove it
      XMLElement root = installation.getFirstChildNamed("packs").getChildAtIndex(pindex);
      Vector v = root.getChildrenNamed("file");
      XMLElement file = (XMLElement) v.get(findex);
      root.removeChild(file);

      // We try to keep something selected
      curFile = (findex < filesList.getModel().getSize() - 2) ? findex
         : filesList.getModel().getSize() - 2;
      curPack = pindex;

      updateComponents();
    }
    else if (source == parsableCheckBox)
    {
      // We get the selected items indices
      int pindex = packsList.getSelectedIndex();
      int findex = filesList.getSelectedIndex();
      if ((findex == -1) || (pindex == -1))
        return;

      // We look for the file attributes
      XMLElement root = installation.getFirstChildNamed("packs").getChildAtIndex(pindex);
      Vector v = root.getChildrenNamed("file");
      XMLElement file = (XMLElement) v.get(findex);
      File destFile = new File(file.getAttribute("src"));
      String destName = file.getAttribute("targetdir") + "/" + destFile.getName();

      // We add or remove the parsable tag
      if (parsableCheckBox.isSelected())
      {
        // Add it
        XMLElement p = new XMLElement("parsable");
        p.setAttribute("targetfile", destName);
        root.addChild(p);
      }
      else
      {
        // Remove it
        Vector pv = root.getChildrenNamed("parsable");
        int size = pv.size();
        XMLElement toRemove = null;
        for (int i = 0; (i < size) && (toRemove == null); i++)
        {
          XMLElement el = (XMLElement) pv.get(i);
          if (el.getAttribute("targetfile").equalsIgnoreCase(destName))
            toRemove = el;
        }
        root.removeChild(toRemove);
      }

      scriptPropsButton.setEnabled(parsableCheckBox.isSelected());
    }
    else if (source == scriptPropsButton)
    {
      // We get the selected items indices
      int pindex = packsList.getSelectedIndex();
      int findex = filesList.getSelectedIndex();
      if ((findex == -1) || (pindex == -1))
        return;

      // We look for the file attributes
      XMLElement root = installation.getFirstChildNamed("packs").getChildAtIndex(pindex);
      Vector v = root.getChildrenNamed("file");
      XMLElement file = (XMLElement) v.get(findex);
      File destFile = new File(file.getAttribute("src"));
      String destName = file.getAttribute("targetdir") + "/" + destFile.getName();

      // We find the tag
      Vector pv = root.getChildrenNamed("parsable");
      int size = pv.size();
      XMLElement toEdit = null;
      for (int i = 0; (i < size) && (toEdit == null); i++)
      {
        XMLElement el = (XMLElement) pv.get(i);
        if (el.getAttribute("targetfile").equalsIgnoreCase(destName))
          toEdit = el;
      }

      // We ask for the extended attributes
      String inputValue;
      inputValue = JOptionPane.showInputDialog(langpack.getString("tabs.packs.f_type"));
      if (inputValue == null)
        return;
      else
        if (inputValue.length() != 0)
        toEdit.setAttribute("type", inputValue);
      else
        toEdit.removeAttribute("type");
      inputValue = JOptionPane.showInputDialog(langpack.getString("tabs.packs.f_enc"));
      if (inputValue == null)
        return;
      else
        if (inputValue.length() != 0)
        toEdit.setAttribute("encoding", inputValue);
      else
        toEdit.removeAttribute("encoding");
    }
  }


  /**
   *  List events handler.
   *
   * @param  e  The event.
   */
  public void valueChanged(ListSelectionEvent e)
  {
    // We get the source
    Object source = e.getSource();

    // We act depending of the source
    if (source == packsList)
    {
      // We get the selected pack index
      int index = packsList.getSelectedIndex();
      if (index == -1)
        return;

      // We update the required check box
      XMLElement el = installation.getFirstChildNamed("packs").getChildAtIndex(index);
      requiredCheckBox.setSelected(el.getAttribute("required").equalsIgnoreCase("yes"));

      // We update the files list
      files = new Vector();
      Vector v = el.getChildrenNamed("file");
      int size = v.size();
      for (int i = 0; i < size; i++)
      {
        XMLElement f = (XMLElement) v.get(i);
        LFile file = new LFile(f.getAttribute("src"), f.getAttribute("targetdir"));
        files.add(file);
      }
      filesList.setListData(files);
    }
    else if (source == filesList)
    {
      // We get the selected items indices
      int pindex = packsList.getSelectedIndex();
      int findex = filesList.getSelectedIndex();
      if ((findex == -1) || (pindex == -1))
        return;

      // We look in the parsables list
      XMLElement root = installation.getFirstChildNamed("packs").getChildAtIndex(pindex);
      Vector v = root.getChildrenNamed("file");
      XMLElement file = (XMLElement) v.get(findex);
      File destFile = new File(file.getAttribute("src"));
      String destName = file.getAttribute("targetdir") + "/" + destFile.getName();
      boolean exists = false;
      Vector vp = root.getChildrenNamed("parsable");
      int size = vp.size();
      for (int i = 0; (i < size) && (!exists); i++)
      {
        XMLElement parsable = (XMLElement) vp.get(i);
        exists = parsable.getAttribute("targetfile").equalsIgnoreCase(destName);
      }
      parsableCheckBox.setSelected(exists);
      scriptPropsButton.setEnabled(exists);
    }
  }


  /**  Updates the components.  */
  public void updateComponents()
  {
    // We get our base root
    XMLElement root = installation.getFirstChildNamed("packs");

    // We fill our vector
    packs = new Vector();
    files = new Vector();
    Vector v = root.getChildrenNamed("pack");
    int size = v.size();
    for (int i = 0; i < size; i++)
    {
      XMLElement el = (XMLElement) v.get(i);
      // TODO: add UI to alter preselection state
      Pack p = new Pack(el.getAttribute("name"),
        el.getFirstChildNamed("description").getContent(),
        com.izforge.izpack.util.OsConstraint.getOsList(el),
        el.getAttribute("required").equalsIgnoreCase("yes"), true);
      packs.add(p);
    }

    // We fill the lists
    packsList.setListData(packs);
    if (curPack != -1)
      packsList.setSelectedIndex(curPack);
    filesList.setListData(files);
    if (curFile != -1)
      filesList.setSelectedIndex(curFile);
    parsableCheckBox.setSelected(false);
    scriptPropsButton.setEnabled(false);
  }


  /**
   *  Represents a file to put it in a JList.
   *
   * @author     julien
   * @created    October 27, 2002
   */
  class LFile
  {
    /**  The source. */
    public String src;

    /**  The target directory. */
    public String targetdir;


    /**
     *  Constructor.
     *
     * @param  src        The source.
     * @param  targetdir  The target directory.
     */
    public LFile(String src, String targetdir)
    {
      this.src = src;
      this.targetdir = targetdir;
    }


    /**
     *  Gets a String representation.
     *
     * @return    The String representation.
     */
    public String toString()
    {
      return src + " [ " + targetdir + " ]";
    }
  }
}

