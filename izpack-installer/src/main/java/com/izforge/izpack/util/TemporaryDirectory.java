/*
 * IzPack - Copyright 2001-2010 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2010 Sean O'Loughlin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.izforge.izpack.util;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Info.TempDir;
import com.izforge.izpack.util.file.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Manages the life-cycle of a temporary directory
 */
public class TemporaryDirectory implements CleanupClient
{
    private File tempdir;
    private final AutomatedInstallData installData;
    private final TempDir tempDirDescription;
    private boolean deleteOnExit = false;

    /**
     * Define a temporary directory
     *
     * @param tempDirDescription describes the parameters of the directory to be created
     * @param installData        The install data in to which the temporary directories variable will be written
     */
    public TemporaryDirectory(TempDir tempDirDescription, AutomatedInstallData installData)
    {
        if (null == tempDirDescription)
        {
            throw new IllegalArgumentException("Unable to create a temporary directory, the temp directory description may not be null.");
        }
        if (null == installData)
        {
            throw new IllegalArgumentException("Unable to create a temporary directory, the install data may not be null.");
        }
        this.installData = installData;
        this.tempDirDescription = tempDirDescription;

    }

    /**
     * Creates the temporary directory and sets the install data variable to point to it
     *
     * @throws IOException if creation of the directory fails
     */
    public void create() throws IOException
    {
        try
        {
            tempdir = File.createTempFile(tempDirDescription.getPrefix(), tempDirDescription.getSuffix());
            tempdir.delete();
            tempdir.mkdir();
        }
        catch (IOException e)
        {
            Debug.error("Unable to create temporary directory for install. IOException: ");
            Debug.error(e);
            throw e;
        }
        installData.setVariable(tempDirDescription.getVariableName(), tempdir.getAbsolutePath());
        Housekeeper.getInstance().registerForCleanup(this);
    }

    /**
     * Configure this temporary directory to be deleted when the installer exits
     * This will delete the directory and all of its contents
     */
    public void deleteOnExit()
    {
        deleteOnExit = true;
    }

    /**
     * Deletes the temporary directory and all it's contents immediately
     */
    @Override
    public void cleanUp()
    {
        if (null != tempdir)
        {
            if (deleteOnExit)
            {
                if (!FileUtils.deleteRecursively(tempdir))
                {
                    Debug.error("Failed to properly clean up files in "
                            + tempdir.getAbsolutePath()
                            + " manual clean up may be required.");
                }
            }
            else
            {
                Debug.log("Temporary directory has not been cleaned up. Files have been left in: " + tempdir.getAbsolutePath());

            }
        }
        else
        {
            Debug.log("TemporaryDirectory registered for cleanup but there is no temp directory to clean up.");
		}
	}



}