package com.izforge.izpack.compiler.container;

import com.izforge.izpack.compiler.merge.resolve.ClassPathCrawler;
import com.izforge.izpack.compiler.merge.resolve.CompilerPathResolver;
import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.merge.resolve.MergeableResolver;
import org.picocontainer.MutablePicoContainer;

import java.util.Properties;

/**
 * Container for com.izforge.izpack.resolve package tests.
 *
 * @author Tim Anderson
 */
public class TestResolveContainer extends AbstractContainer
{
    public void fillContainer(MutablePicoContainer container)
    {
        container.addComponent(Properties.class);
        container.addComponent(CompilerPathResolver.class);
        container.addComponent(ClassPathCrawler.class);
        container.addComponent(MergeableResolver.class);

        Properties properties = container.getComponent(Properties.class);
        properties.put("HelloPanelTestWithDependenciesClass", "com.izforge.izpack.panels.depend");
    }

}
