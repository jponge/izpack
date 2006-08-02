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
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.izforge.izpack.gui.ButtonFactory;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.util.VariableSubstitutor;

/**
 * The finish panel class.
 * 
 * @author Julien Ponge
 */
public class FinishPanel extends IzPanel implements ActionListener
{

    private static final long serialVersionUID = 3257282535107998009L;

    /** The automated installers generation button. */
    private JButton autoButton;

    /** The center panel. */
    private JPanel centerPanel;

    /** The variables substitutor. */
    private VariableSubstitutor vs;

    /**
     * The constructor.
     * 
     * @param parent The parent.
     * @param idata The installation data.
     */
    public FinishPanel(InstallerFrame parent, InstallData idata)
    {
        super(parent, idata);

        vs = new VariableSubstitutor(idata.getVariables());

        // Changed to layout handling of IzPanel to support different anchors.
        // (Klaus Bartz, 2006.06.30)
        GridBagConstraints gbConstraints = getNextYGridBagConstraints();

        gbConstraints.insets = new Insets(0, 0, 0, 0);
        gbConstraints.fill = GridBagConstraints.NONE;
        if (getLayoutHelper().getAnchor() == GridBagConstraints.NONE || getLayoutHelper().getAnchor() == GridBagConstraints.CENTER)
            gbConstraints.anchor = GridBagConstraints.CENTER;
        else
        {
            gbConstraints.weightx = 1.0;
            gbConstraints.anchor = getLayoutHelper().getAnchor();
        }
        

        // We initialize our 'real' layout
        centerPanel = new JPanel();
        BoxLayout layout = new BoxLayout(centerPanel, BoxLayout.Y_AXIS);
        centerPanel.setLayout(layout);
        add(centerPanel, gbConstraints);
        completeGridBagLayout();
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

            // We add the autoButton
            centerPanel.add(Box.createVerticalStrut(20));
            autoButton = ButtonFactory.createButton(parent.langpack.getString("FinishPanel.auto"),
                    parent.icons.getImageIcon("edit"), idata.buttonsHColor);
            autoButton.setToolTipText(parent.langpack.getString("FinishPanel.auto.tip"));
            autoButton.addActionListener(this);
            centerPanel.add(autoButton);
        }
        else
            centerPanel.add(LabelFactory.create(parent.langpack.getString("FinishPanel.fail"),
                    parent.icons.getImageIcon("information"), JLabel.TRAILING));
    }

    /**
     * Actions-handling method.
     * 
     * @param e The event.
     */
    public void actionPerformed(ActionEvent e)
    {
        // Prepares the file chooser
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new File(idata.getInstallPath()));
        fc.setMultiSelectionEnabled(false);
        fc.addChoosableFileFilter(fc.getAcceptAllFileFilter());
        // fc.setCurrentDirectory(new File("."));

        // Shows it
        try
        {
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
            {
                // We handle the xml data writing
                File file = fc.getSelectedFile();
                FileOutputStream out = new FileOutputStream(file);
                BufferedOutputStream outBuff = new BufferedOutputStream(out, 5120);
                parent.writeXMLTree(idata.xmlData, outBuff);
                outBuff.flush();
                outBuff.close();

                autoButton.setEnabled(false);
            }
        }
        catch (Exception err)
        {
            err.printStackTrace();
            JOptionPane.showMessageDialog(this, err.toString(), parent.langpack
                    .getString("installer.error"), JOptionPane.ERROR_MESSAGE);
        }
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
