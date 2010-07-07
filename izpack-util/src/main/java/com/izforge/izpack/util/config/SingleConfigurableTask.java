/*
 * IzPack - Copyright 2001-2010 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2010 Rene Krell
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.util.config;

import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.file.types.EnumeratedAttribute;
import org.ini4j.*;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public abstract class SingleConfigurableTask implements ConfigurableTask
{

    /*
     * Instance variables.
     */

    private boolean patchPreserveEntries = true;

    private boolean patchPreserveValues = true;

    private boolean patchResolveVariables = false;

    protected boolean createConfigurable = true;

    /*
     * Internal variables.
     */

    protected Configurable configurable;

    protected Configurable fromConfigurable;

    private Vector<Entry> entries = new Vector<Entry>();

    /**
     * Whether to preserve equal entries but not necessarily their values from an old configuration,
     * if they can be found (default: true).
     *
     * @param preserveEntries - true to preserve equal entries from an old configuration
     */
    public void setPatchPreserveEntries(boolean preserveEntries)
    {
        this.patchPreserveEntries = preserveEntries;
    }

    /**
     * Whether to preserve the values of equal entries from an old configuration, if they can be
     * found (default: true). Set false to overwrite old configuration values by default with the
     * new ones, regardless whether they have been already set in an old configuration. Values from
     * an old configuration can only be preserved, if the appropriate entries exist in an old
     * configuration.
     *
     * @param preserveValues - true to preserve the values of equal entries from an old
     *                       configuration
     */
    public void setPatchPreserveValues(boolean preserveValues)
    {
        patchPreserveValues = preserveValues;
    }

    /**
     * Whether variables should be resolved during patching.
     *
     * @param resolve - true to resolve in-value variables
     */
    public void setPatchResolveVariables(boolean resolve)
    {
        patchResolveVariables = resolve;
    }

    /**
     * Whether the configuration file or registry root entry should be created if it doesn't already
     * exist (default: true).
     *
     * @param create - whether to create a new configuration file or registry root entry
     */
    public void setCreate(boolean create)
    {
        createConfigurable = create;
    }

    public void execute() throws Exception
    {
        Config.getGlobal().setHeaderComment(false);
        Config.getGlobal().setEmptyLines(true);
        Config.getGlobal().setAutoNumbering(true);
        checkAttributes();
        readConfigurable();
        readSourceConfigurable();
        patchConfigurable();
        executeOperation();
        writeConfigurable();
    }

    public Entry createEntry()
    {
        Entry e = new Entry();
        entries.addElement(e);
        return e;
    }

    private void patchConfigurable() throws Exception
    {
        if (fromConfigurable != null)
        {
            Set<String> toKeySet;
            Set<String> fromKeySet;
            if (configurable instanceof Options)
            {
                toKeySet = ((Options) configurable).keySet();
                fromKeySet = ((Options) fromConfigurable).keySet();
                for (Iterator<String> iterator = fromKeySet.iterator(); iterator.hasNext();)
                {
                    String key = iterator.next();
                    String fromValue = (String) (patchResolveVariables ? ((Options) fromConfigurable)
                            .fetch(key)
                            : ((Options) fromConfigurable).get(key));
                    if (patchPreserveEntries && !toKeySet.contains(key))
                    {
                        Debug.log("Preserve option file entry \"" + key + "\"");
                        ((Options) configurable).add(key, fromValue);
                    }
                    else if (patchPreserveValues && ((Options) configurable).keySet().contains(key))
                    {
                        Debug.log("Preserve option value for key \"" + key + "\": \"" + fromValue
                                + "\"");
                        ((Options) configurable).put(key, fromValue);
                    }
                }
            }
            else if (configurable instanceof Ini)
            {
                Set<String> sectionKeySet = ((Ini) configurable).keySet();
                Set<String> fromSectionKeySet = ((Ini) fromConfigurable).keySet();
                for (Iterator<String> iterator = fromSectionKeySet.iterator(); iterator.hasNext();)
                {
                    String fromSectionKey = iterator.next();
                    if (sectionKeySet.contains(fromSectionKey))
                    {
                        Ini.Section fromSection = (Ini.Section) ((Ini) fromConfigurable)
                                .get(fromSectionKey);
                        Ini.Section toSection = (Ini.Section) ((Ini) configurable)
                                .get(fromSectionKey);
                        fromKeySet = fromSection.keySet();
                        toKeySet = null;
                        if (toSection != null)
                        {
                            toKeySet = toSection.keySet();
                        }
                        for (Iterator<String> iterator2 = fromKeySet.iterator(); iterator2
                                .hasNext();)
                        {
                            String fromKey = (String) iterator2.next();
                            if (toSection == null)
                            {
                                Debug.log("Adding new INI section [" + fromSectionKey + "]");
                                toSection = ((Ini) configurable).add(fromSectionKey);
                            }
                            String fromValue = (String) (patchResolveVariables ? fromSection
                                    .fetch(fromKey) : fromSection.get(fromKey));
                            if (patchPreserveEntries && !toKeySet.contains(fromKey))
                            {
                                Debug.log("Preserve INI file entry \"" + fromKey
                                        + "\" in section [" + fromSectionKey + "]: " + fromValue);
                                toSection.add(fromKey, fromValue);
                            }
                            else if (patchPreserveValues && toKeySet.contains(fromKey))
                            {
                                Debug.log("Preserve INI file entry value for key \"" + fromKey
                                        + "\" in section [" + fromSectionKey + "]: " + fromValue);
                                toSection.put(fromKey, fromValue);
                            }
                        }
                    }
                }
            }
            else if (configurable instanceof Reg)
            {
                Set<String> rootKeySet = ((Reg) configurable).keySet();
                Set<String> fromRootKeySet = ((Reg) fromConfigurable).keySet();
                for (Iterator<String> iterator = fromRootKeySet.iterator(); iterator.hasNext();)
                {
                    String fromRootKey = iterator.next();
                    if (rootKeySet.contains(fromRootKey))
                    {
                        Reg.Key fromRegKey = ((Reg) fromConfigurable).get(fromRootKey);
                        Reg.Key toRegKey = ((Reg) configurable).get(fromRootKey);
                        fromKeySet = fromRegKey.keySet();
                        toKeySet = null;
                        if (toRegKey != null)
                        {
                            toKeySet = toRegKey.keySet();
                        }
                        for (Iterator<String> iterator2 = fromKeySet.iterator(); iterator2
                                .hasNext();)
                        {
                            String fromKey = (String) iterator2.next();
                            if (toRegKey == null)
                            {
                                Debug.log("Adding new registry root key " + fromRootKey);
                                toRegKey = ((Reg) configurable).add(fromRootKey);
                            }
                            String fromValue = (String) (patchResolveVariables ? fromRegKey
                                    .fetch(fromKey) : fromRegKey.get(fromKey));
                            if (patchPreserveEntries && !toKeySet.contains(fromKey))
                            {
                                Debug.log("Preserve registry value " + fromKey + " under root key "
                                        + fromRootKey + ": " + fromValue);
                                toRegKey.add(fromKey, fromValue);
                            }
                            else if (patchPreserveValues && toKeySet.contains(fromKey))
                            {
                                Debug.log("Preserve registry data for value " + fromKey
                                        + " in root key " + fromRootKey + ": " + fromValue);
                                toRegKey.put(fromKey, fromValue);
                            }
                        }
                    }
                }
            }
            else
            {
                throw new Exception("Unknown configurable type class: "
                        + configurable.getClass().getName());
            }
        }
    }

    private void executeOperation() throws Exception
    {
        for (Enumeration<Entry> e = entries.elements(); e.hasMoreElements();)
        {
            Entry entry = (Entry) e.nextElement();
            entry.executeOn(configurable);
        }
    }

    protected abstract void checkAttributes() throws Exception;

    protected abstract void readSourceConfigurable() throws Exception;

    protected abstract void readConfigurable() throws Exception;

    protected abstract void writeConfigurable() throws Exception;

    /**
     * Instance of this class represents nested elements of a task configuration file.
     */
    public static class Entry
    {

        private static final int DEFAULT_INT_VALUE = 0;

        private static final String DEFAULT_DATE_VALUE = "now";

        private static final String DEFAULT_STRING_VALUE = "";

        protected String section = null;

        protected String key = null;

        protected String value = null;

        private boolean resolveVariables = false;

        private int type = Type.STRING_TYPE;

        private int operation = Operation.EQUALS_OPER;

        private String defaultValue = null;

        private String newValue = null;

        private String pattern = null;

        private int field = Calendar.DATE;

        /**
         * Name of the key/value pair
         */
        public void setKey(String value)
        {
            this.key = value;
        }

        /**
         * Value to set (=), to add (+) or subtract (-)
         */
        public void setValue(String value)
        {
            this.value = value;
        }

        /**
         * Whether variables should be resolved during manipulating with explicit modifiers nested
         * in the Ant task.
         *
         * @param resolve - true to resolve variables in explicit modifiers
         */
        public void setResolveVariables(boolean resolve)
        {
            this.resolveVariables = resolve;
        }

        /**
         * operation to apply. &quot;+&quot; or &quot;=&quot; (default) for all datatypes;
         * &quot;-&quot; for date and int only)\.
         */
        public void setOperation(Operation value)
        {
            this.operation = Operation.toOperation(value.getValue());
        }

        /**
         * Regard the value as : int, date or string (default)
         */
        public void setType(Type value)
        {
            this.type = Type.toType(value.getValue());
        }

        /**
         * Initial value to set for a key if it is not already defined in the configuration file.
         * For type date, an additional keyword is allowed: &quot;now&quot;
         */

        public void setDefault(String value)
        {
            this.defaultValue = value;
        }

        /**
         * For int and date type only. If present, Values will be parsed and formatted accordingly.
         */
        public void setPattern(String value)
        {
            this.pattern = value;
        }

        /**
         * The unit of the value to be applied to date +/- operations. Valid Values are:
         * <ul>
         * <li>millisecond</li>
         * <li>second</li>
         * <li>minute</li>
         * <li>hour</li>
         * <li>day (default)</li>
         * <li>week</li>
         * <li>month</li>
         * <li>year</li>
         * </ul>
         * This only applies to date types using a +/- operation.
         */
        public void setUnit(Unit unit)
        {
            field = unit.getCalendarField();
        }

        protected void executeOn(Configurable configurable) throws Exception
        {
            checkParameters();

            // type may be null because it wasn't set
            String oldValue = null;
            if (configurable instanceof Options)
            {
                oldValue = (String) (resolveVariables ? ((Options) configurable).fetch(key)
                        : ((Options) configurable).get(key));
            }
            else if (configurable instanceof Ini)
            {
                oldValue = resolveVariables ? ((Ini) configurable).fetch(section, key)
                        : ((Ini) configurable).get(section, key);
            }
            else if (configurable instanceof Reg)
            {
                oldValue = resolveVariables ? ((Reg) configurable).fetch(section, key)
                        : ((Reg) configurable).get(section, key);
            }
            else
            {
                throw new Exception("Unknown configurable type class: "
                        + configurable.getClass().getName());
            }

            try
            {
                if (type == Type.INTEGER_TYPE)
                {
                    executeInteger(oldValue);
                }
                else if (type == Type.DATE_TYPE)
                {
                    executeDate(oldValue);
                }
                else if (type == Type.STRING_TYPE)
                {
                    executeString(oldValue);
                }
                else
                {
                    throw new Exception("Unknown operation type: " + type);
                }
            }
            catch (NullPointerException npe)
            {
                // Default to string type
                // which means do nothing
                npe.printStackTrace();
            }

            if (newValue == null)
            {
                newValue = "";
            }

            // Insert as a string by default
            if (configurable instanceof Options)
            {
                ((Options) configurable).put(key, newValue);
            }
            else if (configurable instanceof Ini)
            {
                ((Ini) configurable).put(section, key, newValue);
            }
            else if (configurable instanceof Reg)
            {
                ((Reg) configurable).put(section, key, newValue);
            }
            else
            {
                throw new Exception("Unknown configurable type class: "
                        + configurable.getClass().getName());
            }
        }

        /**
         * Handle operations for type <code>date</code>.
         *
         * @param oldValue the current value read from the configuration file or <code>null</code>
         *                 if the <code>key</code> was not contained in the configuration file.
         */
        private void executeDate(String oldValue) throws Exception
        {
            Calendar currentValue = Calendar.getInstance();

            if (pattern == null)
            {
                pattern = "yyyy/MM/dd HH:mm";
            }
            DateFormat fmt = new SimpleDateFormat(pattern);

            String currentStringValue = getCurrentValue(oldValue);
            if (currentStringValue == null)
            {
                currentStringValue = DEFAULT_DATE_VALUE;
            }

            if ("now".equals(currentStringValue))
            {
                currentValue.setTime(new Date());
            }
            else
            {
                try
                {
                    currentValue.setTime(fmt.parse(currentStringValue));
                }
                catch (ParseException pe)
                {
                    // swallow
                }
            }

            if (operation != Operation.EQUALS_OPER)
            {
                int offset = 0;
                try
                {
                    offset = Integer.parseInt(value);
                    if (operation == Operation.DECREMENT_OPER)
                    {
                        offset = -1 * offset;
                    }
                }
                catch (NumberFormatException e)
                {
                    throw new Exception("Value not an integer on " + key);
                }
                currentValue.add(field, offset);
            }

            newValue = fmt.format(currentValue.getTime());
        }

        /**
         * Handle operations for type <code>int</code>.
         *
         * @param oldValue the current value read from the configuration file or <code>null</code>
         *                 if the <code>key</code> was not contained in the configuration file.
         */
        private void executeInteger(String oldValue) throws Exception
        {
            int currentValue = DEFAULT_INT_VALUE;
            int newValue = DEFAULT_INT_VALUE;

            DecimalFormat fmt = (pattern != null) ? new DecimalFormat(pattern)
                    : new DecimalFormat();
            try
            {
                String curval = getCurrentValue(oldValue);
                if (curval != null)
                {
                    currentValue = fmt.parse(curval).intValue();
                }
                else
                {
                    currentValue = 0;
                }
            }
            catch (NumberFormatException nfe)
            {
                // swallow
            }
            catch (ParseException pe)
            {
                // swallow
            }

            if (operation == Operation.EQUALS_OPER)
            {
                newValue = currentValue;
            }
            else
            {
                int operationValue = 1;
                if (value != null)
                {
                    try
                    {
                        operationValue = fmt.parse(value).intValue();
                    }
                    catch (NumberFormatException nfe)
                    {
                        // swallow
                    }
                    catch (ParseException pe)
                    {
                        // swallow
                    }
                }

                if (operation == Operation.INCREMENT_OPER)
                {
                    newValue = currentValue + operationValue;
                }
                else if (operation == Operation.DECREMENT_OPER)
                {
                    newValue = currentValue - operationValue;
                }
            }

            this.newValue = fmt.format(newValue);
        }

        /**
         * Handle operations for type <code>string</code>.
         *
         * @param oldValue the current value read from the configuration file or <code>null</code>
         *                 if the <code>key</code> was not contained in the configuration file.
         */
        private void executeString(String oldValue) throws Exception
        {
            String newValue = DEFAULT_STRING_VALUE;

            String currentValue = getCurrentValue(oldValue);

            if (currentValue == null)
            {
                currentValue = DEFAULT_STRING_VALUE;
            }

            if (operation == Operation.EQUALS_OPER)
            {
                newValue = currentValue;
            }
            else if (operation == Operation.INCREMENT_OPER)
            {
                newValue = currentValue + value;
            }
            this.newValue = newValue;
        }

        /**
         * Check if parameter combinations can be supported
         *
         * @todo make sure the 'unit' attribute is only specified on date fields
         */
        private void checkParameters() throws Exception
        {
            if (type == Type.STRING_TYPE && operation == Operation.DECREMENT_OPER)
            {
                throw new Exception(
                        "- is not supported for string " + "properties (key: " + key + ")");
            }
            if (value == null && defaultValue == null)
            {
                throw new Exception(
                        "\"value\" and/or \"default\" " + "attribute must be specified (key: " + key
                                + ")");
            }
            if (key == null)
            {
                throw new Exception("key is mandatory");
            }
            if (type == Type.STRING_TYPE && pattern != null)
            {
                throw new Exception(
                        "pattern is not supported for string " + "properties (key: " + key + ")");
            }
        }

        private String getCurrentValue(String oldValue)
        {
            String ret = null;
            if (operation == Operation.EQUALS_OPER)
            {
                // If only value is specified, the value is set to it
                // regardless of its previous value.
                if (value != null && defaultValue == null)
                {
                    ret = value;
                }

                // If only default is specified and the value previously
                // existed in the configuration file, it is unchanged.
                if (value == null && defaultValue != null && oldValue != null)
                {
                    ret = oldValue;
                }

                // If only default is specified and the value did not
                // exist in the configuration file, the value is set to default.
                if (value == null && defaultValue != null && oldValue == null)
                {
                    ret = defaultValue;
                }

                // If value and default are both specified and the value
                // previously existed in the configuration file, the value
                // is set to value.
                if (value != null && defaultValue != null && oldValue != null)
                {
                    ret = value;
                }

                // If value and default are both specified and the value
                // did not exist in the configuration file, the value is set
                // to default.
                if (value != null && defaultValue != null && oldValue == null)
                {
                    ret = defaultValue;
                }
            }
            else
            {
                ret = (oldValue == null) ? defaultValue : oldValue;
            }

            return ret;
        }

        /**
         * Enumerated attribute with the values "+", "-", "="
         */
        public static class Operation extends EnumeratedAttribute
        {

            // Property type operations
            public static final int INCREMENT_OPER = 0;

            public static final int DECREMENT_OPER = 1;

            public static final int EQUALS_OPER = 2;

            public String[] getValues()
            {
                return new String[]{"+", "-", "="};
            }

            public static int toOperation(String oper)
            {
                if ("+".equals(oper))
                {
                    return INCREMENT_OPER;
                }
                else if ("-".equals(oper))
                {
                    return DECREMENT_OPER;
                }
                return EQUALS_OPER;
            }
        }

        /**
         * Enumerated attribute with the values "int", "date" and "string".
         */
        public static class Type extends EnumeratedAttribute
        {

            // Property types
            public static final int INTEGER_TYPE = 0;

            public static final int DATE_TYPE = 1;

            public static final int STRING_TYPE = 2;

            public String[] getValues()
            {
                return new String[]{"int", "date", "string"};
            }

            public static int toType(String type)
            {
                if ("int".equals(type))
                {
                    return INTEGER_TYPE;
                }
                else if ("date".equals(type))
                {
                    return DATE_TYPE;
                }
                return STRING_TYPE;
            }
        }

    }

    public static class Unit extends EnumeratedAttribute
    {

        private static final String MILLISECOND = "millisecond";

        private static final String SECOND = "second";

        private static final String MINUTE = "minute";

        private static final String HOUR = "hour";

        private static final String DAY = "day";

        private static final String WEEK = "week";

        private static final String MONTH = "month";

        private static final String YEAR = "year";

        private static final String[] UNITS = {MILLISECOND, SECOND, MINUTE, HOUR, DAY, WEEK,
                MONTH, YEAR};

        private Hashtable<String, Integer> calendarFields = new Hashtable<String, Integer>();

        public Unit()
        {
            calendarFields.put(MILLISECOND, new Integer(Calendar.MILLISECOND));
            calendarFields.put(SECOND, new Integer(Calendar.SECOND));
            calendarFields.put(MINUTE, new Integer(Calendar.MINUTE));
            calendarFields.put(HOUR, new Integer(Calendar.HOUR_OF_DAY));
            calendarFields.put(DAY, new Integer(Calendar.DATE));
            calendarFields.put(WEEK, new Integer(Calendar.WEEK_OF_YEAR));
            calendarFields.put(MONTH, new Integer(Calendar.MONTH));
            calendarFields.put(YEAR, new Integer(Calendar.YEAR));
        }

        public int getCalendarField()
        {
            String key = getValue().toLowerCase();
            Integer i = (Integer) calendarFields.get(key);
            return i.intValue();
        }

        public String[] getValues()
        {
            return UNITS;
        }
    }

}
