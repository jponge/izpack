/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/ http://izpack.codehaus.org/
 *
 * Copyright 2007 Dennis Reil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.izforge.izpack.installer.multiunpacker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.PackFile;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.api.event.InstallerListener;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.handler.AbstractUIProgressHandler;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.io.FileSpanningInputStream;
import com.izforge.izpack.core.io.FileSpanningOutputStream;
import com.izforge.izpack.installer.automation.PanelAutomation;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.unpacker.Cancellable;
import com.izforge.izpack.installer.unpacker.FileUnpacker;
import com.izforge.izpack.installer.unpacker.IMultiVolumeUnpackerHelper;
import com.izforge.izpack.installer.unpacker.UnpackerBase;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.Librarian;
import com.izforge.izpack.util.file.FileUtils;
import com.izforge.izpack.util.os.FileQueue;


/**
 * Unpacker class for a multi volume installation.
 *
 * @author Dennis Reil, <izpack@reil-online.de>
 * @author Tim Anderson
 */
public class MultiVolumeUnpacker extends UnpackerBase
{
    /**
     * The unpacker helper.
     */
    private IMultiVolumeUnpackerHelper helper;

    /**
     * The pack data volumes stream.
     */
    private FileSpanningInputStream volumes;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(MultiVolumeUnpacker.class.getName());

    /**
     * Constructs an <tt>MultiVolumeUnpacker</tt>.
     *
     * @param installData         the installation data
     * @param resourceManager     the resource manager
     * @param rules               the rules engine
     * @param variableSubstitutor the variable substituter
     * @param uninstallData       the uninstallation data
     * @param librarian           the librarian
     * @param housekeeper         the housekeeper
     */
    public MultiVolumeUnpacker(AutomatedInstallData installData, ResourceManager resourceManager, RulesEngine rules,
                               VariableSubstitutor variableSubstitutor, UninstallData uninstallData,
                               Librarian librarian, Housekeeper housekeeper)
    {
        super(installData, resourceManager, rules, variableSubstitutor, uninstallData, librarian, housekeeper);
    }

    /**
     * Sets the progress handler.
     *
     * @param handler the progress handler
     */
    @Override
    public void setHandler(AbstractUIProgressHandler handler)
    {
        super.setHandler(handler);
        if (handler instanceof PanelAutomation)
        {
            logger.fine("running in auto installation mode.");
            helper = new MultiVolumeUnpackerAutomationHelper(getInstallData(), handler);
        }
        else
        {
            logger.fine("running in normal installation mode.");
            helper = new MultiVolumeUnpackerHelper(getInstallData(), handler);
        }
    }

    /**
     * Invoked prior to unpacking.
     * <p/>
     * This notifies the {@link #getHandler() handler}, and any registered {@link InstallerListener listeners}.
     *
     * @throws Exception if the handler or listeners throw an exception
     */
    @Override
    protected void preUnpack() throws Exception
    {
        super.preUnpack();

        InputStream in = null;
        ObjectInputStream objectIn = null;
        try
        {
            // get volume metadata
            in = getResourceManager().getInputStream(FileSpanningOutputStream.VOLUMES_INFO);
            objectIn = new ObjectInputStream(in);
            int volumeCount = objectIn.readInt();
            String volumeName = objectIn.readUTF();
            logger.fine("Reading from " + volumeCount + " volumes with basename " + volumeName + " ");

            String mediaDirectory = MultiVolumeInstaller.getMediadirectory();
            if ((mediaDirectory == null) || (mediaDirectory.length() == 0))
            {
                logger.fine("Mediadirectory wasn't set.");
                mediaDirectory = System.getProperty("java.io.tmpdir"); // try the temporary directory
            }
            logger.fine("Using mediaDirectory = " + mediaDirectory);
            File volume = new File(mediaDirectory + File.separator + volumeName);
            if (!volume.exists())
            {
                volume = helper.enterNextMediaMessage(volume.getAbsolutePath());
            }
            volumes = new FileSpanningInputStream(volume, volumeCount);
        }
        finally
        {
            FileUtils.close(in);
            FileUtils.close(objectIn);
        }
    }

    /**
     * Creates an unpacker to unpack a pack file.
     *
     * @param file        the pack file to unpack
     * @param pack        the parent pack
     * @param queue       the file queue. May be <tt>null</tt>
     * @param cancellable determines if the unpacker should be cancelled
     * @return the unpacker
     * @throws IOException        for any I/O error
     * @throws InstallerException for any installer error
     */
    @Override
    protected FileUnpacker createFileUnpacker(PackFile file, Pack pack, FileQueue queue, Cancellable cancellable)
            throws IOException, InstallerException
    {
        if (pack.loose || file.isPack200Jar())
        {
            return super.createFileUnpacker(file, pack, queue, cancellable);
        }
        return new MultiVolumeFileUnpacker(volumes, helper, cancellable, getHandler(), queue, getLibrarian());
    }

    /**
     * Invoked after unpacking has completed, in order to clean up.
     */
    @Override
    protected void cleanup()
    {
        super.cleanup();
        FileUtils.close(volumes);
    }

}