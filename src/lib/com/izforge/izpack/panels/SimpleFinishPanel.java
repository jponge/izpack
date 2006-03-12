/*
 * IzPack - Copyright 2001-2006 Julien Ponge, All Rights Reserved.
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
import java.io.File;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.util.VariableSubstitutor;

/**
 * The simple finish panel class.
 * 
 * @author Julien Ponge
 */
public class SimpleFinishPanel extends IzPanel
{

    /**
     * 
     */
    private static final long serialVersionUID = 3689911781942572085L;

    /** The layout. */
    private BoxLayout layout;

    /** The center panel. */
    protected JPanel centerPanel;

    /** The variables substitutor. */
    private VariableSubstitutor vs;

    /**
     * The constructor.
     * 
     * @param parent The parent.
     * @param idata The installation data.
     */
    public SimpleFinishPanel(InstallerFrame parent, InstallData idata)
    {
        super(parent, idata);

        vs = new VariableSubstitutor(idata.getVariables());

        // The 'super' layout
        GridBagLayout superLayout = new GridBagLayout();
        setLayout(superLayout);
        GridBagConstraints gbConstraints = new GridBagConstraints();
        gbConstraints.insets = new Insets(0, 0, 0, 0);
        gbConstraints.fill = GridBagConstraints.NONE;
        gbConstraints.anchor = GridBagConstraints.CENTER;

        // We initialize our 'real' layout
        centerPanel = new JPanel();
        layout = new BoxLayout(centerPanel, BoxLayout.Y_AXIS);
        centerPanel.setLayout(layout);
        superLayout.addLayoutComponent(centerPanel, gbConstraints);
        add(centerPanel);
    }

    /**
     * Indicates wether the panel has been validated or not.
     * 
     * @return true if the panel has been validated.
     */
    public boolean isValidated()
    {
        return true;
    }

    /** Called when the panel becomes active. */
    public void panelActivate()
    {
        parent.lockNextButton();
        parent.lockPrevButton();
        parent.setQuitButtonText(parent.langpack.getString("FinishPanel.done"));
        if (idata.installSuccess)
        {
            // We set the information
            centerPanel.add(LabelFactory.create(parent.icons.getImageIcon("check")));
            centerPanel.add(Box.createVerticalStrut(20));
            centerPanel.add(LabelFactory.create(parent.langpack.getString("FinishPanel.success"),
                    parent.icons.getImageIcon("information"), JLabel.TRAILING));
            centerPanel.add(Box.createVerticalStrut(20));

            if (idata.uninstallOutJar != null)
            {
                // We prepare a message for the uninstaller feature
                String path = translatePath("$INSTALL_PATH") + File.separator + "Uninstaller";

                centerPanel.add(LabelFactory.create(parent.langpack
                        .getString("FinishPanel.uninst.info"), parent.icons
                        .getImageIcon("information"), JLabel.TRAILING));
                centerPanel.add(LabelFactory.create(path, parent.icons.getImageIcon("empty"),
                        JLabel.TRAILING));
            }
        }
        else
            centerPanel.add(LabelFactory.create(parent.langpack.getString("FinishPanel.fail"),
                    parent.icons.getImageIcon("information"), JLabel.TRAILING));

    }

    /**
     * Translates a relative path to a local system path.
     * 
     * @param destination The path to translate.
     * @return The translated path.
     */
    private String translatePath(String destination)
    {
        // Parse for variables
        destination = vs.substitute(destination, null);

        // Convert the file separator characters
        return destination.replace('/', File.separatorChar);
    }
}
