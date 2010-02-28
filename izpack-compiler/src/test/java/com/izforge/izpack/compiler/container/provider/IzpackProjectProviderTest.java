package com.izforge.izpack.compiler.container.provider;

import com.izforge.izpack.api.data.binding.IzpackProjectInstaller;
import com.izforge.izpack.api.data.binding.Listener;
import com.izforge.izpack.api.data.binding.OsModel;
import com.izforge.izpack.api.data.binding.Stage;
import org.hamcrest.beans.HasPropertyWithValue;
import org.hamcrest.collection.IsCollectionContaining;
import org.hamcrest.core.AllOf;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for provider
 *
 * @author Anthonin Bonnefoy
 */
public class IzpackProjectProviderTest
{
    private IzpackProjectProvider izpackProjectProvider;

    @Before
    public void setUp() throws Exception
    {
        izpackProjectProvider = new IzpackProjectProvider();
    }

    @Test
    public void bindingListener() throws Exception
    {
        IzpackProjectInstaller izpackProjectInstaller = izpackProjectProvider.provide(
                "bindingTest.xml");
        assertThat(izpackProjectInstaller, Is.is(IzpackProjectInstaller.class));
        List<Listener> listenerList = izpackProjectInstaller.getListeners();

        assertThat(listenerList, IsCollectionContaining.hasItem(
                AllOf.allOf(
                        HasPropertyWithValue.<Listener>hasProperty("classname", Is.is("SummaryLoggerInstallerListener")),
                        HasPropertyWithValue.<Listener>hasProperty("stage", Is.is(Stage.install))
                )
        ));

        assertThat(listenerList, IsCollectionContaining.hasItem(
                AllOf.allOf(
                        HasPropertyWithValue.<Listener>hasProperty("classname", Is.is("RegistryInstallerListener")),
                        HasPropertyWithValue.<Listener>hasProperty("stage", Is.is(Stage.install)),
                        HasPropertyWithValue.<Listener>hasProperty("os",
                                HasPropertyWithValue.<OsModel>hasProperty("family", Is.is("windows"))))
        ));


    }
}
