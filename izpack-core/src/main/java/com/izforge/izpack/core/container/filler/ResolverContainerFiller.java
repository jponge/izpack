package com.izforge.izpack.core.container.filler;

import com.izforge.izpack.api.container.DependenciesFillerContainer;
import com.izforge.izpack.merge.resolve.ClassPathCrawler;
import com.izforge.izpack.merge.resolve.MergeableResolver;
import com.izforge.izpack.merge.resolve.PathResolver;
import org.picocontainer.Characteristics;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.parameters.ComponentParameter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

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
                        .as(Characteristics.USE_NAMES).addComponent(MergeableResolver.class)
                        .addComponent("mergeContent", HashMap.class, ComponentParameter.ZERO)
                        .addComponent("panelDependencies", getPanelDependencies())
                ;
    }

    private Properties getPanelDependencies()
    {
        Properties properties = new Properties();
        try
        {
            properties.load(ClassLoader.getSystemResourceAsStream("panelDependencies.properties"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return properties;
    }
}
