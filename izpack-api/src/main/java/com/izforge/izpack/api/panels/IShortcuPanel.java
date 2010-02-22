package com.izforge.izpack.api.panels;

/**
 * Created by IntelliJ IDEA.
 * User: sora
 * Date: Dec 7, 2009
 * Time: 7:48:08 AM
 * To change this template use File | Settings | File Templates.
 */
public interface IShortcuPanel
{
    void createAndRegisterShortcuts();

    void setCreateImmediately(boolean createImmediately);
}
