package com.izforge.izpack.merge.resolve;

import com.izforge.izpack.core.container.TestMergeContainer;
import com.izforge.izpack.merge.panel.PanelMerge;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.junit.PicoRunner;
import org.hamcrest.beans.HasPropertyWithValue;
import org.hamcrest.core.Is;
import org.hamcrest.text.StringContains;
import org.junit.Test;
import org.junit.internal.matchers.IsCollectionContaining;
import org.junit.runner.RunWith;

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
        Class aClass = classPathCrawler.searchClassInClassPath(PanelMerge.class.getSimpleName());
        assertThat(aClass.getName(), Is.is(PanelMerge.class.getName()));
    }

    @Test
    public void testSearchClassInJar() throws Exception
    {

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

}
