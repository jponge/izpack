package com.izforge.izpack.api.data;


import java.util.Properties;

import com.izforge.izpack.api.exception.IzPackException;

/**
 * A collection of variables.
 *
 * @author Tim Anderson
 */
public interface Variables
{

    /**
     * Sets a variable.
     *
     * @param name  the variable name
     * @param value the variable value. May be {@code null}
     */
    void set(String name, String value);

    /**
     * Returns the value of the specified variable.
     *
     * @param name the variable name
     * @return the value. May be {@code null}
     */
    String get(String name);

    /**
     * Returns the value of the specified variable.
     *
     * @param name         the variable name
     * @param defaultValue the default value if the variable doesn't exist, or is {@code null}
     * @return the value, or {@code defaultValue} if the variable doesn't exist or is {@code null}
     */
    String get(String name, String defaultValue);

    /**
     * Returns the boolean value of the specified variable.
     *
     * @param name the variable name
     * @return the boolean value, or {@code false} if the variable doesn't exist or is not a boolean
     */
    boolean getBoolean(String name);

    /**
     * Returns the boolean value of the specified variable.
     *
     * @param name         the variable name
     * @param defaultValue the default value if the variable doesn't exist, or is {@code null}
     * @return the boolean value, or {@code defaultValue} if the variable doesn't exist or is not a boolean
     */
    boolean getBoolean(String name, boolean defaultValue);

    /**
     * Returns the integer value of the specified variable.
     *
     * @param name the variable name
     * @return the integer value, or {@code -1} if the variable doesn't exist or is not an integer
     */
    int getInt(String name);

    /**
     * Returns the integer value of the specified variable.
     *
     * @param name the variable name
     * @return the integer value, or {@code defaultValue} if the variable doesn't exist or is not an integer
     */
    int getInt(String name, int defaultValue);

    /**
     * Returns the long value of the specified variable.
     *
     * @param name the variable name
     * @return the long value, or {@code -1} if the variable doesn't exist or is not a long
     */
    long getLong(String name);

    /**
     * Returns the long value of the specified variable.
     *
     * @param name the variable name
     * @return the long value, or {@code defaultValue} if the variable doesn't exist or is not a long
     */
    long getLong(String name, long defaultValue);

    /**
     * Replaces any variables in the supplied value.
     *
     * @param value the value. May be {@code null}
     * @return the value with variables replaced, or {@code value} if there were no variables to replace, or
     *         replacement failed
     */
    String replace(String value);

    /**
     * Adds a dynamic variable.
     *
     * @param variable the variable to add
     */
    void add(DynamicVariable variable);

    /**
     * Refreshes dynamic variables.
     *
     * @throws IzPackException if variables cannot be refreshed
     */
    void refresh();

    /**
     * Exposes the variables as properties.
     *
     * @return the variables
     */
    Properties getProperties();

}