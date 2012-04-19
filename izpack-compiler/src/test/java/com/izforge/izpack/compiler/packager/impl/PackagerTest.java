package com.izforge.izpack.compiler.packager.impl;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.izforge.izpack.api.data.GUIPrefs;
import com.izforge.izpack.merge.MergeManager;

public class PackagerTest
{
    /**
     * The merge manager.
     */
    private MergeManager mergeManager;

    @Before
    public void setUp()
    {
        mergeManager = mock(MergeManager.class);
    }

    @Test
    public void noSplash() throws IOException
    {
        final Packager packager = new Packager(null, null, null, null, null, mergeManager, null, null, null);
        packager.setSplashScreenImage(null);
        packager.writeManifest();

        verify(mergeManager).addResourceToMerge(anyString(), eq("META-INF/MANIFEST.MF"));
    }

    @Test
    public void guiPrefsWithSplash() throws IOException
    {
        final File splashImage = new File("image.png");
        final Packager packager = new Packager(null, null, null, null, null, mergeManager, null, null, null);
        packager.setGUIPrefs(new GUIPrefs());
        packager.setSplashScreenImage(splashImage);
        packager.writeManifest();

        verify(mergeManager, times(1)).addResourceToMerge(anyString(), eq("META-INF/image.png"));
        verify(mergeManager, times(1)).addResourceToMerge(anyString(), eq("META-INF/MANIFEST.MF"));
    }

    @Test
    public void noGuiPrefs() throws IOException
    {
        final Packager packager = new Packager(null, null, null, null, null, mergeManager, null, null, null);

        packager.writeManifest();

        verify(mergeManager).addResourceToMerge(anyString(), anyString());

    }
}
