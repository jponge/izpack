package com.izforge.izpack.core.container.filler;

import com.izforge.izpack.api.container.DependenciesFillerContainer;
import com.izforge.izpack.merge.resolve.ClassPathCrawler;
import com.izforge.izpack.merge.resolve.MergeableResolver;
import com.izforge.izpack.merge.resolve.PathResolver;
import org.picocontainer.Characteristics;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.parameters.ComponentParameter;

import java.io.IOException;
import java.io.InputStream;
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
                        .addComponent("panelDependencies", getPanelDependencies())
                        .addComponent("mergeContent", HashMap.class, ComponentParameter.ZERO)
                        .as(Characteristics.USE_NAMES).addComponent(ClassPathCrawler.class)
                        .as(Characteristics.USE_NAMES).addComponent(PathResolver.class)
                        .as(Characteristics.USE_NAMES).addComponent(MergeableResolver.class)
                ;
    }

    private Properties getPanelDependencies()
    {
        Properties properties = new Properties();
        try
        {
            InputStream inStream = getClass().getResourceAsStream("/panelDependencies.properties");
            properties.load(inStream);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return properties;
    }
}
