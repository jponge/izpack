package com.izforge.izpack.merge.resolve;

import com.izforge.izpack.core.container.TestMergeContainer;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.junit.PicoRunner;
import org.hamcrest.beans.HasPropertyWithValue;
import org.hamcrest.core.Is;
import org.hamcrest.text.StringContains;
import org.junit.Test;
import org.junit.internal.matchers.IsCollectionContaining;
import org.junit.runner.RunWith;
import org.picocontainer.PicoContainer;

import java.net.URL;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for classpath crawler
 *
 * @author Anthonin Bonnefoy
 */
@RunWith(PicoRunner.class)
@Container(TestMergeContainer.class)
public class ClassPathCrawlerTest
{
    private ClassPathCrawler classPathCrawler;

    public ClassPathCrawlerTest(ClassPathCrawler classPathCrawler)
    {
        this.classPathCrawler = classPathCrawler;
    }

    @Test
    public void testSearchClassInFile() throws Exception
    {
        Class aClass = classPathCrawler.searchClassInClassPath(ClassPathCrawlerTest.class.getSimpleName());
        assertThat(aClass.getName(), Is.is(ClassPathCrawlerTest.class.getName()));
    }

    @Test
    public void searchClassInJar() throws Exception
    {
        Class aClass = classPathCrawler.searchClassInClassPath(PicoContainer.class.getSimpleName());
        assertThat(aClass.getName(), Is.is(PicoContainer.class.getName()));
    }

    @Test
    public void searchPackageInClassPathForFile() throws Exception
    {
        Collection<URL> urls = classPathCrawler.searchPackageInClassPath("resolve");
        assertThat(urls,
                IsCollectionContaining.hasItem(
                        HasPropertyWithValue.<URL>hasProperty("path", StringContains.containsString("com/izforge/izpack/merge/resolve")))
        );
    }

    @Test
    public void searchPackageInClassPathForJar() throws Exception
    {
        Collection<URL> urls = classPathCrawler.searchPackageInClassPath("hamcrest");
        assertThat(urls,
                IsCollectionContaining.hasItem(
                        HasPropertyWithValue.<URL>hasProperty("path", StringContains.containsString("org/hamcrest")))
        );
    }
}
