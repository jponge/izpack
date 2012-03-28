package com.izforge.izpack.compiler.merge.resolve;

import com.izforge.izpack.compiler.container.TestResolveContainer;
import com.izforge.izpack.compiler.merge.panel.PanelMerge;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.junit.PicoRunner;
import com.izforge.izpack.util.ClassUtils;
import com.izforge.izpack.util.FileUtil;
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
import static org.junit.Assert.assertTrue;

/**
 * Test for classpath crawler
 *
 * @author Anthonin Bonnefoy
 */
@RunWith(PicoRunner.class)
@Container(TestResolveContainer.class)
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
        Class aClass = classPathCrawler.findClass(PanelMerge.class.getSimpleName());
        assertThat(aClass.getName(), Is.is(PanelMerge.class.getName()));
    }

    @Test
    public void searchClassInJar() throws Exception
    {
        File jarResource = FileUtil.convertUrlToFile(
                ClassLoader.getSystemResource("com/izforge/izpack/compiler/merge/resolve/vim-panel-1.0-SNAPSHOT.jar"));
        assertTrue(jarResource.exists());
        ClassUtils.loadJarInSystemClassLoader(jarResource);
        Class aClass = classPathCrawler.findClass("VimPanel");
        assertThat(aClass.getName(), Is.is("com.sora.panel.VimPanel"));
        ClassUtils.unloadLastJar();
    }

    @Test
    public void searchPackageInJarWithSpaces() throws Exception
    {
        File jarResource = FileUtil.convertUrlToFile(ClassLoader.getSystemResource(
                "com/izforge/izpack/compiler/merge/resolve/test space/vim-panel-1.0-SNAPSHOT.jar"));
        assertTrue(jarResource.exists());
        ClassUtils.loadJarInSystemClassLoader(jarResource);
        Class aClass = classPathCrawler.findClass("VimPanel");
        assertThat(aClass.getName(), Is.is("com.sora.panel.VimPanel"));
        ClassUtils.unloadLastJar();
    }

    @Test
    public void searchPackageInJar() throws Exception
    {
        URL jarUrl = loadVimPanel();
        Set<URL> urlList = classPathCrawler.searchPackageInClassPath("com.sora.panel");
        assertThat(urlList, IsCollectionContaining.hasItems(
                new File(FileUtil.convertUrlToFilePath(jarUrl) + "!com/sora/panel").toURI().toURL()
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
                new File(FileUtil.convertUrlToFilePath(jarUrl) + "!com/sora/panel").toURI().toURL()
        ));
        assertThat(urlList.iterator().next().getPath(), StringEndsWith.endsWith("com/sora/panel"));
        ClassUtils.unloadLastJar();
    }

    private URL loadVimPanel() throws Exception
    {
        URL jarUrl = ClassLoader.getSystemResource("samples/silverpeas/vim-panel.jar");
        File jarResource = FileUtil.convertUrlToFile(jarUrl);
        ClassUtils.loadJarInSystemClassLoader(jarResource);
        return jarUrl;
    }

    @Test
    public void searchPackageInClassPathForFile() throws Exception
    {
        Collection<URL> urls = classPathCrawler.searchPackageInClassPath("resolve");
        assertThat(urls,
                   IsCollectionContaining.hasItem(
                           HasPropertyWithValue.<URL>hasProperty("path", StringContains.containsString(
                                   "com/izforge/izpack/merge/resolve")))
        );
    }

}
