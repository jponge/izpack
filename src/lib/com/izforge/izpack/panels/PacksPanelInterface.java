package com.izforge.izpack.panels;

import com.izforge.izpack.LocaleDatabase;

/**
 * This interface is used to be able to access the common information
 * in the PackPanel and the ImgPacksPAnel through a common type.
 * I introduced it so that I can remove the duplicate PacksModel from each
 * class and create a common one for both.
 *
 * This could be avoided by inheriting ImgPacksPanel from PacksPanel
 *
 * User: Gaganis Giorgos
 * Date: Sep 17, 2004
 * Time: 8:29:22 AM
 */

/*
 * @todo evaluate whether we want to eliminate this interface with
 * inheritance
 */
public interface PacksPanelInterface {

    public LocaleDatabase getLangpack();

    public int getBytes();

    public void setBytes(int bytes);

    public void showSpaceRequired();
}
