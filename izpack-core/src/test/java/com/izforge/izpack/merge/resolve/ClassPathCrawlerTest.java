package com.izforge.izpack.merge.resolve;

import com.izforge.izpack.core.container.TestMergeContainer;
import com.izforge.izpack.merge.panel.PanelMerge;
import com.izforge.izpack.test.ClassUtils;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.junit.PicoRunner;
import org.hamcrest.beans.HasPropertyWithValue;
import org.hamcrest.core.Is;
import org.hamcrest.text.StringContains;
import org.hamcrest.text.StringEndsWith;
import org.junit.Test;
import org.junit.internal.matchers.IsCollectionContaining;
import org.junit.runner.RunWith;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Set;

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
    public void searchClassInFile() throws Exception
    {
        Class aClass = classPathCrawler.searchClassInClassPath(PanelMerge.class.getSimpleName());
        assertThat(aClass.getName(), Is.is(PanelMerge.class.getName()));
    }

    @Test
    public void searchClassInJar() throws Exception
    {
        File jarResource = new File(ClassLoader.getSystemResource("com/izforge/izpack/merge/test/vim-panel-1.0-SNAPSHOT.jar").getFile());
        ClassUtils.loadJarInSystemClassLoader(jarResource);
        Class aClass = classPathCrawler.searchClassInClassPath("VimPanel");
        assertThat(aClass.getName(), Is.is("com.sora.panel.VimPanel"));
        ClassUtils.unloadLastJar();
    }

    @Test
    public void searchPackageInJarWithSpaces() throws Exception
    {
        File jarResource = new File(ClassLoader.getSystemResource("com/izforge/izpack/merge/test/test space/vim-panel-1.0-SNAPSHOT.jar").getFile());
        ClassUtils.loadJarInSystemClassLoader(jarResource);
        Class aClass = classPathCrawler.searchClassInClassPath("VimPanel");
        assertThat(aClass.getName(), Is.is("com.sora.panel.VimPanel"));
        ClassUtils.unloadLastJar();
    }

    @Test
    public void searchPackageInJar() throws Exception
    {
        URL jarUrl = loadVimPanel();
        Set<URL> urlList = classPathCrawler.searchPackageInClassPath("com.sora.panel");
        assertThat(urlList, IsCollectionContaining.hasItems(
                new File(jarUrl.getFile() + "!com/sora/panel").toURI().toURL()
        ));
        assertThat(urlList.iterator().next().getPath(), StringEndsWith.endsWith("com/sora/panel"));
        ClassUtils.unloadLastJar();
    }

    @Test
    public void searchPackageWithResourcePathInJar() throws Exception
    {
        URL jarUrl = loadVimPanel();
        Set<URL> urlList = classPathCrawler.searchPackageInClassPath("com/sora/panel/");
        assertThat(urlList, IsCollectionContaining.hasItems(
                new File(jarUrl.getFile() + "!com/sora/panel").toURI().toURL()
        ));
        assertThat(urlList.iterator().next().getPath(), StringEndsWith.endsWith("com/sora/panel"));
        ClassUtils.unloadLastJar();
    }

    private URL loadVimPanel() throws Exception
    {
        URL jarUrl = ClassLoader.getSystemResource("com/izforge/izpack/merge/test/vim-panel-1.0-SNAPSHOT.jar");
        File jarResource = new File(jarUrl.getFile());
        ClassUtils.loadJarInSystemClassLoader(jarResource);
        return jarUrl;
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
