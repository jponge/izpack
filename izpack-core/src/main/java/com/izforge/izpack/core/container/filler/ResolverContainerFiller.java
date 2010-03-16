package com.izforge.izpack.core.container.filler;

import com.izforge.izpack.api.container.DependenciesFillerContainer;
import com.izforge.izpack.merge.resolve.ClassPathCrawler;
import com.izforge.izpack.merge.resolve.MergeableResolver;
import com.izforge.izpack.merge.resolve.PathResolver;
import org.picocontainer.Characteristics;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.parameters.ComponentParameter;

import java.util.HashMap;

/**
 * Fill containter with resolver dependencies
 *
 * @author Anthonin Bonnefoy
 */
public class ResolverContainerFiller implements DependenciesFillerContainer
{
    public MutablePicoContainer fillContainer(MutablePicoContainer picoContainer)
    {
        return
                picoContainer
                        .as(Characteristics.USE_NAMES).addComponent(ClassPathCrawler.class)
                        .as(Characteristics.USE_NAMES).addComponent(PathResolver.class)
                        .addComponent(MergeableResolver.class)
                        .addComponent("mergeContent", HashMap.class, ComponentParameter.ZERO)
                ;
    }
}
