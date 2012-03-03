/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package com.izforge.izpack.installer.requirement;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.InstallerRequirement;
import com.izforge.izpack.api.installer.RequirementChecker;
import com.izforge.izpack.api.rules.Condition;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.util.Debug;

public class InstallerRequirementChecker implements RequirementChecker
{
    private final AutomatedInstallData installData;
    private final RulesEngine rules;
    private final Prompt prompt;

    public InstallerRequirementChecker(AutomatedInstallData installData, RulesEngine rules, Prompt prompt)
    {
        this.installData = installData;
        this.rules = rules;
        this.prompt = prompt;
    }

    /**
     * Determines if installation requirements are met.
     *
     * @return <tt>true</tt> if requirements are met, otherwise <tt>false</tt>
     */
    @Override
    public boolean check()
    {
        boolean fail = false;

        for (InstallerRequirement requirement : installData.getInstallerrequirements())
        {
            String id = requirement.getCondition();
            Condition condition = rules.getCondition(id);
            if (condition == null)
            {
                fail = true;
                Debug.error(id + " is not a valid condition.");
                break;
            }
            if (!condition.isTrue())
            {
                fail = true;
                String message = requirement.getMessage();
                if ((message != null) && (message.length() > 0))
                {
                    String localizedMessage = installData.getLangpack().getString(message);
                    prompt.message(Prompt.Type.ERROR, localizedMessage);
                }
                break;
            }
        }
        return !fail;
    }

}
