/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2003 Julien Ponge
 *
 *  File :               FrontendPanTab.java
 *  Description :        The Frontend panels tab class.
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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import net.n3.nanoxml.XMLElement;

import com.izforge.izpack.LocaleDatabase;
import com.izforge.izpack.gui.ButtonFactory;
import com.izforge.izpack.gui.IconsDatabase;

/**
 *  The frontend 'panels' tab class.
 *
 * @author     Julien Ponge
 */
public class FrontendPanTab extends FrontendTab implements ActionListener
{
  /**  The layout. */
  private GridBagLayout layout;

  /**  The layout constraints. */
  private GridBagConstraints gbConstraints;

  /**  The available packs list. */
  private JList availableList;

  /**  The selected packs list. */
  private JList selectedList;

  /**  The include button. */
  private JButton includeButton;

  /**  The remove button. */
  private JButton removeButton;

  /**  The up button. */
  private JButton upButton;

  /**  The down button. */
  private JButton downButton;

  /**  The available panels. */
  private Vector avPans;

  /**  The selected panels. */
  private Vector selPans;

  /**  The current available panel. */
  private int curAv = -1;

  /**  The current selected panel. */
  private int curSel = -1;

  /**  The panels directory. */
  private static String PAN_DIR = Frontend.IZPACK_HOME + "bin" + File.separator + "panels";


  /**
   *  The constructor.
   *
   * @param  installation  The isntallation XML tree.
   * @param  icons         The icons database.
   * @param  langpack      The language pack.
   */
  public FrontendPanTab(XMLElement installation, IconsDatabase icons,
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

    label = new JLabel(langpack.getString("tabs.pan.available"));
    FrontendFrame.buildConstraints(gbConstraints, 0, 0, 1, 1, 0.4, 0.0);
    gbConstraints.fill = GridBagConstraints.NONE;
    gbConstraints.anchor = GridBagConstraints.SOUTHWEST;
    layout.addLayoutComponent(label, gbConstraints);
    add(label);

    label = new JLabel(langpack.getString("tabs.pan.selected"));
    FrontendFrame.buildConstraints(gbConstraints, 2, 0, 1, 1, 0.4, 0.0);
    layout.addLayoutComponent(label, gbConstraints);
    add(label);

    availableList = new JList();
    availableList.setCellRenderer(FrontendFrame.LIST_RENDERER);
    availableList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    scroller = new JScrollPane(availableList);
    scroller.setPreferredSize(new Dimension(40, 150));
    FrontendFrame.buildConstraints(gbConstraints, 0, 1, 1, 4, 0.4, 0.0);
    gbConstraints.fill = GridBagConstraints.BOTH;
    gbConstraints.anchor = GridBagConstraints.CENTER;
    layout.addLayoutComponent(scroller, gbConstraints);
    add(scroller);

    selectedList = new JList();
    selectedList.setCellRenderer(FrontendFrame.LIST_RENDERER);
    selectedList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    scroller = new JScrollPane(selectedList);
    scroller.setPreferredSize(new Dimension(40, 150));
    FrontendFrame.buildConstraints(gbConstraints, 2, 1, 1, 4, 0.4, 0.0);
    layout.addLayoutComponent(scroller, gbConstraints);
    add(scroller);

    includeButton = ButtonFactory.createButton(langpack.getString("tabs.pan.include"),
      icons.getImageIcon("forward"),
      FrontendFrame.buttonsHColor);
    includeButton.addActionListener(this);
    FrontendFrame.buildConstraints(gbConstraints, 1, 1, 1, 1, 0.2, 0.0);
    gbConstraints.fill = GridBagConstraints.HORIZONTAL;
    gbConstraints.anchor = GridBagConstraints.NORTH;
    layout.addLayoutComponent(includeButton, gbConstraints);
    add(includeButton);

    removeButton = ButtonFactory.createButton(langpack.getString("tabs.pan.remove"),
      icons.getImageIcon("back"),
      FrontendFrame.buttonsHColor);
    removeButton.addActionListener(this);
    FrontendFrame.buildConstraints(gbConstraints, 1, 4, 1, 1, 0.2, 0.0);
    gbConstraints.anchor = GridBagConstraints.SOUTH;
    layout.addLayoutComponent(removeButton, gbConstraints);
    add(removeButton);

    upButton = ButtonFactory.createButton("",
      icons.getImageIcon("up"),
      FrontendFrame.buttonsHColor);
    upButton.addActionListener(this);
    FrontendFrame.buildConstraints(gbConstraints, 1, 2, 1, 1, 0.2, 0.0);
    gbConstraints.anchor = GridBagConstraints.SOUTHEAST;
    gbConstraints.fill = GridBagConstraints.NONE;
    layout.addLayoutComponent(upButton, gbConstraints);
    add(upButton);

    downButton = ButtonFactory.createButton("",
      icons.getImageIcon("down"),
      FrontendFrame.buttonsHColor);
    downButton.addActionListener(this);
    FrontendFrame.buildConstraints(gbConstraints, 1, 3, 1, 1, 0.2, 0.0);
    gbConstraints.anchor = GridBagConstraints.NORTHEAST;
    layout.addLayoutComponent(downButton, gbConstraints);
    add(downButton);

    label = new JLabel(langpack.getString("tabs.pan.tip"),
      icons.getImageIcon("tip"),
      JLabel.TRAILING);
    FrontendFrame.buildConstraints(gbConstraints, 0, 5, 3, 1, 0.0, 0.0);
    gbConstraints.fill = GridBagConstraints.NONE;
    gbConstraints.anchor = GridBagConstraints.SOUTHWEST;
    layout.addLayoutComponent(label, gbConstraints);
    add(label);

    initLists();
  }


  /**  Updates the components.  */
  public void updateComponents()
  {
    // We fill the lists
    availableList.setListData(avPans);
    selectedList.setListData(selPans);

    // We select the items
    if (curAv != -1)
      availableList.setSelectedIndex(curAv);
    if (curSel != -1)
      selectedList.setSelectedIndex(curSel);
  }


  /**  Initialises the lists.  */
  private void initLists()
  {
    try
    {
      // We get the panels list
      File dir = new File(PAN_DIR);
      String[] panels = dir.list(new FilenameFilter() {
        public boolean accept(File dir, String name) {
          return !name.endsWith(".jar");
        }
      });

      // We initialize our vectors
      avPans = new Vector();
      int size = panels.length;
      for (int i = 0; i < size; i++)
        avPans.add(panels[i]);
      selPans = new Vector();
    }
    catch (Exception err)
    {
      err.printStackTrace();
      JOptionPane.showMessageDialog(this, err.toString(),
        langpack.getString("frontend.error"),
        JOptionPane.ERROR_MESSAGE);
    }
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
    if (source == includeButton)
    {
      // We get the selected index
      int index = availableList.getSelectedIndex();
      if (index == -1)
        return;

      // We make the transfer
      selPans.add(avPans.get(index));
      avPans.removeElementAt(index);

      // We keep a selected item (can be -1 if nothing left)
      curAv = (index < (avPans.size() - 1)) ? index : avPans.size() - 1;
    }
    else if (source == removeButton)
    {
      // We get the selected index
      int index = selectedList.getSelectedIndex();
      if (index == -1)
        return;

      // We make the transfer
      avPans.add(selPans.get(index));
      selPans.removeElementAt(index);

      // We keep a selected item (can be -1 if nothing left)
      curSel = (index < (selPans.size() - 1)) ? index : selPans.size() - 1;
    }
    else if (source == upButton)
    {
      // We get the selected index
      int index = selectedList.getSelectedIndex();
      if (index <= 0)
        return;

      // We swap
      Object e1 = selPans.get(index);
      Object e2 = selPans.get(index - 1);
      selPans.set(index - 1, e1);
      selPans.set(index, e2);

      // We keep the selection
      curSel = index - 1;
    }
    else if (source == downButton)
    {
      // We get the selected index
      int index = selectedList.getSelectedIndex();
      if ((index == -1) || (index == (selPans.size() - 1)))
        return;

      // We swap
      Object e1 = selPans.get(index);
      Object e2 = selPans.get(index + 1);
      selPans.set(index + 1, e1);
      selPans.set(index, e2);

      // We keep the selection
      curSel = index + 1;
    }

    updateComponents();
  }


  /**  Updates the central XML tree.  */
  public void updateXMLTree()
  {
    // We get our base root and we clear it
    XMLElement root = installation.getFirstChildNamed("panels");
    int size = root.getChildrenCount();
    int i;
    for (i = 0; i < size; i++)
      root.removeChildAtIndex(0);

    // We put our selected panels
    size = selPans.size();
    for (i = 0; i < size; i++)
    {
      XMLElement pan = new XMLElement("panel");
      pan.setAttribute("classname", (String) selPans.get(i));
      root.addChild(pan);
    }
  }


  /**
   *  Called when the installation XML tree is changed.
   *
   * @param  newXML  The new XML tree.
   */
  public void installationUpdated(XMLElement newXML)
  {
    this.installation = newXML;

    // We get the data panels list
    XMLElement root = installation.getFirstChildNamed("panels");
    Vector packs = root.getChildrenNamed("panel");

    // We adapt to the data
    initLists();
    int size = packs.size();
    for (int i = 0; i < size; i++)
    {
      XMLElement el = (XMLElement) packs.get(i);
      String cn = el.getAttribute("classname");
      selPans.add(cn);
      int index = avPans.indexOf(cn);
      if (index != -1)
        avPans.removeElementAt(index);
    }

    updateComponents();
  }
}

