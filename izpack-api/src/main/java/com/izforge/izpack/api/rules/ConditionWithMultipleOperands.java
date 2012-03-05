package com.izforge.izpack.api.rules;

import java.util.ArrayList;
import java.util.List;

public abstract class ConditionWithMultipleOperands extends Condition
{
    protected List<Condition> nestedConditions = new ArrayList<Condition>();

    public void addOperands(Condition ... operands)
    {
      for (Condition condition : operands)
      {
          nestedConditions.add(condition);
      }
    }
}
