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

    private String destination;

    public FileMerge(File fileToCopy) {
        this(fileToCopy, "");
    }

    public FileMerge(File fileToCopy, String destination) {
        this.fileToCopy = fileToCopy;
        this.destination = destination;
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
            String entryName = resolveName(fileToCopy, this.fileToCopy, this.destination);
            FileInputStream inputStream = new FileInputStream(fileToCopy);
            PackagerHelper.copyStreamToJar(inputStream, outputStream, entryName, fileToCopy.lastModified());
        }
    }

    private String resolveName(File fileToCopy, File basePath, String destination) {
        String path;
        path = basePath.getPath();
        if (!basePath.isDirectory()) {
            path = basePath.getParent();
        }
        path = path + "/";
        StringBuilder builder = new StringBuilder();
        builder.append(destination);
        builder.append(fileToCopy.getAbsolutePath().replaceAll(path, ""));
        return builder.toString();
    }
}
