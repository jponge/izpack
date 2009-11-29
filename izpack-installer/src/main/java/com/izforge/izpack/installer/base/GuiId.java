package com.izforge.izpack.installer.base;

/**
 * Created by IntelliJ IDEA.
 * User: sora
 * Date: Nov 29, 2009
 * Time: 10:34:59 AM
 * To change this template use File | Settings | File Templates.
 */
public enum GuiId {

    NEXT_BUTTON("nextButton"), HELP_BUTTON("HelpButton"), PREV_BUTTON("prevButton"), QUIT_BUTTON("quitButton");

    public String id;

    GuiId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
