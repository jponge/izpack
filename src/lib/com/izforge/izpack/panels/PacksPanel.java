/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2003 Julien Ponge
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

import com.izforge.izpack.*;
import com.izforge.izpack.installer.*;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.*;

import java.util.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import net.n3.nanoxml.*;

/**
 *  The packs selection panel class.
 *
 * @author     Julien Ponge,Jan Blok
 * @created    November 1, 2002
 */
public class PacksPanel extends IzPanel implements ActionListener,ListSelectionListener
{
  /**  The space label. */
  private JLabel spaceLabel;

  /**  The tip label. */
  private JTextArea descriptionArea;

  /**  The tablescroll. */
  private JScrollPane tableScroller;

  /**  The bytes of the current pack. */
  protected int bytes = 0;

  /**  The packs table. */
  private JTable packsTable;

  /**
   *  The constructor.
   *
   * @param  parent  The parent window.
   * @param  idata   The installation data.
   */
  public PacksPanel(InstallerFrame parent, InstallData idata)
  {
    super(parent, idata);

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    JLabel infoLabel = new JLabel(parent.langpack.getString("PacksPanel.info"), parent.icons.getImageIcon("preferences"), JLabel.TRAILING);
    add(infoLabel);

    add(Box.createRigidArea(new Dimension(0, 3)));

    JLabel tipLabel = new JLabel(parent.langpack.getString("PacksPanel.tip"), parent.icons.getImageIcon("tip"), JLabel.TRAILING);
    add(tipLabel);

    add(Box.createRigidArea(new Dimension(0, 5)));

    packsTable = new JTable();
    packsTable.setIntercellSpacing(new Dimension(0,0));
    packsTable.setBackground(Color.white);
    packsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    packsTable.getSelectionModel().addListSelectionListener(this);
    packsTable.setShowGrid(false);
    tableScroller = new JScrollPane(packsTable);
    tableScroller.setAlignmentX(LEFT_ALIGNMENT);
    tableScroller.getViewport().setBackground(Color.white);
    tableScroller.setPreferredSize(new Dimension(300,(idata.guiPrefs.height/3+30)));
    add(tableScroller);

    descriptionArea = new JTextArea();
    descriptionArea.setMargin(new Insets(2,2,2,2));
    descriptionArea.setAlignmentX(LEFT_ALIGNMENT);
    descriptionArea.setCaretPosition(0);
    descriptionArea.setEditable(false);
    descriptionArea.setEditable(false);
    descriptionArea.setOpaque(false);
    descriptionArea.setLineWrap(true);
    descriptionArea.setWrapStyleWord(true);
    descriptionArea.setBorder(BorderFactory.createTitledBorder(parent.langpack.getString("PacksPanel.description")));
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


  /**  Called when the panel becomes active.  */
  public void panelActivate()
  {
    try
    {
      packsTable.setModel(new PacksModel(idata.availablePacks, idata.selectedPacks));
      CheckBoxEditorRenderer packSelectedRenderer = new CheckBoxEditorRenderer(false);
      packsTable.getColumnModel().getColumn(0).setCellRenderer(packSelectedRenderer);
      CheckBoxEditorRenderer packSelectedEditor = new CheckBoxEditorRenderer(true);
      packsTable.getColumnModel().getColumn(0).setCellEditor(packSelectedEditor);
      packsTable.getColumnModel().getColumn(0).setMaxWidth(40);
      DefaultTableCellRenderer renderer1 = new DefaultTableCellRenderer(){
        public void setBorder(Border b)
        {
        }
      };
      packsTable.getColumnModel().getColumn(1).setCellRenderer(renderer1);
      DefaultTableCellRenderer renderer2 = new DefaultTableCellRenderer(){
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
    }
    catch (Exception e)
    {
        e.printStackTrace();
    }
    showSpaceRequired();
  }


  /**  Sets the label text of space required for installation.  */
  protected void showSpaceRequired()
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

    private class PacksModel extends AbstractTableModel
    {
        private List packs;
        private List packsToInstall;
        public PacksModel(List packs,List packsToInstall)
        {
            this.packs = packs;
            this.packsToInstall = packsToInstall;
        }

        /*
         * @see TableModel#getRowCount()
         */
        public int getRowCount()
        {
            return packs.size();
        }

        /*
         * @see TableModel#getColumnCount()
         */
        public int getColumnCount()
        {
            return 3;
        }

        /*
         * @see TableModel#getColumnClass(int)
         */
        public Class getColumnClass(int columnIndex)
        {
            switch(columnIndex)
            {
                case 0:
                    return Integer.class;

                default :
                    return String.class;
            }
        }

        /*
         * @see TableModel#isCellEditable(int, int)
         */
        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            Pack pack = (Pack) packs.get(rowIndex);
            if (pack.required)
            {
                return false;
            }
            else if (columnIndex == 0)
            {
                return true;
            }
            else
            {
                return false;
            }
        }

        /*
         * @see TableModel#getValueAt(int, int)
         */
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            Pack pack = (Pack) packs.get(rowIndex);
            switch(columnIndex)
            {
                case 0:
                    int val = 0;
                    if (pack.required)
                    {
                        val = -1;
                    }
                    else
                    {
                        val = (packsToInstall.contains(pack) ? 1 : 0 );
                    }
                    return new Integer( val );

                case 1:
                    return pack.name;

                case 2:
                    return Pack.toByteUnitsString((int)pack.nbytes);

                default :
                    return null;
            }
        }

        /*
         * @see TableModel#setValueAt(Object, int, int)
         */
        public void setValueAt(Object aValue, int rowIndex, int columnIndex)
        {
            if (columnIndex == 0)
            {
                if (aValue instanceof Integer)
                {
                    Pack pack = (Pack) packs.get(rowIndex);
                    if (((Integer)aValue).intValue() == 1)
                    {
                      packsToInstall.add(pack);
                      bytes += pack.nbytes;
                    }
                    else
                    {
                      packsToInstall.remove(pack);
                      bytes -= pack.nbytes;
                    }
                    showSpaceRequired();
                }
            }
        }
    }
    /**
     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    public void valueChanged(ListSelectionEvent e)
    {
        int i = packsTable.getSelectedRow();
        if (i >= 0)
        {
            Pack pack = (Pack) idata.availablePacks.get(i);
            descriptionArea.setText(pack.description);
        }
    }

    static class CheckBoxEditorRenderer extends AbstractCellEditor implements TableCellRenderer,TableCellEditor,ActionListener
    {
        private JCheckBox display;
        public CheckBoxEditorRenderer(boolean useAsEditor)
        {
            display = new JCheckBox();
            display.setHorizontalAlignment(JLabel.CENTER);
            if (useAsEditor) display.addActionListener(this);

        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                               boolean isSelected, boolean hasFocus, int row, int column) 
        {
          if (isSelected) {
            display.setForeground(table.getSelectionForeground());
            display.setBackground(table.getSelectionBackground());
          }
          else {
            display.setForeground(table.getForeground());
            display.setBackground(table.getBackground());
          }
          int state = ((Integer)value).intValue();
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
            return getTableCellRendererComponent(table, value, isSelected, false, row, column);
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
