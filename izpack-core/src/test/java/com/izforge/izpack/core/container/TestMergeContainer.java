package com.izforge.izpack.core.container;

import com.izforge.izpack.api.exception.ContainerException;
import com.izforge.izpack.merge.MergeManager;
import com.izforge.izpack.merge.MergeManagerImpl;
import com.izforge.izpack.merge.resolve.MergeableResolver;
import com.izforge.izpack.merge.resolve.PathResolver;


/**
 * Container for merge tests.
 *
 * @author Anthonin Bonnefoy
 */
public class TestMergeContainer extends AbstractContainer
{

    /**
     * Constructs a <tt>TestMergeContainer</tt>.
     *
     * @throws ContainerException if initialisation fails
     */
    public TestMergeContainer()
    {
        initialise();
    }

    /**
     * Invoked by {@link #initialise} to fill the container.
     *
     * @throws ContainerException if initialisation fails
     */
    @Override
    protected void fillContainer()
    {
        addComponent(MergeManager.class, MergeManagerImpl.class);
        addComponent(PathResolver.class);
        addComponent(MergeableResolver.class);
    }

}
