package com.izforge.izpack.installer.validator;

import java.util.Set;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.installer.DataValidator;
import com.izforge.izpack.api.rules.RulesEngine;


public class ConditionValidator implements DataValidator
{
    private String lastFailedConditionId;

    @Override
    public Status validateData(AutomatedInstallData idata)
    {
        RulesEngine rules = idata.getRules();
        Set<String> conditionIds = rules.getKnownConditionIds();
        for (String conditionId : conditionIds)
        {
            if (conditionId.toLowerCase().startsWith(this.getClass().getSimpleName().toLowerCase()+"."))
            {
                if (!rules.getCondition(conditionId).isTrue())
                {
                    lastFailedConditionId = conditionId;
                    return Status.ERROR;
                }
            }
        }
        return Status.OK;
    }

    @Override
    public String getErrorMessageId()
    {
        return lastFailedConditionId+".error.message";
    }

    @Override
    public String getWarningMessageId()
    {
        return null;
    }

    @Override
    public boolean getDefaultAnswer()
    {
        return false;
    }

}
