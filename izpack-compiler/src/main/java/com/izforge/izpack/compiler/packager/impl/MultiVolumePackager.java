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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
import com.izforge.izpack.compiler.resource.ResourceFinder;
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
     * The configuration options.
     */
    private IXMLElement configData = null;

    /**
     * The volume size, in bytes.
     */
    private long volumeSize = FileSpanningOutputStream.DEFAULT_VOLUME_SIZE;

    /**
     * The first volume free spaces size, in bytes.
     */
    private long freeSpace = FileSpanningOutputStream.DEFAULT_ADDITIONAL_FIRST_VOLUME_FREE_SPACE_SIZE;

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
     * @param resourceFinder    the resource finder
     * @param compressor        the pack compressor
     * @param compilerData      the compiler data
     */
    public MultiVolumePackager(Properties properties, PackagerListener listener, JarOutputStream installerJar,
                               MergeManager mergeManager, CompilerPathResolver pathResolver,
                               MergeableResolver mergeableResolver, ResourceFinder resourceFinder,
                               PackCompressor compressor, CompilerData compilerData)
    {
        super(properties, listener, installerJar, mergeManager, pathResolver, mergeableResolver, resourceFinder,
              compressor, compilerData);
    }

    public void addConfigurationInformation(IXMLElement data)
    {
        this.configData = data;
    }

    /**
     * Write packs to the installer jar, or each to a separate jar.
     *
     * @throws IOException for any I/O error
     */
    @Override
    protected void writePacks() throws IOException
    {
        if (configData != null)
        {
            volumeSize = Long.valueOf(configData.getAttribute(VOLUME_SIZE, Long.toString(volumeSize)));
            freeSpace = Long.valueOf(configData.getAttribute(FIRST_VOLUME_FREE_SPACE, Long.toString(freeSpace)));
        }

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
        Map storedFiles = new HashMap();

        // First write the serialized files and file metadata data for each pack
        // while counting bytes.

        logger.fine("Volume size: " + volumeSize);
        logger.fine("Extra space on first volume: " + freeSpace);
        FileSpanningOutputStream fout = new FileSpanningOutputStream(
                primaryFile.getParent() + File.separator + primaryFile.getName() + ".pak", volumeSize);
        fout.setFirstvolumefreespacesize(freeSpace);

        int packNumber = 0;
        for (PackInfo packInfo : packs)
        {
            writePack(packInfo, packNumber, fout, storedFiles);
            packNumber++;
        }

        // write metadata for reading in volumes
        int volumes = fout.getVolumeCount();
        logger.fine("Written " + volumes + " volumes");
        String volumeName = primaryFile.getName() + ".pak";

        fout.flush();
        fout.close();

        JarOutputStream installerJar = getInstallerJar();
        installerJar.putNextEntry(new ZipEntry(RESOURCES_PATH + "volumes.info"));
        ObjectOutputStream out = new ObjectOutputStream(installerJar);
        out.writeInt(volumes);
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

    private void writePack(PackInfo packInfo, int packNumber, FileSpanningOutputStream fout, Map storedFiles)
            throws IOException
    {
        Pack pack = packInfo.getPack();
        pack.nbytes = 0;

        sendMsg("Writing Pack " + packNumber + ": " + pack.name, PackagerListener.MSG_VERBOSE);
        logger.fine("Writing Pack " + packNumber + ": " + pack.name);
        ZipEntry entry = new ZipEntry(RESOURCES_PATH + "packs/pack" + packNumber);
        // write the metadata as uncompressed object stream to installerJar
        // first write a packs entry

        JarOutputStream installerJar = getInstallerJar();
        installerJar.putNextEntry(entry);
        ObjectOutputStream objOut = new ObjectOutputStream(installerJar);

        // We write the actual pack files
        objOut.writeInt(packInfo.getPackFiles().size());

        writePackFiles(packInfo, fout, storedFiles, pack, objOut);

        // Write out information about parsable files
        objOut.writeInt(packInfo.getParsables().size());
        for (ParsableFile file : packInfo.getParsables())
        {
            objOut.writeObject(file);
        }

        // Write out information about executable files
        objOut.writeInt(packInfo.getExecutables().size());
        for (ExecutableFile file : packInfo.getExecutables())
        {
            objOut.writeObject(file);
        }

        // Write out information about update check files
        objOut.writeInt(packInfo.getUpdateChecks().size());
        for (UpdateCheck check : packInfo.getUpdateChecks())
        {
            objOut.writeObject(check);
        }

        // Cleanup
        objOut.flush();
    }

    private void writePackFiles(PackInfo packInfo, FileSpanningOutputStream fout, Map storedFiles, Pack pack,
                                ObjectOutputStream objOut) throws IOException
    {
        for (PackFile packfile : packInfo.getPackFiles())
        {
            boolean addFile = !pack.loose;
            XPackFile pf = new XPackFile(packfile);
            File file = packInfo.getFile(packfile);
            logger.fine("Next file: " + file.getAbsolutePath());
            // use a back reference if file was in previous pack, and in
            // same jar
            Object[] info = (Object[]) storedFiles.get(file);
            if (info != null && !packSeparateJars())
            {
                logger.fine("File already included in other pack");
                pf.setPreviousPackFileRef((String) info[0], (Long) info[1]);
                addFile = false;
            }

            if (addFile && !pf.isDirectory())
            {
                long pos = fout.getFilepointer();

                pf.setArchivefileposition(pos);

                // write out the filepointer
                int volumecountbeforewrite = fout.getVolumeCount();

                FileInputStream inStream = new FileInputStream(file);
                long bytesWritten = IoHelper.copyStream(inStream, fout);
                fout.flush();

                long posafterwrite = fout.getFilepointer();
                logger.fine("File (" + pf.sourcePath + ") " + pos + " <-> " + posafterwrite);

                if (fout.getFilepointer() != (pos + bytesWritten))
                {
                    logger.fine("file: " + file.getName());
                    logger.fine("(Filepos/BytesWritten/ExpectedNewFilePos/NewFilePointer) ("
                                        + pos + "/" + bytesWritten + "/" + (pos + bytesWritten)
                                        + "/" + fout.getFilepointer() + ")");
                    logger.fine("Volumecount (before/after) ("
                                        + volumecountbeforewrite + "/" + fout.getVolumeCount() + ")");
                    throw new IOException("Error new filepointer is illegal");
                }

                if (bytesWritten != pf.length())
                {
                    throw new IOException("File size mismatch when reading " + file);
                }
                inStream.close();
                // keine backreferences mglich
                // storedFiles.put(file, new long[] { packNumber, pos});
            }

            objOut.writeObject(pf); // base info
            objOut.flush(); // make sure it is written
            // even if not written, it counts towards pack size
            pack.nbytes += pf.length();
        }
    }

}