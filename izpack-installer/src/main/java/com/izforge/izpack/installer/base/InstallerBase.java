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
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Common utility functions for the GUI and text installers. (Do not import swing/awt classes to
 * this class.)
 *
 * @author Jonathan Halliday
 * @author Julien Ponge
 */
public abstract class InstallerBase
{

    private static final Logger LOGGER = Logger.getLogger(InstallerBase.class.getName());

    protected ResourceManager resourceManager;

    /**
     * Abstract constructor which need resource manager
     *
     * @param resourceManager the resource manager
     */
    protected InstallerBase(ResourceManager resourceManager)
    {
        this.resourceManager = resourceManager;
    }

    /**
     * Refreshes Dynamic Variables.
     */
    public static void refreshDynamicVariables(AutomatedInstallData installdata,
                                               VariableSubstitutor... substitutors) throws Exception
    {
        Map<String, List<DynamicVariable>> dynamicvariables = installdata.getDynamicvariables();
        RulesEngine rules = installdata.getRules();

        LOGGER.log(Level.FINER, "Start refreshing dynamic variables");
        if (dynamicvariables != null)
        {
            for (String dynvarname : dynamicvariables.keySet())
            {
                for (DynamicVariable dynvar : dynamicvariables.get(dynvarname))
                {
                    boolean refresh = true;
                    String conditionid = dynvar.getConditionid();
                    if ((conditionid != null) && (conditionid.length() > 0))
                    {
                        if ((rules != null) && !rules.isConditionTrue(conditionid))
                        {
                            LOGGER.log(Level.FINER, "Refreshing dynamic variable " + dynvarname + " skipped due to unmet condition " + conditionid);
                            refresh = false;
                        }
                    }
                    if (refresh)
                    {
                        String newValue = dynvar.evaluate(substitutors);
                        if (newValue != null) {
                            LOGGER.log(Level.FINER, "Dynamic variable " + dynvar.getName() + " set: " + newValue);
                            installdata.getVariables().setProperty(dynvar.getName(), newValue);
                        } else {
                            LOGGER.log(Level.FINER, "Dynamic variable " + dynvar.getName() + " unchanged: " + dynvar.getValue());
                        }
                    }
                }
            }
        }
        LOGGER.log(Level.FINER, "Finished refreshing dynamic variables");

    }

}
