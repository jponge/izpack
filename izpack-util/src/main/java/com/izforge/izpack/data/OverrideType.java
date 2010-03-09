package com.izforge.izpack.data;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Override type for pack
 *
 * @author Anthonin Bonnefoy
 */
public enum OverrideType {
    OVERRIDE_FALSE("false"), OVERRIDE_TRUE("true"), OVERRIDE_ASK_FALSE("askfalse"), OVERRIDE_ASK_TRUE("asktrue"), OVERRIDE_UPDATE("update");

    private static Map<String, OverrideType> lookup;

    private String attribute;

    OverrideType(String attribute) {
        this.attribute = attribute;
    }

    static {
        lookup = new HashMap<String, OverrideType>();
        for (OverrideType overridetype : EnumSet.allOf(OverrideType.class)) {
            lookup.put(overridetype.getAttribute(), overridetype);
        }
    }

    public String getAttribute() {
        return attribute;
    }

    public static OverrideType getOverrideTypeFromAttribute(String attribute) {
        if (attribute != null && lookup.containsKey(attribute)) {
            return lookup.get(attribute);
        }
        return null;
    }
}
