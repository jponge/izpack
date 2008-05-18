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
package com.izforge.izpack.installer;

import com.izforge.izpack.*;
import com.izforge.izpack.event.InstallerListener;
import com.izforge.izpack.io.CorruptVolumeException;
import com.izforge.izpack.io.FileSpanningInputStream;
import com.izforge.izpack.io.FileSpanningOutputStream;
import com.izforge.izpack.io.VolumeNotFoundException;
import com.izforge.izpack.panels.NextMediaDialog;
import com.izforge.izpack.util.*;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;


/**
 * Unpacker class for a multi volume installation.
 *
 * @author Dennis Reil, <izpack@reil-online.de>
 */
public class MultiVolumeUnpacker extends UnpackerBase
{
    public MultiVolumeUnpacker(AutomatedInstallData idata, AbstractUIProgressHandler handler)
    {
        super(idata, handler);
    }

    protected File enterNextMediaMessage(String volumename, boolean lastcorrupt)
    {
        if (lastcorrupt)
        {
            Component parent = null;
            if ((this.handler != null) && (this.handler instanceof IzPanel))
            {
                parent = ((IzPanel) this.handler).getInstallerFrame();
            }
            JOptionPane.showMessageDialog(parent, idata.langpack
                    .getString("nextmedia.corruptmedia"), idata.langpack
                    .getString("nextmedia.corruptmedia.title"), JOptionPane.ERROR_MESSAGE);
        }
        Debug.trace("Enter next media: " + volumename);

        File nextvolume = new File(volumename);
        NextMediaDialog nmd = null;

        while (!nextvolume.exists() || lastcorrupt)
        {
            if ((this.handler != null) && (this.handler instanceof IzPanel))
            {
                InstallerFrame installframe = ((IzPanel) this.handler).getInstallerFrame();
                nmd = new NextMediaDialog(installframe, idata, volumename);
            }
            else
            {
                nmd = new NextMediaDialog(null, idata, volumename);
            }
            nmd.setVisible(true);
            String nextmediainput = nmd.getNextMedia();
            if (nextmediainput != null)
            {
                nextvolume = new File(nextmediainput);
            }
            else
            {
                Debug.trace("Input from NextMediaDialog was null");
                nextvolume = new File(volumename);
            }
            // selection equal to last selected which was corrupt?
            if (!(volumename.equals(nextvolume.getAbsolutePath()) && lastcorrupt))
            {
                lastcorrupt = false;
            }
        }
        return nextvolume;
    }

    protected File enterNextMediaMessage(String volumename)
    {
        return enterNextMediaMessage(volumename, false);
    }

    /**
     * The run method.
     */
    public void run()
    {
        addToInstances();
        try
        {
            //
            // Initialisations
            FileOutputStream out = null;
            ArrayList<ParsableFile> parsables = new ArrayList<ParsableFile>();
            ArrayList<ExecutableFile> executables = new ArrayList<ExecutableFile>();
            ArrayList<UpdateCheck> updatechecks = new ArrayList<UpdateCheck>();
            List packs = idata.selectedPacks;
            int npacks = packs.size();
            Debug.trace("Unpacker starting");
            handler.startAction("Unpacking", npacks);
            udata = UninstallData.getInstance();
            // Custom action listener stuff --- load listeners ----
            List[] customActions = getCustomActions();
            // Custom action listener stuff --- beforePacks ----
            informListeners(customActions, InstallerListener.BEFORE_PACKS, idata, npacks, handler);
            // vs = new VariableSubstitutor(idata.getVariables());
            packs = idata.selectedPacks;
            npacks = packs.size();
            if (npacks == 0)
            {
                if (performInterrupted())
                { // Interrupt was initiated; perform it.
                    return;
                }

                // Custom action listener stuff --- afterPacks ----
                informListeners(customActions, InstallerListener.AFTER_PACKS, idata, handler, null);
                if (performInterrupted())
                { // Interrupt was initiated; perform it.
                    return;
                }

                // The end :-)
                handler.stopAction();
                return;
            }
            InputStream in = MultiVolumeUnpacker.class
                    .getResourceAsStream(FileSpanningOutputStream.VOLUMES_INFO);
            // get volumes metadata
            ObjectInputStream metadataobj = new ObjectInputStream(in);
            // TODO: create MetadataObject
            int volumes = metadataobj.readInt();
            String volumename = metadataobj.readUTF();
            Debug.trace("Reading from " + volumes + " volumes with basename " + volumename + " ");
            metadataobj.close();
            String mediadirectory = MultiVolumeInstaller.getMediadirectory();
            if ((mediadirectory == null) || (mediadirectory.length() <= 0))
            {
                Debug.trace("Mediadirectory wasn't set.");
                mediadirectory = System.getProperty("java.io.tmpdir"); // try the temporary
                // directory
            }
            Debug.trace("Using mediadirectory = " + mediadirectory);
            File volume = new File(mediadirectory + File.separator + volumename);
            if (!volume.exists())
            {
                volume = enterNextMediaMessage(volume.getAbsolutePath());
            }
            FileSpanningInputStream fin = new FileSpanningInputStream(volume, volumes);

            // We unpack the selected packs
            for (int i = 0; i < npacks; i++)
            {
                // We get the pack stream
                int n = idata.allPacks.indexOf(packs.get(i));

                in = MultiVolumeUnpacker.class.getResourceAsStream("/packs/pack" + n);

                // Custom action listener stuff --- beforePack ----
                informListeners(customActions, InstallerListener.BEFORE_PACK, packs.get(i),
                        npacks, handler);
                // find next Entry
                ObjectInputStream objIn = new ObjectInputStream(in);
                // We unpack the files
                int nfiles = objIn.readInt();

                // We get the internationalized name of the pack
                final Pack pack = ((Pack) packs.get(i));
                // evaluate condition
                if (pack.hasCondition() && (rules != null))
                {
                    if (!rules.isConditionTrue(pack.getCondition()))
                    {
                        // skip pack, condition is not fullfilled.
                        continue;
                    }
                }
                String stepname = pack.name;// the message to be passed to the
                // installpanel
                if (langpack != null && !(pack.id == null || "".equals(pack.id)))
                {

                    final String name = langpack.getString(pack.id);
                    if (name != null && !"".equals(name))
                    {
                        stepname = name;
                    }
                }
                handler.nextStep(stepname, i + 1, nfiles);
                for (int j = 0; j < nfiles; j++)
                {
                    // We read the header
                    XPackFile pf = (XPackFile) objIn.readObject();
                    if (pf.hasCondition() && (rules != null))
                    {
                        if (!rules.isConditionTrue(pf.getCondition()))
                        {
                            // skip file, condition is false
                            continue;
                        }
                    }
                    if (OsConstraint.oneMatchesCurrentSystem(pf.osConstraints()))
                    {
                        // We translate & build the path
                        String path = IoHelper.translatePath(pf.getTargetPath(), vs);
                        File pathFile = new File(path);
                        File dest = pathFile;
                        if (!pf.isDirectory())
                        {
                            dest = pathFile.getParentFile();
                        }

                        if (!dest.exists())
                        {
                            // If there are custom actions which would be called
                            // at
                            // creating a directory, create it recursively.
                            List fileListeners = customActions[customActions.length - 1];
                            if (fileListeners != null && fileListeners.size() > 0)
                            {
                                mkDirsWithEnhancement(dest, pf, customActions);
                            }
                            else
                            // Create it in on step.
                            {
                                if (!dest.mkdirs())
                                {
                                    handler.emitError("Error creating directories",
                                            "Could not create directory\n" + dest.getPath());
                                    handler.stopAction();
                                    this.result = false;
                                    return;
                                }
                            }
                        }

                        if (pf.isDirectory())
                        {
                            continue;
                        }

                        // Custom action listener stuff --- beforeFile ----
                        informListeners(customActions, InstallerListener.BEFORE_FILE, pathFile, pf,
                                null);
                        // We add the path to the log,
                        udata.addFile(path, pack.uninstall);

                        handler.progress(j, path);

                        // if this file exists and should not be overwritten,
                        // check
                        // what to do
                        if ((pathFile.exists()) && (pf.override() != PackFile.OVERRIDE_TRUE))
                        {
                            boolean overwritefile = false;

                            // don't overwrite file if the user said so
                            if (pf.override() != PackFile.OVERRIDE_FALSE)
                            {
                                if (pf.override() == PackFile.OVERRIDE_TRUE)
                                {
                                    overwritefile = true;
                                }
                                else if (pf.override() == PackFile.OVERRIDE_UPDATE)
                                {
                                    // check mtime of involved files
                                    // (this is not 100% perfect, because the
                                    // already existing file might
                                    // still be modified but the new installed
                                    // is just a bit newer; we would
                                    // need the creation time of the existing
                                    // file or record with which mtime
                                    // it was installed...)
                                    overwritefile = (pathFile.lastModified() < pf.lastModified());
                                }
                                else
                                {
                                    int def_choice = -1;

                                    if (pf.override() == PackFile.OVERRIDE_ASK_FALSE)
                                    {
                                        def_choice = AbstractUIHandler.ANSWER_NO;
                                    }
                                    if (pf.override() == PackFile.OVERRIDE_ASK_TRUE)
                                    {
                                        def_choice = AbstractUIHandler.ANSWER_YES;
                                    }

                                    int answer = handler.askQuestion(idata.langpack
                                            .getString("InstallPanel.overwrite.title")
                                            + " - " + pathFile.getName(), idata.langpack
                                            .getString("InstallPanel.overwrite.question")
                                            + pathFile.getAbsolutePath(),
                                            AbstractUIHandler.CHOICES_YES_NO, def_choice);

                                    overwritefile = (answer == AbstractUIHandler.ANSWER_YES);
                                }

                            }

                            if (!overwritefile)
                            {
                                if (!pf.isBackReference() && !((Pack) packs.get(i)).loose)
                                {
                                    // objIn.skip(pf.length());
                                }
                                continue;
                            }

                        }

                        // We copy the file
                        out = new FileOutputStream(pathFile);
                        byte[] buffer = new byte[5120];
                        long bytesCopied = 0;
                        // InputStream pis = objIn;
                        InputStream pis = fin;

                        if (((Pack) packs.get(i)).loose)
                        {
                            pis = new FileInputStream(pf.sourcePath);
                        }

                        // read in the position of this file
                        // long fileposition = objIn.readLong();
                        long fileposition = pf.getArchivefileposition();

                        while (fin.getFilepointer() < fileposition)
                        {
                            // we have to skip some bytes
                            Debug.trace("Skipping bytes to get to file " + pathFile.getName()
                                    + " (" + fin.getFilepointer() + "<" + fileposition
                                    + ") target is: " + (fileposition - fin.getFilepointer()));
                            try
                            {
                                fin.skip(fileposition - fin.getFilepointer());
                                break;
                            }
                            catch (VolumeNotFoundException vnfe)
                            {
                                File nextmedia = enterNextMediaMessage(vnfe.getVolumename());
                                fin.setVolumename(nextmedia.getAbsolutePath());
                            }
                            catch (CorruptVolumeException cve)
                            {
                                Debug.trace("corrupt media found. magic number is not correct");
                                File nextmedia = enterNextMediaMessage(cve.getVolumename(), true);
                                fin.setVolumename(nextmedia.getAbsolutePath());
                            }
                        }

                        if (fin.getFilepointer() > fileposition)
                        {
                            Debug.trace("Error, can't access file in pack.");
                        }

                        while (bytesCopied < pf.length())
                        {
                            try
                            {
                                if (performInterrupted())
                                { // Interrupt was initiated; perform it.
                                    out.close();
                                    if (pis != objIn)
                                    {
                                        pis.close();
                                    }
                                    return;
                                }
                                int maxBytes = (int) Math.min(pf.length() - bytesCopied,
                                        buffer.length);

                                int bytesInBuffer = pis.read(buffer, 0, maxBytes);
                                if (bytesInBuffer == -1)
                                {
                                    Debug.trace("Unexpected end of stream (installer corrupted?)");
                                    throw new IOException(
                                            "Unexpected end of stream (installer corrupted?)");
                                }

                                out.write(buffer, 0, bytesInBuffer);

                                bytesCopied += bytesInBuffer;
                            }
                            catch (VolumeNotFoundException vnfe)
                            {
                                File nextmedia = enterNextMediaMessage(vnfe.getVolumename());
                                fin.setVolumename(nextmedia.getAbsolutePath());
                            }
                            catch (CorruptVolumeException cve)
                            {
                                Debug.trace("corrupt media found. magic number is not correct");
                                File nextmedia = enterNextMediaMessage(cve.getVolumename(), true);
                                fin.setVolumename(nextmedia.getAbsolutePath());
                            }
                        }
                        // Cleanings
                        out.close();
                        // if (pis != objIn) pis.close();

                        // Set file modification time if specified
                        if (pf.lastModified() >= 0)
                        {
                            pathFile.setLastModified(pf.lastModified());
                        }
                        // Custom action listener stuff --- afterFile ----
                        informListeners(customActions, InstallerListener.AFTER_FILE, pathFile, pf,
                                null);
                    }
                    else
                    {
                        if (!pf.isBackReference())
                        {
                            // objIn.skip(pf.length());
                        }
                    }
                }
                // Load information about parsable files
                int numParsables = objIn.readInt();
                Debug.trace("Looking for parsables");
                for (int k = 0; k < numParsables; k++)
                {
                    ParsableFile pf = null;
                    while (true)
                    {
                        try
                        {
                            pf = (ParsableFile) objIn.readObject();
                            break;
                        }
                        catch (VolumeNotFoundException vnfe)
                        {
                            File nextmedia = enterNextMediaMessage(vnfe.getVolumename());
                            fin.setVolumename(nextmedia.getAbsolutePath());
                        }
                        catch (CorruptVolumeException cve)
                        {
                            Debug.trace("corrupt media found. magic number is not correct");
                            File nextmedia = enterNextMediaMessage(cve.getVolumename(), true);
                            fin.setVolumename(nextmedia.getAbsolutePath());
                        }
                        catch (EOFException eofe)
                        {
                            File nextmedia = enterNextMediaMessage("");
                            fin.setVolumename(nextmedia.getAbsolutePath());
                        }
                    }
                    if (pf.hasCondition() && (rules != null))
                    {
                        if (!rules.isConditionTrue(pf.getCondition()))
                        {
                            // skip parsable, condition is false
                            continue;
                        }
                    }
                    pf.path = IoHelper.translatePath(pf.path, vs);
                    Debug.trace("Found parsable: " + pf.path);
                    parsables.add(pf);
                }

                // Load information about executable files
                int numExecutables = objIn.readInt();
                Debug.trace("Looking for executables...");
                for (int k = 0; k < numExecutables; k++)
                {
                    ExecutableFile ef = (ExecutableFile) objIn.readObject();
                    if (ef.hasCondition() && (rules != null))
                    {
                        if (!rules.isConditionTrue(ef.getCondition()))
                        {
                            // skip, condition is false
                            continue;
                        }
                    }
                    ef.path = IoHelper.translatePath(ef.path, vs);
                    if (null != ef.argList && !ef.argList.isEmpty())
                    {
                        String arg = null;
                        for (int j = 0; j < ef.argList.size(); j++)
                        {
                            arg = ef.argList.get(j);
                            arg = IoHelper.translatePath(arg, vs);
                            ef.argList.set(j, arg);
                        }
                    }
                    Debug.trace("Found executable: " + ef.path);
                    executables.add(ef);
                    if (ef.executionStage == ExecutableFile.UNINSTALL)
                    {
                        udata.addExecutable(ef);
                    }
                }
                // Custom action listener stuff --- uninstall data ----
                handleAdditionalUninstallData(udata, customActions);

                // Load information about updatechecks
                int numUpdateChecks = objIn.readInt();
                Debug.trace("Looking for updatechecks");
                for (int k = 0; k < numUpdateChecks; k++)
                {
                    UpdateCheck uc = (UpdateCheck) objIn.readObject();
                    Debug.trace("found updatecheck");
                    updatechecks.add(uc);
                }

                // objIn.close();

                if (performInterrupted())
                { // Interrupt was initiated; perform it.
                    return;
                }

                // Custom action listener stuff --- afterPack ----
                informListeners(customActions, InstallerListener.AFTER_PACK, packs.get(i),
                        i, handler);
            }
            Debug.trace("Trying to parse files");
            // We use the scripts parser
            ScriptParser parser = new ScriptParser(parsables, vs);
            parser.parseFiles();
            Debug.trace("parsed files");
            if (performInterrupted())
            { // Interrupt was initiated; perform it.
                return;
            }
            Debug.trace("Trying to execute files");
            // We use the file executor
            FileExecutor executor = new FileExecutor(executables);
            if (executor.executeFiles(ExecutableFile.POSTINSTALL, handler) != 0)
            {
                handler.emitError("File execution failed", "The installation was not completed");
                this.result = false;
                Debug.trace("File execution failed");
            }

            if (performInterrupted())
            { // Interrupt was initiated; perform it.
                return;
            }
            Debug.trace("Create uninstaller");
            // We put the uninstaller (it's not yet complete...)
            putUninstaller();
            Debug.trace("Uninstaller created");
            // update checks _after_ uninstaller was put, so we don't delete it
            Debug.trace("Perform updateChecks");
            performUpdateChecks(updatechecks);
            Debug.trace("updatechecks performed.");
            if (performInterrupted())
            { // Interrupt was initiated; perform it.
                return;
            }

            // Custom action listener stuff --- afterPacks ----
            informListeners(customActions, InstallerListener.AFTER_PACKS, idata, handler, null);
            if (performInterrupted())
            { // Interrupt was initiated; perform it.
                return;
            }

            // write installation information
            writeInstallationInformation();

            this.writeConfigInformation();
            // The end :-)
            handler.stopAction();
            Debug.trace("Installation complete");
        }
        catch (Exception err)
        {
            // TODO: finer grained error handling with useful error messages
            handler.stopAction();
            handler.emitError("An error occured", err.toString());
            err.printStackTrace();
            Debug.trace("Error while installing: " + err.toString());
            this.result = false;
        }
        finally
        {
            removeFromInstances();
        }
    }

    protected void writeConfigInformation()
    {
        // save the variables
        Properties installerproperties = idata.getVariables();
        Enumeration installerpropertieskeys = installerproperties.keys();
        try
        {
            String installpath = idata.getVariable("INSTALL_PATH");
            PrintWriter pw = new PrintWriter(new FileOutputStream(installpath + File.separator
                    + "installer.properties"));
            pw.println("# Installer properties, written by MultiVolumeUnpacker.");
            while (installerpropertieskeys.hasMoreElements())
            {
                String key = (String) installerpropertieskeys.nextElement();
                if (key.startsWith("SYSTEM_"))
                {
                    // skip
                    continue;
                }
                else if (key.startsWith("password_"))
                {
                    // skip
                    continue;
                }
                pw.println(key + "=" + installerproperties.getProperty(key));
            }
            pw.flush();
            pw.close();
        }
        catch (Exception e)
        {
            Debug.trace("Error while writing config information in MultiVolumeUnpacker: "
                    + e.getMessage());
        }
    }
}