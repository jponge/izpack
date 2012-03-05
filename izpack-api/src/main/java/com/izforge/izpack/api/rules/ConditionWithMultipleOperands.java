package com.izforge.izpack.api.rules;

import java.util.HashSet;
import java.util.Set;

public abstract class ConditionWithMultipleOperands extends Condition
{
    protected Set<Condition> nestedConditions = new HashSet<Condition>();

    public void addOperands(Condition ... operands)
    {
      for (Condition condition : operands)
      {
          nestedConditions.add(condition);
      }
    }
}
