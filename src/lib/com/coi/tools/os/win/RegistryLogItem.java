/*
 *  $Id$
 *  COIOSHelper
 *  Copyright (C) 2005 Klaus Bartz
 *
 *  File :               RegistryLogItem.java
 *  Description :        Container for registry data.
 *                       
 *  Author's email :     bartzkau@users.berlios.de
 *  Website :            http://www.izforge.com
 * 
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
package com.coi.tools.os.win;

import java.io.Serializable;

/**
 * Data container for Windows registry logging. This container is used to hold old and new created
 * registry data used at rewinding the registry changes.
 * 
 * @author Klaus Bartz
 *  
 */
public class RegistryLogItem implements Cloneable, Serializable
{

    /** Types of log items */
    public static final int REMOVED_KEY = 1;

    public static final int CREATED_KEY = 2;

    public static final int REMOVED_VALUE = 3;

    public static final int CREATED_VALUE = 4;

    public static final int CHANGED_VALUE = 5;

    private int type;

    private int root;

    private String key;

    private String valueName;

    private RegDataContainer newValue = null;

    private RegDataContainer oldValue = null;

    /**
     * Default constructor.
     */
    private RegistryLogItem()
    {
        super();
    }

    /**
     * Constructor with settings.
     * 
     * @param type
     *            type of loging item. Possible are REMOVED_KEY, CREATED_KEY, REMOVED_VALUE,
     *            CREATED_VALUE and CHANGED_VALUE
     * @param root
     *            id for the registry root
     * @param key
     *            key name of the item which should be logged
     * @param valueName
     *            name of the value of the item which should be logged if it is a value type, else
     *            null
     * @param newValue
     *            new value of the registry entry if it is a value type, else null
     * @param oldValue
     *            old value of the registry entry if it is a value type, else null
     */
    public RegistryLogItem(int type, int root, String key, String valueName,
            RegDataContainer newValue, RegDataContainer oldValue)
    {
        this.type = type;
        this.root = root;
        this.key = key;
        this.valueName = valueName;
        this.newValue = newValue;
        this.oldValue = oldValue;
    }

    /**
     * Returns the key name of this logging item.
     * 
     * @return the key name of this logging item
     */
    public String getKey()
    {
        return key;
    }

    /**
     * Returns the new value of this logging item.
     * 
     * @return the new value of this logging item
     */
    public RegDataContainer getNewValue()
    {
        return newValue;
    }

    /**
     * Returns the old value of this logging item.
     * 
     * @return the old value of this logging item
     */
    public RegDataContainer getOldValue()
    {
        return oldValue;
    }

    /**
     * Returns the root id of this logging item.
     * 
     * @return the root id of this logging item
     */
    public int getRoot()
    {
        return root;
    }

    /**
     * Returns the type id of this logging item.
     * 
     * @return the type id of this logging item
     */
    public int getType()
    {
        return type;
    }

    /**
     * Returns the value name of this logging item.
     * 
     * @return the value name of this logging item
     */
    public String getValueName()
    {
        return valueName;
    }

    /**
     * Sets the key name to the given string
     * 
     * @param string
     *            to be used as key name
     */
    public void setKey(String string)
    {
        key = string;
    }

    /**
     * Sets the new value to the given RegDataContainer.
     * 
     * @param container
     *            to be used as new value
     */
    public void setNewValue(RegDataContainer container)
    {
        newValue = container;
    }

    /**
     * Sets the old value to the given RegDataContainer.
     * 
     * @param container
     *            to be used as old value
     */
    public void setOldValue(RegDataContainer container)
    {
        oldValue = container;
    }

    /**
     * Sets the root id for this logging item.
     * 
     * @param i
     *            root id to be used for this logging item
     */
    public void setRoot(int i)
    {
        root = i;
    }

    /**
     * Sets the type id for this logging item.
     * 
     * @param i
     *            type id to be used for this logging item
     */
    public void setType(int i)
    {
        type = i;
    }

    /**
     * Sets the value name to the given string
     * 
     * @param string
     *            to be used as value name
     */
    public void setValueName(String string)
    {
        valueName = string;
    }

    public Object clone() throws CloneNotSupportedException
    {
        RegistryLogItem retval = (RegistryLogItem) super.clone();
        if (newValue != null) retval.newValue = (RegDataContainer) newValue.clone();
        if (oldValue != null) retval.oldValue = (RegDataContainer) oldValue.clone();
        return (retval);

    }
}