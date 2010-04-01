/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2001 Johannes Lehtinen
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

package com.izforge.izpack.util.substitutor;

import java.io.*;
import java.util.Properties;

import com.izforge.izpack.api.substitutor.*;
import com.izforge.izpack.util.IoHelper;

/**
 * Implementation of variable substitutor
 */
public class VariableSubstitutorImpl implements VariableSubstitutor
{

    private static final long serialVersionUID = 3907213762447685687L;

    /**
     * The variable value mappings
     */
    protected transient Properties variables;

    /**
     * Whether braces are required for substitution.
     */
    protected boolean bracesRequired = false;

    /**
     * PLAIN = "plain"
     */
    public final static String PLAIN = "plain";

    /**
     * Constructs a new substitutor using the specified variable value mappings. The environment
     * hashtable is copied by reference. Braces are not required by default
     *
     * @param properties the map with variable value mappings
     */
    public VariableSubstitutorImpl(Properties properties)
    {
        this.variables = properties;
    }

    public void setBracesRequired(boolean braces)
    {
        bracesRequired = braces;
    }

    public String substitute(String str) throws IllegalArgumentException
    {
        return substitute(str, SubstitutionType.TYPE_PLAIN);
    }

    public String substitute(String str, SubstitutionType type) throws IllegalArgumentException
    {
        if (str == null)
        {
            return null;
        }

        // Create reader and write for the strings
        StringReader reader = new StringReader(str);
        StringWriter writer = new StringWriter();

        // Substitute any variables
        try
        {
            substitute(reader, writer, type);
        }
        catch (IOException e)
        {
            throw new Error("Unexpected I/O exception when reading/writing memory "
                    + "buffer; nested exception is: " + e);
        }

        // Return the resulting string
        return writer.getBuffer().toString();
    }

    public int substitute(InputStream in, OutputStream out, SubstitutionType type, String encoding)
            throws IllegalArgumentException, IOException
    {
        // Check if file type specific default encoding known
        if (encoding == null)
        {
            switch (type)
            {
                case TYPE_JAVA_PROPERTIES:
                    encoding = "ISO-8859-1";
                    break;
                case TYPE_XML:
                    encoding = "UTF-8";
                    break;
            }
        }

        // Create the reader and write
        InputStreamReader reader = (encoding != null ? new InputStreamReader(in, encoding)
                : new InputStreamReader(in));
        OutputStreamWriter writer = (encoding != null ? new OutputStreamWriter(out, encoding)
                : new OutputStreamWriter(out));

        // Copy the data and substitute variables
        int subs = substitute(reader, writer, type);

        // Flush the write so that everything gets written out
        writer.flush();

        return subs;
    }

    public String substitute(InputStream in, SubstitutionType type)
            throws IllegalArgumentException, IOException
    {
        // Check if file type specific default encoding known
        String encoding = PLAIN;
        {
            switch (type)
            {
                case TYPE_JAVA_PROPERTIES:
                    encoding = "ISO-8859-1";

                    break;

                case TYPE_XML:
                    encoding = "UTF-8";

                    break;
            }
        }

        // Create the reader and write
        InputStreamReader reader = ((encoding != null)
                ? new InputStreamReader(in, encoding)
                : new InputStreamReader(in));
        StringWriter writer = new StringWriter();

        // Copy the data and substitute variables
        substitute(reader, writer, type);

        // Flush the write so that everything gets written out
        writer.flush();

        return writer.getBuffer().toString();
    }


    public int substitute(Reader reader, Writer writer, SubstitutionType type)
            throws IllegalArgumentException, IOException
    {

        // determine character which starts (and ends) a variable
        char variable_start = '$';
        char variable_end = '\0';
        switch (type)
        {
            case TYPE_SHELL:
                variable_start = '%';
                break;
            case TYPE_AT:
                variable_start = '@';
                break;
            case TYPE_ANT:
                variable_start = '@';
                variable_end = '@';
        }


        int subs = 0;

        // Copy data and substitute variables
        int c = reader.read();

        while (true)
        {
            // Find the next potential variable reference or EOF
            while (c != -1 && c != variable_start)
            {
                writer.write(c);
                c = reader.read();
            }
            if (c == -1)
            {
                return subs;
            }

            // Check if braces used or start char escaped
            boolean braces = false;
            c = reader.read();
            if (c == '{')
            {
                braces = true;
                c = reader.read();
            }
            else if (bracesRequired)
            {
                writer.write(variable_start);
                continue;
            }
            else if (c == -1)
            {
                writer.write(variable_start);
                return subs;
            }

            // Read the variable name
            StringBuffer nameBuffer = new StringBuffer();
            while (c != -1 && (braces && c != '}') || (c >= 'a' && c <= 'z')
                    || (c >= 'A' && c <= 'Z') || (braces && ((c == '[') || (c == ']')))
                    || (((c >= '0' && c <= '9') || c == '_' || c == '.' || c == '-') && nameBuffer.length() > 0))
            {
                nameBuffer.append((char) c);
                c = reader.read();
            }
            String name = nameBuffer.toString();

            // Check if a legal and defined variable found
            String varvalue = null;

            if (((!braces || c == '}') &&
                    (!braces || variable_end == '\0' || variable_end == c)
            ) && name.length() > 0)
            {
                // check for environment variables
                if (braces && name.startsWith("ENV[")
                        && (name.lastIndexOf(']') == name.length() - 1))
                {
                    varvalue = IoHelper.getenv(name.substring(4, name.length() - 1));
                    if (varvalue == null)
                    {
                        varvalue = "";
                    }
                }
                else
                {
                    varvalue = variables.getProperty(name);
                }

                subs++;
            }

            // Substitute the variable...
            if (varvalue != null)
            {
                writer.write(escapeSpecialChars(varvalue, type));
                if (braces || variable_end != '\0')
                {
                    c = reader.read();
                }
            }
            // ...or ignore it
            else
            {
                writer.write(variable_start);
                if (braces)
                {
                    writer.write('{');
                }
                writer.write(name);
            }
        }
    }

    /**
     * Escapes the special characters in the specified string using file type specific rules.
     *
     * @param str  the string to check for special characters
     * @param type the target file type (one of TYPE_xxx)
     * @return the string with the special characters properly escaped
     */
    protected String escapeSpecialChars(String str, SubstitutionType type)
    {
        StringBuffer buffer;
        int len;
        int i;
        switch (type)
        {
            case TYPE_PLAIN:
            case TYPE_AT:
            case TYPE_ANT:
                return str;
            case TYPE_SHELL:
                //apple mac has major problem with \r, make sure they are gone
                return str.replace("\r", "");
            case TYPE_JAVA_PROPERTIES:
            case TYPE_JAVA:
                buffer = new StringBuffer(str);
                len = str.length();
                boolean leading = true;
                for (i = 0; i < len; i++)
                {
                    // Check for control characters
                    char c = buffer.charAt(i);
                    if (type.equals(SubstitutionType.TYPE_JAVA_PROPERTIES))
                    {
                        if (c == '\t' || c == '\n' || c == '\r')
                        {
                            char tag;
                            if (c == '\t')
                            {
                                tag = 't';
                            }
                            else if (c == '\n')
                            {
                                tag = 'n';
                            }
                            else
                            {
                                tag = 'r';
                            }
                            buffer.replace(i, i + 1, "\\" + tag);
                            len++;
                            i++;
                        }

                        // Check for special characters
                        // According to the spec:
                        // 'For the element, leading space characters, but not embedded or trailing space characters,
                        // are written with a preceding \ character'
                        else if (c == ' ')
                        {
                            if (leading)
                            {
                                buffer.insert(i, '\\');
                                len++;
                                i++;
                            }
                        }
                        else if (c == '\\' || c == '"' || c == '\'')
                        {
                            leading = false;
                            buffer.insert(i, '\\');
                            len++;
                            i++;
                        }
                        else
                        {
                            leading = false;
                        }
                    }
                    else
                    {
                        if (c == '\\')
                        {
                            buffer.replace(i, i + 1, "\\\\");
                            len++;
                            i++;
                        }
                    }
                }
                return buffer.toString();
            case TYPE_XML:
                buffer = new StringBuffer(str);
                len = str.length();
                for (i = 0; i < len; i++)
                {
                    String r = null;
                    char c = buffer.charAt(i);
                    switch (c)
                    {
                        case '<':
                            r = "&lt;";
                            break;
                        case '>':
                            r = "&gt;";
                            break;
                        case '&':
                            r = "&amp;";
                            break;
                        case '\'':
                            r = "&apos;";
                            break;
                        case '"':
                            r = "&quot;";
                            break;
                    }
                    if (r != null)
                    {
                        buffer.replace(i, i + 1, r);
                        len = buffer.length();
                        i += r.length() - 1;
                    }
                }
                return buffer.toString();
            default:
                throw new Error("Unknown file type constant " + type);
        }
    }
}
