package com.izforge.izpack.panels.info;

import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.installer.console.AbstractTextPanelConsole;

/**
 * Console implementation of {@link InfoPanel}.
 *
 * @author Tim Anderson
 */
public class InfoPanelConsole extends AbstractTextPanelConsole
{

    /**
     * The resources.
     */
    private final Resources resources;

    /**
     * Constructs an <tt>InfoPanelConsole</tt>.
     *
     * @param resources the resources
     */
    public InfoPanelConsole(Resources resources)
    {
        this.resources = resources;
    }

    /**
     * Returns the text to display.
     *
     * @return the text
     */
    @Override
    protected String getText()
    {
        String defaultValue = "Error : could not load the info text !";
        return resources.getString("InfoPanel.info", defaultValue);
    }
}
