package com.izforge.izpack.merge.file;

import com.izforge.izpack.api.exception.MergeException;
import com.izforge.izpack.merge.Mergeable;
import com.izforge.izpack.util.IoHelper;
import org.apache.tools.zip.ZipOutputStream;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

/**
 * File merge. Can be a single file or a directory.
 *
 * @author Anthonin Bonnefoy
 */
public class FileMerge implements Mergeable {

    private File fileToCopy;

    private URL url;
    private String destination;

    public FileMerge(URL url) {
        this(url, "");
    }

    public FileMerge(URL url, String destination) {
        this.url = url;
        this.fileToCopy = new File(url.getFile());
        this.destination = destination;
    }

    public File find(FileFilter fileFilter) {
        return findRecursivelyForFile(fileFilter, fileToCopy);
    }

    /**
     * Recursively search a file matching the fileFilter
     *
     * @param fileFilter  Filter accepting directory and file matching a classname pattern
     * @param currentFile Current directory
     * @return the first found file or null
     */
    private File findRecursivelyForFile(FileFilter fileFilter, File currentFile) {
        if (currentFile.isDirectory()) {
            for (File files : currentFile.listFiles(fileFilter)) {
                File file = findRecursivelyForFile(fileFilter, files);
                if (file != null) {
                    return file;
                }
            }
        } else {
            return currentFile;
        }
        return null;
    }

    public void merge(ZipOutputStream outputStream) {
        try {
            copyFileToJar(fileToCopy, outputStream);
        } catch (IOException e) {
            throw new MergeException(e);
        }
    }

    public void merge(java.util.zip.ZipOutputStream outputStream) {
        try {
            copyFileToJar(fileToCopy, outputStream);
        } catch (IOException e) {
            throw new MergeException(e);
        }
    }

    private void copyFileToJar(File fileToCopy, java.util.zip.ZipOutputStream outputStream) throws IOException {
        if (fileToCopy.isDirectory()) {
            for (File file : fileToCopy.listFiles()) {
                copyFileToJar(file, outputStream);
            }
        } else {
            String entryName = resolveName(fileToCopy, this.destination);
            FileInputStream inputStream = new FileInputStream(fileToCopy);
            IoHelper.copyStreamToJar(inputStream, outputStream, entryName, fileToCopy.lastModified());
        }
    }

    private void copyFileToJar(File fileToCopy, ZipOutputStream outputStream) throws IOException {
        if (fileToCopy.isDirectory()) {
            for (File file : fileToCopy.listFiles()) {
                copyFileToJar(file, outputStream);
            }
        } else {
            String entryName = resolveName(fileToCopy, this.destination);
            FileInputStream inputStream = new FileInputStream(fileToCopy);
            IoHelper.copyStreamToJar(inputStream, outputStream, entryName, fileToCopy.lastModified());
        }
    }

    private String resolveName(File fileToCopy, String destination) {
        if (isFile(destination)) {
            return destination;
        }
        String path = this.fileToCopy.getAbsolutePath();
        if (destination.equals("")) {
            path = this.fileToCopy.getParentFile().getAbsolutePath();
        }
        path = path + '/';
        StringBuilder builder = new StringBuilder();
        builder.append(destination);
        builder.append(fileToCopy.getAbsolutePath().replaceAll(path, ""));
        return builder.toString().replaceAll("//", "/");
    }

    private boolean isFile(String destination) {
        if (destination.isEmpty()) {
            return false;
        }
        return !destination.endsWith("/");
    }
}
