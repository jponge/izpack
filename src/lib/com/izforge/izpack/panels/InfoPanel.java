/*
 * IzPack Version 3.0.0 pre4 (build 2002.06.15)
 * Copyright (C) 2001,2002 Julien Ponge
 *
 * File :               InfoPanel.java
 * Description :        A panel to show some textual information.
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
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

public class InfoPanel extends IzPanel
{
    //.....................................................................
    // The fields

    private GridBagLayout layout;
    private GridBagConstraints gbConstraints;
    private JLabel infoLabel;
    private JTextArea textArea;
    private JScrollPane scroller;
    private String info;

    // The constructor
    public InfoPanel(InstallerFrame parent, InstallData idata)
    {
        super(parent, idata);

        // We initialize our layout
        layout = new GridBagLayout();
        gbConstraints = new GridBagConstraints();
        setLayout(layout);

        // We load the text
        loadInfo();

        // We add the components

        infoLabel = new JLabel(parent.langpack.getString("InfoPanel.info"),
                               parent.icons.getImageIcon("edit"), JLabel.TRAILING);
        parent.buildConstraints(gbConstraints, 0, 0, 1, 1, 1.0, 0.1);
        gbConstraints.insets = new Insets(5, 5, 5, 5);
        gbConstraints.fill = GridBagConstraints.NONE;
        gbConstraints.anchor = GridBagConstraints.SOUTHWEST;
        layout.addLayoutComponent(infoLabel, gbConstraints);
        add(infoLabel);

        textArea = new JTextArea(info);
        textArea.setCaretPosition(0);
        textArea.setEditable(false);
        scroller = new JScrollPane(textArea);
        parent.buildConstraints(gbConstraints, 0, 1, 1, 1, 1.0, 0.9);
        gbConstraints.fill = GridBagConstraints.BOTH;
        gbConstraints.anchor = GridBagConstraints.CENTER;
        layout.addLayoutComponent(scroller, gbConstraints);
        add(scroller);
    }

    //.....................................................................
    // The methods

    // Loads the info text
/*
    private void loadInfo()
    {
        try
        {
            // We read it
            InputStream in = parent.getResource("InfoPanel.info");
            ByteArrayOutputStream infoData = new ByteArrayOutputStream();
            byte[] buffer = new byte[5120];
            int bytesInBuffer;
            while ((bytesInBuffer = in.read(buffer)) != -1)
            {
                infoData.write(buffer, 0, bytesInBuffer);
            }
            info = infoData.toString();
        }
        catch (Exception err)
        {
            info = "Error : could not load the info text !";
        }
    }
*/
    private void loadInfo()    {
        try  {
            String resNamePrifix = "InfoPanel.info";
            String resName = resNamePrifix + "_" + idata.localeISO3;

            InputStream in = parent.getResource(resName);
            if (null == in ) {
                in = parent.getResource(resNamePrifix);
            }

            ByteArrayOutputStream infoData = new ByteArrayOutputStream();
            byte[] buffer = new byte[5120];
            int bytesInBuffer;
            while ((bytesInBuffer = in.read(buffer)) != -1) {
                infoData.write(buffer, 0, bytesInBuffer);
            }
            info = infoData.toString();
        }
        catch (Exception err) {
            info = "Error : could not load the info text !";
        }
    }

    // Indicates wether the panel has been validated or not
    public boolean isValidated()
    {
        return true;
    }

    //.....................................................................
}
