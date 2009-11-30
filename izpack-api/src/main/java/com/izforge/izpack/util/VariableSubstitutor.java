package com.izforge.izpack.util;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: sora
 * Date: Nov 30, 2009
 * Time: 8:09:57 PM
 * To change this template use File | Settings | File Templates.
 */
public interface VariableSubstitutor extends Serializable {
    boolean areBracesRequired();

    void setBracesRequired(boolean braces);

    String substitute(String str, String type) throws IllegalArgumentException;

    int substitute(InputStream in, OutputStream out, String type, String encoding)
            throws IllegalArgumentException, UnsupportedEncodingException, IOException;

    String substitute(InputStream in, String type
            )
                    throws IllegalArgumentException, UnsupportedEncodingException,
                    IOException;

    int substitute(Reader reader, Writer writer, String type)
                            throws IllegalArgumentException, IOException;
}
