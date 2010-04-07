/*
 * $Id: Compiler.java 1918 2007-11-29 14:02:17Z dreil $
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2010 René Krell
 *
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

package com.izforge.izpack.core.data;

import java.io.Serializable;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.DynamicConditionValidator;
import com.izforge.izpack.api.rules.RulesEngine;


/**
 * Implicit data validator for checking a set of conditions on each panel change
 */
public class DynamicConditionValidatorImpl implements DynamicConditionValidator, Serializable
{
    /**
     *
     */
    private static final long serialVersionUID = -3752323264590369711L;

    private String conditionId;
    private String errorMessage;


    public DynamicConditionValidatorImpl(String conditionId, String errorMessage)
    {
        this.conditionId = conditionId;
        this.errorMessage = errorMessage;
    }

    public Status validateData(AutomatedInstallData idata)
    {
        RulesEngine rules = idata.getRules();
        if (!rules.isConditionTrue(conditionId))
        {
            return Status.ERROR;
        }

        return Status.OK;
    }

    public String getErrorMessageId()
    {
        if (errorMessage != null)
            return errorMessage;

        return null;
    }

    public String getWarningMessageId()
    {
        return null;
    }

    public boolean getDefaultAnswer()
    {
        return false;
    }

}
