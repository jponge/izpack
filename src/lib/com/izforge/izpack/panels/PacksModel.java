package com.izforge.izpack.panels;

import com.izforge.izpack.Pack;
import com.izforge.izpack.LocaleDatabase;

import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
 * User: Gaganis Giorgos
 * Date: Sep 17, 2004
 * Time: 8:33:21 AM
 */
class PacksModel extends AbstractTableModel
{
  private List packs;
  private List packsToInstall;
  private PacksPanelInterface panel;
  private LocaleDatabase langpack;
  public PacksModel(List packs, List packsToInstall,PacksPanelInterface panel)
  {

    this.packs = packs;
    this.packsToInstall = packsToInstall;
    this.panel = panel;
    langpack = panel.getLangpack();
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

        if (langpack == null || pack.id == null || pack.id.equals("")){
          return pack.name;
        }else{
          return langpack.getString(pack.id);
        }

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

          int bytes = panel.getBytes();
          bytes += pack.nbytes;
          panel.setBytes(bytes);
        } else
        {
          packsToInstall.remove(pack);

          int bytes = panel.getBytes();
          bytes -= pack.nbytes;
          panel.setBytes(bytes);
        }
        panel.showSpaceRequired();
      }
    }
  }
}