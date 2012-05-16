package com.izforge.izpack.installer.language;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.fest.swing.fixture.DialogFixture;
import org.hamcrest.core.Is;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.container.Container;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.installer.container.TestLanguageContainer;
import com.izforge.izpack.test.junit.PicoRunner;

/**
 * Test for an installation using mock data
 */
@RunWith(PicoRunner.class)
@com.izforge.izpack.test.Container(TestLanguageContainer.class)
public class LanguageDialogTest
{
    private Container installerContainer;
    private DialogFixture dialogFixture;
    private Locales locales;

    public LanguageDialogTest(Container installerContainer, DialogFixture dialogFixture, Locales locales)
    {
        this.installerContainer = installerContainer;
        this.dialogFixture = dialogFixture;
        this.locales = locales;
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
        assertNotNull(locales.getLocale());
        assertEquals("fra", locales.getLocale().getISO3Language());
    }


}