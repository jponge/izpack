package com.izforge.izpack.installer.base;

/**
 * Enum with identifier of frames
 */
public enum GuiId {

    BUTTON_NEXT("nextButton"), BUTTON_HELP("HelpButton"), BUTTON_PREV("prevButton"), BUTTON_QUIT("quitButton"),
    BUTTON_LANG_OK("okButtonLang"), COMBO_BOX_LANG_FLAG("comboBox-lang-flag");

    public String id;

    GuiId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
