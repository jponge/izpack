/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2003 Jonathan Halliday
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.izforge.izpack.LocaleDatabase;
import com.izforge.izpack.Pack;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelConsole;
import com.izforge.izpack.installer.PanelConsoleHelper;
import com.izforge.izpack.installer.ResourceManager;
import com.izforge.izpack.util.Debug;

/**
 * Console implementation for the TreePacksPanel.
 * 
 * Based on PacksPanelConsoleHelper 
 *
 * @author Sergiy Shyrkov
 * @author Dustin Kut Moy Cheung
 */
public class TreePacksPanelConsoleHelper extends PanelConsoleHelper implements PanelConsole
{
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    /**
     * Simple method to print the String in the argument.
     * Used to avoid seeeing the System.out.println() code _ALL_ the time.
     *
     * @param message String to print
     * @return void
     */
    private static void out(String message) 
    {
        System.out.println(message);
    }

    private String getI18n(LocaleDatabase langpack, String key, String defaultValue)
    {
        String text = langpack.getString(key);
        return text != null && !text.equals(key) ? text : defaultValue;
    }

    /**
     * Method that is called in console mode for TreePacksPanel
     *
     * @param installData The "Database" of izpack
     *
     * @return whether the console mode is supported or not.
     */
    public boolean runConsole(AutomatedInstallData installData)
    {
        Map<String, List<String>> treeData      = new HashMap<String, List<String>>();
        Map<String, Pack> idToPack              = new HashMap<String, Pack>();
        List<String> packParents                = new LinkedList<String>();
        List<Pack> selectedPacks                = new LinkedList<Pack>();
        List<String> kids;

        // load I18N
        LocaleDatabase langpack = installData.langpack;

        try {
            InputStream inputStream = ResourceManager.getInstance().getInputStream("packsLang.xml");
            langpack.add(inputStream);
        } catch (Exception e) {
            Debug.trace(e);
        }
        // initialize selection
        out("");
        out("TreePacksPanel");
        out("");

        for (Pack pack : installData.availablePacks) {

            kids = null;
            idToPack.put(pack.id, pack);

            if (pack.parent != null) {

                if (treeData.containsKey(pack.parent)) {
                    kids = treeData.get(pack.parent);
                } else {
                    kids = new ArrayList<String>();
                }
                kids.add(pack.id);
            } else {
                // add to packParents packs that do not have parents
                // that is, they are top-level packs
                packParents.add(pack.id);
            }
            treeData.put(pack.parent, kids);
        }
        // Go through the top-level packs and retrieve their children
        for(String packParentName : packParents) {
            drawHelper(treeData, selectedPacks, idToPack, packParentName, true, "\t");
        }

        out("...pack selection done.");

        installData.selectedPacks = selectedPacks;

        if (selectedPacks.size() == 0) {
            out("You have not selected any packs!");
            out("Are you sure you want to continue?");
        }

        // No need for break statements since we are using "return"
        switch(askEndOfConsolePanel()) {
            case 1: return true;
            case 2: return false;
            default: return runConsole(installData);
        }
    }
    /**
     * It is used to "draw" the appropriate tree-like structure of the packs and ask if you want to install
     * the pack. The pack will automatically be selected if it is required; otherwise you will be prompted if
     * you want to install that pack. If a pack is not selected, then their child packs won't be installed as
     * well and you won't be prompted to install them.
     *
     * @param treeData          - Map that contains information on the parent pack and its children
     * @param selectedPacks     - the packs that are selected by the user are added there
     * @param idToPack          - Map that mapds the id of the available packs to the actual Pack object
     * @param packParent        - The current "parent" pack to process
     * @param packMaster        - boolean to know if packParent is a top-level pack
     * @param indent            - String to know by how much the child packs should be indented
     *
     * @return void
     */
    private void drawHelper(final Map<String, List<String>> treeData, final List<Pack> selectedPacks, 
                            final Map<String, Pack> idToPack, final String packParent, boolean packMaster, String indent) 
    {

        if(treeData.containsKey(packParent)) {
            if(packMaster) {
                if(!idToPack.get(packParent).required) {
                    out("[" + packParent + "] [required]");
                    selectedPacks.add(idToPack.get(packParent));
                } else {
                    System.out.print("[" + packParent + "] [y/n] ");
                    if (readPrompt()) {
                        selectedPacks.add(idToPack.get(packParent));
                    } else {
                        return;
                    }
                }
            }

            for (String id : treeData.get(packParent)) {
                if (idToPack.get(id).required) {
                    selectedPacks.add(idToPack.get(id));
                    out(indent + idToPack.get(id).name + " [required]");
                } else {
                    System.out.print(indent + idToPack.get(id).name + " [y/n] ");
                    if (readPrompt()) {
                        selectedPacks.add(idToPack.get(id));
                        drawHelper(treeData, selectedPacks, idToPack, id, false, indent + indent);
                    } else {
                        System.out.print(" [Not installed] ");
                    }
                }
            }
        }
    }
    /**
     * Helper method to read the input of user
     * Method returns true if user types "y", "yes" or <Enter> 
     *
     * @return boolean  - true if condition above satisfied. Otherwise false
     */
    private boolean readPrompt() 
    {
        String answer = "No";
        try {
            answer = br.readLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes") || answer.equals(""));
    }

    public boolean runConsoleFromPropertiesFile(AutomatedInstallData installData, Properties p) 
    {
        // not implemented
        return false;
    }

    public boolean runGeneratePropertiesFile(AutomatedInstallData installData,
                                                PrintWriter printWriter) 
    {
        // not implemented
        return false;
    }
}
