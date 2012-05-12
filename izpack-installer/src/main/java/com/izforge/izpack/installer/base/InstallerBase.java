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

import java.util.logging.Logger;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;

/**
 * Common utility functions for the GUI and text installers. (Do not import swing/awt classes to
 * this class.)
 *
 * @author Jonathan Halliday
 * @author Julien Ponge
 */
public abstract class InstallerBase
{

    private static final Logger logger = Logger.getLogger(InstallerBase.class.getName());

    /**
     * The resources.
     */
    private final ResourceManager resourceManager;

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
     *
     * @deprecated see {@link com.izforge.izpack.api.data.AutomatedInstallData#refreshVariables()}
     */
    @Deprecated
    public static void refreshDynamicVariables(AutomatedInstallData installdata,
                                               VariableSubstitutor... substitutors) throws Exception
    {
        installdata.refreshVariables();
        logger.fine("Finished refreshing dynamic variables");

    }

    /**
     * Returns the resource manager.
     *
     * @return the resource manager
     */
    protected ResourceManager getResourceManager()
    {
        return resourceManager;
    }

}
