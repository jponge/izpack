package com.izforge.izpack.container;

import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.merge.MergeManager;
import com.izforge.izpack.merge.MergeManagerImpl;
import com.izforge.izpack.merge.resolve.ClassPathCrawler;
import com.izforge.izpack.merge.resolve.MergeableResolver;
import com.izforge.izpack.merge.resolve.PathResolver;
import org.picocontainer.PicoBuilder;

/**
 * Container for merge tests
 *
 * @author Anthonin Bonnefoy
 */
public class TestMergeContainer extends AbstractContainer
{

    public void initBindings() throws Exception
    {
        pico = new PicoBuilder().withConstructorInjection().withCaching().build()
                .addComponent(PathResolver.class)
                .addComponent(MergeableResolver.class)
                .addComponent(ClassPathCrawler.class)
                .addComponent(MergeManager.class, MergeManagerImpl.class);
    }
}
