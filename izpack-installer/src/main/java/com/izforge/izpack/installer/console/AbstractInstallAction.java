package com.izforge.izpack.installer.console;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.installer.data.UninstallDataWriter;

/**
 * A {@link ConsoleAction} for performing installations.
 * <p/>
 * This writes uninstallation information if required, at the end of a successful installation.
 *
 * @author Tim Anderson
 */
public abstract class AbstractInstallAction extends ConsoleAction
{
    /**
     * The uninstallation data writer.
     */
    private final UninstallDataWriter writer;


    /**
     * Constructs an <tt>AbstractConsoleInstallAction</tt>.
     *
     * @param installData the installation data
     * @param writer      the uninstallation data writer
     */
    public AbstractInstallAction(AutomatedInstallData installData, UninstallDataWriter writer)
    {
        super(installData);
        this.writer = writer;
    }

    /**
     * Invoked after the action has been successfully run for each panel.
     * <p/>
     * This writes uninstallation information, if required.
     *
     * @return {@code true} if the operation succeeds; {@code false} if it fails
     */
    @Override
    public boolean complete()
    {
        boolean result = true;
        if (writer.isUninstallRequired())
        {
            result = writer.write();
        }
        return result;
    }
}
