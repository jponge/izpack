package com.izforge.izpack.merge.jar;

import com.izforge.izpack.matcher.DuplicateMatcher;
import com.izforge.izpack.matcher.MergeMatcher;
import com.izforge.izpack.matcher.ZipMatcher;
import com.izforge.izpack.merge.Mergeable;
import com.izforge.izpack.merge.resolve.PathResolver;
import org.apache.tools.zip.ZipOutputStream;
import org.hamcrest.core.Is;
import org.hamcrest.text.StringContains;
import org.junit.Before;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for merge jar
 *
 * @author Anthonin Bonnefoy
 */
public class JarMergeTest
{
    private PathResolver pathResolver;

    @Before
    public void setUp()
    {
        this.pathResolver = new PathResolver();
    }


    @Test
    public void testAddJarDuplicated() throws Exception
    {
        URL resource = ClassLoader.getSystemResource("com/izforge/izpack/merge/test/jar-hellopanel-1.0-SNAPSHOT.jar");
        Mergeable jarMerge = pathResolver.getMergeableFromURL(resource);
        File tempFile = File.createTempFile("test", ".zip");
        ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(tempFile));
//        jarMerge.merge(outputStream);
        jarMerge.merge(outputStream);
        outputStream.close();
        assertThat(tempFile, ZipMatcher.isZipMatching(
                DuplicateMatcher.isEntryUnique("jar/izforge/izpack/panels/hello/HelloPanelConsoleHelper.class")
        ));
    }

    @Test
    public void testAddJarContent() throws Exception
    {
        URL resource = ClassLoader.getSystemResource("com/izforge/izpack/merge/test/jar-hellopanel-1.0-SNAPSHOT.jar");
        Mergeable jarMerge = pathResolver.getMergeableFromURL(resource);
        assertThat(jarMerge, MergeMatcher.isMergeableContainingFiles("jar/izforge/izpack/panels/hello/HelloPanel.class")
        );
    }

    @Test
    public void testMergeClassFromJarFile() throws Exception
    {
        List<Mergeable> jarMergeList = pathResolver.getMergeableFromPath("org/fest/assertions/Assert.class");

        assertThat(jarMergeList.size(), Is.is(1));

        Mergeable jarMerge = jarMergeList.get(0);
        assertThat(jarMerge, MergeMatcher.isMergeableContainingFiles("org/fest/assertions/Assert.class")
        );
    }


    @Test
    public void testMergeJarFoundDynamicallyLoaded() throws Exception
    {
        URL urlJar = ClassLoader.getSystemResource("com/izforge/izpack/merge/test/jar-hellopanel-1.0-SNAPSHOT.jar");
        URLClassLoader loader = URLClassLoader.newInstance(new URL[]{urlJar}, ClassLoader.getSystemClassLoader());

        Mergeable jarMerge = pathResolver.getMergeableFromURLWithDestination(loader.getResource("jar/izforge"), "com/dest");

        assertThat(jarMerge, MergeMatcher.isMergeableContainingFiles("com/dest/izpack/panels/hello/HelloPanel.class"));
    }


    @Test
    public void testFindPanelInJar() throws Exception
    {
        URL resource = ClassLoader.getSystemResource("com/izforge/izpack/merge/test/izpack-panel-5.0.0-SNAPSHOT.jar");
        Mergeable jarMerge = pathResolver.getMergeableFromURL(resource);
        File file = jarMerge.find(new FileFilter()
        {
            public boolean accept(File pathname)
            {
                return pathname.isDirectory() ||
                        pathname.getName().replaceAll(".class", "").equalsIgnoreCase("CheckedHelloPanel");
            }
        });
        assertThat(file.getAbsolutePath(), StringContains.containsString("com/izforge/izpack/panels/checkedhello/CheckedHelloPanel.class"));
    }


    @Test
    public void testFindFileInJarFoundWithURL() throws Exception
    {
        URL urlJar = ClassLoader.getSystemResource("com/izforge/izpack/merge/test/jar-hellopanel-1.0-SNAPSHOT.jar");
        URLClassLoader loader = URLClassLoader.newInstance(new URL[]{urlJar}, ClassLoader.getSystemClassLoader());

        Mergeable jarMerge = pathResolver.getMergeableFromURL(loader.getResource("jar/izforge"));
        File file = jarMerge.find(new FileFilter()
        {
            public boolean accept(File pathname)
            {
                return pathname.getName().matches(".*HelloPanel\\.class") || pathname.isDirectory();
            }
        });
        assertThat(file.getName(), Is.is("HelloPanel.class"));
    }
}
