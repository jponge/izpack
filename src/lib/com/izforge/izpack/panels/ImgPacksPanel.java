/*
 * IzPack Version 3.1.0 pre2 (build 2002.10.19)
 * Copyright (C) 2001,2002 Julien Ponge
 *
 * File :               ImgPacksPanel.java
 * Description :        A panel to select the packs to install.
 * Author's email :     julien@izforge.com
 * Author's Website :   http://www.izforge.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.izforge.izpack.panels;

import com.izforge.izpack.*;
import com.izforge.izpack.gui.*;
import com.izforge.izpack.installer.*;

import java.awt.*;
import java.awt.event.*;

import java.net.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import net.n3.nanoxml.*;

public class ImgPacksPanel extends IzPanel implements ActionListener, ListSelectionListener
{
    //.....................................................................
    
    // The fields
    private GridBagLayout layout;
    private GridBagConstraints gbConstraints;
    private JLabel packsLabel;
    private JLabel snapLabel;
    private JLabel spaceLabel;
    private int bytes = 0;
    private JList packsList;
    private JLabel imgLabel;
    private JCheckBox checkBox;
    private JLabel descLabel;
    private ArrayList images;
    private int index = 0;
    
    // The constructor
    public ImgPacksPanel(InstallerFrame parent, InstallData idata)
    {
        super(parent, idata);
        
        // We initialize our layout
        layout = new GridBagLayout();
        gbConstraints = new GridBagConstraints();
        setLayout(layout);
        
        preLoadImages();
        
        packsLabel = new JLabel(parent.langpack.getString("ImgPacksPanel.packs"),
                                parent.icons.getImageIcon("preferences"), JLabel.TRAILING);
        parent.buildConstraints(gbConstraints, 0, 0, 1, 1, 0.25, 0.0);
        gbConstraints.insets = new Insets(5, 5, 5, 5);
        gbConstraints.fill = GridBagConstraints.NONE;
        gbConstraints.anchor = GridBagConstraints.WEST;
        layout.addLayoutComponent(packsLabel, gbConstraints);
        add(packsLabel);
        
        packsLabel = new JLabel(parent.langpack.getString("ImgPacksPanel.snap"),
                                parent.icons.getImageIcon("tip"), JLabel.TRAILING);
        parent.buildConstraints(gbConstraints, 1, 0, 1, 1, 0.75, 0.0);
        layout.addLayoutComponent(packsLabel, gbConstraints);
        add(packsLabel);
        
        packsList = new JList(idata.availablePacks.toArray());
        packsList.addListSelectionListener(this);
        packsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroller1 = new JScrollPane(packsList);
        parent.buildConstraints(gbConstraints, 0, 1, 1, 1, 0.25, 1.0);
        gbConstraints.fill = GridBagConstraints.BOTH;
        gbConstraints.anchor = GridBagConstraints.CENTER;
        layout.addLayoutComponent(scroller1, gbConstraints);
        add(scroller1);
        
        imgLabel = new JLabel( (ImageIcon) images.get(0));
        JScrollPane scroller2 = new JScrollPane(imgLabel);
        parent.buildConstraints(gbConstraints, 1, 1, 1, 1, 0.75, 1.0);
        layout.addLayoutComponent(scroller2, gbConstraints);
        add(scroller2);
        
        checkBox = new JCheckBox(parent.langpack.getString("ImgPacksPanel.checkbox"));
        checkBox.addActionListener(this);
        parent.buildConstraints(gbConstraints, 0, 2, 1, 1, 0.0, 0.0);
        gbConstraints.fill = GridBagConstraints.NONE;
        gbConstraints.anchor = GridBagConstraints.WEST;
        layout.addLayoutComponent(checkBox, gbConstraints);
        add(checkBox);
        
        spaceLabel = new JLabel(parent.langpack.getString("PacksPanel.space"));
        parent.buildConstraints(gbConstraints, 0, 2, 1, 1, 0.0, 0.0);
        gbConstraints.fill = GridBagConstraints.NONE;
        gbConstraints.anchor = GridBagConstraints.EAST;
        layout.addLayoutComponent(spaceLabel, gbConstraints);
        add(spaceLabel);
        
        descLabel = new JLabel("");
        parent.buildConstraints(gbConstraints, 1, 2, 1, 1, 0.0, 0.0);
        gbConstraints.fill = GridBagConstraints.NONE;
        gbConstraints.anchor = GridBagConstraints.WEST;
        layout.addLayoutComponent(descLabel, gbConstraints);
        add(descLabel);
        
        packsList.setSelectedIndex(0);
        
        // We update the checkbox and the description
        Pack pack = (Pack) idata.availablePacks.get(index);
        checkBox.setEnabled(!pack.required);
        checkBox.setSelected(idata.selectedPacks.contains(pack));
        descLabel.setText(pack.description);
        
    }
    
    //.....................................................................
    // The methods

    //sets the label text of space requiered for installation
    private void showSpaceRequired()
    {
        StringBuffer result = new StringBuffer(parent.langpack.getString("PacksPanel.space"));
        result.append(Pack.toByteUnitsString(bytes));
        spaceLabel.setText(result.toString());
    }  
    
    // Called when the panel becomes active 
    public void panelActivate()
    {
            //calculate the bytes required by selected panels
            java.util.Iterator iter = idata.availablePacks.iterator();
            bytes = 0;
            while(iter.hasNext())
            {
                Pack p = (Pack)iter.next();
                if(idata.selectedPacks.contains(p)) 
                {
                    bytes += p.nbytes;
                }
            }
                
        showSpaceRequired();
    }
    
    // Actions-handling method
    public void actionPerformed(ActionEvent e)
    {
        // We select or not the current pack
        Pack pack = (Pack) idata.availablePacks.get(index);
        if (checkBox.isSelected())
        {
            idata.selectedPacks.add(pack);
            bytes += pack.nbytes;
        }
        else
        {
            idata.selectedPacks.remove(idata.selectedPacks.indexOf(pack));   
            bytes -= pack.nbytes;
        }
        showSpaceRequired();
    }
    
    public void valueChanged(ListSelectionEvent e)
    {
        // We update the image
        if (e.getValueIsAdjusting()) return;
        int last = e.getLastIndex();
        int first = e.getFirstIndex();
        index = (index < last) ? last : first;
        imgLabel.setIcon( (ImageIcon) images.get(index));
        
        // We update the checkbox and the description
        Pack pack = (Pack) idata.availablePacks.get(index);
        checkBox.setEnabled(!pack.required);
        checkBox.setSelected(idata.selectedPacks.contains(pack));
        descLabel.setText(pack.description);
    }
    
    // Pre-loads the images
    private void preLoadImages()
    {
        int size = idata.availablePacks.size();
        images = new ArrayList(size);
        for (int i = 0; i < size; i++)
        {
            try
            {
                URL url = super.getResourceManager().getURL("ImgPacksPanel.img." + i);
                ImageIcon img = new ImageIcon(url);
                images.add(img);
            }
            catch (Exception err)
            {
                err.printStackTrace();
            }
        }
    }
            
    // Indicates wether the panel has been validated or not
    public boolean isValidated()
    {
        return true;
    }
    
    // Asks to make the XML panel data
    public void makeXMLData(XMLElement panelRoot)
    {
        // Selected packs markup
        XMLElement sel = new XMLElement("selected");
        
        // We add each selected pack to sel
        int size = idata.selectedPacks.size();
        for (int i = 0; i < size; i++)
        {
            XMLElement el = new XMLElement("pack");
            Pack pack = (Pack) idata.selectedPacks.get(i);
            Integer integer = new Integer(idata.availablePacks.indexOf(pack));
            el.setAttribute("index", integer.toString());
            sel.addChild(el);
        }
        
        // Joining
        panelRoot.addChild(sel);
    }
    
    // Asks to run in the automated mode
    public void runAutomated(XMLElement panelRoot)
    {
        // We get the selected markup
        XMLElement sel = panelRoot.getFirstChildNamed("selected");
        
        // We get the packs markups
        Vector pm = sel.getChildrenNamed("pack");
        
        // We select each of them
        int size = pm.size();
        idata.selectedPacks.clear();
        for (int i = 0; i < size; i++)
        {
            XMLElement el = (XMLElement) pm.get(i);
            Integer integer = new Integer(el.getAttribute("index"));
            int index = integer.intValue();
            idata.selectedPacks.add(idata.availablePacks.get(index));
        }
    }
    
    //.....................................................................
}
