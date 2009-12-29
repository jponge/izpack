package com.izforge.izpack.util.substitutor;

import java.io.*;

/**
 * Interface of the variable substitutor
 */
public interface VariableSubstitutor extends Serializable {
    boolean areBracesRequired();

    void setBracesRequired(boolean braces);

    String substitute(String str, SubstitutionType type) throws IllegalArgumentException;

    int substitute(InputStream in, OutputStream out, SubstitutionType type, String encoding)
            throws IllegalArgumentException, IOException;

    String substitute(InputStream in, SubstitutionType type) throws IllegalArgumentException, IOException;

    int substitute(Reader reader, Writer writer, SubstitutionType type) throws IllegalArgumentException, IOException;
}
