package com.izforge.izpack.api.data;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Anthonin Bonnefoy
 */
public enum LookAndFeels
{
    LIQUID("liquid"), LOOKS("looks"), SUBSTANCE("substance"), NIMBUS("nimbus"), KUNSTSTOFF("kunststoff");

    private String name;

    private static Map<String, LookAndFeels> lookup;

    static
    {
        lookup = new HashMap<String, LookAndFeels>();
        EnumSet<LookAndFeels> enumSet = EnumSet.allOf(LookAndFeels.class);
        for (LookAndFeels elem : enumSet)
        {
            lookup.put(elem.name, elem);
        }
    }

    LookAndFeels(String name)
    {
        this.name = name;
    }

    public static LookAndFeels lookup(String laf)
    {
        if (lookup.containsKey(laf))
        {
            return lookup.get(laf);
        }
        return null;
    }

}
