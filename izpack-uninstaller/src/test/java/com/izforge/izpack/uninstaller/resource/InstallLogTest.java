package com.izforge.izpack.uninstaller.resource;


import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.apache.commons.io.input.ReaderInputStream;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.izforge.izpack.api.resource.Resources;

/**
 * Tests the {@link InstallLog} class.
 *
 * @author Tim Anderson
 */
public class InstallLogTest
{

    /**
     * The resources.
     */
    private Resources resources;


    /**
     * Sets up the test case.
     *
     * @throws IOException for any I/O error
     */
    @Before
    public void setUp() throws IOException
    {
        // set up a mock resource
        String installLog = "myapp\n"
                + "myapp/dir2/dir3\n"
                + "myapp/dir2/dir3/file2\n"
                + "myapp/dir2/file1\n"
                + "myapp/dir1\n";
        StringReader reader = new StringReader(installLog);
        resources = Mockito.mock(Resources.class);
        when(resources.getInputStream("install.log")).thenReturn(new ReaderInputStream(reader));
    }

    /**
     * Tests the {@link InstallLog#getInstallPath(Resources)} method.
     *
     * @throws IOException for any I/O error
     */
    @Test
    public void testStaticGetInstallPath() throws IOException
    {
        assertEquals("myapp", InstallLog.getInstallPath(resources));
    }

    /**
     * Tests the {@link InstallLog#getInstallPath()} and {@link InstallLog#getInstalled()} method.
     *
     * @throws IOException for any I/O error
     */
    @Test
    public void testInstalled() throws IOException
    {
        // create the install log
        InstallLog log = new InstallLog(resources);

        // verify the install path
        assertEquals("myapp", log.getInstallPath());

        // verify there are 4 installed files, and they are ordered leaf paths first
        List<File> installed = log.getInstalled();
        assertEquals(4, installed.size());
        assertEquals(new File("myapp/dir2/file1"), installed.get(0));
        assertEquals(new File("myapp/dir2/dir3/file2"), installed.get(1));
        assertEquals(new File("myapp/dir2/dir3"), installed.get(2));
        assertEquals(new File("myapp/dir1"), installed.get(3));
    }

}
