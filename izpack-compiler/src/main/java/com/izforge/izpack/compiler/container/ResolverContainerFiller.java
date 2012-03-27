package com.izforge.izpack.compiler.container;

import com.izforge.izpack.api.container.DependenciesFillerContainer;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.compiler.merge.resolve.ClassPathCrawler;
import com.izforge.izpack.compiler.merge.resolve.CompilerPathResolver;
import com.izforge.izpack.merge.resolve.MergeableResolver;
import org.picocontainer.Characteristics;
import org.picocontainer.MutablePicoContainer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * Fill container with resolver dependencies.
 *
 * @author Anthonin Bonnefoy
 */
public class ResolverContainerFiller implements DependenciesFillerContainer
{
    public void fillContainer(MutablePicoContainer picoContainer)
    {
        Properties properties = picoContainer.getComponent(Properties.class);
        for (Map.Entry<Object, Object> entry : getPanelDependencies().entrySet())
        {
            properties.put(entry.getKey(), entry.getValue());
        }
        picoContainer
                .as(Characteristics.USE_NAMES).addComponent(ClassPathCrawler.class)
                .as(Characteristics.USE_NAMES).addComponent(CompilerPathResolver.class)
                .as(Characteristics.USE_NAMES).addComponent(MergeableResolver.class)
        ;
    }

    private Properties getPanelDependencies()
    {
        Properties properties = new Properties();
        try
        {
            InputStream inStream = getClass().getResourceAsStream("panelDependencies.properties");
            properties.load(inStream);
        }
        catch (IOException e)
        {
            throw new IzPackException(e);
        }
        return properties;
    }
}
