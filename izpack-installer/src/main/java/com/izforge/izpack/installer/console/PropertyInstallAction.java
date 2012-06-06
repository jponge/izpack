package com.izforge.izpack.installer.console;

import java.util.Properties;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.installer.data.UninstallDataWriter;


/**
 * Performs installation from properties.
 *
 * @author Tim Anderson
 */
class PropertyInstallAction extends AbstractInstallAction
{
    /**
     * The properties to use for installation.
     */
    private final Properties properties;

    /**
     * Constructs a <tt>PropertyInstallAction</tt>.
     *
     * @param installData the installation data
     * @param writer      the uninstallation data writer
     * @param properties  the installation properties
     */
    public PropertyInstallAction(InstallData installData, UninstallDataWriter writer, Properties properties)
    {
        super(installData, writer);
        this.properties = properties;
    }

    /**
     * Runs the action for the panel.
     *
     * @param panel the panel
     * @return {@code true} if the action was successful, otherwise {@code false}
     */
    @Override
    public boolean run(ConsolePanelView panel)
    {
        return panel.getView().runConsoleFromProperties(getInstallData(), properties);
    }

}
