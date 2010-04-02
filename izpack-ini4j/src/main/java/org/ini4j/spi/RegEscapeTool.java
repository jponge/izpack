/*
 * Copyright 2005,2009 Ivan SZKIBA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ini4j.spi;

import org.ini4j.Registry;

import org.ini4j.Registry.Type;

import java.io.UnsupportedEncodingException;

public class RegEscapeTool extends EscapeTool
{

    private static final RegEscapeTool INSTANCE = ServiceFinder.findService(RegEscapeTool.class);

    private static final String HEX_CHARSET_NAME = "UTF-16LE";

    private static final int LOWER_DIGIT = 0x0f;

    private static final int UPPER_DIGIT = 0xf0;

    private static final int DIGIT_SIZE = 4;

    public static final RegEscapeTool getInstance()
    {
        return INSTANCE;
    }

    public TypeValuesPair decode(String raw)
    {
        Type type = type(raw);
        String value = (type == Type.REG_SZ) ? unquote(raw) : raw.substring(type.toString()
                .length() + 1);
        String[] values;

        switch (type)
        {

        case REG_EXPAND_SZ:
        case REG_MULTI_SZ:
            byte[] bytes = binary(value);

            try
            {
                value = new String(bytes, 0, bytes.length - 2, HEX_CHARSET_NAME);
            }
            catch (UnsupportedEncodingException e)
            {
                throw new RuntimeException(e);
            }
            break;

        case REG_DWORD:
            value = String.valueOf(Long.parseLong(value, HEX_RADIX));
            break;

        case REG_SZ:
            break;

        default:
            break;
        }

        if (type == Type.REG_MULTI_SZ)
        {
            values = splitMulti(value);
        }
        else
        {
            values = new String[] { value};
        }

        return new TypeValuesPair(type, values);
    }

    public String encode(TypeValuesPair data)
    {
        String ret = null;

        if (data.getType() == Type.REG_SZ)
        {
            ret = quote(data.getValues()[0]);
        }
        else if (data.getValues()[0] != null)
        {
            ret = encode(data.getType(), data.getValues());
        }

        return ret;
    }

    byte[] binary(String value)
    {
        byte[] bytes = new byte[value.length()];
        int idx = 0;
        int shift = DIGIT_SIZE;

        for (int i = 0; i < value.length(); i++)
        {
            char c = value.charAt(i);

            if (Character.isWhitespace(c))
            {
                continue;
            }

            if (c == ',')
            {
                idx++;
                shift = DIGIT_SIZE;
            }
            else
            {
                int digit = Character.digit(c, HEX_RADIX);

                if (digit >= 0)
                {
                    bytes[idx] |= digit << shift;
                    shift = 0;
                }
            }
        }

        return copyOfRange(bytes, 0, idx + 1);
    }

    String encode(Type type, String[] values)
    {
        StringBuilder buff = new StringBuilder();

        buff.append(type.toString());
        buff.append(Type.SEPARATOR_CHAR);
        switch (type)
        {

        case REG_EXPAND_SZ:
            buff.append(hexadecimal(values[0]));
            break;

        case REG_DWORD:
            buff.append(String.format("%08x", Long.parseLong(values[0])));
            break;

        case REG_MULTI_SZ:
            int n = values.length;

            for (int i = 0; i < n; i++)
            {
                buff.append(hexadecimal(values[i]));
                buff.append(',');
            }

            buff.append("00,00");
            break;

        default:
            buff.append(values[0]);
            break;
        }

        return buff.toString();
    }

    String hexadecimal(String value)
    {
        StringBuilder buff = new StringBuilder();

        if ((value != null) && (value.length() != 0))
        {
            byte[] bytes;
            try
            {
                bytes = value.getBytes(HEX_CHARSET_NAME);
                for (int i = 0; i < bytes.length; i++)
                {
                    buff.append(Character.forDigit((bytes[i] & UPPER_DIGIT) >> DIGIT_SIZE,
                            HEX_RADIX));
                    buff.append(Character.forDigit(bytes[i] & LOWER_DIGIT, HEX_RADIX));
                    buff.append(',');
                }

                buff.append("00,00");
            }
            catch (UnsupportedEncodingException e)
            {
                throw new RuntimeException(e);
            }
        }

        return buff.toString();
    }

    Registry.Type type(String raw)
    {
        Registry.Type type;

        if (raw.charAt(0) == DOUBLE_QUOTE)
        {
            type = Registry.Type.REG_SZ;
        }
        else
        {
            int idx = raw.indexOf(Registry.TYPE_SEPARATOR);

            type = (idx < 0) ? Registry.Type.REG_SZ : Registry.Type.fromString(raw
                    .substring(0, idx));
        }

        return type;
    }

    private String[] splitMulti(String value)
    {
        int len = value.length();
        int start;
        int end;
        int n = 0;

        start = 0;
        for (end = value.indexOf(0, start); end >= 0; end = value.indexOf(0, start))
        {
            n++;
            start = end + 1;
            if (start >= len)
            {
                break;
            }
        }

        String[] values = new String[n];

        start = 0;
        for (int i = 0; i < n; i++)
        {
            end = value.indexOf(0, start);
            values[i] = value.substring(start, end);
            start = end + 1;
        }

        return values;
    }

    //
    // Java 1.5 convenience methods
    //

    /**
     * From Array class:<br>
     * Copies the specified range of the specified array into a new array. The initial index of the
     * range (<tt>from</tt>) must lie between zero and <tt>original.length</tt>, inclusive. The
     * value at <tt>original[from]</tt> is placed into the initial element of the copy (unless
     * <tt>from == original.length</tt> or <tt>from == to</tt>). Values from subsequent elements in
     * the original array are placed into subsequent elements in the copy. The final index of the
     * range (<tt>to</tt>), which must be greater than or equal to <tt>from</tt>, may be greater
     * than <tt>original.length</tt>, in which case <tt>(byte)0</tt> is placed in all elements of
     * the copy whose index is greater than or equal to <tt>original.length - from</tt>. The length
     * of the returned array will be <tt>to - from</tt>.
     *
     * @param original the array from which a range is to be copied
     * @param from the initial index of the range to be copied, inclusive
     * @param to the final index of the range to be copied, exclusive. (This index may lie outside
     * the array.)
     * @return a new array containing the specified range from the original array, truncated or
     * padded with zeros to obtain the required length
     * @throws ArrayIndexOutOfBoundsException if <tt>from &lt; 0</tt> or
     * <tt>from &gt; original.length()</tt>
     * @throws IllegalArgumentException if <tt>from &gt; to</tt>
     * @throws NullPointerException if <tt>original</tt> is null
     * @since 1.6
     */
    private static byte[] copyOfRange(byte[] original, int from, int to)
    {
        int newLength = to - from;
        if (newLength < 0) throw new IllegalArgumentException(from + " > " + to);
        byte[] copy = new byte[newLength];
        System.arraycopy(original, from, copy, 0, Math.min(original.length - from, newLength));
        return copy;
    }

}
