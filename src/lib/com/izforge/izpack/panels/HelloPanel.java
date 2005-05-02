/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2004 Julien Ponge
 *
 *  File :               HelloPanel.java
 *  Description :        A panel to welcome the user.
 *  Author's email :     julien@izforge.com
 *  Author's Website :   http://www.izforge.com
 *
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.izforge.izpack.Info;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;

/**
 * The Hello panel class.
 * 
 * @author Julien Ponge
 */
public class HelloPanel extends IzPanel
{

    /**
     * 
     */
    private static final long serialVersionUID = 3257848774955905587L;

    /** The layout. */
    private BoxLayout layout;

    /** The welcome label. */
    private JLabel welcomeLabel;

    /** The application authors label. */
    private JLabel appAuthorsLabel;

    /** The application URL label. */
    private JLabel appURLLabel;

    /**
     * The constructor.
     * 
     * @param parent
     *            The parent.
     * @param idata
     *            The installation data.
     */
    public HelloPanel(InstallerFrame parent, InstallData idata)
    {
        super(parent, idata);

        // The 'super' layout
        GridBagLayout superLayout = new GridBagLayout();
        setLayout(superLayout);
        GridBagConstraints gbConstraints = new GridBagConstraints();
        gbConstraints.insets = new Insets(0, 0, 0, 0);
        gbConstraints.fill = GridBagConstraints.NONE;
        gbConstraints.anchor = GridBagConstraints.CENTER;

        // We initialize our 'real' layout
        JPanel centerPanel = new JPanel();
        layout = new BoxLayout(centerPanel, BoxLayout.Y_AXIS);
        centerPanel.setLayout(layout);
        superLayout.addLayoutComponent(centerPanel, gbConstraints);
        add(centerPanel);

        // We create and put the labels
        String str;

        centerPanel.add(Box.createVerticalStrut(10));

        str = parent.langpack.getString("HelloPanel.welcome1") + idata.info.getAppName() + " "
                + idata.info.getAppVersion() + parent.langpack.getString("HelloPanel.welcome2");
        welcomeLabel = LabelFactory.create(str, parent.icons.getImageIcon("host"), JLabel.TRAILING);
        centerPanel.add(welcomeLabel);

        centerPanel.add(Box.createVerticalStrut(20));

        ArrayList authors = idata.info.getAuthors();
        int size = authors.size();
        if (size > 0)
        {
            str = parent.langpack.getString("HelloPanel.authors");
            appAuthorsLabel = LabelFactory.create(str, parent.icons.getImageIcon("information"),
                    JLabel.TRAILING);
            centerPanel.add(appAuthorsLabel);

            JLabel label;
            for (int i = 0; i < size; i++)
            {
                Info.Author a = (Info.Author) authors.get(i);
                String email = (a.getEmail() != null) ? (" <" + a.getEmail() + ">") : "";
                label = LabelFactory.create(" - " + a.getName() + email, parent.icons
                        .getImageIcon("empty"), JLabel.TRAILING);
                centerPanel.add(label);
            }

            centerPanel.add(Box.createVerticalStrut(20));
        }

        if (idata.info.getAppURL() != null)
        {
            str = parent.langpack.getString("HelloPanel.url") + idata.info.getAppURL();
            appURLLabel = LabelFactory.create(str, parent.icons.getImageIcon("bookmark"),
                    JLabel.TRAILING);
            centerPanel.add(appURLLabel);
        }
    }

    /**
     * Indicates wether the panel has been validated or not.
     * 
     * @return Always true.
     */
    public boolean isValidated()
    {
        return true;
    }
}
