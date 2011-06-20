package com.izforge.izpack.compiler.container.provider;

import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.data.binding.*;
import org.hamcrest.beans.HasPropertyWithValue;
import org.hamcrest.collection.IsCollectionContaining;
import org.hamcrest.core.AllOf;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for provider
 *
 * @author Anthonin Bonnefoy
 */
public class IzpackProjectProviderTest
{
    private IzpackProjectProvider izpackProjectProvider;
    private IzpackProjectInstaller izpackProjectInstaller;

    @Before
    public void setUp() throws Exception
    {
        izpackProjectProvider = new IzpackProjectProvider();
        izpackProjectInstaller = izpackProjectProvider.provide(
                "bindingTest.xml");
        assertThat(izpackProjectInstaller, Is.is(IzpackProjectInstaller.class));
    }

    @Test
    public void bindingWithXInclude() throws Exception
    {
        izpackProjectInstaller = izpackProjectProvider.provide(
                "bindingXInclude/main.xml");
        // xincluded 4 panels
        assertThat(izpackProjectInstaller.getPanels().size(), is(4));
    }

    @Test
    public void bindingListener() throws Exception
    {
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
                                IsCollectionContaining.hasItems(
                                        HasPropertyWithValue.<OsModel>hasProperty("family", Is.is("windows")),
                                        HasPropertyWithValue.<OsModel>hasProperty("arch", Is.is("ppc"))
                                )))));
    }

    @Test
    public void bindingSimplePanel() throws Exception
    {
        List<Panel> panelList = izpackProjectInstaller.getPanels();

        assertThat(panelList, IsCollectionContaining.hasItem(
                AllOf.allOf(
                        HasPropertyWithValue.<Panel>hasProperty("className", Is.is("CheckedHelloPanel")),
                        HasPropertyWithValue.<Panel>hasProperty("panelid", Is.is("hellopanel")),
                        HasPropertyWithValue.<Panel>hasProperty("condition", Is.is("test.cond"))
                )
        ));
    }

    @Test
    public void bindingPanelWithOsConstraint() throws Exception
    {
        List<Panel> panelList = izpackProjectInstaller.getPanels();
        assertThat(panelList, IsCollectionContaining.hasItem(
                AllOf.allOf(
                        HasPropertyWithValue.<Panel>hasProperty("className", Is.is("HTMLInfoPanel")),
                        HasPropertyWithValue.<Panel>hasProperty("panelid", Is.is("infopanel")),
                        HasPropertyWithValue.<Panel>hasProperty("osConstraints",
                                IsCollectionContaining.hasItems(
                                        HasPropertyWithValue.<OsModel>hasProperty("family", Is.is("BSD")),
                                        HasPropertyWithValue.<OsModel>hasProperty("arch", Is.is("x666"))
                                ))
                )));
    }

    @Test
    public void bindingPanelWithHelp() throws Exception
    {
        List<Panel> panelList = izpackProjectInstaller.getPanels();
        assertThat(panelList, IsCollectionContaining.hasItem(
                HasPropertyWithValue.<Panel>hasProperty("helps",
                        IsCollectionContaining.hasItem(
                                AllOf.allOf(
                                        HasPropertyWithValue.<Help>hasProperty("src", Is.is("HelloPanelHelp_deu.html")),
                                        HasPropertyWithValue.<Help>hasProperty("iso3", Is.is("deu"))
                                )
                        ))
        ));
    }

}
