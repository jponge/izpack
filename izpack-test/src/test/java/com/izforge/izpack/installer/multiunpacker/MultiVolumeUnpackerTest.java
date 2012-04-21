package com.izforge.izpack.installer.multiunpacker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Blockable;
import com.izforge.izpack.api.data.Info;
import com.izforge.izpack.api.data.OverrideType;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.api.handler.AbstractUIProgressHandler;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.compiler.compressor.DefaultPackCompressor;
import com.izforge.izpack.compiler.compressor.PackCompressor;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.compiler.listener.PackagerListener;
import com.izforge.izpack.compiler.merge.resolve.CompilerPathResolver;
import com.izforge.izpack.compiler.packager.impl.MultiVolumePackager;
import com.izforge.izpack.compiler.stream.JarOutputStream;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;
import com.izforge.izpack.data.PackInfo;
import com.izforge.izpack.installer.data.InstallData;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.merge.MergeManager;
import com.izforge.izpack.merge.resolve.MergeableResolver;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.Librarian;
import com.izforge.izpack.util.Platforms;

/**
 * Tests the {@link MultiVolumeUnpacker}.
 *
 * @author Tim Anderson
 */
public class MultiVolumeUnpackerTest
{
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();


    /**
     * Tests unpacking of multiple volume installation.
     *
     * @throws Exception for any error
     */
    @Test
    public void testUnpack() throws Exception
    {
        File baseDir = temporaryFolder.getRoot();
        File installerJar = new File(baseDir, "installer.jar");
        File installDir = new File(baseDir, "install");

        MultiVolumePackager packager = createPackager(baseDir, installerJar);

        // create some packs
        PackInfo base = new PackInfo("Base pack", "base", "The base package", true, false, null, true);
        File file1 = createFile(baseDir, "file1.dat", 1024);
        File file2 = createFile(baseDir, "file2.dat", 2048);
        File file3 = createFile(baseDir, "file3.dat", 4096);
        addFiles(base, baseDir, file1, file2, file3);

        PackInfo pack1 = new PackInfo("Pack 1", "pack1", "The first pack", true, false, null, true);
        File file4 = createFile(baseDir, "file4.dat", 8192);
        File file5 = createFile(baseDir, "file5.dat", 16384);
        File file6 = createFile(baseDir, "file6.dat", 32768);
        addFiles(pack1, baseDir, file4, file5, file6);

        PackInfo pack2 = new PackInfo("Pack 2", "pack2", "The second pack", true, false, null, true);
        File file7 = createFile(baseDir, "file7.dat", 65536);
        File file8 = createFile(baseDir, "file8.dat", 131072);
        File file9 = createFile(baseDir, "file9.dat", 262144);
        addFiles(pack2, baseDir, file7, file8, file9);

        long maxVolumeSize = 100000;
        long freeSpace = 60000;
        packager.setMaxVolumeSize(maxVolumeSize);
        packager.setFirstVolumeFreeSpace(freeSpace);

        packager.addPack(base);
        packager.addPack(pack1);
        packager.addPack(pack2);

        // package the installer
        packager.createInstaller();

        // verify the installer exists
        assertTrue(installerJar.exists());

        // verify the installer volumes have been created
        ResourceManager resources = createResourceManager(installerJar);
        checkVolumes(baseDir, resources, maxVolumeSize, freeSpace);

        // unpack the installer
        AutomatedInstallData installData = createInstallData(baseDir, installDir, resources);
        MultiVolumeUnpacker unpacker = createUnpacker(resources, installData);
        unpacker.unpack();

        // verify the expected files exists in the installation directory
        checkFile(installDir, file1);
        checkFile(installDir, file2);
        checkFile(installDir, file3);
        checkFile(installDir, file4);
        checkFile(installDir, file5);
        checkFile(installDir, file6);
        checkFile(installDir, file7);
        checkFile(installDir, file8);
        checkFile(installDir, file9);
    }

    /**
     * Verifies that there are multiple volumes of the expected size.
     *
     * @param baseDir       the directory to find the volumes in
     * @param resources     the resources used to determine the volume name and count
     * @param maxVolumeSize the maximum volume size
     * @param freeSpace     the first volume free space size
     * @throws IOException for any I/O error
     */
    private void checkVolumes(File baseDir, ResourceManager resources, long maxVolumeSize, long freeSpace)
            throws IOException
    {
        // get the volume information
        ObjectInputStream info = new ObjectInputStream(resources.getInputStream(MultiVolumeUnpacker.VOLUMES_INFO));
        int count = info.readInt();
        String name = info.readUTF();
        info.close();
        assertTrue(count >= 1);

        // verify the primary volume exists, with the expected size
        File volume = new File(baseDir, name);
        assertTrue(volume.exists());
        assertEquals(maxVolumeSize - freeSpace, volume.length());

        // check the existence and size of the remaining volumes
        for (int i = 1; i < count; ++i)
        {
            volume = new File(baseDir, name + "." + i);
            assertTrue(volume.exists());
            if (i < count - 1)
            {
                // can't check the size of the last volume
                assertEquals(maxVolumeSize, volume.length());
            }
        }
    }

    /**
     * Creates a new unpacker.
     *
     * @param resources   the resources
     * @param installData the installation data
     * @return a new unpacker
     */
    private MultiVolumeUnpacker createUnpacker(ResourceManager resources, AutomatedInstallData installData)
    {
        VariableSubstitutor replacer = new VariableSubstitutorImpl(installData.getVariables());
        Housekeeper housekeeper = Mockito.mock(Housekeeper.class);
        RulesEngine rules = Mockito.mock(RulesEngine.class);
        UninstallData uninstallData = new UninstallData();
        Librarian librarian = Mockito.mock(Librarian.class);
        MultiVolumeUnpacker unpacker = new MultiVolumeUnpacker(installData, resources, rules, replacer, uninstallData,
                                                               Platforms.WINDOWS, librarian, housekeeper);
        unpacker.setHandler(Mockito.mock(AbstractUIProgressHandler.class));
        return unpacker;
    }

    /**
     * Creates the installation data.
     *
     * @param mediaDir   the directory to locate volumes
     * @param installDir the installation directory
     * @param resources  the resources
     * @return the installation data
     * @throws IOException            for any I/O error
     * @throws ClassNotFoundException
     */
    private AutomatedInstallData createInstallData(File mediaDir, File installDir, ResourceManager resources)
            throws IOException, ClassNotFoundException
    {
        AutomatedInstallData installData = new InstallData(new Properties());

        installData.setInstallPath(installDir.getPath());
        installData.setMediaPath(mediaDir.getPath());
        installData.setInfo(new Info());
        List<Pack> packs = getPacks(resources);
        installData.setAvailablePacks(packs);
        installData.setSelectedPacks(packs);
        return installData;
    }

    /**
     * Creates a new {@link ResourceManager} that reads resources from the supplied jar.
     *
     * @param installerJar the installer jar.
     * @return a new resource manager
     * @throws IOException for any I/O error
     */
    private ResourceManager createResourceManager(File installerJar) throws IOException
    {
        final URLClassLoader loader = new URLClassLoader(new URL[]{installerJar.toURI().toURL()},
                                                         getClass().getClassLoader());
        return new ResourceManager(new Properties(), loader);
    }

    /**
     * Creates a {@link MultiVolumePackager}.
     *
     * @param baseDir      the base directory
     * @param installerJar the jar to create
     * @return a new packager
     * @throws IOException for any I/O error
     */
    private MultiVolumePackager createPackager(File baseDir, File installerJar) throws IOException
    {
        Properties properties = new Properties();
        PackagerListener packagerListener = Mockito.mock(PackagerListener.class);
        JarOutputStream jar = new JarOutputStream(installerJar);
        MergeManager mergeManager = Mockito.mock(MergeManager.class);
        CompilerPathResolver resolver = Mockito.mock(CompilerPathResolver.class);
        MergeableResolver mergeableResolver = Mockito.mock(MergeableResolver.class);
        PackCompressor compressor = new DefaultPackCompressor(new VariableSubstitutorImpl(properties));
        CompilerData data = new CompilerData(null, baseDir.getPath(), installerJar.getPath(), true);
        MultiVolumePackager packager = new MultiVolumePackager(properties, packagerListener, jar, mergeManager,
                                                               resolver, mergeableResolver, compressor, data);
        packager.setInfo(new Info());
        return packager;
    }

    /**
     * Helper to add files to a pack.
     *
     * @param pack    the pack to add the files to
     * @param baseDir the base directory
     * @param files   the files to add
     * @throws IOException for any I/O error
     */
    private void addFiles(PackInfo pack, File baseDir, File... files) throws IOException
    {
        for (File file : files)
        {
            pack.addFile(baseDir, file, "$INSTALL_PATH/" + file.getName(), null, OverrideType.OVERRIDE_FALSE, null,
                         Blockable.BLOCKABLE_NONE, null, null);
        }
    }

    /**
     * Helper to read the pack meta-data.
     *
     * @param resources the resources
     * @return the pack meta-data
     * @throws IOException            for any I/O error
     * @throws ClassNotFoundException if the class of a serialized object cannot be found
     */
    private List<Pack> getPacks(ResourceManager resources) throws IOException, ClassNotFoundException
    {
        // We read the packs data
        InputStream in = resources.getInputStream("packs.info");
        ObjectInputStream objIn = new ObjectInputStream(in);
        int size = objIn.readInt();
        List<Pack> packs = new ArrayList<Pack>();

        for (int i = 0; i < size; i++)
        {
            Pack pack = (Pack) objIn.readObject();
            packs.add(pack);
        }
        objIn.close();
        return packs;
    }

    /**
     * Helper to create a file with the specified size containing random data.
     *
     * @param baseDir the base directory
     * @param name    the file name
     * @param size    the file size
     * @return a new file
     * @throws IOException for any I/O error
     */
    private File createFile(File baseDir, String name, int size) throws IOException
    {
        File file = new File(baseDir, name);
        byte[] data = new byte[size];
        Random random = new Random();
        random.nextBytes(data);
        FileOutputStream stream = new FileOutputStream(file);
        stream.write(data);
        return file;
    }

    /**
     * Verifies a file has been installed.
     *
     * @param installDir the installation directory
     * @param expected   the file that should be installed
     * @throws IOException for any I/O error
     */
    private void checkFile(File installDir, File expected) throws IOException
    {
        File file = new File(installDir, expected.getName());
        assertTrue(file.exists());
        assertFalse(file.getAbsolutePath().equals(expected.getAbsolutePath()));
        assertEquals(FileUtils.checksumCRC32(expected), FileUtils.checksumCRC32(file));
    }

}
