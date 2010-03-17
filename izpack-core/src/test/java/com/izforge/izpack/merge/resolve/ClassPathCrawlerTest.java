package com.izforge.izpack.merge.resolve;

import com.izforge.izpack.core.container.TestMergeContainer;
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
    public void testSearchClassInFile() throws Exception
    {
        Class aClass = classPathCrawler.searchClassInClassPath(ClassPathCrawlerTest.class.getSimpleName());
        assertThat(aClass.getName(), Is.is(ClassPathCrawlerTest.class.getName()));
    }

    @Test
    public void searchClassInJar() throws Exception
    {
        Class aClass = classPathCrawler.searchClassInClassPath(Is.class.getSimpleName());
        assertThat(aClass.getName(), Is.is(Is.class.getName()));
    }


}
