package com.izforge.izpack.compiler.container;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import com.izforge.izpack.api.container.Container;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.compiler.merge.resolve.ClassPathCrawler;
import com.izforge.izpack.compiler.merge.resolve.CompilerPathResolver;
import com.izforge.izpack.merge.resolve.MergeableResolver;

/**
 * Fill container with resolver dependencies.
 *
 * @author Anthonin Bonnefoy
 */
public class ResolverContainerFiller
{
    public void fillContainer(Container container)
    {
        Properties properties = container.getComponent(Properties.class);
        for (Map.Entry<Object, Object> entry : getPanelDependencies().entrySet())
        {
            properties.put(entry.getKey(), entry.getValue());
        }
        container.addComponent(ClassPathCrawler.class);
        container.addComponent(CompilerPathResolver.class);
        container.addComponent(MergeableResolver.class);
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
