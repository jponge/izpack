package com.izforge.izpack.installer.unpacker;


import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.URL;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.ResourceException;
import com.izforge.izpack.api.exception.ResourceInterruptedException;
import com.izforge.izpack.api.exception.ResourceNotFoundException;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.installer.web.WebRepositoryAccessor;
import com.izforge.izpack.util.IoHelper;


/**
 * {@link PackResources} implementation for the GUI-based installer.
 * <p/>
 * This supports both local and web-based pack resources.
 */
public class GUIPackResources extends AbstractPackResources
{

    /**
     * Temporary directory.
     */
    private static final String tempSubPath = "/IzpackWebTemp";

    /**
     * Constructs a {@code GUIPackResources}.
     *
     * @param resources   the resources
     * @param installData the installation data
     */
    public GUIPackResources(Resources resources, InstallData installData)
    {
        super(resources, installData);
    }

    /**
     * Returns the stream to a web-based pack resource.
     *
     * @param name      the resource name
     * @param webDirURL the web URL to load the resource from
     * @return a stream to the resource
     * @throws ResourceNotFoundException    if the resource cannot be found
     * @throws ResourceInterruptedException if resource retrieval is interrupted
     */
    protected InputStream getWebPackStream(String name, String webDirURL)
    {
        InputStream result;

        // TODO: Look first in same directory as primary jar
        // This may include prompting for changing of media
        // TODO: download and cache them all before starting copy process

        // See compiler.Packager#getJarOutputStream for the counterpart
        InstallData installData = getInstallData();
        String baseName = installData.getInfo().getInstallerBase();
        String packURL = webDirURL + "/" + baseName + ".pack-" + name + ".jar";
        String tempFolder = IoHelper.translatePath(
                installData.getInfo().getUninstallerPath() + GUIPackResources.tempSubPath,
                installData.getVariables());
        String tempFile;
        try
        {
            tempFile = WebRepositoryAccessor.getCachedUrl(packURL, tempFolder);
        }
        catch (InterruptedIOException exception)
        {
            throw new ResourceInterruptedException("Retrieval of " + webDirURL + " interrupted", exception);
        }
        catch (IOException exception)
        {
            throw new ResourceException("Failed to read " + webDirURL, exception);
        }
        try
        {
            URL url = new URL("jar:" + tempFile + "!/packs/pack-" + name);
            result = url.openStream();
        }
        catch (IOException exception)
        {
            throw new ResourceException("Failed to read pack", exception);
        }
        return result;
    }


}
