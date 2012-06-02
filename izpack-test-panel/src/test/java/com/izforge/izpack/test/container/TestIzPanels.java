package com.izforge.izpack.test.container;

import java.util.Collections;
import java.util.List;

import com.izforge.izpack.api.container.Container;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.IzPanel;
import com.izforge.izpack.installer.gui.IzPanels;
import com.izforge.izpack.installer.panel.PanelView;

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
    private List<PanelView<IzPanel>> panels;

    /**
     * Constructs a {@code IzPanels}.
     *
     * @param container   the container to register {@link IzPanel}s with
     * @param installData the installation data
     */
    public TestIzPanels(Container container, GUIInstallData installData)
    {
        super(Collections.<PanelView<IzPanel>>emptyList(), container, installData);
    }

    /**
     * Sets the panels.
     *
     * @param panels the panels
     */
    public void setPanels(List<PanelView<IzPanel>> panels) {
        this.panels = panels;
    }

    /**
     * Returns the panels.
     *
     * @return the panels
     */
    @Override
    public List<PanelView<IzPanel>> getPanels()
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
