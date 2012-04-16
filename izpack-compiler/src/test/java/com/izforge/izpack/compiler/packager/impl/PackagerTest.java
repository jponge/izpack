package com.izforge.izpack.compiler.packager.impl;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.dom4j.dom.DOMElement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.compiler.resource.ResourceFinder;
import com.izforge.izpack.merge.MergeManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest(FileUtils.class)
public class PackagerTest
{

    private ResourceFinder resourceFinder;

    private MergeManager mergeManager;

    @Before
    public void setUpMocks()
    {
        resourceFinder = mock(ResourceFinder.class);
        mergeManager = mock(MergeManager.class);
    }

    @Test
    public void guiPrefsWithNoSplash() throws IOException
    {
        final DOMElement rootNode = new DOMElement("installation");
        rootNode.add(new DOMElement("guiprefs"));
        when(resourceFinder.getXMLTree()).thenReturn(new XMLElementImpl(rootNode));

        final Packager packager = new Packager(null, null, null, null, null, mergeManager, null, null, resourceFinder,
                                               null);

        packager.writeManifest();

        verify(mergeManager).addResourceToMerge(anyString(), eq("META-INF/MANIFEST.MF"));
    }

    @Test
    public void guiPrefsWithSplash() throws IOException
    {
        PowerMockito.mockStatic(FileUtils.class);
        final File splashImage = new File("image.png");
        when(FileUtils.toFile(null)).thenReturn(splashImage);

        IXMLElement rootNode = new XMLElementImpl("installation");
        IXMLElement guiPrefsNode = new XMLElementImpl("guiprefs", rootNode);
        IXMLElement splashNode = new XMLElementImpl("splash", rootNode);
        splashNode.setContent("image.png");
        guiPrefsNode.addChild(splashNode);
        rootNode.addChild(guiPrefsNode);
        when(resourceFinder.getXMLTree()).thenReturn(rootNode);

        final Packager packager = new Packager(null, null, null, null, null, mergeManager, null, null, resourceFinder,
                                               null);
        packager.writeManifest();

        verify(mergeManager, times(1)).addResourceToMerge(anyString(), eq("META-INF/image.png"));
        verify(mergeManager, times(1)).addResourceToMerge(anyString(), eq("META-INF/MANIFEST.MF"));
    }

    @Test
    public void noGuiPrefs() throws IOException
    {
        when(resourceFinder.getXMLTree()).thenReturn(
                new XMLElementImpl(new DOMElement("installation")));

        final Packager packager = new Packager(null, null, null, null, null, mergeManager, null, null, resourceFinder,
                                               null);

        packager.writeManifest();

        verify(mergeManager).addResourceToMerge(anyString(), anyString());

    }
}
