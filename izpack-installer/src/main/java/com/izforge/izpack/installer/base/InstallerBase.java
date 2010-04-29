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

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.DynamicVariable;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.api.installer.InstallerRequirementDisplay;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.substitutor.DynamicVariableSubstitutor;

import java.util.List;
import java.util.Map;

/**
 * Common utility functions for the GUI and text installers. (Do not import swing/awt classes to
 * this class.)
 *
 * @author Jonathan Halliday
 * @author Julien Ponge
 */
public abstract class InstallerBase implements InstallerRequirementDisplay
{

    protected ResourceManager resourceManager;

    /**
     * Abstract constructor which need resource manager
     *
     * @param resourceManager
     */
    protected InstallerBase(ResourceManager resourceManager)
    {
        this.resourceManager = resourceManager;
    }

    public abstract void showMissingRequirementMessage(String message);

    /**
     * Refreshes Dynamic Variables.
     */
    public static void refreshDynamicVariables(AutomatedInstallData installdata,
                                               VariableSubstitutor... substitutors)
            throws Exception
    {
        Map<String, List<DynamicVariable>> dynamicvariables = installdata.getDynamicvariables();
        RulesEngine rules = installdata.getRules();

        //Debug.log("refreshing dynamic variables");
        if (dynamicvariables != null)
        {
            for (String dynvarname : dynamicvariables.keySet())
            {
                //Debug.log("Dynamic variable: " + dynvarname);
                for (DynamicVariable dynvar : dynamicvariables.get(dynvarname))
                {
                    boolean refresh = true;
                    String conditionid = dynvar.getConditionid();
                    //Debug.log("condition: " + conditionid);
                    if ((conditionid != null) && (conditionid.length() > 0))
                    {
                        if ((rules != null) && !rules.isConditionTrue(conditionid))
                        {
                            //Debug.log("skipped refreshing dynamic variable due to unmet condition " + conditionid);
                            // condition for this rule is true
                            refresh = false;
                        }
                    }
                    if (refresh)
                    {
                        // Add self replacing of previously replaced dynamic variables
                        VariableSubstitutor[] newsubstitutors = new DynamicVariableSubstitutor[substitutors.length + 1];
                        for (int i = 0; i < substitutors.length; i++)
                        {
                            newsubstitutors[i] = substitutors[i];
                        }
                        newsubstitutors[substitutors.length] = new DynamicVariableSubstitutor(dynamicvariables, rules);

                        String newValue = dynvar.evaluate(newsubstitutors);
                        if (newValue != null)
                        {
                            //Debug.log("dynamic variable " + dynvar.getName() + ": " + newValue);
                            installdata.getVariables().setProperty(dynvar.getName(), newValue);
                        }
                        else
                        {
                            //Debug.log("dynamic variable " + dynvar.getName() + " unchanged: " + dynvar.getValue());
                        }
                    }
                }
            }
        }
    }

}
