package com.izforge.izpack.test.container;

import com.izforge.izpack.api.container.BindeableContainer;
import com.izforge.izpack.api.data.GUIInstallData;
import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.core.container.filler.ResolverContainerFiller;
import com.izforge.izpack.installer.manager.PanelManager;
import com.izforge.izpack.merge.MergeManager;
import com.izforge.izpack.merge.MergeManagerImpl;
import org.mockito.Mockito;
import org.picocontainer.MutablePicoContainer;

/**
 * Container for panel manager
 *
 * @author Anthonin Bonnefoy
 */
public class TestPanelManagerContainer extends AbstractContainer
{

    /**
     * Init component bindings
     */
    public void fillContainer(MutablePicoContainer pico)
    {
        pico
                .addComponent(Mockito.mock(GUIInstallData.class))
                .addComponent(Mockito.mock(BindeableContainer.class))
                .addComponent(MergeManager.class, MergeManagerImpl.class)
                .addComponent(PanelManager.class);
        new ResolverContainerFiller().fillContainer(pico);
    }
}
