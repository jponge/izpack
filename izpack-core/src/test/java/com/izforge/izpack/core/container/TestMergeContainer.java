package com.izforge.izpack.core.container;

import com.izforge.izpack.merge.MergeManager;
import com.izforge.izpack.merge.MergeManagerImpl;
import com.izforge.izpack.merge.resolve.MergeableResolver;
import com.izforge.izpack.merge.resolve.PathResolver;
import org.picocontainer.Characteristics;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.parameters.ComponentParameter;

import java.util.HashMap;
import java.util.Properties;

/**
 * Container for merge tests
 *
 * @author Anthonin Bonnefoy
 */
public class TestMergeContainer extends AbstractContainer
{

    public void fillContainer(MutablePicoContainer picoContainer)
    {
        picoContainer
                .addComponent(MergeManager.class, MergeManagerImpl.class)
                .as(Characteristics.USE_NAMES).addComponent(PathResolver.class)
                .as(Characteristics.USE_NAMES).addComponent(MergeableResolver.class)
                .addComponent("mergeContent", HashMap.class, ComponentParameter.ZERO);
    }

}
