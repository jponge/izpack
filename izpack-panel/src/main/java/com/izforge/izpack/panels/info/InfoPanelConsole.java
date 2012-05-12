package com.izforge.izpack.panels.info;

import com.izforge.izpack.api.data.ResourceManager;
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
    private final ResourceManager resources;

    /**
     * Constructs an <tt>InfoPanelConsole</tt>.
     *
     * @param resources the resources
     */
    public InfoPanelConsole(ResourceManager resources)
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
        String result;
        try
        {
            result = resources.getString("InfoPanel.info");
        }
        catch (Exception exception)
        {
            result = "Error: could not load the info text!";
        }
        return result;
    }
}
