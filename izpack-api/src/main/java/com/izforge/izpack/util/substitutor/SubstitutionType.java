package com.izforge.izpack.util.substitutor;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration for substitution type
 *
 * @author Anthonin Bonnefoy
 */
public enum SubstitutionType {

    /**
     * Plain file.
     */
    TYPE_PLAIN("plain"),

    /**
     * Java properties file.
     */
    TYPE_JAVA_PROPERTIES("javaprop"),

    /**
     * XML file.
     */
    TYPE_XML("xml"),

    /**
     * Shell file.
     */
    TYPE_SHELL("shell"),

    /**
     * Plain file with '@' start char.
     */
    TYPE_AT("at"),

    /**
     * Java file, where \ have to be escaped.
     */
    TYPE_JAVA("java"),

    /**
     * Plain file with ANT-like variable markers, ie @param@
     */
    TYPE_ANT("ant");

    private String type;

    SubstitutionType(String type) {
        this.type = type;
    }

    private static Map<String, SubstitutionType> lookup;

    static {
        lookup = new HashMap<String, SubstitutionType>();
        for (SubstitutionType substitutionType : EnumSet.allOf(SubstitutionType.class)) {
            lookup.put(substitutionType.getType(), substitutionType);
        }
    }

    public String getType() {
        return type;
    }

    /**
     * Return the substitution type
     *
     * @param typeString Type of substitution in a string form
     * @return Type of substitution from enum. Default is plain.
     */
    public static SubstitutionType lookup(String typeString) {
        if (lookup.containsKey(typeString)) {
            return lookup.get(typeString);
        }
        return TYPE_PLAIN;
    }
}
