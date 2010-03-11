package org.codehaus.izpack.test.panel;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.installer.base.InstallerFrame;
import com.izforge.izpack.installer.data.GUIInstallData;
import org.codehaus.izpack.test.container.TestContainer;
import org.codehaus.izpack.test.customrunner.Container;
import org.codehaus.izpack.test.customrunner.PicoRunner;
import org.fest.swing.fixture.FrameFixture;
import org.hamcrest.text.StringContains;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Manual test for finish panel
 */
@RunWith(PicoRunner.class)
@Container(TestContainer.class)
public class TestFinishPanel
{

    private GUIInstallData guiInstallData;
    private FrameFixture frameFixture;
    private ResourceManager resourceManager;
    private InstallerFrame installerFrame;

    public TestFinishPanel(GUIInstallData guiInstallData, InstallerFrame installerFrame, ResourceManager resourceManager, FrameFixture frameFixture)
    {
        this.guiInstallData = guiInstallData;
        this.installerFrame = installerFrame;
        this.resourceManager = resourceManager;
        this.frameFixture = frameFixture;
    }

    @After
    public void after()
    {
        frameFixture.cleanUp();
    }


    @Test
    public void htmlInfoPanelShouldDisplayText() throws Exception
    {
        resourceManager.setResourceBasePath("/org/codehaus/izpack/test/panel/");
        addPanelAndShow("com.izforge.izpack.panels.htmlinfo.HTMLInfoPanel");
        String textArea = frameFixture.textBox(GuiId.HTML_INFO_PANEL_TEXT.id).text();
        assertThat(textArea, StringContains.containsString("This is a test"));
    }

    @Test
    public void simpleFinishPanelShouldDisplayFinishingText() throws Exception
    {
        addPanelAndShow("com.izforge.izpack.panels.simplefinish.SimpleFinishPanel");
        String text = frameFixture.label(GuiId.SIMPLE_FINISH_LABEL.id).text();
        assertThat(text, StringContains.containsString("Installation has completed"));
    }

    @Test
    public void finishPanelShouldDisplay() throws Exception
    {
        addPanelAndShow("com.izforge.izpack.panels.finish.FinishPanel");
        String text = frameFixture.label(GuiId.FINISH_PANEL_LABEL.id).text();
        assertThat(text, StringContains.containsString("Installation has completed"));
    }

    private void addPanelAndShow(String className)
            throws ClassNotFoundException
    {
        Panel checked = new Panel();
        checked.setClassName(className);
        guiInstallData.getPanelsOrder().add(checked);
        installerFrame.loadPanels();
        installerFrame.enableFrame();
    }
}
