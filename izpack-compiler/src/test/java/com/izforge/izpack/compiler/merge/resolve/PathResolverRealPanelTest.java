package com.izforge.izpack.compiler.merge.resolve;

import com.izforge.izpack.compiler.container.TestResolveContainer;
import com.izforge.izpack.compiler.merge.panel.PanelMerge;
import com.izforge.izpack.matcher.MergeMatcher;
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
@Container(TestResolveContainer.class)
public class PathResolverRealPanelTest
{
    private CompilerPathResolver pathResolver;

    public PathResolverRealPanelTest(CompilerPathResolver pathResolver)
    {
        this.pathResolver = pathResolver;
    }


    @Test
    public void testAddProcessPanel() throws Exception
    {
        PanelMerge panelMerge = pathResolver.getPanelMerge("com.izforge.izpack.panels.process.ProcessPanel");
        assertThat(panelMerge, IsNot.not(
                MergeMatcher.isMergeableContainingFiles("com/izforge/izpack/panels/process/VariableCondition.class")));
        assertThat(panelMerge,
                   MergeMatcher.isMergeableContainingFiles("com/izforge/izpack/panels/process/ProcessPanel.class"));
    }
}
