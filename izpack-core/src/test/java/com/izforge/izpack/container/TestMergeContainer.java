package com.izforge.izpack.container;

import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.core.container.filler.ResolverContainerFiller;
import com.izforge.izpack.merge.MergeManager;
import com.izforge.izpack.merge.MergeManagerImpl;
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
                .addComponent(MergeManager.class, MergeManagerImpl.class);
        fillContainer(new ResolverContainerFiller());
    }
}
