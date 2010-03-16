package com.izforge.izpack.merge.jar;

import com.izforge.izpack.api.merge.Mergeable;
import com.izforge.izpack.container.TestMergeContainer;
import com.izforge.izpack.matcher.MergeMatcher;
import com.izforge.izpack.merge.resolve.PathResolver;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.junit.PicoRunner;
import org.hamcrest.core.Is;
import org.hamcrest.text.StringContains;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for merge jar
 *
 * @author Anthonin Bonnefoy
 */
@RunWith(PicoRunner.class)
@Container(TestMergeContainer.class)
public class JarMergeTest
{
    private PathResolver pathResolver;

    public JarMergeTest(PathResolver pathResolver)
    {
        this.pathResolver = pathResolver;
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
