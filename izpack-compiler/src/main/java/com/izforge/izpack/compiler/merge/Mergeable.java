package com.izforge.izpack.compiler.merge;

import org.apache.tools.zip.ZipOutputStream;

/**
 * Interface to merge in a given output stream
 *
 * @author Anthonin Bonnefoy
 */
public interface Mergeable {

    void merge(ZipOutputStream outputStream);
}
