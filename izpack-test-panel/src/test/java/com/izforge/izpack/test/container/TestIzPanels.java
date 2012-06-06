package com.izforge.izpack.test.container;

import java.util.Collections;
import java.util.List;

import com.izforge.izpack.api.container.Container;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.IzPanel;
import com.izforge.izpack.installer.gui.IzPanelView;
import com.izforge.izpack.installer.gui.IzPanels;

/**
 * Hack implementation of {@link IzPanels} to enable {@code PanelDisplayTest} to set the panels after the
 * {@code InstallerFrame} has been constructed.
 *
 * @author Tim Anderson
 */
public class TestIzPanels extends IzPanels
{
    /**
     * The panels
     */
    private List<IzPanelView> panels;

    /**
     * Constructs a {@code IzPanels}.
     *
     * @param container   the container to register {@link IzPanel}s with
     * @param installData the installation data
     */
    public TestIzPanels(Container container, GUIInstallData installData)
    {
        super(Collections.<IzPanelView>emptyList(), container, installData);
    }

    /**
     * Sets the panels.
     *
     * @param panels the panels
     */
    public void setPanels(List<IzPanelView> panels)
    {
        this.panels = panels;
    }


    /**
     * Returns the panels.
     *
     * @return the panels
     */
    @Override
    public List<IzPanelView> getPanelViews()
    {
        return panels;
    }

    /**
     * Initialises the {@link IzPanel} instances.
     */
    @Override
    public void initialise()
    {
        super.initialise();
        setNextEnabled(!panels.isEmpty());
    }
}
