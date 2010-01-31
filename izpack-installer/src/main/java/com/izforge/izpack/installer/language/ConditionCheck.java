package com.izforge.izpack.installer.language;

import com.izforge.izpack.api.data.InstallerRequirement;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.api.installer.InstallerRequirementDisplay;
import com.izforge.izpack.api.rules.Condition;
import com.izforge.izpack.core.rules.RulesEngineImpl;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.FileExecutor;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Checker for java version, JDK and running install
 */
public class ConditionCheck {
    private GUIInstallData installdata;
    private ResourceManager resourceManager;

    public ConditionCheck(GUIInstallData installdata, ResourceManager resourceManager) {
        this.installdata = installdata;
        this.resourceManager = resourceManager;
    }

    public void check() throws Exception {
        checkJavaVersion();
        checkJDKAvailable();
        // Check for already running instance
        checkLockFile();
        checkLangPackAvaible();
    }

    /**
     * Sets a lock file. Not using java.nio.channels.FileLock to prevent
     * the installer from accidentally keeping a lock on a file if the install
     * fails or is killed.
     *
     * @throws Exception Description of the Exception
     */
    public void checkLockFile() throws Exception {
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
     * @throws Exception Description of the Exception
     */
    private void checkJavaVersion() throws Exception {
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
     */
    private void checkJDKAvailable() {
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


    public boolean checkInstallerRequirements(InstallerRequirementDisplay display) throws Exception {
        boolean result = true;
        for (InstallerRequirement installerrequirement : installdata.getInstallerrequirements()) {
            String conditionid = installerrequirement.getCondition();
            Condition condition = RulesEngineImpl.getCondition(conditionid);
            if (condition == null) {
                Debug.log(conditionid + " not a valid condition.");
                throw new Exception(conditionid + "could not be found as a defined condition");
            }
            if (!condition.isTrue()) {
                String message = installerrequirement.getMessage();
                if ((message != null) && (message.length() > 0)) {
                    String localizedMessage = installdata.getLangpack().getString(message);
                    display.showMissingRequirementMessage(localizedMessage);
                }
                result = false;
                break;
            }
        }
        return result;
    }

    public boolean checkLangPackAvaible() throws Exception {
        java.util.List<String> availableLangPacks = resourceManager.getAvailableLangPacks();
        return availableLangPacks.size() != 0;
    }

}

