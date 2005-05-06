/*
 * IzPack - Copyright 2001-2005 Julien Ponge, All Rights Reserved.
 * 
 * http://www.izforge.com/izpack/
 * http://developer.berlios.de/projects/izpack/
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

package com.izforge.izpack.panels;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.installer.ResourceManager;

/**
 * The HTML info panel.
 * 
 * @author Julien Ponge
 */
public class HTMLInfoPanel extends IzPanel implements HyperlinkListener
{

    private static final long serialVersionUID = 3257008769514025270L;

    /** The layout. */
    private GridBagLayout layout;

    /** The layout constraints. */
    private GridBagConstraints gbConstraints;

    /** The info label. */
    private JLabel infoLabel;

    /** The text area. */
    private JEditorPane textArea;

    /**
     * The constructor.
     * 
     * @param parent The parent.
     * @param idata The installation data.
     */
    public HTMLInfoPanel(InstallerFrame parent, InstallData idata)
    {
        super(parent, idata);

        // We initialize our layout
        layout = new GridBagLayout();
        gbConstraints = new GridBagConstraints();
        setLayout(layout);

        // We add the components

        infoLabel = LabelFactory.create(parent.langpack.getString("InfoPanel.info"), parent.icons
                .getImageIcon("edit"), JLabel.TRAILING);
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
        catch (Exception err)
        {
            err.printStackTrace();
        }
    }

    /**
     * Loads the info.
     * 
     * @return The info URL.
     */
    private URL loadInfo()
    {
        String resNamePrifix = "HTMLInfoPanel.info";
        try
        {
            return ResourceManager.getInstance().getURL(resNamePrifix);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return null;
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

    /**
     * Hyperlink events handler.
     * 
     * @param e The event.
     */
    public void hyperlinkUpdate(HyperlinkEvent e)
    {
        try
        {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
                textArea.setPage(e.getURL());
        }
        catch (Exception err)
        {}
    }
}
