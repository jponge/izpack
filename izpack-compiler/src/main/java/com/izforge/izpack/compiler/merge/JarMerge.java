package com.izforge.izpack.compiler.merge;

import com.izforge.izpack.compiler.packager.PackagerHelper;
import org.apache.tools.zip.ZipOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;

/**
 * Jar files merger
 *
 * @author Anthonin Bonnefoy
 */
public class JarMerge implements Mergeable {
    private JarInputStream jarInputStream;
    private String jarPath;

    private String regexp = ".*";

    public JarMerge(String path) {
        jarPath = MergeManager.getJarAbsolutePath(path);
        regexp = path + ".*";
    }

    public JarMerge(String jarPath, String regexp) {
        this.jarPath = jarPath;
        this.regexp = regexp;
    }

    public void merge(ZipOutputStream outputStream) {
        ZipEntry zentry;
        try {
            jarInputStream = new JarInputStream(new FileInputStream(new File(jarPath)));
            while ((zentry = jarInputStream.getNextEntry()) != null) {
                if (zentry.isDirectory()) {
                    continue;
                }
                String entryName = zentry.getName();
                if (entryName.matches(regexp)) {
                    PackagerHelper.copyStreamToJar(jarInputStream, outputStream, entryName, zentry.getTime());
                }
            }
            jarInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}