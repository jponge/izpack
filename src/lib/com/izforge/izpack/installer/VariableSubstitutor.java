/*
 * IzPack Version 3.0.0 (build 2002.08.13)
 * Copyright (C) 2001 Johannes Lehtinen
 *
 * File :               VariableSubstitutor.java
 * Description :        Variable substitutor backend.
 * Author's email :     johannes.lehtinen@iki.fi
 * Author's Website :   http://www.iki.fi/jle/
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.izforge.izpack.installer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Substitutes variables occurring in an input stream or a string. This
 * implementation supports a generic variable value mapping and escapes
 * the possible special characters occurring in the substituted values.
 * The file types specifically supported are plain text files (no
 * escaping), Java properties files, and XML files.
 *
 * A valid variable name matches the regular expression
 * [a-zA-Z][a-zA-Z0-9_]* and names are case sensitive. Variables are
 * referenced either by $NAME or ${NAME} (the latter syntax being useful
 * in situations like ${NAME}NOTPARTOFNAME). If a referenced variable
 * is undefined then it is not substituted but the corresponding part of
 * the stream is copied as is.
 *
 * @author Johannes Lehtinen <johannes.lehtinen@iki.fi>
 * @version $Revision$
 */
public class VariableSubstitutor
{

    /** The variable value mappings */
    protected Map environment;

    /** A constant for file type. Plain file. */
    protected static final int TYPE_PLAIN = 0;

    /** A constant for file type. Java properties file. */
    protected static final int TYPE_JAVA_PROPERTIES = 1;

    /** A constant for file type. XML file. */
    protected static final int TYPE_XML = 2;

    /** A mapping of file type names to corresponding integer constants. */
    protected static Map typeNameToConstantMap;

    // Initialize the file type map
    static
    {
        typeNameToConstantMap = new HashMap();
        typeNameToConstantMap.put("plain", new Integer(TYPE_PLAIN));
        typeNameToConstantMap.put("javaprop",
                                  new Integer(TYPE_JAVA_PROPERTIES));
        typeNameToConstantMap.put("xml", new Integer(TYPE_XML));
    }

    /**
     * Constructs a new substitutor using the specified variable
     * value mappings. The environment hashtable is copied by reference.
     *
     * @param environment the environment with variable value mappings
     */
    public VariableSubstitutor(Map environment)
    {
        this.environment = environment;
    }

    /**
     * Substitutes the variables found in the specified string. Escapes
     * special characters using file type specific escaping if necessary.
     *
     * @param str the string to check for variables
     * @param type the escaping type or null for plain
     * @return the string with substituted variables
     * @exception IllegalArgumentException if unknown escaping type specified
     */
    public String substitute(String str, String type)
        throws IllegalArgumentException
    {
        // Create reader and writer for the strings
        StringReader reader = new StringReader(str);
        StringWriter writer = new StringWriter();

        // Substitute any variables
        try
        {
            substitute(reader, writer, type);
        }
        catch (IOException e)
        {
            throw new Error
                ("Unexpected I/O exception when reading/writing memory "
                 + "buffer; nested exception is: " + e);
        }

        // Return the resulting string
        return writer.getBuffer().toString();
    }

    /**
     * Substitutes the variables found in the specified input stream.
     * Escapes special characters using file type specific escaping if
     * necessary.
     *
     * @param in the input stream to read
     * @param out the output stream to write
     * @param type the file type or null for plain
     * @param encoding the character encoding or null for default
     * @exception IllegalArgumentException if unknown file type specified
     * @exception UnsupportedEncodingException if encoding not supported
     * @exception IOException if an I/O error occurs
     */
    public void substitute(InputStream in, OutputStream out,
                           String type, String encoding)
        throws IllegalArgumentException, UnsupportedEncodingException,
            IOException
    {
        // Check if file type specific default encoding known
        if (encoding == null)
        {
            int t = getTypeConstant(type);
            switch(t)
            {
                case TYPE_JAVA_PROPERTIES:
                    encoding = "ISO-8859-1";
                    break;

                case TYPE_XML:
                    encoding = "UTF-8";
                    break;
            }
        }

        // Create the reader and writer
        InputStreamReader reader =
            (encoding != null ?
             new InputStreamReader(in, encoding) : new InputStreamReader(in));
        OutputStreamWriter writer =
            (encoding != null ?
             new OutputStreamWriter(out, encoding) :
             new OutputStreamWriter(out));

        // Copy the data and substitute variables
        substitute(reader, writer, type);

        // Flush the writer so that everything gets written out
        writer.flush();
    }

    /**
     * Substitutes the variables found in the data read from the specified
     * reader. Escapes special characters using file type specific escaping
     * if necessary.
     *
     * @param reader the reader to read
     * @param writer the writer used to write data out
     * @param type the file type or null for plain
     * @exception IllegalArgumentException if unknown file type specified
     * @exception IOException if an I/O error occurs
     */
    public void substitute(Reader reader, Writer writer, String type)
        throws IllegalArgumentException, IOException
    {
        // Check the file type
        int t = getTypeConstant(type);

        // Copy data and substitute variables
        int c = reader.read();
        while (true)
        {
            // Find the next potential variable reference or EOF
            while (c != -1 && c != '$')
            {
                writer.write(c);
                c = reader.read();
            }
            if (c == -1)
                return;

            // Check if braces used
            boolean braces = false;
            c = reader.read();
            if (c == '{')
            {
                braces = true;
                c = reader.read();
            }
            else if (c == -1)
            {
                writer.write('$');
                return;
            }

            // Read the variable name
            StringBuffer nameBuffer = new StringBuffer();
            while ((c >= 'a' && c <= 'z') ||
                   (c >= 'A' && c <= 'Z') ||
                   (((c >= '0' && c <= '9') || c == '_') &&
                    nameBuffer.length() > 0))
            {
                nameBuffer.append((char) c);
                c = reader.read();
            }
            String name = nameBuffer.toString();

            // Check if a legal and defined variable found
            boolean found = ((!braces || c == '}') &&
                             name.length() > 0 &&
                             environment.containsKey(name));

            // Substitute the variable...
            if (found)
            {
                writer.write
                    (escapeSpecialChars((String) environment.get(name), t));
                if (braces)
                    c = reader.read();
            }
            // ...or ignore it
            else
            {
                writer.write('$');
                if (braces)
                    writer.write('{');
                writer.write(name);
            }
        }
    }

    /**
     * Returns the internal constant for the specified file type.
     *
     * @param type the type name or null for plain
     * @return the file type constant
     * @exception IllegalArgumentException if file type is unknown
     */
    protected int getTypeConstant(String type) {
        if (type == null)
            return TYPE_PLAIN;
        Integer integer = (Integer) typeNameToConstantMap.get(type);
        if (integer == null)
            throw new IllegalArgumentException
                ("Unknown file type " + type);
        else
            return integer.intValue();
    }

    /**
     * Escapes the special characters in the specified string using file
     * type specific rules.
     *
     * @param str the string to check for special characters
     * @param type the target file type (one of TYPE_xxx)
     * @return the string with the special characters properly escaped
     */
    protected String escapeSpecialChars(String str, int type)
    {
        StringBuffer buffer;
        int len;
        int i;
        switch (type) {
            case TYPE_PLAIN:
                return str;
 
           case TYPE_JAVA_PROPERTIES:
                buffer = new StringBuffer(str);
                len = str.length();
                for (i = 0; i < len; i++)
                {
                    // Check for control characters
                    char c = buffer.charAt(i);
                    if (c == '\t' || c == '\n' || c == '\r')
                    {
                        char tag;
                        if (c == '\t')
                            tag = 't';
                        else if (c == '\n')
                            tag = 'n';
                        else
                            tag = 'r';
                        buffer.replace(i, i + 1, "\\" + tag);
                        len++;
                        i++;
                    }

                    // Check for special characters
                    if (c == '\\' || c == '"' || c == '\'' || c == ' ')
                    {
                        buffer.insert(i, '\\');
                        len++;
                        i++;
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
                    if (r != null) {
                        buffer.replace(i, i+1, r);
                        len = buffer.length();
                        i += r.length() - 1;
                    }
                }
                return buffer.toString();

            default:
                throw new Error
                    ("Unknown file type constant " + type);
        }
    }
}
