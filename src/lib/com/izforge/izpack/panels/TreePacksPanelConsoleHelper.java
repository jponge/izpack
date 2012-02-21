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
import com.izforge.izpack.rules.RulesEngine;
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
            drawHelper(treeData, selectedPacks, installData, idToPack, packParentName, true, "\t");
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
     * @param installData       - Database of izpack
     * @param idToPack          - Map that mapds the id of the available packs to the actual Pack object
     * @param packParent        - The current "parent" pack to process
     * @param packMaster        - boolean to know if packParent is a top-level pack
     * @param indent            - String to know by how much the child packs should be indented
     *
     * @return void
     */
    private void drawHelper(final Map<String, List<String>> treeData, final List<Pack> selectedPacks,final AutomatedInstallData installData,
                            final Map<String, Pack> idToPack, final String packParent, boolean packMaster,final String indent) 
    {
        Pack p                      = null;

        /*
         * If that packParent contains children,
         * then run recursively and ask whether
         * you want to install the child packs 
         * too [if parent pack selected]
         */
        if (treeData.containsKey(packParent)) {
            p = idToPack.get(packParent);

            // If the pack is a top-level pack and that top-level pack was not 
            // selected, then return. This will avoid prompting the user to
            // install the child packs.
            if(packMaster && !selectHelper(treeData, selectedPacks, installData, idToPack, p, packMaster,indent)) {
                   return;
            }
            // Now iterate through the child packs of the parent pack.
            for (String id : treeData.get(packParent)) {
                p = idToPack.get(id);
                selectHelper(treeData, selectedPacks, installData, idToPack, p, false, indent);
            }
        }
    }
    /**
     * Helper method to ask/check if the pack can/needs to be installed
     * If top-level pack, square brackets will be placed in between
     * the pack id.
     *
     * It asks the user if it wants to install the pack if:
     * 1. the pack is not required
     * 2. the pack has no condition string
     *
     * @return true     - if pack selected
     * @return false    - if pack not selected
     */
    private boolean selectHelper(final Map<String, List<String>> treeData, final List<Pack> selectedPacks,final AutomatedInstallData installData,
                            final Map<String, Pack> idToPack, final Pack p, boolean packMaster,final String indent) 
    {
        Boolean conditionSatisfied  = checkCondition(installData, p);
        Boolean conditionExists     = !(conditionSatisfied == null);
        String packName             = p.name;
        String id                   = p.id;

        // If a condition is set to that pack
        if (conditionExists) {
            if (conditionSatisfied) {
                out((packMaster ? "[" + id + "]" : indent + packName) + " [Already Selected]");

                selectedPacks.add(p);
                // we call drawHelper again to check if that pack has child packs
                // If that pack is a top-level pack, then don't run drawHelper as
                // it will create an infinite loop 
                if (!packMaster) drawHelper(treeData, selectedPacks, installData, idToPack, id, packMaster, indent + indent);
                return true;
            } else {
                // condition says don't install!
                out((packMaster ? "[" + id + "]" : indent + packName) + " [Not Selected]");
                return false;
            }
        // If no condition specified
        } else if (p.required) {
            out((packMaster ? "[" + packName + "]" : indent + packName) + " [required]");

            selectedPacks.add(p);
            if (!packMaster) drawHelper(treeData, selectedPacks, installData, idToPack, id, packMaster, indent + indent);
            return true;
        // Prompt the user
        } else {
            System.out.print((packMaster ? "["+ packName + "] ":indent + packName) + " [y/n] ");
            if (readPrompt()) {
                selectedPacks.add(p);
                if (!packMaster) drawHelper(treeData, selectedPacks, installData, idToPack, id, packMaster, indent + indent);
                return true;
            } else {
                return false;
            }
        }
    }
    /**
     * helper method to know if the condition assigned to the pack is satisfied
     *
     * @param installData       - the data of izpack
     * @param pack              - the pack whose condition needs to be checked 
     * @return true             - if the condition is satisfied
     *         false            - if condition not satisfied
     *         null             - if no condition assigned
     */

    private Boolean checkCondition(AutomatedInstallData installData, Pack pack) 
    {
        if (pack.hasCondition()) {
            return installData.getRules().isConditionTrue(pack.getCondition());
        } else {
            return null;
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
