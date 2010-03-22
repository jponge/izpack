package com.izforge.izpack.merge.panel;

import com.izforge.izpack.api.merge.Mergeable;
import com.izforge.izpack.core.container.TestMergeContainer;
import com.izforge.izpack.matcher.DuplicateMatcher;
import com.izforge.izpack.matcher.MergeMatcher;
import com.izforge.izpack.matcher.ZipMatcher;
import com.izforge.izpack.merge.resolve.MergeableResolver;
import com.izforge.izpack.merge.resolve.PathResolver;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.MergeUtils;
import com.izforge.izpack.test.junit.PicoRunner;
import org.hamcrest.core.Is;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit tests for panel merge
 *
 * @author Anthonin Bonnefoy
 */
@RunWith(PicoRunner.class)
@Container(TestMergeContainer.class)
public class PanelMergeTest
{
    private PanelMerge panelMerge;

    private PathResolver pathResolver;
    private MergeableResolver mergeableResolver;

    public PanelMergeTest(PathResolver pathResolver, MergeableResolver mergeableResolver)
    {
        this.pathResolver = pathResolver;
        this.mergeableResolver = mergeableResolver;
    }

    @Test
    public void testResolvePanelNameFromFile() throws Exception
    {
        panelMerge = pathResolver.getPanelMerge("HelloPanelTestClass");
        assertThat(panelMerge, MergeMatcher.isMergeableContainingFiles("com/izforge/izpack/panels/hello/HelloPanelTestClass.class"));
    }

    @Test
    public void testResolvePanelWithCompleteNameFromFile() throws Exception
    {
        panelMerge = pathResolver.getPanelMerge("com.izforge.izpack.panels.hello.HelloPanelTestClass");
        assertThat(panelMerge, MergeMatcher.isMergeableContainingFiles("com/izforge/izpack/panels/hello/HelloPanelTestClass.class"));
    }

    @Test
    public void testGetClassNameFromPanelMergeWithFullClassGiven() throws Exception
    {
        panelMerge = pathResolver.getPanelMerge("com.izforge.izpack.panels.hello.HelloPanelTestClass");
        assertThat(panelMerge.getPanelClass().getName(), Is.is("com.izforge.izpack.panels.hello.HelloPanelTestClass"));
    }

    @Test
    public void testGetClassNameFromPanelMergeWithOnlyPanelName() throws Exception
    {
        panelMerge = pathResolver.getPanelMerge("HelloPanelTestClass");
        assertThat(panelMerge.getPanelClass().getName(), Is.is("com.izforge.izpack.panels.hello.HelloPanelTestClass"));
    }

    @Test
    public void testMergeDuplicatePanel() throws Exception
    {
        Mergeable mergeable = pathResolver.getPanelMerge("com.izforge.izpack.panels.hello.HelloPanelTestClass");
        File tempFile = MergeUtils.doDoubleMerge(mergeable);
        assertThat(tempFile, ZipMatcher.isZipMatching(
                DuplicateMatcher.isEntryUnique("com/izforge/izpack/panels/hello/HelloPanelTestClass.class")
        ));
    }
}
