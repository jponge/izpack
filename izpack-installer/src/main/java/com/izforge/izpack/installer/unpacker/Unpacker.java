/*
 * $Id$
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2001 Johannes Lehtinen
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

package com.izforge.izpack.installer.unpacker;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.Librarian;

/**
 * Unpacker class.
 *
 * @author Julien Ponge
 * @author Johannes Lehtinen
 * @author Tim Anderson
 */
public class Unpacker extends UnpackerBase
{

    /**
     * Constructs an <tt>Unpacker</tt>.
     *
     * @param installData         the installation data
     * @param resourceManager     the resource manager
     * @param rules               the rules engine
     * @param variableSubstitutor the variable substituter
     * @param uninstallData       the uninstallation data
     * @param librarian           the librarian
     * @param housekeeper         the housekeeper
     */
    public Unpacker(AutomatedInstallData installData, ResourceManager resourceManager, RulesEngine rules,
                    VariableSubstitutor variableSubstitutor, UninstallData uninstallData,
                    Librarian librarian, Housekeeper housekeeper)
    {
        super(installData, resourceManager, rules, variableSubstitutor, uninstallData, librarian, housekeeper);
    }

}

