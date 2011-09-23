/*
 * $Id$
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
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

package com.izforge.izpack.compiler.packager.impl;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.PackFile;
import com.izforge.izpack.api.data.binding.IzpackProjectInstaller;
import com.izforge.izpack.api.exception.CompilerException;
import com.izforge.izpack.compiler.compressor.PackCompressor;
import com.izforge.izpack.compiler.container.CompilerContainer;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.compiler.listener.PackagerListener;
import com.izforge.izpack.compiler.resource.ResourceFinder;
import com.izforge.izpack.compiler.stream.ByteCountingOutputStream;
import com.izforge.izpack.compiler.stream.JarOutputStream;
import com.izforge.izpack.data.ExecutableFile;
import com.izforge.izpack.data.PackInfo;
import com.izforge.izpack.data.ParsableFile;
import com.izforge.izpack.data.UpdateCheck;
import com.izforge.izpack.merge.MergeManager;
import com.izforge.izpack.merge.resolve.MergeableResolver;
import com.izforge.izpack.merge.resolve.PathResolver;
import com.izforge.izpack.util.FileUtil;
import com.izforge.izpack.util.IoHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Pack200;
import java.util.zip.ZipInputStream;

/**
 * The packager class. The packager is used by the compiler to put files into an installer, and
 * create the actual installer files.
 *
 * @author Julien Ponge
 * @author Chadwick McHenry
 */
public class Packager extends PackagerBase
{

    /**
     * Executable zipped output stream. First to open, last to close.
     * Attention! This is our own JarOutputStream, not the java standard!
     */
    private JarOutputStream primaryJarStream;

    private CompilerData compilerData;

    /**
     * Decoration of the primary jar stream.
     * May be compressed or not depending on the compiler data.
     */
    private OutputStream outputStream;
    private MergeManager mergeManager;
    private ResourceFinder resourceFinder;

    /**
     * The constructor.
     *
     * @throws com.izforge.izpack.api.exception.CompilerException
     *
     */
    public Packager(Properties properties, CompilerData compilerData, CompilerContainer compilerContainer, PackagerListener listener, JarOutputStream jarOutputStream, PackCompressor packCompressor, OutputStream outputStream, MergeManager mergeManager, PathResolver pathResolver, IzpackProjectInstaller izpackInstallModel, MergeableResolver mergeableResolver, ResourceFinder resourceFinder) throws CompilerException
    {
        super(properties, compilerContainer, listener, mergeManager, pathResolver, izpackInstallModel, mergeableResolver);
        this.compilerData = compilerData;
        this.primaryJarStream = jarOutputStream;
        this.resourceFinder = resourceFinder;
        this.compressor = packCompressor;
        this.outputStream = outputStream;
        this.mergeManager = mergeManager;
    }

    /* (non-Javadoc)
    * @see com.izforge.izpack.compiler.packager.IPackager#createInstaller(java.io.File)
    */

    public void createInstaller() throws Exception
    {
        // preliminary work
        info.setInstallerBase(compilerData.getOutput().replaceAll(".jar", ""));

        packJarsSeparate = (info.getWebDirURL() != null);

        // primary (possibly only) jar. -1 indicates primary

        sendStart();

        writeInstaller();

        // Finish up. closeAlways is a hack for pack compressions other than
        // default. Some of it (e.g. BZip2) closes the slave of it also.
        // But this should not be because the jar stream should be open 
        // for the next pack. Therefore an own JarOutputStream will be used
        // which close method will be blocked.
        primaryJarStream.closeAlways();

        sendStop();
    }

    /***********************************************************************************************
     * Private methods used when writing out the installer to jar files.
     **********************************************************************************************/

    /**
     * Write skeleton installer to primary jar. It is just an included jar, except that we copy the
     * META-INF as well.
     */
    protected void writeSkeletonInstaller() throws IOException
    {
        sendMsg("Copying the skeleton installer", PackagerListener.MSG_VERBOSE);
        mergeManager.addResourceToMerge("com/izforge/izpack/installer/");
        mergeManager.addResourceToMerge("org/picocontainer/");
        mergeManager.addResourceToMerge("com/izforge/izpack/img/");
        mergeManager.addResourceToMerge("com/izforge/izpack/bin/");
        mergeManager.addResourceToMerge("com/izforge/izpack/api/");
        mergeManager.addResourceToMerge("com/izforge/izpack/event/");
        mergeManager.addResourceToMerge("com/izforge/izpack/core/");
        mergeManager.addResourceToMerge("com/izforge/izpack/data/");
        mergeManager.addResourceToMerge("com/izforge/izpack/gui/");
        mergeManager.addResourceToMerge("com/izforge/izpack/merge/");
        mergeManager.addResourceToMerge("com/izforge/izpack/util/");
        mergeManager.addResourceToMerge("org/apache/regexp/");
        mergeManager.addResourceToMerge("com/coi/tools/");
        mergeManager.addResourceToMerge("org/apache/tools/zip/");
        mergeManager.merge(primaryJarStream);
    }

    /**
     * Write an arbitrary object to primary jar.
     */
    protected void writeInstallerObject(String entryName, Object object) throws IOException
    {
        primaryJarStream.putNextEntry(new org.apache.tools.zip.ZipEntry(RESOURCES_PATH + entryName));
        ObjectOutputStream out = new ObjectOutputStream(primaryJarStream);
        out.writeObject(object);
        out.flush();
        primaryJarStream.closeEntry();
    }

    /**
     * Write the data referenced by URL to primary jar.
     */
    protected void writeInstallerResources() throws IOException
    {
        sendMsg("Copying " + installerResourceURLMap.size() + " files into installer");

        for (Map.Entry<String, URL> stringURLEntry : installerResourceURLMap.entrySet())
        {
            URL url = stringURLEntry.getValue();
            InputStream in = url.openStream();

            org.apache.tools.zip.ZipEntry newEntry = new org.apache.tools.zip.ZipEntry(RESOURCES_PATH + stringURLEntry.getKey());
            long dateTime = FileUtil.getFileDateTime(url);
            if (dateTime != -1)
            {
                newEntry.setTime(dateTime);
            }
            primaryJarStream.putNextEntry(newEntry);

            IoHelper.copyStream(in, primaryJarStream);
            primaryJarStream.closeEntry();
            in.close();
        }
    }

    /**
     * Copy included jars to primary jar.
     */
    protected void writeIncludedJars() throws IOException
    {
        sendMsg("Merging " + includedJarURLs.size() + " jars into installer");

        for (Object[] includedJarURL : includedJarURLs)
        {
            InputStream is = ((URL) includedJarURL[0]).openStream();
            ZipInputStream inJarStream = new ZipInputStream(is);
            IoHelper.copyZip(inJarStream, primaryJarStream, (List<String>) includedJarURL[1], alreadyWrittenFiles);
        }
    }

    /**
     * Write manifest in the install jar.
     */
    @Override
    public void writeManifest() throws IOException
    {
        IXMLElement data = resourceFinder.getXMLTree();
        IXMLElement guiPrefsElement = data.getFirstChildNamed("guiprefs");
        // Add splash screen configuration
        List<String> lines = IOUtils.readLines(getClass().getResourceAsStream("MANIFEST.MF"));
        IXMLElement splashNode = null;
        if (guiPrefsElement != null)
        {
            splashNode = guiPrefsElement.getFirstChildNamed("splash");
        }
        if (splashNode != null)
        {
            // Add splash image to installer jar
            File splashImage = FileUtils.toFile(resourceFinder.findProjectResource(splashNode.getContent(), "Resource", splashNode));
            String destination = String.format("META-INF/%s", splashImage.getName());
            mergeManager.addResourceToMerge(splashImage.getAbsolutePath(), destination);
            lines.add(String.format("SplashScreen-Image: %s", destination));
        }
        lines.add("");
        File tempManifest = com.izforge.izpack.util.file.FileUtils.createTempFile("MANIFEST", ".MF");
        FileUtils.writeLines(tempManifest, lines);
        mergeManager.addResourceToMerge(tempManifest.getAbsolutePath(), "META-INF/MANIFEST.MF");
    }

    /**
     * Write Packs to primary jar or each to a separate jar.
     */
    protected void writePacks() throws Exception
    {
        final int num = packsList.size();
        sendMsg("Writing " + num + " Pack" + (num > 1 ? "s" : "") + " into installer");

        // Map to remember pack number and bytes offsets of back references
        Map<File, Object[]> storedFiles = new HashMap<File, Object[]>();

        // Pack200 files map
        Map<Integer, File> pack200Map = new HashMap<Integer, File>();
        int pack200Counter = 0;

        // Force UTF-8 encoding in order to have proper ZipEntry names.
        primaryJarStream.setEncoding("utf-8");

        // First write the serialized files and file metadata data for each pack
        // while counting bytes.

        int packNumber = 0;
        IXMLElement root = new XMLElementImpl("packs");

        for (PackInfo packInfo : packsList)
        {
            Pack pack = packInfo.getPack();
            pack.nbytes = 0;
            if ((pack.id == null) || (pack.id.length() == 0))
            {
                pack.id = pack.name;
            }

            // create a pack specific jar if required
            // REFACTOR : Repare web installer
            // REFACTOR : Use a mergeManager for each packages that will be added to the main merger

//            if (packJarsSeparate) {
            // See installer.Unpacker#getPackAsStream for the counterpart
//                String name = baseFile.getName() + ".pack-" + pack.id + ".jar";
//                packStream = IoHelper.getJarOutputStream(name, baseFile.getParentFile());
//            }

            sendMsg("Writing Pack " + packNumber + ": " + pack.name, PackagerListener.MSG_VERBOSE);

            // Retrieve the correct output stream
            org.apache.tools.zip.ZipEntry entry = new org.apache.tools.zip.ZipEntry(RESOURCES_PATH + "packs/pack-" + pack.id);
            primaryJarStream.putNextEntry(entry);
            primaryJarStream.flush(); // flush before we start counting


            ByteCountingOutputStream dos = new ByteCountingOutputStream(outputStream);
            ObjectOutputStream objOut = new ObjectOutputStream(dos);

            // We write the actual pack files
            objOut.writeInt(packInfo.getPackFiles().size());

            for (PackFile packFile : packInfo.getPackFiles())
            {
                boolean addFile = !pack.loose;
                boolean pack200 = false;
                File file = packInfo.getFile(packFile);

                if (file.getName().toLowerCase().endsWith(".jar") && info.isPack200Compression() && isNotSignedJar(file))
                {
                    packFile.setPack200Jar(true);
                    pack200 = true;
                }

                // use a back reference if file was in previous pack, and in
                // same jar
                Object[] info = storedFiles.get(file);
                if (info != null && !packJarsSeparate)
                {
                    packFile.setPreviousPackFileRef((String) info[0], (Long) info[1]);
                    addFile = false;
                }

                objOut.writeObject(packFile); // base info

                if (addFile && !packFile.isDirectory())
                {
                    long pos = dos.getByteCount(); // get the position

                    if (pack200)
                    {
                        /*
                         * Warning!
                         * 
                         * Pack200 archives must be stored in separated streams, as the Pack200 unpacker
                         * reads the entire stream...
                         *
                         * See http://java.sun.com/javase/6/docs/api/java/util/jar/Pack200.Unpacker.html
                         */
                        pack200Map.put(pack200Counter, file);
                        objOut.writeInt(pack200Counter);
                        pack200Counter = pack200Counter + 1;
                    }
                    else
                    {
                        FileInputStream inStream = new FileInputStream(file);
                        long bytesWritten = IoHelper.copyStream(inStream, objOut);
                        inStream.close();
                        if (bytesWritten != packFile.length())
                        {
                            throw new IOException("File size mismatch when reading " + file);
                        }
                    }

                    storedFiles.put(file, new Object[]{pack.id, pos});
                }

                // even if not written, it counts towards pack size
                pack.nbytes += packFile.size();
            }

            // Write out information about parsable files
            objOut.writeInt(packInfo.getParsables().size());

            for (ParsableFile parsableFile : packInfo.getParsables())
            {
                objOut.writeObject(parsableFile);
            }

            // Write out information about executable files
            objOut.writeInt(packInfo.getExecutables().size());
            for (ExecutableFile executableFile : packInfo.getExecutables())
            {
                objOut.writeObject(executableFile);
            }

            // Write out information about updatecheck files
            objOut.writeInt(packInfo.getUpdateChecks().size());
            for (UpdateCheck updateCheck : packInfo.getUpdateChecks())
            {
                objOut.writeObject(updateCheck);
            }

            // Cleanup
            objOut.flush();
            if (!compressor.useStandardCompression())
            {
                outputStream.close();
            }

            primaryJarStream.closeEntry();

            // close pack specific jar if required
            if (packJarsSeparate)
            {
                primaryJarStream.closeAlways();
            }

            IXMLElement child = new XMLElementImpl("pack", root);
            child.setAttribute("nbytes", Long.toString(pack.nbytes));
            child.setAttribute("name", pack.name);
            if (pack.id != null)
            {
                child.setAttribute("id", pack.id);
            }
            root.addChild(child);

            packNumber++;
        }

        // Now that we know sizes, write pack metadata to primary jar.
        primaryJarStream.putNextEntry(new org.apache.tools.zip.ZipEntry(RESOURCES_PATH + "packs.info"));
        ObjectOutputStream out = new ObjectOutputStream(primaryJarStream);
        out.writeInt(packsList.size());

        for (PackInfo packInfo : packsList)
        {
            out.writeObject(packInfo.getPack());
        }
        out.flush();
        primaryJarStream.closeEntry();

        // Pack200 files
        Pack200.Packer packer = createAgressivePack200Packer();
        for (Integer key : pack200Map.keySet())
        {
            File file = pack200Map.get(key);
            primaryJarStream.putNextEntry(new org.apache.tools.zip.ZipEntry(RESOURCES_PATH + "packs/pack200-" + key));
            JarFile jar = new JarFile(file);
            packer.pack(jar, primaryJarStream);
            jar.close();
            primaryJarStream.closeEntry();
        }
    }

    private Pack200.Packer createAgressivePack200Packer()
    {
        Pack200.Packer packer = Pack200.newPacker();
        Map<String, String> packerProperties = packer.properties();
        packerProperties.put(Pack200.Packer.EFFORT, "9");
        packerProperties.put(Pack200.Packer.SEGMENT_LIMIT, "-1");
        packerProperties.put(Pack200.Packer.KEEP_FILE_ORDER, Pack200.Packer.FALSE);
        packerProperties.put(Pack200.Packer.DEFLATE_HINT, Pack200.Packer.FALSE);
        packerProperties.put(Pack200.Packer.MODIFICATION_TIME, Pack200.Packer.LATEST);
        packerProperties.put(Pack200.Packer.CODE_ATTRIBUTE_PFX + "LineNumberTable", Pack200.Packer.STRIP);
        packerProperties.put(Pack200.Packer.CODE_ATTRIBUTE_PFX + "LocalVariableTable", Pack200.Packer.STRIP);
        packerProperties.put(Pack200.Packer.CODE_ATTRIBUTE_PFX + "SourceFile", Pack200.Packer.STRIP);
        return packer;
    }

    private boolean isNotSignedJar(File file) throws IOException
    {
        JarFile jar = new JarFile(file);
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements())
        {
            JarEntry entry = entries.nextElement();
            if (entry.getName().startsWith("META-INF") && entry.getName().endsWith(".SF"))
            {
                jar.close();
                return false;
            }
        }
        jar.close();
        return true;
    }

    /**
     * ********************************************************************************************
     * Stream utilites for creation of the installer.
     * ********************************************************************************************
     */

    /* (non-Javadoc)
    * @see com.izforge.izpack.compiler.packager.IPackager#addConfigurationInformation(com.izforge.izpack.api.adaptator.IXMLElement)
    */
    public void addConfigurationInformation(IXMLElement data)
    {
        // TODO Auto-generated method stub

    }
}
