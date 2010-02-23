package com.izforge.izpack.merge.panel;

import com.izforge.izpack.matcher.MergeMatcher;
import com.izforge.izpack.merge.resolve.PathResolver;
import org.hamcrest.core.Is;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit tests for panel merge
 *
 * @author Anthonin Bonnefoy
 */
public class PanelMergeTest
{
    private PanelMerge panelMerge;

    private PathResolver pathResolver;

    @BeforeMethod
    public void setUp()
    {
        pathResolver = new PathResolver();
    }

    @Test
    public void testResolvePanelNameFromFile() throws Exception
    {
        panelMerge = pathResolver.getPanelMerge("HelloPanel");
        assertThat(panelMerge, MergeMatcher.isMergeableContainingFiles("com/izforge/izpack/panels/hello/HelloPanel.class"));
    }

    @Test
    public void testResolvePanelWithCompleteNameFromFile() throws Exception
    {
        panelMerge = pathResolver.getPanelMerge("com.izforge.izpack.panels.hello.HelloPanel");
        assertThat(panelMerge, MergeMatcher.isMergeableContainingFiles("com/izforge/izpack/panels/hello/HelloPanel.class"));
    }

    @Test
    public void testGetClassNameFromPanelMergeWithFullClassGiven() throws Exception
    {
        panelMerge = pathResolver.getPanelMerge("com.izforge.izpack.panels.hello.HelloPanel");
        assertThat(panelMerge.getFullClassNameFromPanelName(), Is.is("com.izforge.izpack.panels.hello.HelloPanel"));
    }

    @Test
    public void testGetClassNameFromPanelMergeWithOnlyPanelName() throws Exception
    {
        panelMerge = pathResolver.getPanelMerge("HelloPanel");
        assertThat(panelMerge.getFullClassNameFromPanelName(), Is.is("com.izforge.izpack.panels.hello.HelloPanel"));
    }
}
