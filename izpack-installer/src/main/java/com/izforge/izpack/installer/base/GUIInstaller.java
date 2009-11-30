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

import com.izforge.izpack.data.LocaleDatabase;
import com.izforge.izpack.data.ResourceManager;
import com.izforge.izpack.installer.data.InstallData;
import com.izforge.izpack.installer.unpacker.ScriptParser;
import com.izforge.izpack.util.Debug;

import javax.swing.*;
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

    /**
     * Checker for java version, JDK and running install
     */
    private ConditionCheck conditionCheck;


    /**
     * The constructor.
     *
     * @param installdata
     * @throws Exception Description of the Exception
     */
    public GUIInstaller(InstallData installdata, ResourceManager resourceManager, ConditionCheck conditionCheck) throws Exception {
        super(resourceManager);
        this.installdata = installdata;
        this.conditionCheck = conditionCheck;
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
        conditionCheck.check();

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
            LanguageDialog picker=null ;//= applicationComponent.getComponent(LanguageDialog.class);
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
}
