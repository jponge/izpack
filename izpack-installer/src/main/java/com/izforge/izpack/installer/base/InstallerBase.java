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
import com.izforge.izpack.installer.ResourceNotFoundException;
import com.izforge.izpack.rules.Condition;
import com.izforge.izpack.rules.RulesEngineImpl;
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


    protected ResourceManager resourceManager;

    /**
     * Abstract constructor which need resource manager
     * @param resourceManager
     */
    protected InstallerBase(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
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



    public boolean checkInstallerRequirements(AutomatedInstallData installdata) throws Exception {
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
                    this.showMissingRequirementMessage(localizedMessage);
                }
                result = false;
                break;
            }
        }
        return result;
    }
}
