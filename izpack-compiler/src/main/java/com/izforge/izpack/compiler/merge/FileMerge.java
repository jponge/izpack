package com.izforge.izpack.compiler.merge;

import com.izforge.izpack.compiler.packager.PackagerHelper;
import org.apache.tools.zip.ZipOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * File merge. Can be a single file or a directory.
 *
 * @author Anthonin Bonnefoy
 */
public class FileMerge implements Mergeable {

    private File fileToCopy;

    public FileMerge(File fileToCopy) {
        this.fileToCopy = fileToCopy;
    }

    public void merge(ZipOutputStream outputStream) {
        try {
            copyFileToJar(fileToCopy, outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyFileToJar(File fileToCopy, ZipOutputStream outputStream) throws IOException {
        if (fileToCopy.isDirectory()) {
            for (File file : fileToCopy.listFiles()) {
                copyFileToJar(file, outputStream);
            }
        } else {
            FileInputStream inputStream = new FileInputStream(fileToCopy);
            PackagerHelper.copyStreamToJar(inputStream, outputStream, fileToCopy.getName(), fileToCopy.lastModified());
        }
    }
}
