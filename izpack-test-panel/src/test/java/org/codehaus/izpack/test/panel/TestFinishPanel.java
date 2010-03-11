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
import org.mockito.Mockito;

import java.util.zip.ZipOutputStream;

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
    public void helloThenFinishPanelShouldDisplay() throws Exception
    {
        guiInstallData.setUninstallOutJar(Mockito.mock(ZipOutputStream.class));
        addPanelAndShow("com.izforge.izpack.panels.hello.HelloPanel",
                "com.izforge.izpack.panels.simplefinish.SimpleFinishPanel");
        String welcomLabel = frameFixture.label(GuiId.HELLO_PANEL_LABEL.id).text();
        assertThat(welcomLabel, StringContains.containsString("Welcome to the installation of"));
        frameFixture.button(GuiId.BUTTON_NEXT.id).click();
        String uninstallLabel = frameFixture.label(GuiId.SIMPLE_FINISH_UNINSTALL_LABEL.id).text();
        assertThat(uninstallLabel, StringContains.containsString("An uninstaller program has been created in"));
    }

    @Test
    public void finishPanelShouldDisplay() throws Exception
    {
        addPanelAndShow("com.izforge.izpack.panels.finish.FinishPanel");
        String text = frameFixture.label(GuiId.FINISH_PANEL_LABEL.id).text();
        assertThat(text, StringContains.containsString("Installation has completed"));
        // Is automatic installation xml button visible?
        frameFixture.button(GuiId.FINISH_PANEL_AUTO_BUTTON.id).requireVisible();
    }

    private void addPanelAndShow(String... classNames)
            throws ClassNotFoundException
    {
        for (String className : classNames)
        {
            Panel panel = new Panel();
            panel.setClassName(className);
            guiInstallData.getPanelsOrder().add(panel);
        }
        installerFrame.loadPanels();
        installerFrame.enableFrame();
    }
}
