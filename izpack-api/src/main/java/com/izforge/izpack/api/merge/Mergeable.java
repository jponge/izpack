package com.izforge.izpack.api.merge;

import org.apache.tools.zip.ZipOutputStream;

import java.io.File;
import java.io.FileFilter;

/**
 * Interface to merge in a given output stream
 *
 * @author Anthonin Bonnefoy
 */
public interface Mergeable
{

    void merge(ZipOutputStream outputStream);

    File find(FileFilter fileFilter);

    void merge(java.util.zip.ZipOutputStream outputStream);
}
