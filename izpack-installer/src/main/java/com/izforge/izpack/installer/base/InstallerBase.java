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

package com.izforge.izpack.installer.base;

import com.izforge.izpack.data.*;
import com.izforge.izpack.data.DynamicVariable;
import com.izforge.izpack.installer.ResourceNotFoundException;
import com.izforge.izpack.rules.Condition;
import com.izforge.izpack.rules.RulesEngine;
import com.izforge.izpack.util.*;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.*;

/**
 * Common utility functions for the GUI and text installers. (Do not import swing/awt classes to
 * this class.)
 *
 * @author Jonathan Halliday
 * @author Julien Ponge
 */
public abstract class InstallerBase {

    private List<InstallerRequirement> installerrequirements;
    private Map<String, List<DynamicVariable>> dynamicvariables;

    protected ResourceManager resourceManager;

    /**
     * Abstract constructor which need resource manager
     * @param resourceManager
     */
    protected InstallerBase(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    /**
     * Returns an ArrayList of the available langpacks ISO3 codes.
     *
     * @return The available langpacks list.
     * @throws Exception Description of the Exception
     */
    public List<String> getAvailableLangPacks() throws Exception {
        // We read from the langpacks file in the jar
        InputStream in = resourceManager.getInputStream("langpacks.info");
        ObjectInputStream objIn = new ObjectInputStream(in);
        List<String> available = (List<String>) objIn.readObject();
        objIn.close();
        return available;
    }

    /**
     * Loads Dynamic Variables.
     */
    protected void loadDynamicVariables() {
        try {
            InputStream in = InstallerFrame.class.getResourceAsStream("/dynvariables");
            ObjectInputStream objIn = new ObjectInputStream(in);
            dynamicvariables = (Map<String, List<DynamicVariable>>) objIn.readObject();
            objIn.close();
        }
        catch (Exception e) {
            Debug.trace("Cannot find optional dynamic variables");
            System.out.println(e);
        }
    }

    /**
     * Load installer conditions
     *
     * @throws Exception
     */
    public void loadInstallerRequirements() throws Exception {
        InputStream in = InstallerBase.class.getResourceAsStream("/installerrequirements");
        ObjectInputStream objIn = new ObjectInputStream(in);
        installerrequirements = (List<InstallerRequirement>) objIn.readObject();
        objIn.close();
    }


    protected void showMissingRequirementMessage(String message) {
        Debug.log(message);
    }

    /**
     * Gets the stream to a resource.
     *
     * @param res The resource id.
     * @return The resource value, null if not found
     * @throws Exception
     */
    public InputStream getResource(String res) throws Exception {
        InputStream result;
        String basePath = "";
        ResourceManager rm;
        try {
            rm = ResourceManager.getInstance();
            basePath = rm.getResourceBasePath();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        result = this.getClass().getResourceAsStream(basePath + res);

        if (result == null) {
            throw new ResourceNotFoundException("Warning: Resource not found: "
                    + res);
        }
        return result;
    }

    /**
     * Refreshes Dynamic Variables.
     */
    protected void refreshDynamicVariables(VariableSubstitutor substitutor, AutomatedInstallData installdata, RulesEngine rules) {
        Debug.log("refreshing dyamic variables.");
        if (dynamicvariables != null) {
            for (String dynvarname : dynamicvariables.keySet()) {
                Debug.log("Variable: " + dynvarname);
                for (DynamicVariable dynvar : dynamicvariables.get(dynvarname)) {
                    boolean refresh = false;
                    String conditionid = dynvar.getConditionid();
                    Debug.log("condition: " + conditionid);
                    if ((conditionid != null) && (conditionid.length() > 0)) {
                        if ((rules != null) && rules.isConditionTrue(conditionid)) {
                            Debug.log("refresh condition");
                            // condition for this rule is true
                            refresh = true;
                        }
                    } else {
                        Debug.log("refresh condition");
                        // empty condition
                        refresh = true;
                    }
                    if (refresh) {
                        String newvalue = substitutor.substitute(dynvar.getValue(), null);
                        Debug.log("newvalue: " + newvalue);
                        installdata.getVariables().setProperty(dynvar.getName(), newvalue);
                    }
                }
            }
        }
    }


    public boolean checkInstallerRequirements(AutomatedInstallData installdata) throws Exception {
        boolean result = true;

        for (InstallerRequirement installerrequirement : this.installerrequirements) {
            String conditionid = installerrequirement.getCondition();
            Condition condition = RulesEngine.getCondition(conditionid);
            if (condition == null) {
                Debug.log(conditionid + " not a valid condition.");
                throw new Exception(conditionid + "could not be found as a defined condition");
            }
            if (!condition.isTrue()) {
                String message = installerrequirement.getMessage();
                if ((message != null) && (message.length() > 0)) {
                    String localizedMessage = installdata.getLangpack().getString(message);
                    this.showMissingRequirementMessage(localizedMessage);
                }
                result = false;
                break;
            }
        }
        return result;
    }
}
