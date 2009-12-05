package com.izforge.izpack.installer.base;

/**
 * Enum with identifier of frames
 */
public enum GuiId {

    BUTTON_NEXT("nextButton"), BUTTON_HELP("HelpButton"), BUTTON_PREV("prevButton"), BUTTON_QUIT("quitButton"),
    BUTTON_LANG_OK("okButtonLang"), COMBO_BOX_LANG_FLAG("comboBox-lang-flag"), DIALOG_PICKER("dialogPicker"), LICENCE_NO_RADIO("LicenceNoRadio"), LICENCE_YES_RADIO("LicenceYesRadio"), FINISH_PANEL_AUTO_BUTTON("finishPanel_autoButton"), FINISH_PANEL_FILE_CHOOSER("finishPanelFileChooser");

    public String id;

    GuiId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
