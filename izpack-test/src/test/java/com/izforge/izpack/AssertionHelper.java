package com.izforge.izpack;

import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.collection.IsCollectionContaining;
import org.hamcrest.text.StringContains;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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

}
