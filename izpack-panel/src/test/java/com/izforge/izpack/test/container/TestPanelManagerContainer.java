package com.izforge.izpack.test.container;

import com.izforge.izpack.api.container.BindeableContainer;
import com.izforge.izpack.api.data.GUIInstallData;
import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.installer.manager.PanelManager;
import com.izforge.izpack.merge.MergeManager;
import com.izforge.izpack.merge.MergeManagerImpl;
import com.izforge.izpack.merge.resolve.ClassPathCrawler;
import com.izforge.izpack.merge.resolve.MergeableResolver;
import com.izforge.izpack.merge.resolve.PathResolver;
import org.mockito.Mockito;
import org.picocontainer.PicoBuilder;

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
    public void initBindings()
    {
        pico = new PicoBuilder().withConstructorInjection().build();
        pico.addComponent(Mockito.mock(GUIInstallData.class));
        pico.addComponent(Mockito.mock(BindeableContainer.class));
        pico.addComponent(ClassPathCrawler.class);
        pico.addComponent(MergeableResolver.class);
        pico.addComponent(PathResolver.class);
        pico.addComponent(MergeManager.class, MergeManagerImpl.class);
        pico.addComponent(PanelManager.class);
    }
}
