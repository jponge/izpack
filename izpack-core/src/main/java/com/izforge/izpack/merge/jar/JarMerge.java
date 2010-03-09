package com.izforge.izpack.merge.jar;

import com.izforge.izpack.api.exception.MergeException;
import com.izforge.izpack.merge.Mergeable;
import com.izforge.izpack.merge.resolve.PathResolver;
import com.izforge.izpack.util.IoHelper;
import org.apache.tools.zip.ZipOutputStream;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
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


    public JarMerge(URL resource) {
        jarPath = PathResolver.processUrlToJarPath(resource);
        destination = resource.getFile().replaceAll(jarPath, "").replaceAll("file:", "").replaceAll("!/", "");
        regexp = new StringBuilder().append(destination).append("(.*)").toString();
    }

    public JarMerge(URL resource, String destination) {
        jarPath = PathResolver.processUrlToJarPath(resource);
        String insideJar = PathResolver.processUrlToJarPackage(resource);
        this.destination = destination;
        regexp = new StringBuilder().append(insideJar).append("(.*)").toString();
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

    public void merge(java.util.zip.ZipOutputStream outputStream) {
        Pattern pattern = Pattern.compile(regexp);
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
            throw new RuntimeException(e);
        }
    }

    public void merge(ZipOutputStream outJar) {
        Pattern pattern = Pattern.compile(regexp);
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
                    IoHelper.copyStreamToJar(jarInputStream, outJar, dest, zentry.getTime());
                }

            }
            jarInputStream.close();
        } catch (IOException e) {
            throw new MergeException(e);
        }
    }
}