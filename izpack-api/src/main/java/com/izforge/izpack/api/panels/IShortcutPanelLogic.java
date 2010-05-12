package com.izforge.izpack.api.panels;

/**
 * Created by IntelliJ IDEA. User: sora Date: Dec 7, 2009 Time: 7:48:08 AM To change this template
 * use File | Settings | File Templates.
 */
public interface IShortcutPanelLogic
{

    /**
     * Creates the shortcuts at a specified time. Before this function can be called a ShortcutPanel
     * must be used to initialize the logic properly.
     * 
     * @throws Exception
     */
    public void createAndRegisterShortcuts() throws Exception;

    /**
     * Tell the ShortcutPaneld to not create the shortcuts immediately after clicking next.
     * 
     * @param createShortcutsImmediately
     */
    public void setCreateShortcutsImmediately(boolean createShortcutsImmediately);

    /**
     * @return <code>true</code> it the shortcuts will be created after clicking next, otherwise
     * <code>false</code>
     */
    public boolean isCreateShortcutsImmediately();
}
