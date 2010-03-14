package com.izforge.izpack.installer.language;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.container.BindeableContainer;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.installer.container.TestLanguageContainer;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.junit.PicoRunner;
import org.fest.swing.fixture.DialogFixture;
import org.hamcrest.core.Is;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for an installation using mock data
 */
@RunWith(PicoRunner.class)
@Container(TestLanguageContainer.class)
public class LanguageDialogTest
{
    private BindeableContainer installerContainer;
    private DialogFixture dialogFixture;
    private ResourceManager resourceManager;

    public LanguageDialogTest(BindeableContainer installerContainer, DialogFixture dialogFixture, ResourceManager resourceManager)
    {
        this.installerContainer = installerContainer;
        this.dialogFixture = dialogFixture;
        this.resourceManager = resourceManager;
    }

    @After
    public void tearBinding()
    {
        installerContainer.dispose();
        if (dialogFixture != null)
        {
            dialogFixture.cleanUp();
            dialogFixture = null;
        }
    }

    @Test
    public void testLangPickerChoseFra() throws Exception
    {
        dialogFixture.show();
        assertThat(dialogFixture.comboBox(GuiId.COMBO_BOX_LANG_FLAG.id).contents(), Is.is(new String[]{"eng", "fra"}));
        dialogFixture.comboBox(GuiId.COMBO_BOX_LANG_FLAG.id).selectItem(1);
        dialogFixture.button(GuiId.BUTTON_LANG_OK.id).click();
        Mockito.verify(resourceManager).setLocale("fra");
    }


}