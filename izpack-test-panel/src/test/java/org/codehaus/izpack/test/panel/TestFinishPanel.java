package org.codehaus.izpack.test.panel;

import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.installer.base.InstallerFrame;
import com.izforge.izpack.installer.data.GUIInstallData;
import org.codehaus.izpack.TestContainer;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Manual test for finish panel
 */
public class TestFinishPanel
{

    private TestContainer testContainer;

    @Before
    public void setUp() throws Exception
    {
        testContainer = new TestContainer();
        testContainer.initBindings();
    }


    @Ignore
    @Test
    public void launchTest() throws Exception
    {
        InstallerFrame installerFrame = testContainer.getComponent(InstallerFrame.class);
        GUIInstallData guiInstallData = testContainer.getComponent(GUIInstallData.class);
        ResourceManager resourceManager = testContainer.getComponent(ResourceManager.class);

        resourceManager.setResourceBasePath("/org/codehaus/izpack/test/panel/");
//        Panel helloPanel = new Panel();
//        helloPanel.setClassName("com.izforge.izpack.panels.hello.HelloPanel");
//        guiInstallData.getPanelsOrder().add(helloPanel);

        Panel checked = new Panel();
        checked.setClassName("com.izforge.izpack.panels.htmlinfo.HTMLInfoPanel");
        guiInstallData.getPanelsOrder().add(checked);

        Panel finishPanel = new Panel();
        finishPanel.setClassName("com.izforge.izpack.panels.simplefinish.SimpleFinishPanel");
        guiInstallData.getPanelsOrder().add(finishPanel);

        installerFrame.loadPanels();
        installerFrame.enableFrame();

//        SimpleFinishPanel panel = testContainer.getComponent(SimpleFinishPanel.class);
//        panel.setVisible(true);

        Thread.sleep(10000);
    }
}
