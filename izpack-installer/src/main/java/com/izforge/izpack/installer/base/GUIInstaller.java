/*
 * $Id$
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
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

package com.izforge.izpack.installer.base;

import com.izforge.izpack.bootstrap.IApplicationComponent;
import com.izforge.izpack.data.LocaleDatabase;
import com.izforge.izpack.data.ResourceManager;
import com.izforge.izpack.installer.data.InstallData;
import com.izforge.izpack.installer.unpacker.ScriptParser;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.FileExecutor;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.util.List;

/**
 * The IzPack graphical installer class.
 *
 * @author Julien Ponge
 */
public class GUIInstaller extends InstallerBase {

    /**
     * The installation data.
     */
    private InstallData installdata;

    private IApplicationComponent applicationComponent;


    /**
     * The constructor.
     *
     * @param installdata
     * @throws Exception Description of the Exception
     */
    public GUIInstaller(InstallData installdata, ResourceManager resourceManager, IApplicationComponent applicationComponent) throws Exception {
        super(resourceManager);
        this.applicationComponent=applicationComponent;
        this.installdata = installdata;
        initData();
    }

    private void showFatalError(Throwable e) {
        try {
            JOptionPane.showMessageDialog(null, "Error: " + e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    public void initData() throws Exception {
        // Checks the Java version
        checkJavaVersion(installdata);
        checkJDKAvailable(installdata);
        // Check for already running instance
        checkLockFile(installdata);

        // Loads the suitable langpack
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                try {
                    loadLangPack(installdata);
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                }
            }
        });

        // create the resource manager (after the language selection!)
        resourceManager.setLocale(installdata.getLocaleISO3());

        configureGuiButtons(installdata);

        // loads installer conditions
        loadInstallerRequirements();
        // load dynamic variables
        loadDynamicVariables();
        // check installer conditions
        if (!checkInstallerRequirements(installdata)) {
            Debug.log("not all installerconditions are fulfilled.");
            System.exit(-1);
            return;
        }
    }


    /**
     * Loads the suitable langpack.
     *
     * @param installdata
     * @throws Exception Description of the Exception
     */
    private void loadLangPack(InstallData installdata) throws Exception {
        // Initialisations
        List<String> availableLangPacks = resourceManager.getAvailableLangPacks();
        int npacks = availableLangPacks.size();
        if (npacks == 0) {
            throw new Exception("no language pack available");
        }
        String selectedPack;

        // We get the langpack name
        if (npacks != 1) {
//            LanguageDialog picker = new LanguageDialog(frame, resourceManager, installdata);
            LanguageDialog picker = applicationComponent.getComponent(LanguageDialog.class);
            selectedPack = picker.runPicker();
        } else {
            selectedPack = availableLangPacks.get(0);
        }

        // We add an xml data information
        installdata.getXmlData().setAttribute("langpack", selectedPack);

        // We load the langpack
        installdata.setLocaleISO3(selectedPack);
        installdata.setVariable(ScriptParser.ISO3_LANG, installdata.getLocaleISO3());
        InputStream in = resourceManager.getInputStream("langpacks/" + selectedPack + ".xml");
        installdata.setLangpack(new LocaleDatabase(in));
    }

    /**
     * @param installdata
     */
    private void configureGuiButtons(InstallData installdata) {
        UIManager.put("OptionPane.yesButtonText", installdata.getLangpack().getString("installer.yes"));
        UIManager.put("OptionPane.noButtonText", installdata.getLangpack().getString("installer.no"));
        UIManager.put("OptionPane.cancelButtonText", installdata.getLangpack()
                .getString("installer.cancel"));
    }

    public void showMissingRequirementMessage(String message) {
        JOptionPane.showMessageDialog(null, message);
    }


    /**
     * Sets a lock file. Not using java.nio.channels.FileLock to prevent
     * the installer from accidentally keeping a lock on a file if the install
     * fails or is killed.
     *
     * @param installdata
     * @throws Exception Description of the Exception
     */
    private void checkLockFile(InstallData installdata) throws Exception {
        String tempDir = System.getProperty("java.io.tmpdir");
        String appName = installdata.getInfo().getAppName();
        String fileName = "iz-" + appName + ".tmp";
        Debug.trace("Making temp file: " + fileName);
        Debug.trace("In temp directory: " + tempDir);
        File file = new File(tempDir, fileName);
        if (file.exists()) {
            // Ask user if they want to proceed.
            Debug.trace("Lock File Exists, asking user for permission to proceed.");
            StringBuffer msg = new StringBuffer();
            msg.append("<html>");
            msg.append("The " + appName + " installer you are attempting to run seems to have a copy already running.<br><br>");
            msg.append("This could be from a previous failed installation attempt or you may have accidentally launched <br>");
            msg.append("the installer twice. <b>The recommended action is to select 'Exit'</b> and wait for the other copy of <br>");
            msg.append("the installer to start. If you are sure there is no other copy of the installer running, click <br>");
            msg.append("the 'Continue' button to allow this installer to run. <br><br>");
            msg.append("Are you sure you want to continue with this installation?");
            msg.append("</html>");
            JLabel label = new JLabel(msg.toString());
            label.setFont(new Font("Sans Serif", Font.PLAIN, 12));
            Object[] optionValues = {"Continue", "Exit"};
            int selectedOption = JOptionPane.showOptionDialog(null, label, "Warning",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, optionValues,
                    optionValues[1]);
            Debug.trace("Selected option: " + selectedOption);
            if (selectedOption == 0) {
                // Take control of the file so it gets deleted after this installer instance exits.
                Debug.trace("Setting temp file to delete on exit");
                file.deleteOnExit();
            } else {
                // Leave the file as it is.
                Debug.trace("Leaving temp file alone and exiting");
                System.exit(1);
            }
        } else {
            try {
                // Create the new lock file
                if (file.createNewFile()) {
                    Debug.trace("Temp file created");
                    file.deleteOnExit();
                } else {
                    Debug.trace("Temp file could not be created");
                    Debug.trace("*** Multiple instances of installer will be allowed ***");
                }
            }
            catch (Exception e) {
                Debug.trace("Temp file could not be created: " + e);
                Debug.trace("*** Multiple instances of installer will be allowed ***");
            }
        }
    }

    /**
     * Checks the Java version.
     *
     * @param installdata
     * @throws Exception Description of the Exception
     */
    private void checkJavaVersion(InstallData installdata) throws Exception {
        String version = System.getProperty("java.version");
        String required = installdata.getInfo().getJavaVersion();
        if (version.compareTo(required) < 0) {
            StringBuffer msg = new StringBuffer();
            msg.append("The application that you are trying to install requires a ");
            msg.append(required);
            msg.append(" version or later of the Java platform.\n");
            msg.append("You are running a ");
            msg.append(version);
            msg.append(" version of the Java platform.\n");
            msg.append("Please upgrade to a newer version.");

            System.out.println(msg.toString());
            JOptionPane.showMessageDialog(null, msg.toString(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    /**
     * Checks if a JDK is available.
     *
     * @param installdata
     */
    private void checkJDKAvailable(InstallData installdata) {
        if (!installdata.getInfo().isJdkRequired()) {
            return;
        }

        FileExecutor exec = new FileExecutor();
        String[] output = new String[2];
        String[] params = {"javac", "-help"};
        if (exec.executeCommand(params, output) != 0) {
            String[] message = {
                    "It looks like your system does not have a Java Development Kit (JDK) available.",
                    "The software that you plan to install requires a JDK for both its installation and execution.",
                    "\n",
                    "Do you still want to proceed with the installation process?"
            };
            int status = JOptionPane.showConfirmDialog(null, message, "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (status == JOptionPane.NO_OPTION) {
                System.exit(1);
            }
        }
    }
}
