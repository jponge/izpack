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
package com.izforge.izpack.compiler.packager;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.compiler.CompilerException;
import com.izforge.izpack.compiler.container.CompilerContainer;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.compiler.stream.JarOutputStream;
import com.izforge.izpack.data.Pack;
import com.izforge.izpack.data.PackFile;
import com.izforge.izpack.data.PackInfo;
import com.izforge.izpack.data.XPackFile;
import com.izforge.izpack.io.FileSpanningOutputStream;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.FileUtil;
import org.apache.tools.zip.ZipEntry;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

/**
 * The packager class. The packager is used by the compiler to put files into an installer, and
 * create the actual installer files.
 * <p/>
 * This is a packager, which packs everything into multi volumes.
 *
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 */
public class MultiVolumePackager extends PackagerBase {

    public static final String INSTALLER_PAK_NAME = "installer";

    /**
     * Executable zipped output stream. First to open, last to close.
     */
    private JarOutputStream primaryJarStream;


    private IXMLElement configdata = null;
    private CompilerData compilerData;

    /**
     * The constructor.
     *
     * @throws com.izforge.izpack.compiler.CompilerException
     *
     */
    public MultiVolumePackager(Properties properties, CompilerContainer compilerContainer, PackagerListener listener, CompilerData compilerData) throws CompilerException {
        super(properties, compilerContainer, listener);
        this.compilerData = compilerData;
    }

    /**
     * Create the installer, beginning with the specified jar. If the name specified does not end in
     * ".jar", it is appended. If secondary jars are created for packs (if the Info object added has
     * a webDirURL set), they are created in the same directory, named sequentially by inserting
     * ".pack#" (where '#' is the pack number) ".jar" suffix: e.g. "foo.pack1.jar". If any file
     * exists, it is overwritten.
     */
    public void createInstaller() throws Exception {
        // first analyze the configuration
        this.analyzeConfigurationInformation();

        packJarsSeparate = (info.getWebDirURL() != null);

        sendStart();

        writeInstaller();

        // Pack File Data may be written to separate jars

        String packfile = compilerData.getOutput() + File.separator + INSTALLER_PAK_NAME;
        writePacks(new File(packfile));

        // Finish up. closeAlways is a hack for pack compressions other than
        // default. Some of it (e.g. BZip2) closes the slave of it also.
        // But this should not be because the jar stream should be open
        // for the next pack. Therefore an own JarOutputStream will be used
        // which close method will be blocked.
        // primaryJarStream.closeAlways();
        primaryJarStream.close();

        sendStop();
    }

    /**
     * ********************************************************************************************
     * Listener assistance
     * ********************************************************************************************
     */

    private void analyzeConfigurationInformation() {
        String classname = this.getClass().getName();
        String sizeprop = classname + ".volumesize";
        String freespaceprop = classname + ".firstvolumefreespace";

        if (this.configdata == null) {
            // no configdata given, set default values
            this.variables.setProperty(sizeprop, Long.toString(FileSpanningOutputStream.DEFAULT_VOLUME_SIZE));
            this.variables.setProperty(freespaceprop, Long.toString(FileSpanningOutputStream.DEFAULT_ADDITIONAL_FIRST_VOLUME_FREE_SPACE_SIZE));
        } else {
            // configdata was set
            String volumesize = configdata.getAttribute("volumesize", Long.toString(FileSpanningOutputStream.DEFAULT_VOLUME_SIZE));
            String freespace = configdata.getAttribute("firstvolumefreespace", Long.toString(FileSpanningOutputStream.DEFAULT_ADDITIONAL_FIRST_VOLUME_FREE_SPACE_SIZE));
            this.variables.setProperty(sizeprop, volumesize);
            this.variables.setProperty(freespaceprop, freespace);
        }
    }

    /***********************************************************************************************
     * Private methods used when writing out the installer to jar files.
     **********************************************************************************************/

    /**
     * Write skeleton installer to primary jar. It is just an included jar, except that we copy the
     * META-INF as well.
     */
    protected void writeSkeletonInstaller() throws IOException {
        sendMsg("Copying the skeleton installer", PackagerListener.MSG_VERBOSE);


        InputStream is = MultiVolumePackager.class.getResourceAsStream("/" + getSkeletonSubpath());
        if (is == null) {
            File skeleton = new File(CompilerData.IZPACK_HOME, getSkeletonSubpath());
            is = new FileInputStream(skeleton);
        }
        ZipInputStream inJarStream = new ZipInputStream(is);

        // copy anything except the manifest.mf
        List<String> excludes = new ArrayList<String>();
        excludes.add("META-INF.MANIFEST.MF");
        copyZipWithoutExcludes(inJarStream, primaryJarStream, excludes);

        // ugly code to modify the manifest-file to set MultiVolumeInstaller as main class
        // reopen Stream
        is = MultiVolumePackager.class.getResourceAsStream("/" + getSkeletonSubpath());
        if (is == null) {
            File skeleton = new File(CompilerData.IZPACK_HOME, getSkeletonSubpath());
            is = new FileInputStream(skeleton);
        }
        inJarStream = new ZipInputStream(is);
        boolean found = false;
        java.util.zip.ZipEntry ze;
        String modifiedmanifest = null;
        while (((ze = inJarStream.getNextEntry()) != null) && !found) {
            if ("META-INF/MANIFEST.MF".equals(ze.getName())) {
                long size = ze.getSize();
                byte[] buffer = new byte[4096];
                int readbytes = 0;
                int totalreadbytes = 0;
                StringBuffer manifest = new StringBuffer();
                while (((readbytes = inJarStream.read(buffer)) > 0) && (totalreadbytes < size)) {
                    totalreadbytes += readbytes;
                    String tmp = new String(buffer, 0, readbytes, "utf-8");
                    manifest.append(tmp);
                }


                StringReader stringreader = new StringReader(manifest.toString());
                BufferedReader reader = new BufferedReader(stringreader);
                String line = null;
                StringBuffer modified = new StringBuffer();
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("Main-Class:")) {
                        line = "Main-Class: com.izforge.izpack.installer.MultiVolumeInstaller";
                    }
                    modified.append(line);
                    modified.append("\r\n");
                }
                reader.close();
                modifiedmanifest = modified.toString();
                /*
                System.out.println("Manifest:");
                System.out.println(manifest.toString());
                System.out.println("Modified Manifest:");
                System.out.println(modified.toString());
                */
                break;
            }
        }

        primaryJarStream.putNextEntry(new ZipEntry(RESOURCES_PATH + "META-INF/MANIFEST.MF"));
        primaryJarStream.write(modifiedmanifest.getBytes());
        primaryJarStream.closeEntry();
    }

    /**
     * Write an arbitrary object to primary jar.
     */
    protected void writeInstallerObject(String entryName, Object object) throws IOException {
        primaryJarStream.putNextEntry(new ZipEntry(entryName));
        ObjectOutputStream out = new ObjectOutputStream(primaryJarStream);
        out.writeObject(object);
        out.flush();
        primaryJarStream.closeEntry();
    }

    /**
     * Write the data referenced by URL to primary jar.
     */
    protected void writeInstallerResources() throws IOException {
        sendMsg("Copying " + installerResourceURLMap.size() + " files into installer");

        for (String s : installerResourceURLMap.keySet()) {
            String name = s;
            InputStream in = (installerResourceURLMap.get(name)).openStream();

            org.apache.tools.zip.ZipEntry newEntry = new org.apache.tools.zip.ZipEntry(name);
            long dateTime = FileUtil.getFileDateTime(installerResourceURLMap.get(name));
            if (dateTime != -1) {
                newEntry.setTime(dateTime);
            }
            primaryJarStream.putNextEntry(newEntry);

            PackagerHelper.copyStream(in, primaryJarStream);
            primaryJarStream.closeEntry();
            in.close();
        }
    }

    /**
     * Copy included jars to primary jar.
     */
    protected void writeIncludedJars() throws IOException {
        sendMsg("Merging " + includedJarURLs.size() + " jars into installer");

        for (Object[] includedJarURL : includedJarURLs) {
            Object[] current = includedJarURL;
            InputStream is = ((URL) current[0]).openStream();
            ZipInputStream inJarStream = new ZipInputStream(is);
            PackagerHelper.copyZip(inJarStream, primaryJarStream, (List<String>) current[1], alreadyWrittenFiles);
        }
    }

    /**
     * Write Packs to primary jar or each to a separate jar.
     */
    private void writePacks(File primaryfile) throws Exception {

        final int num = packsList.size();
        sendMsg("Writing " + num + " Pack" + (num > 1 ? "s" : "") + " into installer");
        Debug.trace("Writing " + num + " Pack" + (num > 1 ? "s" : "") + " into installer");
        // Map to remember pack number and bytes offsets of back references
        Map storedFiles = new HashMap();

        // First write the serialized files and file metadata data for each pack
        // while counting bytes.

        String classname = this.getClass().getName();
        String volumesize = this.getVariables().getProperty(classname + ".volumesize");
        String extraspace = this.getVariables().getProperty(classname + ".firstvolumefreespace");

        long volumesizel = FileSpanningOutputStream.DEFAULT_VOLUME_SIZE;
        long extraspacel = FileSpanningOutputStream.DEFAULT_ADDITIONAL_FIRST_VOLUME_FREE_SPACE_SIZE;

        if (volumesize != null) {
            volumesizel = Long.parseLong(volumesize);
        }
        if (extraspace != null) {
            extraspacel = Long.parseLong(extraspace);
        }
        Debug.trace("Volumesize: " + volumesizel);
        Debug.trace("Extra space on first volume: " + extraspacel);
        FileSpanningOutputStream fout = new FileSpanningOutputStream(primaryfile.getParent()
                + File.separator + primaryfile.getName() + ".pak", volumesizel);
        fout.setFirstvolumefreespacesize(extraspacel);

        int packNumber = 0;
        for (PackInfo aPacksList : packsList) {
            PackInfo packInfo = aPacksList;
            Pack pack = packInfo.getPack();
            pack.nbytes = 0;

            sendMsg("Writing Pack " + packNumber + ": " + pack.name, PackagerListener.MSG_VERBOSE);
            Debug.trace("Writing Pack " + packNumber + ": " + pack.name);
            ZipEntry entry = new ZipEntry(RESOURCES_PATH + "packs/pack" + packNumber);
            // write the metadata as uncompressed object stream to primaryJarStream
            // first write a packs entry

            primaryJarStream.putNextEntry(entry);
            ObjectOutputStream objOut = new ObjectOutputStream(primaryJarStream);

            // We write the actual pack files
            objOut.writeInt(packInfo.getPackFiles().size());

            Iterator iter = packInfo.getPackFiles().iterator();
            for (Object o : packInfo.getPackFiles()) {
                boolean addFile = !pack.loose;
                PackFile packfile = (PackFile) o;
                XPackFile pf = new XPackFile(packfile);
                File file = packInfo.getFile(packfile);
                Debug.trace("Next file: " + file.getAbsolutePath());
                // use a back reference if file was in previous pack, and in
                // same jar
                Object[] info = (Object[]) storedFiles.get(file);
                if (info != null && !packJarsSeparate) {
                    Debug.trace("File already included in other pack");
                    pf.setPreviousPackFileRef((String) info[0], (Long) info[1]);
                    addFile = false;
                }

                if (addFile && !pf.isDirectory()) {
                    long pos = fout.getFilepointer();

                    pf.setArchivefileposition(pos);

                    // write out the filepointer
                    int volumecountbeforewrite = fout.getVolumeCount();

                    FileInputStream inStream = new FileInputStream(file);
                    long bytesWritten = PackagerHelper.copyStream(inStream, fout);
                    fout.flush();

                    long posafterwrite = fout.getFilepointer();
                    Debug.trace("File (" + pf.sourcePath + ") " + pos + " <-> " + posafterwrite);

                    if (fout.getFilepointer() != (pos + bytesWritten)) {
                        Debug.trace("file: " + file.getName());
                        Debug.trace("(Filepos/BytesWritten/ExpectedNewFilePos/NewFilePointer) ("
                                + pos + "/" + bytesWritten + "/" + (pos + bytesWritten)
                                + "/" + fout.getFilepointer() + ")");
                        Debug.trace("Volumecount (before/after) ("
                                + volumecountbeforewrite + "/" + fout.getVolumeCount() + ")");
                        throw new IOException("Error new filepointer is illegal");
                    }

                    if (bytesWritten != pf.length()) {
                        throw new IOException(
                                "File size mismatch when reading " + file);
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
            // Write out information about parsable files
            objOut.writeInt(packInfo.getParsables().size());
            iter = packInfo.getParsables().iterator();
            while (iter.hasNext()) {

                objOut.writeObject(iter.next());
            }

            // Write out information about executable files
            objOut.writeInt(packInfo.getExecutables().size());
            iter = packInfo.getExecutables().iterator();
            while (iter.hasNext()) {
                objOut.writeObject(iter.next());
            }

            // Write out information about updatecheck files
            objOut.writeInt(packInfo.getUpdateChecks().size());
            iter = packInfo.getUpdateChecks().iterator();
            while (iter.hasNext()) {
                objOut.writeObject(iter.next());
            }

            // Cleanup
            objOut.flush();
            packNumber++;
        }

        // write metadata for reading in volumes
        int volumes = fout.getVolumeCount();
        Debug.trace("Written " + volumes + " volumes");
        String volumename = primaryfile.getName() + ".pak";

        fout.flush();
        fout.close();

        primaryJarStream.putNextEntry(new ZipEntry(RESOURCES_PATH + "volumes.info"));
        ObjectOutputStream out = new ObjectOutputStream(primaryJarStream);
        out.writeInt(volumes);
        out.writeUTF(volumename);
        out.flush();
        primaryJarStream.closeEntry();

        // Now that we know sizes, write pack metadata to primary jar.
        primaryJarStream.putNextEntry(new ZipEntry(RESOURCES_PATH + "packs.info"));
        out = new ObjectOutputStream(primaryJarStream);
        out.writeInt(packsList.size());

        for (PackInfo aPacksList : packsList) {
            PackInfo pack = aPacksList;
            out.writeObject(pack.getPack());
        }
        out.flush();
        primaryJarStream.closeEntry();
    }

    /***********************************************************************************************
     * Stream utilites for creation of the installer.
     **********************************************************************************************/

    /**
     * Copies specified contents of one jar to another without the specified files
     * <p/>
     * <p/>
     * TODO: it would be useful to be able to keep signature information from signed jar files, can
     * we combine manifests and still have their content signed?
     */
    private void copyZipWithoutExcludes(ZipInputStream zin, JarOutputStream out, List<String> excludes) throws IOException {
        java.util.zip.ZipEntry zentry;
        if (!alreadyWrittenFiles.containsKey(out)) {
            alreadyWrittenFiles.put(out, new HashSet<String>());
        }
        HashSet<String> currentSet = alreadyWrittenFiles.get(out);
        while ((zentry = zin.getNextEntry()) != null) {
            String currentName = zentry.getName();
            String testName = currentName.replace('/', '.');
            testName = testName.replace('\\', '.');
            if (excludes != null) {
                Iterator<String> i = excludes.iterator();
                boolean skip = false;
                while (i.hasNext()) {
                    // Make "excludes" self to support regex.
                    String doExclude = i.next();
                    if (testName.matches(doExclude)) {
                        skip = true;
                        break;
                    }
                }
                if (skip) {
                    continue;
                }
            }
            if (currentSet.contains(currentName)) {
                continue;
            }
            try {
                // Create new entry for zip file.
                ZipEntry newEntry = new ZipEntry(currentName);
                // Get input file date and time.
                long fileTime = zentry.getTime();
                // Make sure there is date and time set.
                if (fileTime != -1) {
                    newEntry.setTime(fileTime); // If found set it into output file.
                }
                out.putNextEntry(newEntry);

                PackagerHelper.copyStream(zin, out);
                out.closeEntry();
                zin.closeEntry();
                currentSet.add(currentName);
            }
            catch (ZipException x) {
                // This avoids any problem that can occur with duplicate
                // directories. for instance all META-INF data in jars
                // unfortunately this do not work with the apache ZipOutputStream...
            }
        }
    }

    public void addConfigurationInformation(IXMLElement data) {
        this.configdata = data;
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.compiler.packager.PackagerBase#writePacks()
     */

    protected void writePacks() throws Exception {
        // TODO Auto-generated method stub

    }

}