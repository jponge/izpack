package com.izforge.izpack.core.container;

import com.izforge.izpack.merge.MergeManager;
import com.izforge.izpack.merge.MergeManagerImpl;
import com.izforge.izpack.merge.resolve.ClassPathCrawler;
import com.izforge.izpack.merge.resolve.MergeableResolver;
import com.izforge.izpack.merge.resolve.PathResolver;
import org.picocontainer.Characteristics;
import org.picocontainer.PicoBuilder;
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

    public void initBindings() throws Exception
    {
        Properties property = new Properties();
        property.put("HelloPanelTestWithDependenciesClass", "com.izforge.izpack.panels.depend");
        pico = new PicoBuilder().withConstructorInjection().withCaching().build()
                .addComponent(MergeManager.class, MergeManagerImpl.class)
                .as(Characteristics.USE_NAMES).addComponent(ClassPathCrawler.class)
                .as(Characteristics.USE_NAMES).addComponent(PathResolver.class)
                .as(Characteristics.USE_NAMES).addComponent(MergeableResolver.class)
                .addComponent("mergeContent", HashMap.class, ComponentParameter.ZERO)
                .addComponent("panelDependencies", property);
    }
}
