package com.izforge.izpack.installer.console;

import java.util.List;

import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.installer.panel.AbstractPanels;
import com.izforge.izpack.installer.panel.Panels;


/**
 * Implementation of {@link Panels} for {@link ConsolePanelView}.
 *
 * @author Tim Anderson
 */
public class ConsolePanels extends AbstractPanels<ConsolePanelView, PanelConsole>
{

    /**
     * The action to run when switching panels.
     */
    private ConsoleAction action;


    /**
     * Constructs a {@code ConsolePanels}.
     *
     * @param panels    the panels
     * @param variables the variables. These are refreshed prior to each panel switch
     */
    public ConsolePanels(List<ConsolePanelView> panels, Variables variables)
    {
        super(panels, variables);
    }

    /**
     * Sets the action to invoke when switching panels.
     *
     * @param action the action
     */
    public void setAction(ConsoleAction action)
    {
        this.action = action;
    }

    /**
     * Switches panels.
     *
     * @param newPanel the panel to switch to
     * @param oldPanel the panel to switch from, or {@code null} if there was no prior panel
     * @return {@code true} if the switch was successful
     */
    @Override
    protected boolean switchPanel(ConsolePanelView newPanel, ConsolePanelView oldPanel)
    {
        boolean result = false;
        newPanel.executePreActivationActions();
        if (action != null)
        {
            do
            {
                result = action.run(newPanel);
                if (!result)
                {
                    break;
                }
            } while (!newPanel.isValid());
        }

        return result;
    }

}
