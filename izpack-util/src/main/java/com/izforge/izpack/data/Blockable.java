package com.izforge.izpack.data;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Blockable enum for pack file
 *
 * @author Anthonin Bonnefoy
 */
public enum Blockable
{
    BLOCKABLE_NONE("none"), BLOCKABLE_AUTO("auto"), BLOCKABLE_FORCE("force");

    private static Map<String, Blockable> lookup;

    private String attribute;

    Blockable(String attribute)
    {
        this.attribute = attribute;
    }

    static
    {
        lookup = new HashMap<String, Blockable>();
        for (Blockable blockable : EnumSet.allOf(Blockable.class))
        {
            lookup.put(blockable.getAttribute(), blockable);
        }
    }

    public String getAttribute()
    {
        return attribute;
    }

    public static Blockable getBlockableFromAttribute(String attribute)
    {
        if (attribute != null && lookup.containsKey(attribute))
        {
            return lookup.get(attribute);
        }
        return null;
    }
}
