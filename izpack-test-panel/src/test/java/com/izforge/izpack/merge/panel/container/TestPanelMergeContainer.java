package com.izforge.izpack.merge.panel.container;

import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.merge.MergeManager;
import com.izforge.izpack.merge.MergeManagerImpl;
import com.izforge.izpack.merge.resolve.ClassPathCrawler;
import com.izforge.izpack.merge.resolve.MergeableResolver;
import com.izforge.izpack.merge.resolve.PathResolver;
import org.picocontainer.PicoBuilder;

/**
 * Container for panel merge test
 *
 * @author Anthonin Bonnefoy
 */
public class TestPanelMergeContainer extends AbstractContainer
{

    public void initBindings() throws Exception
    {
        pico = new PicoBuilder().withConstructorInjection().withCaching().build();
        pico.addComponent(PathResolver.class);
        pico.addComponent(MergeableResolver.class);
        pico.addComponent(ClassPathCrawler.class);
        pico.addComponent(MergeManager.class, MergeManagerImpl.class);
    }
}
