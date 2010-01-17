package com.izforge.izpack.compiler.merge;

import com.izforge.izpack.compiler.packager.PackagerHelper;
import org.apache.tools.zip.ZipOutputStream;

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

    private String path;
    private String jarPath;

    public JarMerge(String path) {
        this.path = path;
        jarPath = MergeManager.getJarAbsolutePath(path);
//        ClassLoader.getSystemClassLoader().getResource(sourcePath);
    }

    public void merge(ZipOutputStream outputStream) {
        ZipEntry zentry;
        try {
            while ((zentry = jarInputStream.getNextEntry()) != null) {
                String entryName = zentry.getName();
                System.out.println(entryName);
                PackagerHelper.copyStreamToJar(jarInputStream, outputStream, entryName, zentry.getTime());
            }
            jarInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}