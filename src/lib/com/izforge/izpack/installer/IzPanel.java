/*
 * IzPack Version 3.1.0 pre2 (build 2002.10.19)
 * Copyright (C) 2001,2002 Julien Ponge
 *
 * File :               IzPanel.java
 * Description :        The class for the panels.
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

package com.izforge.izpack.installer;

import com.izforge.izpack.*;
import com.izforge.izpack.gui.*;

import javax.swing.*;

import net.n3.nanoxml.*;

/**
 * Defines the base class for the IzPack panels. Any panel should be a subclass of
 * it and should belong to the <code>com.izforge.izpack.panels</code> package.
 *
 * @author Julien PONGE
 * @version 3.1.0 pre2 (build 2002.10.19)
 * @see javax.swing.JPanel
 */
public class IzPanel extends JPanel
{
    //.....................................................................
    
    // The fields

    /**
     * The installer internal data (actually a melting-pot class with all-public fields.
     */
    protected InstallData idata;

    /**
     * The parent IzPack installer frame.
     */
    protected InstallerFrame parent;
    
    /**
     * ResourceManager for easy access to resources
     */
    private ResourceManager resourceManager;
    
    /**
     * The constructor.
     *
     * @param parent The parent IzPack installer frame.
     * @param idata The installer internal data.
     */
    public IzPanel(InstallerFrame parent, InstallData idata)
    {
        super();
        
        this.idata = idata;
        this.parent = parent;
        this.resourceManager = new ResourceManager(this.idata);
    }
    
    //.....................................................................
    // The methods
    
    /**
     * Indicates wether the panel has been validated or not. The installer
     * won't let the user go further through the installation process until
     * the panel is validated. Default behaviour is to return
     * <code>true</code>.
     *
     * @return A boolean stating wether the panel has been validated or not.
     */
    public boolean isValidated()
    {
        return true;
    }
    
    /**
     * This method is called when the panel becomes active. Default is to do
     * nothing : feel free to implement what you need in your subclasses.
     * A panel becomes active when the user reaches it during the installation
     * process.
     */
    public void panelActivate() { }
    
    /**
     * This method is called when the panel gets desactivated, when the user
     * switches to the next panel. By default it doesn't do anything.
     */
    public void panelDesactivate() { }
    
    /**
     * Asks the panel to set its own XML data that can be brought back for
     * an automated installation process. Use it as a blackbox if your panel
     * needs to do something even in automated mode.
     *
     * @param panelRoot The XML root element of the panels blackbox tree.
     */
    public void makeXMLData(XMLElement panelRoot) { }
    
    // Asks to run in the automated mode
    /**
     * Makes the panel work in automated mode. Default is to do nothing, but
     * any panel doing something 'effective' during the installation process
     * should implement this method.
     *
     * @param panelRoot The XML root element of the panels blackbox tree.
     */
    public void runAutomated(XMLElement panelRoot) { }
    
    /**
     * Called when the installer works in non-GUI mode. All the panel job must
     * be done from here.
     */
    public void runNoGUI() { }
        
    /**
     * Returns the current ResourceManager.
     * with this ResourceManager you can access IzPack resources easily
     * @return Value of property resourceManager.
     */
    public ResourceManager getResourceManager() {
        return resourceManager;
    }
    
    //.....................................................................
}
