package com.izforge.izpack.test;

import com.izforge.izpack.api.exception.MergeException;
import com.izforge.izpack.installer.base.IzPanel;
import com.izforge.izpack.installer.manager.PanelManager;
import com.izforge.izpack.panels.checkedhello.CheckedHelloPanel;
import com.izforge.izpack.test.container.TestPanelManagerContainer;
import com.izforge.izpack.test.junit.PicoRunner;
import org.hamcrest.core.Is;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for panel manager
 */

@RunWith(PicoRunner.class)
@Container(TestPanelManagerContainer.class)
public class PanelManagerTest
{
    private PanelManager panelManager;

    public PanelManagerTest(PanelManager panelManager)
    {
        this.panelManager = panelManager;
    }

    @Test
    public void resolveClassNameShouldAddDefaultPrefix() throws Exception
    {
        Class<?> aClass = panelManager.resolveClassName("HelloPanel");
        assertThat(aClass.getName(), Is.is("com.izforge.izpack.panels.hello.HelloPanel"));
        aClass = panelManager.resolveClassName("FinishPanel");
        assertThat(aClass.getName(), Is.is("com.izforge.izpack.panels.finish.FinishPanel"));
    }

    @Test
    public void resolveClassNameShouldNotAddPrefixWithCompleteClass() throws Exception
    {
        Class<?> aClass = panelManager.resolveClassName("com.izforge.izpack.panels.hello.HelloPanel");
        assertThat(aClass.getName(), Is.is("com.izforge.izpack.panels.hello.HelloPanel"));
        aClass = panelManager.resolveClassName("com.izforge.izpack.api.container.BindeableContainer");
        assertThat(aClass.getName(), Is.is("com.izforge.izpack.api.container.BindeableContainer"));
    }

    @Test
    public void shouldSearchAutomaticallyInPackage() throws Exception
    {
        Class<? extends IzPanel> aClass = panelManager.resolveClassName("CheckedHelloPanel");
        assertThat(aClass.getName(), Is.is(CheckedHelloPanel.class.getName()));
    }

    @Test(expected = MergeException.class)
    public void resolveClassNameShouldThrowException() throws Exception
    {
        panelManager.resolveClassName("unknown");
    }
}
