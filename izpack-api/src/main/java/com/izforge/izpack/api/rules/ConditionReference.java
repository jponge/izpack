package com.izforge.izpack.api.rules;

public abstract class ConditionReference extends Condition
{
    private Condition referencedCondition;

    public Condition getReferencedCondition()
    {
        return referencedCondition;
    }

    public void setReferencedCondition(Condition referencedCondition)
    {
        this.referencedCondition = referencedCondition;
    }

    public abstract void resolveReference();
}
