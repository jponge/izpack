package com.izforge.izpack.api.rules;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Comparison operators that can be used for in conditions
 *
 * @author Rene Krell
 */
public enum ComparisonOperator
{
    EQUAL("eq"), NOTEQUAL("ne"),
    LESSEQUAL("leq"), LESS("lt"),
    GREATEREQUAL("geq"), GREATER("gt");

    private static Map<String, ComparisonOperator> lookup;

    private String attribute;

    ComparisonOperator(String attribute)
    {
        this.attribute = attribute;
    }

    static
    {
        lookup = new HashMap<String, ComparisonOperator>();
        for (ComparisonOperator op : EnumSet.allOf(ComparisonOperator.class))
        {
            lookup.put(op.getAttribute(), op);
        }
    }

    public String getAttribute()
    {
        return attribute;
    }

    public static ComparisonOperator getComparisonOperatorFromAttribute(String attribute)
    {
        if (attribute != null && lookup.containsKey(attribute))
        {
            return lookup.get(attribute);
        }
        return null;
    }
}
