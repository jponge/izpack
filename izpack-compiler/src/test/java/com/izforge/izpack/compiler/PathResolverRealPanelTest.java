package com.izforge.izpack.compiler;

import com.izforge.izpack.core.container.TestMergeContainer;
import com.izforge.izpack.matcher.MergeMatcher;
import com.izforge.izpack.merge.panel.PanelMerge;
import com.izforge.izpack.merge.resolve.PathResolver;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.junit.PicoRunner;
import org.hamcrest.core.IsNot;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Anthonin Bonnefoy
 */

@RunWith(PicoRunner.class)
@Container(TestMergeContainer.class)
public class PathResolverRealPanelTest
{
    private PathResolver pathResolver;

    public PathResolverRealPanelTest(PathResolver pathResolver)
    {
        this.pathResolver = pathResolver;
    }


    @Test
    public void testAddProcessPanel() throws Exception
    {
        PanelMerge panelMerge = pathResolver.getPanelMerge("com.izforge.izpack.panels.process.ProcessPanel");
        assertThat(panelMerge, IsNot.not(MergeMatcher.isMergeableContainingFiles("com/izforge/izpack/panels/process/VariableCondition.class")));
        assertThat(panelMerge, MergeMatcher.isMergeableContainingFiles("com/izforge/izpack/panels/process/ProcessPanel.class"));
    }
}
