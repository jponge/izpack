package com.izforge.izpack.merge;

import com.izforge.izpack.util.IoHelper;
import org.apache.tools.zip.ZipOutputStream;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Jar files merger.
 *
 * @author Anthonin Bonnefoy
 */
public class JarMerge implements Mergeable {
    private String jarPath;

    private String regexp = ".*";

    public JarMerge(String path) {
        jarPath = MergeManager.getJarAbsolutePath(path);
        regexp = new StringBuilder().append(path).append(".*").toString();
    }

    public JarMerge(URL resource) {
        jarPath = MergeManager.processUrlToJarPath(resource);
        regexp = new StringBuilder().append(resource.getPath().replaceAll(jarPath, "")).append(".*").toString();
    }

    public JarMerge(File classFile) {
        String[] strings = classFile.getAbsolutePath().split(".jar!/");
        jarPath = strings[0] + ".jar";
        regexp = new StringBuilder().append(strings[1]).append(".*").toString();
    }

    public File find(FileFilter fileFilter) {
        try {
            ArrayList<String> fileNameInZip = getFileNameInZip();
            for (String fileName : fileNameInZip) {
                File file = new File(jarPath + "!/" + fileName);
                if (fileFilter.accept(file)) {
                    return file;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public ArrayList<String> getFileNameInZip() throws IOException {
        ZipInputStream inputStream = new ZipInputStream(new FileInputStream(jarPath));
        ArrayList<String> arrayList = new ArrayList<String>();
        ZipEntry zipEntry;
        while ((zipEntry = inputStream.getNextEntry()) != null) {
            arrayList.add(zipEntry.getName());
        }
        return arrayList;
    }

    public void merge(ZipOutputStream outputStream) {
        ZipEntry zentry;
        try {
            JarInputStream jarInputStream = new JarInputStream(new FileInputStream(new File(jarPath)));
            while ((zentry = jarInputStream.getNextEntry()) != null) {
                if (zentry.isDirectory()) {
                    continue;
                }
                String entryName = zentry.getName();
                if (entryName.matches(regexp)) {
                    IoHelper.copyStreamToJar(jarInputStream, outputStream, entryName, zentry.getTime());
                }
            }
            jarInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}