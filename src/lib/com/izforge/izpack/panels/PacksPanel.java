/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2004 Julien Ponge
 *
 *  File :               PacksPanel.java
 *  Description :        A panel to select the packs to install.
 *  Author's email :     julien@izforge.com
 *  Author's Website :   http://www.izforge.com
 *
 *  Portions are Copyright (C) 2002 Marcus Wolschon
 *  Portions are Copyright (C) 2002 Jan Blok (jblok@profdata.nl - PDM - www.profdata.nl)
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import net.n3.nanoxml.XMLElement;

import com.izforge.izpack.LocaleDatabase;
import com.izforge.izpack.Pack;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.installer.ResourceManager;

/**
 *  The packs selection panel class.
 *
 * @author     Julien Ponge
 * @author     Jan Blok
 */
public class PacksPanel
  extends IzPanel
  implements ActionListener, ListSelectionListener,PacksPanelInterface
{
  /**  The space label. */
  private JLabel spaceLabel;

  /**  The tip label. */
  private JTextArea descriptionArea;
  /**  The dependencies label. */
  private JTextArea dependencyArea;

  /** Map that connects names with pack objects */
  private Map names;
  /**  The tablescroll. */
  private JScrollPane tableScroller;

  /**  The bytes of the current pack. */
  protected int bytes = 0;

  /**  The packs table. */
  private JTable packsTable;
  
  /** The packs locale database. */
  protected LocaleDatabase langpack = null;
  
  /** The name of the XML file that specifies the panel langpack */
  private static final String LANG_FILE_NAME = "packsLang.xml";

  /**
   *  The constructor.
   *
   * @param  parent  The parent window.
   * @param  idata   The installation data.
   */
  public PacksPanel(InstallerFrame parent, InstallData idata)
  {
    super(parent, idata);
  
    try
    {
      String resource = LANG_FILE_NAME + "_" + idata.localeISO3;
      this.langpack = new LocaleDatabase(ResourceManager.getInstance().getInputStream(resource));
    }
    catch (Throwable exception)
    {}

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    JLabel infoLabel =
      new JLabel(
        parent.langpack.getString("PacksPanel.info"),
        parent.icons.getImageIcon("preferences"),
        JLabel.TRAILING);
    add(infoLabel);

    add(Box.createRigidArea(new Dimension(0, 3)));

    JLabel tipLabel =
      new JLabel(
        parent.langpack.getString("PacksPanel.tip"),
        parent.icons.getImageIcon("tip"),
        JLabel.TRAILING);
    add(tipLabel);

    add(Box.createRigidArea(new Dimension(0, 5)));

    packsTable = new JTable();
    packsTable.setIntercellSpacing(new Dimension(0, 0));
    packsTable.setBackground(Color.white);
    packsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    packsTable.getSelectionModel().addListSelectionListener(this);
    packsTable.setShowGrid(false);
    tableScroller = new JScrollPane(packsTable);
    tableScroller.setAlignmentX(LEFT_ALIGNMENT);
    tableScroller.getViewport().setBackground(Color.white);
    tableScroller.setPreferredSize(
      new Dimension(300, (idata.guiPrefs.height / 3 + 30)));
    add(tableScroller);

    dependencyArea = new JTextArea();
    dependencyArea.setMargin(new Insets(2, 2, 2, 2));
    dependencyArea.setAlignmentX(LEFT_ALIGNMENT);
    dependencyArea.setCaretPosition(0);
    dependencyArea.setEditable(false);
    dependencyArea.setEditable(false);
    dependencyArea.setOpaque(false);
    dependencyArea.setLineWrap(true);
    dependencyArea.setWrapStyleWord(true);
    dependencyArea.setBorder(
            BorderFactory.createTitledBorder(
                    parent.langpack.getString("PacksPanel.dependencyList")));
    add(dependencyArea);

    descriptionArea = new JTextArea();
    descriptionArea.setMargin(new Insets(2, 2, 2, 2));
    descriptionArea.setAlignmentX(LEFT_ALIGNMENT);
    descriptionArea.setCaretPosition(0);
    descriptionArea.setEditable(false);
    descriptionArea.setEditable(false);
    descriptionArea.setOpaque(false);
    descriptionArea.setLineWrap(true);
    descriptionArea.setWrapStyleWord(true);
    descriptionArea.setBorder(
      BorderFactory.createTitledBorder(
        parent.langpack.getString("PacksPanel.description")));
    add(descriptionArea);



    JPanel spacePanel = new JPanel();
    spacePanel.setAlignmentX(LEFT_ALIGNMENT);
    spacePanel.setLayout(new BoxLayout(spacePanel, BoxLayout.X_AXIS));
    spacePanel.add(new JLabel(parent.langpack.getString("PacksPanel.space")));
    spacePanel.add(Box.createHorizontalGlue());
    spaceLabel = new JLabel("");
    //    spaceLabel.setFont(new Font("Monospaced",Font.PLAIN,11));
    spacePanel.add(spaceLabel);
    add(spacePanel);
  }
  public void createMap(List packs)
  {
    names = new HashMap();
    for (int i = 0; i < packs.size(); i++)
    {
      Pack pack = (Pack) packs.get(i);
      names.put(pack.name,pack);
    }
  }

  /**  Called when the panel becomes active.  */
  public void panelActivate()
  {
    //init the map
    createMap(idata.availablePacks);
    try
    {
      packsTable.setModel(
        new PacksModel(idata.availablePacks, idata.selectedPacks,this));
      CheckBoxEditorRenderer packSelectedRenderer =
        new CheckBoxEditorRenderer(false);
      packsTable.getColumnModel().getColumn(0).setCellRenderer(
        packSelectedRenderer);
      CheckBoxEditorRenderer packSelectedEditor =
        new CheckBoxEditorRenderer(true);
      packsTable.getColumnModel().getColumn(0).setCellEditor(
        packSelectedEditor);
      packsTable.getColumnModel().getColumn(0).setMaxWidth(40);
      DefaultTableCellRenderer renderer1 = new DefaultTableCellRenderer()
      {
        public void setBorder(Border b)
        {
        }
      };
      packsTable.getColumnModel().getColumn(1).setCellRenderer(renderer1);
      DefaultTableCellRenderer renderer2 = new DefaultTableCellRenderer()
      {
        public void setBorder(Border b)
        {
        }

          //          public void setFont(Font f)
    //          {
    //              super.setFont(new Font("Monospaced",Font.PLAIN,11));
    //          }
  };
      renderer2.setHorizontalAlignment(JLabel.RIGHT);
      packsTable.getColumnModel().getColumn(2).setCellRenderer(renderer2);
      packsTable.getColumnModel().getColumn(2).setMaxWidth(100);

      //remove header,so we don't need more strings
      tableScroller.remove(packsTable.getTableHeader());
      tableScroller.setColumnHeaderView(null);
      tableScroller.setColumnHeader(null);

      // set the JCheckBoxes to the currently selected panels. The selection might have changed in another panel
      java.util.Iterator iter = idata.availablePacks.iterator();
      bytes = 0;
      while (iter.hasNext())
      {
        Pack p = (Pack) iter.next();
        if (p.required)
        {
          bytes += p.nbytes;
          continue;
        }
        if (idata.selectedPacks.contains(p))
          bytes += p.nbytes;
      }
    } catch (Exception e)
    {
      e.printStackTrace();
    }
    showSpaceRequired();
  }

  public void setBytes(int bytes)
  {
        this.bytes = bytes;
  }
  public int getBytes()
  {
    return bytes;
  }
  public LocaleDatabase getLangpack()
  {
    return langpack;
  }
  /**  Sets the label text of space required for installation.  */
  public void showSpaceRequired()
  {
    spaceLabel.setText(Pack.toByteUnitsString(bytes));
  }

  /**
   *  Actions-handling method.
   *
   * @param  e  The event.
   */
  public void actionPerformed(ActionEvent e)
  {
  }

  /**
   *  Indicates wether the panel has been validated or not.
   *
   * @return    Always true.
   */
  public boolean isValidated()
  {
    return true;
  }

  /**
   *  Asks to make the XML panel data.
   *
   * @param  panelRoot  The XML tree to write the data in.
   */
  public void makeXMLData(XMLElement panelRoot)
  {
    new PacksPanelAutomationHelper().makeXMLData(idata, panelRoot);
  }


  /**
   * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
   */
  public void valueChanged(ListSelectionEvent e)
  {
    int i = packsTable.getSelectedRow();
    //Operations for the description
    if (i >= 0)
    {
      Pack pack = (Pack) idata.availablePacks.get(i);
      String desc = "";
      String key = pack.id+".description";
      if (langpack != null && pack.id != null && !pack.id.equals(""))
      {
        desc = langpack.getString(key);
      }
      if (desc.equals("") || key.equals(desc))
      {
      	desc = pack.description;
      }
      descriptionArea.setText(desc);
    }
    //Operation for the dependency listing
    if(i >=0)
    {
      Pack pack = (Pack) idata.availablePacks.get(i);
      List dep = pack.dependencies;
      String list="";
      for (int j = 0; dep != null && j < dep.size(); j++)
      {
        String name = (String)dep.get(j);
        //Internationalization code
        Pack childPack = (Pack)names.get(name);
        String childName = "";
        String key = childPack.id;
        if (langpack != null && childPack.id != null && !childPack.id.equals(""))
        {
          childName = langpack.getString(key);
        }
        if  (childName.equals("") || key.equals(childName))
        {
          childName = childPack.name;
        }
        //End internationalization
        list += childName;
        if(j != dep.size()-1)
          list +=", ";
      }
      dependencyArea.setText(list);
    }
  }

  static class CheckBoxEditorRenderer
    extends AbstractCellEditor
    implements TableCellRenderer, TableCellEditor, ActionListener
  {
    private JCheckBox display;
    public CheckBoxEditorRenderer(boolean useAsEditor)
    {
      display = new JCheckBox();
      display.setHorizontalAlignment(JLabel.CENTER);
      if (useAsEditor)
        display.addActionListener(this);

    }

    public Component getTableCellRendererComponent(
      JTable table,
      Object value,
      boolean isSelected,
      boolean hasFocus,
      int row,
      int column)
    {
      if (isSelected)
      {
        display.setForeground(table.getSelectionForeground());
        display.setBackground(table.getSelectionBackground());
      } else
      {
        display.setForeground(table.getForeground());
        display.setBackground(table.getBackground());
      }
      int state = ((Integer) value).intValue();
      display.setSelected((value != null && Math.abs(state) == 1));
      display.setEnabled(state >= 0);
      return display;
    }
    /**
     * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int, int)
     */
    public Component getTableCellEditorComponent(
      JTable table,
      Object value,
      boolean isSelected,
      int row,
      int column)
    {
      return getTableCellRendererComponent(
        table,
        value,
        isSelected,
        false,
        row,
        column);
    }

    public Object getCellEditorValue()
    {
      return new Integer(display.isSelected() ? 1 : 0);
    }

    public void actionPerformed(ActionEvent e)
    {
      stopCellEditing();
    }
  }
}
