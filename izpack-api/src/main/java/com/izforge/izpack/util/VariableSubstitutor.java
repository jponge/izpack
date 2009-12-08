package com.izforge.izpack.util;

import java.io.*;

/**
 * Interface of the variable substitutor
 */
public interface VariableSubstitutor extends Serializable {
    boolean areBracesRequired();

    void setBracesRequired(boolean braces);

    String substitute(String str, String type) throws IllegalArgumentException;

    int substitute(InputStream in, OutputStream out, String type, String encoding)
            throws IllegalArgumentException, IOException;

    String substitute(InputStream in, String type) throws IllegalArgumentException, IOException;

    int substitute(Reader reader, Writer writer, String type) throws IllegalArgumentException, IOException;
}
