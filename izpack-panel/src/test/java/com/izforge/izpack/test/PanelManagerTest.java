package com.izforge.izpack.test;

import com.izforge.izpack.installer.manager.PanelManager;
import com.izforge.izpack.test.container.TestPanelManagerContainer;
import com.izforge.izpack.test.junit.PicoRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

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
    public void testVoid() throws Exception
    {
    }
}
