package com.izforge.izpack.compiler.merge;

import com.izforge.izpack.compiler.helper.IoHelper;
import org.apache.tools.zip.ZipOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;

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

    public JarMerge(String jarPath, String regexp) {
        this.jarPath = jarPath;
        this.regexp = regexp;
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