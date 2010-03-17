package com.izforge.izpack.merge.resolve;

import com.izforge.izpack.container.TestMergeContainer;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.junit.PicoRunner;
import org.hamcrest.core.Is;
import org.junit.Test;
import org.junit.runner.RunWith;

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
    public void testSearchFullClassNameInClassPath() throws Exception
    {
        Class aClass = classPathCrawler.searchFullClassNameInClassPath(ClassPathCrawlerTest.class.getName());
        assertThat(aClass, Is.is(ClassPathCrawler.class));
    }

    @Test
    public void testSearchPackageInClassPath() throws Exception
    {
    }
}
