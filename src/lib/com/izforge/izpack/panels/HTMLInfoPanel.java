/*
 * IzPack Version 3.1.0 pre1 (build 2002.09.21)
 * Copyright (C) 2001,2002 Julien Ponge
 *
 * File :               HTMLInfoPanel.java
 * Description :        A panel to show some HTML information.
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

import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

public class HTMLInfoPanel extends IzPanel implements HyperlinkListener
{
    //.....................................................................
    // The fields

    private GridBagLayout layout;
    private GridBagConstraints gbConstraints;
    private JLabel infoLabel;
    private JEditorPane textArea;
    private RenderingHints antialiaser;

    // The constructor
    public HTMLInfoPanel(InstallerFrame parent, InstallData idata)
    {
        super(parent, idata);

        // We initialize our layout
        layout = new GridBagLayout();
        gbConstraints = new GridBagConstraints();
        setLayout(layout);

        // We add the components

        infoLabel = new JLabel(parent.langpack.getString("InfoPanel.info"),
                               parent.icons.getImageIcon("edit"), JLabel.TRAILING);
        parent.buildConstraints(gbConstraints, 0, 0, 1, 1, 1.0, 0.0);
        gbConstraints.insets = new Insets(5, 5, 5, 5);
        gbConstraints.fill = GridBagConstraints.NONE;
        gbConstraints.anchor = GridBagConstraints.SOUTHWEST;
        layout.addLayoutComponent(infoLabel, gbConstraints);
        add(infoLabel);

        try
        {
            textArea = new JEditorPane();
            textArea.setEditable(false);
            textArea.addHyperlinkListener(this);
            JScrollPane scroller = new JScrollPane(textArea);
            textArea.setPage(loadInfo());
            parent.buildConstraints(gbConstraints, 0, 1, 1, 1, 1.0, 1.0);
            gbConstraints.anchor = GridBagConstraints.CENTER;
            gbConstraints.fill = GridBagConstraints.BOTH;
            layout.addLayoutComponent(scroller, gbConstraints);
            add(scroller);
        }
        catch (Exception err) { err.printStackTrace(); }
    }

    //.....................................................................
    // The methods

//    // Loads the info text
//
//    private URL loadInfo() throws Exception
//
//    {
//
//        return getClass().getResource("/res/HTMLInfoPanel.info");
//
//    }

    private URL loadInfo()
    {
        URL retVal = null;

        String resNamePrifix = "/res/HTMLInfoPanel.info";
        String resName = resNamePrifix + "_" + idata.localeISO3;

        retVal = getClass().getResource(resName);

        if (null == retVal ) {
            retVal =  getClass().getResource(resNamePrifix);
        }
        return retVal;
    }

    // Indicates wether the panel has been validated or not
    public boolean isValidated()
    {
        return true;
    }

    // Hyperlink events handler
    public void hyperlinkUpdate(HyperlinkEvent e)
    {
        try
        {
            if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
                textArea.setPage(e.getURL());
        }
        catch (Exception err) { }
    }

    //.....................................................................
}
