/*
 * IzPack - Copyright 2001-2010 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2010 Rene Krell
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

package com.izforge.izpack.event;

import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.config.ConfigurableTask;


public class ConfigurationActionTask
{
    private String condition;
    private ConfigurableTask task;
    private RulesEngine rules;


    public ConfigurationActionTask(ConfigurableTask task, String condition, RulesEngine rules)
    {
        this.task = task;
        this.condition = condition;
        this.rules = rules;
    }

    public ConfigurableTask getConfigurableTask()
    {
        return task;
    }

    public void setConfigurableTask(ConfigurableTask task)
    {
        this.task = task;
    }

    public String getCondition()
    {
        return condition;
    }

    public void setCondition(String condition)
    {
        this.condition = condition;
    }

    public void execute() throws Exception
    {
        if (condition == null || condition.trim().length() == 0 || rules.isConditionTrue(condition))
        {
            Debug.trace("Executing configuration task class "+task.getClass().getName());
            this.task.execute();
        }
        else
        {
            Debug.trace("Condition "+condition+" not met - skipping configuration task class "+task.getClass().getName());
        }
    }
}
