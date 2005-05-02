/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2004 Julien Ponge
 *
 *  File :               PacksModel.java
 *  Description :        A table model for packs.
 *  Author's email :     julien@izforge.com
 *  Author's Website :   http://www.izforge.com
 *
 *  Portions are Copyright (C) 2002 Marcus Wolschon
 *  Portions are Copyright (C) 2002 Jan Blok (jblok@profdata.nl - PDM - www.profdata.nl)
 *  Portions are Copyright (C) 2004 Gaganis Giorgos (gaganis@users.berlios.de)
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import com.izforge.izpack.LocaleDatabase;
import com.izforge.izpack.Pack;

/**
 * User: Gaganis Giorgos Date: Sep 17, 2004 Time: 8:33:21 AM
 */
class PacksModel extends AbstractTableModel
{

    /**
     * 
     */
    private static final long serialVersionUID = 3258128076746733110L;

    private List packs;

    private List packsToInstall;

    private PacksPanelInterface panel;

    private LocaleDatabase langpack;

    // This is used to represent the status of the checkbox
    private int[] checkValues;

    // Map to hold the object name relationship
    Map namesObj;

    // Map to hold the object name relationship
    Map namesPos;

    public PacksModel(List packs, List packsToInstall, PacksPanelInterface panel)
    {

        this.packs = packs;
        this.packsToInstall = packsToInstall;
        this.panel = panel;
        langpack = panel.getLangpack();
        checkValues = new int[packs.size()];
        reverseDeps();
        initvalues();
    }

    /**
     * Creates the reverse dependency graph
     */
    private void reverseDeps()
    {
        // name to pack map
        namesObj = new HashMap();
        for (int i = 0; i < packs.size(); i++)
        {
            Pack pack = (Pack) packs.get(i);
            namesObj.put(pack.name, pack);
        }
        // process each pack
        for (int i = 0; i < packs.size(); i++)
        {
            Pack pack = (Pack) packs.get(i);
            List deps = pack.dependencies;
            for (int j = 0; deps != null && j < deps.size(); j++)
            {
                String name = (String) deps.get(j);
                Pack parent = (Pack) namesObj.get(name);
                parent.addRevDep(pack.name);
            }
        }

    }

    private void initvalues()
    {
        // name to pack position map
        namesPos = new HashMap();
        for (int i = 0; i < packs.size(); i++)
        {
            Pack pack = (Pack) packs.get(i);
            namesPos.put(pack.name, new Integer(i));
        }
        // Init to the first values
        for (int i = 0; i < packs.size(); i++)
        {
            Pack pack = (Pack) packs.get(i);
            if (packsToInstall.contains(pack)) checkValues[i] = 1;
        }
        // Check out and disable the ones that are excluded by non fullfiled
        // deps
        for (int i = 0; i < packs.size(); i++)
        {
            Pack pack = (Pack) packs.get(i);
            if (checkValues[i] == 0)
            {
                List deps = pack.revDependencies;
                for (int j = 0; deps != null && j < deps.size(); j++)
                {
                    String name = (String) deps.get(j);
                    int pos = getPos(name);
                    checkValues[pos] = -2;
                }
            }
        }
        // The required ones must propagate their required status to all the
        // ones
        // that they depend on
        for (int i = 0; i < packs.size(); i++)
        {
            Pack pack = (Pack) packs.get(i);
            if (pack.required == true) propRequirement(pack.name);
        }
    }

    private void propRequirement(String name)
    {

        final int pos = getPos(name);
        checkValues[pos] = -1;
        List deps = ((Pack) packs.get(pos)).dependencies;
        for (int i = 0; deps != null && i < deps.size(); i++)
        {
            String s = (String) deps.get(i);
            propRequirement(s);
        }

    }

    /**
     * Given a map of names and Integer for position and a name it return the
     * position of this name as an int
     * 
     * @return position of the name
     */
    private int getPos(String name)
    {
        return ((Integer) namesPos.get(name)).intValue();
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
        case 0:
            return Integer.class;

        default:
            return String.class;
        }
    }

    /*
     * @see TableModel#isCellEditable(int, int)
     */
    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        if (checkValues[rowIndex] < 0)
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
        switch (columnIndex)
        {
        case 0:

            return new Integer(checkValues[rowIndex]);

        case 1:

            if (langpack == null || pack.id == null || pack.id.equals(""))
            {
                return pack.name;
            }
            else
            {
                return langpack.getString(pack.id);
            }

        case 2:
            return Pack.toByteUnitsString((int) pack.nbytes);

        default:
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
                    checkValues[rowIndex] = 1;
                    updateDeps();

                    int bytes = panel.getBytes();
                    bytes += pack.nbytes;
                    panel.setBytes(bytes);
                }
                else
                {
                    checkValues[rowIndex] = 0;
                    updateDeps();

                    int bytes = panel.getBytes();
                    bytes -= pack.nbytes;
                    panel.setBytes(bytes);
                }
                fireTableDataChanged();
                refreshPacksToInstall();
                panel.showSpaceRequired();
            }
        }
    }

    private void refreshPacksToInstall()
    {

        packsToInstall.clear();
        for (int i = 0; i < packs.size(); i++)
        {
            Object pack = packs.get(i);
            if (Math.abs(checkValues[i]) == 1) packsToInstall.add(pack);

        }

    }

    /**
     * This function updates the checkboxes after a change by disabling packs
     * that cannot be installed anymore and enabling those that can after the
     * change. This is accomplished by running a search that pinpoints the packs
     * that must be disabled by a non-fullfiled dependency.
     */
    private void updateDeps()
    {
        int[] statusArray = new int[packs.size()];
        for (int i = 0; i < statusArray.length; i++)
        {
            statusArray[i] = 0;
        }
        dfs(statusArray);
        for (int i = 0; i < statusArray.length; i++)
        {
            if (statusArray[i] == 0 && checkValues[i] < 0) checkValues[i] += 2;
            if (statusArray[i] == 1 && checkValues[i] >= 0) checkValues[i] = -2;

        }
        // The required ones must propagate their required status to all the
        // ones
        // that they depend on
        for (int i = 0; i < packs.size(); i++)
        {
            Pack pack = (Pack) packs.get(i);
            if (pack.required == true) propRequirement(pack.name);
        }

    }

    /**
     * We use a modified dfs graph search algorithm as described in: Thomas H.
     * Cormen, Charles Leiserson, Ronald Rivest and Clifford Stein. Introduction
     * to algorithms 2nd Edition 540-549,MIT Press, 2001
     */
    private int dfs(int[] status)
    {
        for (int i = 0; i < packs.size(); i++)
        {
            for (int j = 0; j < packs.size(); j++)
            {
                ((Pack) packs.get(j)).color = Pack.WHITE;
            }
            Pack pack = (Pack) packs.get(i);
            boolean wipe = false;

            if (dfsVisit(pack, status, wipe) != 0) return -1;

        }
        return 0;
    }

    private int dfsVisit(Pack u, int[] status, boolean wipe)
    {
        u.color = Pack.GREY;
        int check = checkValues[getPos(u.name)];

        if (Math.abs(check) != 1)
        {
            wipe = true;
        }
        List deps = u.revDependencies;
        if (deps != null)
        {
            for (int i = 0; i < deps.size(); i++)
            {
                String name = (String) deps.get(i);
                Pack v = (Pack) namesObj.get(name);
                if (wipe)
                {
                    status[getPos(v.name)] = 1;
                }
                if (v.color == Pack.WHITE)
                {

                    final int result = dfsVisit(v, status, wipe);
                    if (result != 0) return result;
                }
            }
        }
        u.color = Pack.BLACK;
        return 0;
    }
}
