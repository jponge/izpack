package com.izforge.izpack.container;

import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.merge.MergeManager;
import com.izforge.izpack.merge.MergeManagerImpl;
import com.izforge.izpack.merge.resolve.PathResolver;
import org.picocontainer.PicoBuilder;

/**
 * Container for duplication test
 *
 * @author Anthonin Bonnefoy
 */
public class TestMergeDuplicationContainer extends AbstractContainer
{
    public void initBindings() throws Exception
    {
        pico = new PicoBuilder().withConstructorInjection().withCaching().build();
        pico.addComponent(PathResolver.class);
        pico.addComponent(MergeManager.class, MergeManagerImpl.class);

    }
}
