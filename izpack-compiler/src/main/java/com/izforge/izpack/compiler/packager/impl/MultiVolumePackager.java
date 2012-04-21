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
package com.izforge.izpack.compiler.packager.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.tools.zip.ZipEntry;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.PackFile;
import com.izforge.izpack.api.data.XPackFile;
import com.izforge.izpack.compiler.compressor.PackCompressor;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.compiler.listener.PackagerListener;
import com.izforge.izpack.compiler.merge.resolve.CompilerPathResolver;
import com.izforge.izpack.compiler.stream.JarOutputStream;
import com.izforge.izpack.core.io.FileSpanningOutputStream;
import com.izforge.izpack.data.ExecutableFile;
import com.izforge.izpack.data.PackInfo;
import com.izforge.izpack.data.ParsableFile;
import com.izforge.izpack.data.UpdateCheck;
import com.izforge.izpack.merge.MergeManager;
import com.izforge.izpack.merge.resolve.MergeableResolver;
import com.izforge.izpack.util.IoHelper;


/**
 * An {@link com.izforge.izpack.compiler.packager.IPackager} that packs everything into multiple volumes.
 * <p/>
 * <p/>
 * Here's an example how to specify an installer which will create multiple volumes. In this example the volumes shall
 * be CDs with 650 megabytes. There will be an additional free space of 150 megabytes on the first volume.
 * <br/>
 * This will result in the creation of an installer.jar and multiple installer.pak* files.
 * The installer.jar plus installer.pak plus the additional resources have to be copied on the first volume,
 * each installer.pak.&lt;number&gt; on several volumes.
 * <pre>
 * {@code
 * <packaging>
 *       <packager class="com.izforge.izpack.compiler.packager.impl.MultiVolumePackager">
 *           <!-- 650 MB volumes, 150 MB space on the first volume -->
 *           <options volumesize="681574400" firstvolumefreespace="157286400"/>
 *       </packager>
 *       <unpacker class="com.izforge.izpack.installer.multiunpacker.MultiVolumeUnpacker" />
 * </packaging>
 * }
 * </pre>
 *
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 * @author Tim Anderson
 */
public class MultiVolumePackager extends PackagerBase
{

    /**
     * The volume size, in bytes.
     */
    private long volumeSize = FileSpanningOutputStream.DEFAULT_VOLUME_SIZE;

    /**
     * The first volume free space size, in bytes.
     */
    private long freeSpace = 0;

    /**
     * The configuration attribute to specify the volume size.
     */
    private static final String VOLUME_SIZE = "volumesize";

    /**
     * The configuration attribute to specify the first volume free space size.
     */
    private static final String FIRST_VOLUME_FREE_SPACE = "firstvolumefreespace";

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(MultiVolumePackager.class.getName());


    /**
     * Constructs a <tt>MultiVolumePackager</tt>.
     *
     * @param properties        the properties
     * @param listener          the packager listener
     * @param mergeManager      the merge manager
     * @param pathResolver      the path resolver
     * @param mergeableResolver the mergeable resolver
     * @param compressor        the pack compressor
     * @param compilerData      the compiler data
     */
    public MultiVolumePackager(Properties properties, PackagerListener listener, JarOutputStream installerJar,
                               MergeManager mergeManager, CompilerPathResolver pathResolver,
                               MergeableResolver mergeableResolver, PackCompressor compressor,
                               CompilerData compilerData)
    {
        super(properties, listener, installerJar, mergeManager, pathResolver, mergeableResolver, compressor,
              compilerData);
    }

    /**
     * Sets the maximum volume size.
     *
     * @param size the maximum volume size, in bytes
     */
    public void setMaxVolumeSize(long size)
    {
        volumeSize = size;
    }

    /**
     * Sets the first volume free space size.
     * <p/>
     * When specified, this limits the size of the first volume to <em>maxVolumeSize - firstVolumeFreeSpace</em>.
     * <p/>
     * This may be used to allocate space for additional files on CD beside the pack files.
     *
     * @param size the free space size, in bytes
     */
    public void setFirstVolumeFreeSpace(long size)
    {
        freeSpace = size;
    }

    public void addConfigurationInformation(IXMLElement data)
    {
        if (data != null)
        {
            setMaxVolumeSize(Long.valueOf(data.getAttribute(VOLUME_SIZE, Long.toString(volumeSize))));
            setFirstVolumeFreeSpace(Long.valueOf(data.getAttribute(FIRST_VOLUME_FREE_SPACE, Long.toString(freeSpace))));
        }
    }

    /**
     * Write packs to the installer jar, or each to a separate jar.
     *
     * @throws IOException for any I/O error
     */
    @Override
    protected void writePacks() throws IOException
    {
        String classname = getClass().getSimpleName();

        // propagate the configuration to the variables, for debugging purposes
        getVariables().setProperty(classname + "." + VOLUME_SIZE, Long.toString(volumeSize));
        getVariables().setProperty(classname + "." + FIRST_VOLUME_FREE_SPACE, Long.toString(freeSpace));

        // Pack File data may be written to separate jars
        writePacks(new File(getInfo().getInstallerBase()));
    }

    /**
     * Write Packs to primary jar or each to a separate jar.
     */
    private void writePacks(File primaryFile) throws IOException
    {
        List<PackInfo> packs = getPacksList();
        final int num = packs.size();
        sendMsg("Writing " + num + " Pack" + (num > 1 ? "s" : "") + " into installer");
        logger.fine("Writing " + num + " Pack" + (num > 1 ? "s" : "") + " into installer");
        // Map to remember pack number and bytes offsets of back references

        // First write the serialized files and file metadata data for each pack
        // while counting bytes.

        logger.fine("Volume size: " + volumeSize);
        logger.fine("Extra space on first volume: " + freeSpace);
        FileSpanningOutputStream volumes = new FileSpanningOutputStream(primaryFile.getAbsolutePath() + ".pak",
                                                                        volumeSize, freeSpace);

        for (PackInfo packInfo : packs)
        {
            writePack(packInfo, volumes);
        }

        // write metadata for reading in volumes
        logger.fine("Written " + volumes.getVolumes() + " volumes");
        String volumeName = primaryFile.getName() + ".pak";

        volumes.flush();
        volumes.close();

        JarOutputStream installerJar = getInstallerJar();
        installerJar.putNextEntry(new ZipEntry(RESOURCES_PATH + "volumes.info"));
        ObjectOutputStream out = new ObjectOutputStream(installerJar);
        out.writeInt(volumes.getVolumes());
        out.writeUTF(volumeName);
        out.flush();
        installerJar.closeEntry();

        // Now that we know sizes, write pack metadata to primary jar.
        installerJar.putNextEntry(new ZipEntry(RESOURCES_PATH + "packs.info"));
        out = new ObjectOutputStream(installerJar);
        out.writeInt(packs.size());

        for (PackInfo pack : packs)
        {
            out.writeObject(pack.getPack());
        }
        out.flush();
        installerJar.closeEntry();
    }

    /**
     * Writes a pack.
     * <p/>
     * Pack information is written to the installer jar, while the actual files are written to the volumes.
     *
     * @param packInfo the pack information
     * @param volumes  the volumes
     * @throws IOException for any I/O error
     */
    private void writePack(PackInfo packInfo, FileSpanningOutputStream volumes) throws IOException
    {
        Pack pack = packInfo.getPack();
        pack.nbytes = 0;

        sendMsg("Writing Pack: " + pack.id, PackagerListener.MSG_VERBOSE);
        logger.fine("Writing Pack: " + pack.id);
        ZipEntry entry = new ZipEntry(RESOURCES_PATH + "packs/pack-" + pack.id);

        JarOutputStream installerJar = getInstallerJar();
        installerJar.putNextEntry(entry);
        ObjectOutputStream packStream = new ObjectOutputStream(installerJar);

        writePackFiles(packInfo, volumes, pack, packStream);

        // Write out information about parsable files
        packStream.writeInt(packInfo.getParsables().size());
        for (ParsableFile file : packInfo.getParsables())
        {
            packStream.writeObject(file);
        }

        // Write out information about executable files
        packStream.writeInt(packInfo.getExecutables().size());
        for (ExecutableFile file : packInfo.getExecutables())
        {
            packStream.writeObject(file);
        }

        // Write out information about update check files
        packStream.writeInt(packInfo.getUpdateChecks().size());
        for (UpdateCheck check : packInfo.getUpdateChecks())
        {
            packStream.writeObject(check);
        }

        // Cleanup
        packStream.flush();
    }

    /**
     * Writes the pack files.
     * <p/>
     * The file data is written to <tt>volumes</tt>, whilst the meta-data is written to <tt>packStream</tt>.
     *
     * @param packInfo   the pack information
     * @param volumes    the volumes to write to
     * @param pack       the pack
     * @param packStream the stream to write the pack meta-data to
     * @throws IOException
     */
    private void writePackFiles(PackInfo packInfo, FileSpanningOutputStream volumes, Pack pack,
                                ObjectOutputStream packStream) throws IOException
    {
        // write the file meta-data
        Set<PackFile> files = packInfo.getPackFiles();
        packStream.writeInt(files.size());

        for (PackFile packfile : files)
        {
            XPackFile pf = new XPackFile(packfile);
            File file = packInfo.getFile(packfile);
            logger.fine("Next file: " + file.getAbsolutePath());

            if (!pack.loose && !pf.isDirectory())
            {
                long beforePosition = volumes.getFilePointer();
                pf.setArchiveFilePosition(beforePosition);

                // write the file to the volumes
                int volumeCount = volumes.getVolumes();

                FileInputStream in = new FileInputStream(file);
                long bytesWritten = IoHelper.copyStream(in, volumes);
                long afterPosition = volumes.getFilePointer();
                logger.fine("File (" + pf.sourcePath + ") " + beforePosition + " <-> " + afterPosition);

                if (volumes.getFilePointer() != (beforePosition + bytesWritten))
                {
                    logger.fine("file: " + file.getName());
                    logger.fine("(Filepos/BytesWritten/ExpectedNewFilePos/NewFilePointer) ("
                                        + beforePosition + "/" + bytesWritten + "/" + (beforePosition + bytesWritten)
                                        + "/" + volumes.getFilePointer() + ")");
                    logger.fine("Volumes (before/after) (" + volumeCount + "/" + volumes.getVolumes() + ")");
                    throw new IOException("Error new file pointer is illegal");
                }

                if (bytesWritten != pf.length())
                {
                    throw new IOException("File size mismatch when reading " + file);
                }
                in.close();
            }

            // write pack file meta-data
            packStream.writeObject(pf);
            packStream.flush(); // make sure it is written
            // even if not written, it counts towards pack size
            pack.nbytes += pf.length();
        }
    }

}