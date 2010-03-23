package com.izforge.izpack.merge.resolve;

import com.izforge.izpack.api.merge.Mergeable;
import com.izforge.izpack.core.container.TestMergeContainer;
import com.izforge.izpack.matcher.MergeMatcher;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.junit.PicoRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for mergeableResolver
 *
 * @author Anthonin Bonnefoy
 */
@RunWith(PicoRunner.class)
@Container(TestMergeContainer.class)
public class MergeableResolverTest
{
    private MergeableResolver mergeableResolver;
    private URL resource;

    public MergeableResolverTest(MergeableResolver mergeableResolver)
    {
        this.mergeableResolver = mergeableResolver;
    }

    @Before
    public void before() throws MalformedURLException
    {
        resource = ClassLoader.getSystemResource("com/izforge/izpack/merge/test/jar-hellopanel-1.0-SNAPSHOT.jar");
        resource = new File(resource.getFile() + "!jar/izforge").toURI().toURL();
    }


    @Test
    public void testGetMergeableFromURL() throws Exception
    {
        Mergeable mergeable = mergeableResolver.getMergeableFromURL(resource);
        assertThat(mergeable, MergeMatcher.isMergeableContainingFile("jar/izforge/izpack/panels/hello/HelloPanel.class"));
    }


    @Test
    public void testGetMergeableFromURLWithDestination() throws Exception
    {
        Mergeable jarMerge = mergeableResolver.getMergeableFromURLWithDestination(resource, "ga");
        assertThat(jarMerge, MergeMatcher.isMergeableContainingFiles("ga/izpack/panels/hello/HelloPanel.class")
        );
    }


}
