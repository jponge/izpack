package com.izforge.izpack.matcher;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.collection.IsCollectionContaining;
import org.hamcrest.core.Is;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Matcher for zip files
 *
 * @author Anthonin Bonnefoy
 */
public class ZipMatcher extends TypeSafeMatcher<File> {
    private Matcher<Iterable<String>> listMatcher;

    ZipMatcher(Matcher<Iterable<String>> listMatcher) {
        this.listMatcher = listMatcher;
    }

    @Override
    public boolean matchesSafely(File file) {
        try {
            List<String> fileList = new ArrayList<String>();
            FileInputStream fis = new FileInputStream(file);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                fileList.add(ze.getName());
                zis.closeEntry();
            }
            zis.close();
            MatcherAssert.assertThat(fileList, listMatcher);
            return true;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public void describeTo(Description description) {
        description.appendText("Excepting collection containing ").appendValue(listMatcher);
    }

    public static ZipMatcher isZipContainingFile(String fileName) {
        return new ZipMatcher(IsCollectionContaining.hasItem(Is.is(fileName)));
    }

    public static ZipMatcher isZipMatching(Matcher<Iterable<String>> matcher) {
        return new ZipMatcher(matcher);
    }


}
