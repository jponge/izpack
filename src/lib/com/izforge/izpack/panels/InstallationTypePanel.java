/**
 * 
 */
package com.izforge.izpack.panels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;


/**
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 * @version $Id: $
 */
public class InstallationTypePanel extends IzPanel implements ActionListener
{
    private JRadioButton normalinstall;
    private JRadioButton modifyinstall;      

    public InstallationTypePanel(InstallerFrame parent, InstallData idata)
    {
        super(parent, idata, new IzPanelLayout());
        
    }
    
    private void buildGUI() {
        // We put our components

        add(LabelFactory.create(parent.langpack.getString("InstallationTypePanel.info"),
                parent.icons.getImageIcon("history"),  LEADING), NEXT_LINE);
        
        
        ButtonGroup group = new ButtonGroup();
        
        boolean modifyinstallation = Boolean.valueOf(idata.getVariable(InstallData.MODIFY_INSTALLATION)).booleanValue();

        normalinstall = new JRadioButton(parent.langpack.getString("InstallationTypePanel.normal"), !modifyinstallation);
        normalinstall.addActionListener(this);
        group.add(normalinstall);
        add(normalinstall, NEXT_LINE);
        
        modifyinstall = new JRadioButton(parent.langpack.getString("InstallationTypePanel.modify"), modifyinstallation);
        modifyinstall.addActionListener(this);
        group.add(modifyinstall);
        add(modifyinstall, NEXT_LINE);
                
        setInitialFocus(normalinstall);
        getLayoutHelper().completeLayout();
    }

    /**
     * 
     */
    private static final long serialVersionUID = -8178770882900584122L; 

    /* (non-Javadoc)
     * @see com.izforge.izpack.installer.IzPanel#panelActivate()
     */
    public void panelActivate()
    {
        buildGUI();
    }
   
    public void actionPerformed(ActionEvent e)
    {
        if (normalinstall.isSelected()) {
            idata.setVariable(InstallData.MODIFY_INSTALLATION, "false");
        }
        else {
            idata.setVariable(InstallData.MODIFY_INSTALLATION, "true");
        } 
        
    }
}

