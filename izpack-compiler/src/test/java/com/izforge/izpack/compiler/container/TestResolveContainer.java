package com.izforge.izpack.compiler.container;

import java.util.Properties;

import org.picocontainer.PicoException;

import com.izforge.izpack.api.exception.ContainerException;
import com.izforge.izpack.compiler.merge.resolve.ClassPathCrawler;
import com.izforge.izpack.compiler.merge.resolve.CompilerPathResolver;
import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.merge.resolve.MergeableResolver;

/**
 * Container for com.izforge.izpack.resolve package tests.
 *
 * @author Tim Anderson
 */
public class TestResolveContainer extends AbstractContainer
{

    /**
     * Constructs a <tt>TestResolveContainer</tt>.
     *
     * @throws ContainerException if initialisation fails
     */
    public TestResolveContainer()
    {
        initialise();
    }

    /**
     * Invoked by {@link #initialise} to fill the container.
     *
     * @throws ContainerException if initialisation fails
     * @throws PicoException      for any PicoContainer error
     */
    @Override
    protected void fillContainer()
    {
        addComponent(Properties.class);
        addComponent(CompilerPathResolver.class);
        addComponent(ClassPathCrawler.class);
        addComponent(MergeableResolver.class);

        Properties properties = getComponent(Properties.class);
        properties.put("HelloPanelTestWithDependenciesClass", "com.izforge.izpack.panels.depend");
    }

}
