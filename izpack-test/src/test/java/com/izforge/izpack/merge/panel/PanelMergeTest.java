package com.izforge.izpack.merge.panel;

import com.izforge.izpack.matcher.MergeMatcher;
import org.hamcrest.core.Is;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit tests for panel merge
 *
 * @author Anthonin Bonnefoy
 */
public class PanelMergeTest {
    private PanelMerge panelMerge;

    @Test
    public void mergeManagerShouldTransformClassNameToPackagePath() throws Exception {
        panelMerge = new PanelMerge("");
        String pathFromClassName = panelMerge.getPackagePathFromClassName("com.test.sora.UneClasse");
        assertThat(pathFromClassName, Is.is("com/test/sora/"));
    }

    @Test
    public void mergeManagerShouldReturnDefaultPackagePath() throws Exception {
        panelMerge = new PanelMerge("");
        String pathFromClassName = panelMerge.getPackagePathFromClassName("UneClasse");
        assertThat(pathFromClassName, Is.is("com/izforge/izpack/panels/"));
    }

    @Test
    public void testResolvePanelNameFromFile() throws Exception {
        panelMerge = new PanelMerge("HelloPanel");
        assertThat(panelMerge, MergeMatcher.isMergeableContainingFiles("com/izforge/izpack/panels/hello/HelloPanel.class"));
    }

    @Test
    public void testResolvePanelWithCompleteNameFromFile() throws Exception {
        panelMerge = new PanelMerge("com.izforge.izpack.panels.hello.HelloPanel");
        assertThat(panelMerge, MergeMatcher.isMergeableContainingFiles("com/izforge/izpack/panels/hello/HelloPanel.class"));
    }

    @Test
    public void testGetClassNameFromPanelMergeWithFullClassGiven() throws Exception {
        panelMerge = new PanelMerge("com.izforge.izpack.panels.hello.HelloPanel");
        assertThat(panelMerge.getFullClassNameFromPanelName(), Is.is("com.izforge.izpack.panels.hello.HelloPanel"));
    }

    @Test
    public void testGetClassNameFromPanelMergeWithOnlyPanelName() throws Exception {
        panelMerge = new PanelMerge("HelloPanel");
        assertThat(panelMerge.getFullClassNameFromPanelName(), Is.is("com.izforge.izpack.panels.hello.HelloPanel"));
    }
}
