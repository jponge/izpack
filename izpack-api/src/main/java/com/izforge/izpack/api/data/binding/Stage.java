package com.izforge.izpack.api.data.binding;

/**
 * Possibles stage of the installation.
 *
 * @author Anthonin Bonnefoy
 */
public enum Stage
{
    install, uninstall, compiler;


    public static boolean isInInstaller(Stage stage)
    {
        return uninstall.equals(stage) || install.equals(stage);
    }

}
