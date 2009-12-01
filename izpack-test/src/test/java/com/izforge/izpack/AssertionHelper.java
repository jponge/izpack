package com.izforge.izpack;

import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.collection.IsCollectionContaining;
import org.hamcrest.text.StringContains;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Test helper containing assertions.
 */
public class AssertionHelper {


    public static void assertZipContainsMatch(File inFile, String string) throws IOException {
        assertZipContainsMatch(inFile, StringContains.containsString(string));

    }

    public static void assertZipContainsMatch(File inFile, Matcher<String> stringMatcher) throws IOException {
        List<String> fileList = new ArrayList<String>();
        FileInputStream fis = new FileInputStream(inFile);
        ZipInputStream zis = new ZipInputStream(fis);
        ZipEntry ze;
        while ((ze = zis.getNextEntry()) != null) {
            fileList.add(ze.getName());
            zis.closeEntry();
        }
        zis.close();
        MatcherAssert.assertThat(fileList, IsCollectionContaining.hasItem(
                stringMatcher
        ));
    }

    public static void unzipJar(File jar, File outputDirectory) throws IOException {
        FileInputStream fis = new FileInputStream(jar);
        ZipInputStream zin = new ZipInputStream(fis);
        ZipEntry ze;
        while ((ze = zin.getNextEntry()) != null) {
            File file = new File(outputDirectory, ze.getName());
            if (!ze.isDirectory()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
                unzip(zin, file);
            }
        }
        zin.close();
    }

    public static void unzip(ZipInputStream zin, File dest)
            throws IOException {
        FileOutputStream out = new FileOutputStream(dest);
        byte[] b = new byte[512];
        int len = 0;
        while ((len = zin.read(b)) != -1) {
            out.write(b, 0, len);
        }
        out.close();
    }

}
