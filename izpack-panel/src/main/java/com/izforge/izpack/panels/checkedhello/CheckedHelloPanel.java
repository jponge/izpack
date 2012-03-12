/*
 * $Id$
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/ http://izpack.codehaus.org/
 *
 * Copyright 2005 Klaus Bartz
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.izforge.izpack.panels.checkedhello;

import com.coi.tools.os.win.MSWinConstants;
import com.coi.tools.os.win.RegDataContainer;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.core.os.RegistryHandler;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.base.InstallerFrame;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.panels.hello.HelloPanel;

import java.util.logging.Logger;

/**
 * An extended hello panel class which detects whether the product was already installed or not.
 * This class should be only used if the RegistryInstallerListener will be also used. Current the
 * check will be only performed on Windows operating system. This class can be used also as example
 * how to use the registry stuff to get informations from the current system.
 *
 * @author Klaus Bartz
 */
public class CheckedHelloPanel extends HelloPanel implements MSWinConstants
{

    /**
     * Required (serializable)
     */
    private static final long serialVersionUID = 1737042770727953387L;
    /**
     * Flag to break installation or not.
     */
    protected boolean abortInstallation;

    /**
     * The registry handler, or <tt>null</tt> if the platform doesn't support it.
     */
    private final RegistryHandler registryHandler;

    /**
     * The logger.
     */
    private static Logger logger = Logger.getLogger(CheckedHelloPanel.class.getName());

    /**
     * The constructor.
     *
     * @param parent          the parent frame
     * @param installData     the installation data
     * @param resourceManager the resource manager
     * @param handler         the registry handler instance
     * @param log             the log
     * @throws Exception if it cannot be determined if the application is registered
     */
    public CheckedHelloPanel(InstallerFrame parent, GUIInstallData installData, ResourceManager resourceManager,
                             RegistryDefaultHandler handler, Log log) throws Exception
    {
        super(parent, installData, resourceManager, log);
        registryHandler = handler.getInstance();
        abortInstallation = isRegistered();
    }

    /**
     * This method should only be called if this product was allready installed. It resolves the
     * install path of the first already installed product and asks the user whether to install
     * twice or not.
     *
     * @return whether a multiple Install should be performed or not.
     * @throws Exception
     */
    protected boolean multipleInstall() throws Exception
    {
        // Let us play a little bit with the regstry...
        // Just for fun we would resolve the path of the already
        // installed application.
        int oldVal = registryHandler.getRoot(); // Only for security...
        // We know, that the product is already installed, else we
        // would not in this method. Now we search for the path...
        String uninstallName = registryHandler.getUninstallName();
        String oldInstallPath = "<not found>";
        while (true) // My goto alternative :-)
        {

            if (uninstallName == null)
            {
                break; // Should never be...
            }
            // First we "create" the reg key.
            String keyName = RegistryHandler.UNINSTALL_ROOT + uninstallName;
            registryHandler.setRoot(HKEY_LOCAL_MACHINE);
            if (!registryHandler.valueExist(keyName, "UninstallString"))
            // We assume that the application was installed with
            // IzPack. Therefore there should be the value "UninstallString"
            // which contains the uninstaller call. If not we can do nothing.
            {
                break;
            }
            // Now we would get the value. A value can have different types.
            // Therefore we get an container which can handle all possible types.
            // There are different ways to handle. Use normally only one of the
            // ways; at this point more are used to demonstrate the different ways.

            // 1. If we are secure about the type, we can extract the value immediately.
            String valString = registryHandler.getValue(keyName, "UninstallString").getStringData();

            // 2. If we are not so much interessted at the type, we can get the value
            // as Object. A DWORD is then a Long Object not a long primitive type.
            Object valObj = registryHandler.getValue(keyName, "UninstallString").getDataAsObject();
            if (valObj instanceof String) // Only to inhibit warnings about local variable never read.
            {
                valString = (String) valObj;
            }

            // 3. If we are not secure about the type we should differ between possible
            // types.
            RegDataContainer val = registryHandler.getValue(keyName, "UninstallString");
            int typeOfVal = val.getType();
            switch (typeOfVal)
            {
                case REG_EXPAND_SZ:
                case REG_SZ:
                    valString = val.getStringData();
                    break;
                case REG_BINARY:
                case REG_DWORD:
                case REG_LINK:
                case REG_MULTI_SZ:
                    throw new Exception("Bad installDataGUI type of chosen registry value " + keyName);
                default:
                    throw new Exception("Unknown installDataGUI type of chosen registry value " + keyName);
            }
            // That's all with registry this time... Following preparation of
            // the received value.
            // It is [java path] -jar [uninstaller path]
            int start = valString.lastIndexOf("-jar") + 5;
            if (start < 5 || start >= valString.length())
            // we do not know what todo with it.
            {
                break;
            }
            String uPath = valString.substring(start).trim();
            if (uPath.startsWith("\""))
            {
                uPath = uPath.substring(1).trim();
            }
            int end = uPath.indexOf("uninstaller");
            if (end < 0)
            // we do not know what todo with it.
            {
                break;
            }
            oldInstallPath = uPath.substring(0, end - 1);
            // Much work for such a peanuts...
            break; // That's the problem with the goto alternative. Forget this
            // break produces an endless loop.
        }

        registryHandler.setRoot(oldVal); // Only for security...

        // The text will be to long for one line. Therefore we should use
        // the multi line label. Unfortunately it has no icon. Nothing is
        // perfect...
        String noLuck = installData.getLangpack().getString("CheckedHelloPanel.productAlreadyExist0")
                + oldInstallPath
                + installData.getLangpack().getString("CheckedHelloPanel.productAlreadyExist1");
        return (askQuestion(installData.getLangpack().getString("installer.error"), noLuck,
                AbstractUIHandler.CHOICES_YES_NO) == AbstractUIHandler.ANSWER_YES);
    }

    /**
     * Returns whether the handled application is already registered or not. The validation will be
     * made only on systems which contains a registry (Windows).
     *
     * @return <tt>true</tt> if the application is registered
     * @throws Exception if it cannot be determined if the application is registered
     */
    protected boolean isRegistered() throws Exception
    {
        boolean result = false;
        if (registryHandler != null)
        {
            registryHandler.verify(installData);
            result = registryHandler.isProductRegistered();
        }
        return result;
    }

    /**
     * Indicates wether the panel has been validated or not.
     *
     * @return true if the internal abort flag is not set, else false
     */
    public boolean isValidated()
    {
        return (!abortInstallation);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.izforge.izpack.installer.IzPanel#panelActivate()
     */

    public void panelActivate()
    {
        if (abortInstallation)
        {
            parent.lockNextButton();
            try
            {
                if (multipleInstall())
                {
                    setUniqueUninstallKey();
                    abortInstallation = false;
                    parent.unlockNextButton();
                }
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        if (registryHandler != null)
        {
            installData.setVariable("UNINSTALL_NAME", registryHandler.getUninstallName());
        }
    }

    /**
     * @throws Exception
     */
    private void setUniqueUninstallKey() throws Exception
    {
        // Let us play a little bit with the regstry again...
        // Now we search for an unique uninstall key.
        int oldVal = registryHandler.getRoot(); // Only for security...
        // We know, that the product is already installed, else we
        // would not in this method. First we get the
        // "default" uninstall key.
        if (oldVal > 100) // Only to inhibit warnings about local variable never read.
        {
            return;
        }
        String uninstallName = registryHandler.getUninstallName();
        int uninstallModifier = 1;
        while (true)
        {
            if (uninstallName == null)
            {
                break; // Should never be...
            }
            // Now we define a new uninstall name.
            String newUninstallName = uninstallName + "(" + Integer.toString(uninstallModifier)
                    + ")";
            // Then we "create" the reg key with it.
            String keyName = RegistryHandler.UNINSTALL_ROOT + newUninstallName;
            registryHandler.setRoot(HKEY_LOCAL_MACHINE);
            if (!registryHandler.keyExist(keyName))
            { // That's the name for which we searched.
                // Change the uninstall name in the reg helper.
                registryHandler.setUninstallName(newUninstallName);
                // Now let us inform the user.
                emitNotification(installData.getLangpack()
                        .getString("CheckedHelloPanel.infoOverUninstallKey")
                        + newUninstallName);
                // Now a little hack if the registry spec file contains
                // the pack "UninstallStuff".
                break;
            }
            uninstallModifier++;
        }
    }

}
