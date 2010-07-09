package com.izforge.izpack.test.panel;

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.*;

import org.fest.swing.fixture.*;
import org.hamcrest.text.StringContains;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.data.*;
import com.izforge.izpack.api.data.binding.Help;
import com.izforge.izpack.installer.base.InstallerController;
import com.izforge.izpack.installer.data.*;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.container.TestPanelContainer;
import com.izforge.izpack.test.junit.PicoRunner;

/**
 * Manual test for finish panel
 */
@RunWith(PicoRunner.class)
@Container(TestPanelContainer.class)
public class TestPanelDisplay
{

    private GUIInstallData guiInstallData;
    private FrameFixture frameFixture;
    private ResourceManager resourceManager;
    private UninstallDataWriter uninstallDataWriter;
    private InstallerController installerController;

    public TestPanelDisplay(GUIInstallData guiInstallData, ResourceManager resourceManager, FrameFixture frameFixture, UninstallDataWriter uninstallDataWriter, InstallerController installerController)
    {
        this.guiInstallData = guiInstallData;
        this.resourceManager = resourceManager;
        this.frameFixture = frameFixture;
        this.uninstallDataWriter = uninstallDataWriter;
        this.installerController = installerController;
    }

    @Before
    public void setUp()
    {
        resourceManager.setResourceBasePath("/com/izforge/izpack/test/panel/");
    }


    @After
    public void after()
    {
        frameFixture.cleanUp();
    }


    @Test
    public void htmlInfoPanelShouldDisplayText() throws Exception
    {
        addPanelAndShow("com.izforge.izpack.panels.htmlinfo.HTMLInfoPanel");
        String textArea = frameFixture.textBox(GuiId.HTML_INFO_PANEL_TEXT.id).text();
        assertThat(textArea, StringContains.containsString("This is a test"));
    }

    @Test
    public void licencePanelShouldDisplayText() throws Exception
    {
        addPanelAndShow("com.izforge.izpack.panels.licence.LicencePanel");
        String textArea = frameFixture.textBox(GuiId.LICENCE_TEXT_AREA.id).text();
        assertThat(textArea, StringContains.containsString("This is a licenSe panel"));
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
        Mockito.when(uninstallDataWriter.isUninstallShouldBeWriten()).thenReturn(true);
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
            throws Exception
    {
        ArrayList<Panel> panelList = new ArrayList<Panel>();
        for (String className : classNames)
        {
            Panel panel = new Panel();
            panel.setClassName(className);
            panelList.add(panel);
        }
        addPanelAndShow(panelList);
    }

    private void addPanelAndShow(List<Panel> panelList)
            throws Exception
    {
        for (Panel panel : panelList)
        {
            guiInstallData.getPanelsOrder().add(panel);
        }
        installerController.buildInstallation();
        installerController.launchInstallation();
    }

    @Test
    public void helpShouldDisplay() throws Exception
    {
        Panel panel = new Panel();
        panel.setClassName("com.izforge.izpack.panels.hello.HelloPanel");
        panel.setHelps(Arrays.asList(new Help("eng", "un.html")));
        addPanelAndShow(Collections.singletonList(panel));
        frameFixture.button(GuiId.BUTTON_HELP.id).requireVisible();
        frameFixture.button(GuiId.BUTTON_HELP.id).click();
        DialogFixture dialogFixture = frameFixture.dialog(GuiId.HELP_WINDOWS.id);
        dialogFixture.requireVisible();
        assertThat(dialogFixture.textBox().text(), StringContains.containsString("toto"));
    }
}
