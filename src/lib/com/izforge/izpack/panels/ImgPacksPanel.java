/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2004 Julien Ponge, Volker Friedritz
 *
 *  File : ImgPacksPanel.java
 *  Description : A panel to select the packs to install.
 *  Author's email : julien@izforge.com
 *  Author's email : volker.friedritz@gmx.de
 *  Author's Website : http://www.izforge.com
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

import java.awt.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.*;
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

import com.izforge.izpack.Pack;
import com.izforge.izpack.installer.*;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;

/**
 *  The ImgPacks panel class. Allows the packages selection with a small picture
 *  displayed for every pack.
 *  This new version combines the old PacksPanel and the ImgPacksPanel so that
 *  the positive characteristics of both are combined.
 *
 * @author     Julien Ponge
 * @author     Volker Friedritz
 */
public class ImgPacksPanel extends IzPanel
  implements ActionListener, ListSelectionListener
{
  /**  The space label. */
  private JLabel spaceLabel;

  /**  The description area. */
  private JTextArea descriptionArea;
  
  /**  The description scroll. */
  private JScrollPane descriptionScroller;
  

  /**  The tablescroll. */
  private JScrollPane tableScroller;

  /**  The bytes of the current pack. */
  protected int bytes = 0;

  /**  The packs table. */
  private JTable packsTable;

  /**  The images to display. */
  private ArrayList images;  
  
  /**  The img label. */
  private JLabel imgLabel;
  
  /**  The current image index. */
  private int index = 0;
  
  /**  The layout. */
  private GridBagLayout layout;

  /**  The layout constraints. */
  private GridBagConstraints gbConstraints;

  
  /**
   *  The constructor.
   *
   * @param  parent  The parent window.
   * @param  idata   The installation data.
   */
  public ImgPacksPanel(InstallerFrame parent, InstallData idata)
  {
    super(parent, idata);
    preLoadImages();
    
    layout = new GridBagLayout();
    gbConstraints = new GridBagConstraints();
    
    setLayout(layout);

    JLabel infoLabel =
      new JLabel(
        parent.langpack.getString("PacksPanel.info"),
        parent.icons.getImageIcon("preferences"),
				JLabel.LEFT);
        
    parent.buildConstraints(gbConstraints, 0, 0, 1, 1, 0.25, 0.0);
    gbConstraints.insets = new Insets(5, 5, 5, 5);
    gbConstraints.fill = GridBagConstraints.BOTH;
    gbConstraints.anchor = GridBagConstraints.WEST;
    layout.addLayoutComponent(infoLabel, gbConstraints);
    add(infoLabel);

    JLabel descriptionLabel =
      new JLabel(
        parent.langpack.getString("ImgPacksPanel.snap"),
        parent.icons.getImageIcon("tip"),
        JLabel.LEFT);

    parent.buildConstraints(gbConstraints, 1, 0, 1, 1, 0.50, 0.0);
    layout.addLayoutComponent(descriptionLabel, gbConstraints);
    add(descriptionLabel);
    

    packsTable = new JTable();
    packsTable.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
    packsTable.setIntercellSpacing(new Dimension(0, 0));
    packsTable.setBackground(Color.white);
    packsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    packsTable.getSelectionModel().addListSelectionListener(this);
    packsTable.setShowGrid(false);
    tableScroller = new JScrollPane(packsTable);
    tableScroller.setAlignmentX(LEFT_ALIGNMENT);
    tableScroller.getViewport().setBackground(Color.white);
    tableScroller.setPreferredSize(
      new Dimension(250, (idata.guiPrefs.height / 3 + 30)));

    parent.buildConstraints(gbConstraints, 0, 1, 1, 3, 0.50, 0.0);
    layout.addLayoutComponent(tableScroller, gbConstraints);
    add(tableScroller);
    
    imgLabel = new JLabel((ImageIcon) images.get(0));
    JScrollPane imgScroller = new JScrollPane(imgLabel);
    imgScroller.setPreferredSize(getPreferredSizeFromImages());

    parent.buildConstraints(gbConstraints, 1, 1, 1, 1, 0.5, 1.0);
    layout.addLayoutComponent(imgScroller, gbConstraints);
    add(imgScroller);
    
    Component strut = Box.createVerticalStrut(20);
    parent.buildConstraints(gbConstraints, 1, 2, 1, 3, 0.0, 0.0);
    layout.addLayoutComponent(strut, gbConstraints);
    add(strut);
    
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

    descriptionScroller = new JScrollPane(descriptionArea);
    descriptionScroller.setPreferredSize(new Dimension(200, 60));
    
    parent.buildConstraints(gbConstraints, 1, 3, 1, 1, 0.50, 0.50);
    layout.addLayoutComponent(descriptionScroller, gbConstraints);
    add(descriptionScroller);
    
    JLabel tipLabel =
      new JLabel(
        parent.langpack.getString("PacksPanel.tip"),
        parent.icons.getImageIcon("tip"),
        JLabel.LEFT);
    
    parent.buildConstraints(gbConstraints, 0, 4, 2, 1, 0.0, 0.0);
    layout.addLayoutComponent(tipLabel, gbConstraints);
    add(tipLabel);
    
//    strut = Box.createVerticalStrut(20);
//    parent.buildConstraints(gbConstraints, 0, 5, 1, 1, 0.0, 0.0);
//    layout.addLayoutComponent(strut, gbConstraints);
//    add(strut);    

    
    JPanel spacePanel = new JPanel();
    spacePanel.setAlignmentX(LEFT_ALIGNMENT);
    spacePanel.setLayout(new BoxLayout(spacePanel, BoxLayout.X_AXIS));
    spacePanel.add(new JLabel(parent.langpack.getString("PacksPanel.space")));
    spacePanel.add(Box.createHorizontalGlue());
    spaceLabel = new JLabel("");
    //    spaceLabel.setFont(new Font("Monospaced",Font.PLAIN,11));
    spacePanel.add(spaceLabel);
    
    parent.buildConstraints(gbConstraints, 0, 6, 2, 1, 0.0, 0.0);
    layout.addLayoutComponent(spacePanel, gbConstraints);
    add(spacePanel);
  }

  /**  Pre-loads the images.  */
  private void preLoadImages()
  {
    int size = idata.availablePacks.size();
    images = new ArrayList(size);
    for (int i = 0; i < size; i++)
      try
      {
        URL url =
          ResourceManager.getInstance().getURL("ImgPacksPanel.img." + i);
        ImageIcon img = new ImageIcon(url);
        images.add(img);
      } catch (Exception err)
      {
        err.printStackTrace();
      }
  }

  /**  Try to find a good preferredSize for imgScroller by checking all 
   *   loaded images' width and height.
   */
  private Dimension getPreferredSizeFromImages()
  {
  	int maxWidth = 80, maxHeight = 60;
  	ImageIcon icon;
  	
  	for (Iterator it = images.iterator(); it.hasNext(); )
  	{
  		icon = (ImageIcon) it.next();
  		maxWidth = Math.max(maxWidth, icon.getIconWidth());
  		maxHeight = Math.max(maxHeight, icon.getIconHeight());
  	}
  	
  	maxWidth = Math.min(maxWidth + 20, idata.guiPrefs.width - 150);
  	maxHeight = Math.min(maxHeight + 20, idata.guiPrefs.height - 150);
  	
  	return new Dimension(maxWidth, maxHeight);
  }
  
  
  /**  Called when the panel becomes active.  */
  public void panelActivate()
  {
    try
    {
      packsTable.setModel(
        new PacksModel(idata.availablePacks, idata.selectedPacks));
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
    packsTable.getSelectionModel().setSelectionInterval(0, 0);
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
    new ImgPacksPanelAutomationHelper().makeXMLData(idata, panelRoot);
  }

  private class PacksModel extends AbstractTableModel
  {
    private List packs;
    private List packsToInstall;
    public PacksModel(List packs, List packsToInstall)
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
      switch (columnIndex)
      {
        case 0 :
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
      } else if (columnIndex == 0)
      {
        return true;
      } else
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
      switch (columnIndex)
      {
        case 0 :
          int val = 0;
          if (pack.required)
          {
            val = -1;
          } else
          {
            val = (packsToInstall.contains(pack) ? 1 : 0);
          }
          return new Integer(val);

        case 1 :
          return pack.name;

        case 2 :
          return Pack.toByteUnitsString((int) pack.nbytes);

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
          if (((Integer) aValue).intValue() == 1)
          {
            packsToInstall.add(pack);
            bytes += pack.nbytes;
          } else
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
    if (e.getValueIsAdjusting())
      return;
  	
  	//  We update the the description and the image  	
    int i = packsTable.getSelectedRow();
    if (i >= 0)
    {
      Pack pack = (Pack) idata.availablePacks.get(i);
      descriptionArea.setText(pack.description);
      
      int last = e.getLastIndex();
      int first = e.getFirstIndex();
      index = (index < last) ? last : first;
      imgLabel.setIcon((ImageIcon) images.get(index));
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
