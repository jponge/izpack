package com.izforge.izpack.merge.resolve;

import com.izforge.izpack.matcher.MergeMatcher;
import com.izforge.izpack.merge.Mergeable;
import org.hamcrest.collection.IsCollectionContaining;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsNot;
import org.hamcrest.number.IsGreaterThan;
import org.hamcrest.text.StringContains;
import org.junit.Test;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test of path resolver
 *
 * @author Anthonin Bonnefoy
 */
public class PathResolverTest {

    @Test
    public void testResolvePathOfJar() {
        List<URL> urlList = PathResolver.resolvePath("com/izforge");
        assertThat(urlList.size(), new IsGreaterThan<Integer>(1));
        assertThat(getListPathFromListURL(urlList), IsNot.not(IsCollectionContaining.hasItem(
                StringContains.containsString("test"))
        ));
    }

    @Test
    public void testResolvePathOfFileAndJar() throws Exception {
        List<URL> urlList = PathResolver.resolvePath("META-INF/MANIFEST.MF");
        assertThat(getListPathFromListURL(urlList), IsCollectionContaining.hasItems(
                StringContains.containsString("jar!"),
                IsNot.not(StringContains.containsString("jar!"))
        ));
    }

    @Test
    public void testResolvePathOfDirectory() throws Exception {
        List<URL> urlList = PathResolver.resolvePath("com/izforge/izpack/merge/");
        assertThat(urlList.size(), Is.is(1));
        assertThat(getListPathFromListURL(urlList), IsCollectionContaining.hasItems(
                IsNot.not(StringContains.containsString("jar!")),
                StringContains.containsString("target/classes")
        ));
    }

    @Test
    public void testGetMergeableFromDirectory() throws Exception {
        List<Mergeable> mergeables = PathResolver.getMergeableFromPath("com/izforge/izpack/merge/");
        assertThat(mergeables.size(), Is.is(1));
        Mergeable mergeable = mergeables.get(0);
        assertThat(mergeable, MergeMatcher.isMergeableMatching(
                IsCollectionContaining.hasItems(
                        Is.is("com/izforge/izpack/merge/resolve/PathResolver.class")
                )));
    }


    private List<String> getListPathFromListURL(List<URL> urlList) {
        ArrayList<String> arrayList = new ArrayList<String>();
        for (URL url : urlList) {
            arrayList.add(url.getPath());
        }
        return arrayList;
    }
}
