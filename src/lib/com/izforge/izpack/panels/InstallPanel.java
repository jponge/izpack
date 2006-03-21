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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.util.AbstractUIProgressHandler;

/**
 * The install panel class. Launches the actual installation job.
 * 
 * @author Julien Ponge
 */
public class InstallPanel extends IzPanel implements AbstractUIProgressHandler
{

    private static final long serialVersionUID = 3257282547959410992L;

    /** The tip label. */
    protected JLabel tipLabel;

    /** The operation label . */
    protected JLabel packOpLabel;

    /** The operation label . */
    protected JLabel overallOpLabel;

    /** The pack progress bar. */
    protected JProgressBar packProgressBar;

    /** The progress bar. */
    protected JProgressBar overallProgressBar;

    /** True if the installation has been done. */
    private volatile boolean validated = false;

    /** How many packs we are going to install. */
    private int noOfPacks = 0;

    /**
     * The constructor.
     * 
     * @param parent The parent window.
     * @param idata The installation data.
     */
    public InstallPanel(InstallerFrame parent, InstallData idata)
    {
        super(parent, idata);

        // We initialize our layout
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints gbConstraints = new GridBagConstraints();
        setLayout(layout);

        int row = 1;

        this.tipLabel = LabelFactory.create(parent.langpack.getString("InstallPanel.tip"),
                parent.icons.getImageIcon("information"), JLabel.TRAILING);
        parent.buildConstraints(gbConstraints, 0, row++, 2, 1, 1.0, 0.0);
        gbConstraints.fill = GridBagConstraints.NONE;
        gbConstraints.anchor = GridBagConstraints.NORTHWEST;
        layout.addLayoutComponent(this.tipLabel, gbConstraints);
        add(this.tipLabel);

        this.packOpLabel = LabelFactory.create(" ", JLabel.TRAILING);
        parent.buildConstraints(gbConstraints, 0, row++, 2, 1, 1.0, 0.0);
        gbConstraints.anchor = GridBagConstraints.SOUTHWEST;
        layout.addLayoutComponent(this.packOpLabel, gbConstraints);
        add(this.packOpLabel);

        this.packProgressBar = new JProgressBar();
        this.packProgressBar.setStringPainted(true);
        this.packProgressBar.setString(parent.langpack.getString("InstallPanel.begin"));
        this.packProgressBar.setValue(0);
        parent.buildConstraints(gbConstraints, 0, row++, 2, 1, 1.0, 0.0);
        gbConstraints.anchor = GridBagConstraints.NORTH;
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        layout.addLayoutComponent(this.packProgressBar, gbConstraints);
        add(this.packProgressBar);

        // make sure there is some space between the progress bars
        JSeparator sep = new JSeparator();
        Dimension dim = new Dimension(0, 10);
        sep.setPreferredSize(dim);
        sep.setMinimumSize(dim);
        sep.setMaximumSize(dim);
        parent.buildConstraints(gbConstraints, 0, row++, 2, 1, 1.0, 0.0);
        layout.addLayoutComponent(sep, gbConstraints);
        add(sep);

        this.overallOpLabel = LabelFactory.create(parent.langpack
                .getString("InstallPanel.progress"), parent.icons.getImageIcon("information"),
                JLabel.TRAILING);
        parent.buildConstraints(gbConstraints, 0, row++, 2, 1, 1.0, 0.0);
        gbConstraints.anchor = GridBagConstraints.NORTHWEST;
        gbConstraints.fill = GridBagConstraints.NONE;
        layout.addLayoutComponent(this.overallOpLabel, gbConstraints);
        add(this.overallOpLabel);

        this.overallProgressBar = new JProgressBar();
        this.overallProgressBar.setStringPainted(true);
        this.overallProgressBar.setString("");
        this.overallProgressBar.setValue(0);
        parent.buildConstraints(gbConstraints, 0, row++, 2, 1, 1.0, 0.0);
        gbConstraints.anchor = GridBagConstraints.NORTH;
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        layout.addLayoutComponent(this.overallProgressBar, gbConstraints);
        add(this.overallProgressBar);
    }

    /**
     * Indicates wether the panel has been validated or not.
     * 
     * @return The validation state.
     */
    public boolean isValidated()
    {
        return this.validated;
    }

    /** The unpacker starts. */
    public void startAction(String name, int noOfJobs)
    {
        this.noOfPacks = noOfJobs;
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                parent.blockGUI();
                
                // figure out how many packs there are to install
                overallProgressBar.setMinimum(0);
                overallProgressBar.setMaximum(noOfPacks);
                overallProgressBar.setString("0 / " + Integer.toString(noOfPacks));
            }
        });
    }

    /**
     * An error was encountered.
     * 
     * @param error The error text.
     */
    public void emitError(String title, String error)
    {
        this.packOpLabel.setText(error);
        idata.installSuccess = false;
        JOptionPane.showMessageDialog(this, error, parent.langpack.getString("installer.error"),
                JOptionPane.ERROR_MESSAGE);
    }

    /** The unpacker stops. */
    public void stopAction()
    {
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                parent.releaseGUI();
                parent.lockPrevButton();
                
                // With custom actions it is possible, that the current value
                // is not max - 1. Therefore we use always max for both
                // progress bars to signal finish state.
                overallProgressBar.setValue(overallProgressBar.getMaximum());
                int ppbMax = packProgressBar.getMaximum();
                if (ppbMax < 1)
                {
                    ppbMax = 1;
                    packProgressBar.setMaximum(ppbMax);
                }
                packProgressBar.setValue(ppbMax);

                packProgressBar.setString(parent.langpack.getString("InstallPanel.finished"));
                packProgressBar.setEnabled(false);
                String no_of_packs = Integer.toString(noOfPacks);
                overallProgressBar.setString(no_of_packs + " / " + no_of_packs);
                overallProgressBar.setEnabled(false);
                packOpLabel.setText(" ");
                packOpLabel.setEnabled(false);
                idata.canClose = true;
                validated = true;
                if (idata.panels.indexOf(this) != (idata.panels.size() - 1)) parent.unlockNextButton();
            }
        });
    }

    /**
     * Normal progress indicator.
     * 
     * @param val The progression value.
     * @param msg The progression message.
     */
    public void progress(final int val, final String msg)
    {
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                packProgressBar.setValue(val + 1);
                packOpLabel.setText(msg);
            }
        });
    }

    /**
     * Pack changing.
     * 
     * @param packName The pack name.
     * @param stepno The number of the pack.
     * @param max The new maximum progress.
     */
    public void nextStep(final String packName, final int stepno, final int max)
    {
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                packProgressBar.setValue(0);
                packProgressBar.setMinimum(0);
                packProgressBar.setMaximum(max);
                packProgressBar.setString(packName);
                overallProgressBar.setValue(stepno - 1);
                overallProgressBar.setString(Integer.toString(stepno) + " / "
                        + Integer.toString(noOfPacks));                
            }
        });
    }

    /** Called when the panel becomes active. */
    public void panelActivate()
    {
        // We clip the panel
        Dimension dim = parent.getPanelsContainerSize();
        dim.width -= (dim.width / 4);
        dim.height = 150;
        setMinimumSize(dim);
        setMaximumSize(dim);
        setPreferredSize(dim);
        parent.lockNextButton();

        parent.install(this);
    }

}
