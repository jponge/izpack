/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 *
 * Copyright 2007 Dennis Reil
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.installer;

import java.awt.Color;
import java.awt.Component;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;


/**
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 * @version $Id: $
 */
public class ConditionHistoryTableCellRenderer extends DefaultTableCellRenderer
{           
    private static final long serialVersionUID = 6779914244548965230L;
    private Map conditionhistory;
    
    public ConditionHistoryTableCellRenderer(Map conditionhistory) {
        this.conditionhistory = conditionhistory;
    }
    
    
    /* (non-Javadoc)
     * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
     */
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column)
    {
        JComponent comp = null;                               
        
        ConditionHistory ch = (ConditionHistory) value;               
        
        JLabel label = new JLabel(); 
        label.setAutoscrolls(true);
        comp = label;            
        
        label.setText(ch.toString());
       
        comp.setOpaque(true);
        if (ch.isNewcondition()) {
            comp.setBackground(Color.green);
        }
        else if(ch.isChangedcondition()) {
            comp.setBackground(Color.yellow);
        }
        return comp;
    }
    
    public void clearState() {        
        for (Iterator iterator = conditionhistory.keySet().iterator(); iterator.hasNext();)
        {
            ConditionHistory ch = (ConditionHistory) conditionhistory.get(iterator.next());
            ch.clearState();            
        }
    }            
}


