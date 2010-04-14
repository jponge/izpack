package com.izforge.izpack.installer.base;

/**
 * Interface for Installer view control
 *
 * @author Anthonin Bonnefoy
 */
public interface InstallerView
{
    void lockPrevButton();

    void lockNextButton();

    void unlockPrevButton();

    void unlockNextButton();

    void unlockNextButton(boolean requestFocus);

    void navigateNext();

    void navigatePrevious();

    void showHelp();

    void sizeFrame();
}
