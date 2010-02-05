package com.izforge.izpack.merge;

import com.izforge.izpack.util.IoHelper;
import org.apache.tools.zip.ZipOutputStream;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.jar.JarInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    private String destination;
    private Pattern pattern;


    public JarMerge(String path) {
        jarPath = MergeManagerImpl.getJarAbsolutePath(path);
        destination = path;
        regexp = new StringBuilder().append(path).append("(.*)").toString();
        pattern = Pattern.compile(regexp);
    }

    public JarMerge(String path, String destination) {
        jarPath = MergeManagerImpl.getJarAbsolutePath(path);
        this.destination = destination;
        regexp = new StringBuilder().append(path).append("(.*)").toString();
        pattern = Pattern.compile(regexp);
    }

    public JarMerge(File classFile) {
        String[] strings = classFile.getAbsolutePath().split(".jar!/");
        jarPath = strings[0] + ".jar";
        destination = strings[1];
        regexp = new StringBuilder().append(destination).append("(.*)").toString();
        pattern = Pattern.compile(regexp);
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
                Matcher matcher = pattern.matcher(zentry.getName());
                if (matcher.matches()) {
                    String dest = destination + matcher.group(1);
                    IoHelper.copyStreamToJar(jarInputStream, outputStream, dest, zentry.getTime());
                }

            }
            jarInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}