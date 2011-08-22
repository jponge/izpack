/*
 * $Id$
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2001 Johannes Lehtinen
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

package com.izforge.izpack.installer.unpacker;

import com.izforge.izpack.api.data.*;
import com.izforge.izpack.api.event.InstallerListener;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.data.ExecutableFile;
import com.izforge.izpack.data.ParsableFile;
import com.izforge.izpack.data.UpdateCheck;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.web.WebAccessor;
import com.izforge.izpack.installer.web.WebRepositoryAccessor;
import com.izforge.izpack.util.*;
import com.izforge.izpack.util.os.FileQueue;

import java.io.*;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Pack200;

/**
 * Unpacker class.
 *
 * @author Julien Ponge
 * @author Johannes Lehtinen
 */
public class Unpacker extends UnpackerBase
{
    private static final String tempSubPath = "/IzpackWebTemp";

    private Pack200.Unpacker unpacker;


    /**
     * The constructor.
     *
     * @param variableSubstitutor
     * @param udata
     * @param idata               The installation data.
     */
    public Unpacker(AutomatedInstallData idata, ResourceManager resourceManager, RulesEngine rules, VariableSubstitutor variableSubstitutor, UninstallData udata)
    {
        super(idata, resourceManager, rules, variableSubstitutor, udata);
    }

    /* (non-Javadoc)
    * @see com.izforge.izpack.installer.IUnpacker#run()
    */

    public void run()
    {
        addToInstances();
        try
        {
            //
            // Initialisations
            FileOutputStream out;
            FileQueue fq = null;
            ArrayList<ParsableFile> parsables = new ArrayList<ParsableFile>();
            ArrayList<ExecutableFile> executables = new ArrayList<ExecutableFile>();
            ArrayList<UpdateCheck> updatechecks = new ArrayList<UpdateCheck>();
            List<Pack> packs = idata.getSelectedPacks();
            int npacks = packs.size();
            handler.startAction("Unpacking", npacks);
            // Custom action listener stuff --- load listeners ----
            List<InstallerListener> customActions = idata.getInstallerListener();
            // Custom action listener stuff --- beforePacks ----
            informListeners(customActions, InstallerListener.BEFORE_PACKS, idata, npacks, handler);
            packs = idata.getSelectedPacks();
            npacks = packs.size();

            // We unpack the selected packs
            for (int i = 0; i < npacks; i++)
            {
                // We get the pack stream
                //int n = installData.allPacks.indexOf(packs.get(i));
                Pack p = packs.get(i);

                // evaluate condition
                if (p.hasCondition())
                {
                    if (rules != null)
                    {
                        if (!rules.isConditionTrue(p.getCondition()))
                        {
                            // skip pack, condition is not fullfilled.
                            continue;
                        }
                    }
                    else
                    {
                        // TODO: skip pack, because condition can not be checked
                    }
                }

                // Custom action listener stuff --- beforePack ----
                informListeners(customActions, InstallerListener.BEFORE_PACK, packs.get(i),
                        npacks, handler);
                ObjectInputStream objIn = new ObjectInputStream(getPackAsStream(p.id, p.uninstall));

                // We unpack the files
                int nfiles = objIn.readInt();

                // We get the internationalized name of the pack
                final Pack pack = (packs.get(i));
                String stepname = pack.name;// the message to be passed to the

                // installpanel
                if (!(pack.id == null || "".equals(pack.id)))
                {
                    final String name = idata.getLangpack().getString(pack.id);
                    if (name != null && !"".equals(name))
                    {
                        stepname = name;
                    }
                }
                if (pack.isHidden())
                {
                    // TODO: hide the pack completely
                    // hide the pack name if pack is hidden
                    stepname = "";
                }
                handler.nextStep(stepname, i + 1, nfiles);
                for (int j = 0; j < nfiles; j++)
                {
                    // We read the header
                    PackFile pf = (PackFile) objIn.readObject();
                    // TODO: reaction if condition can not be checked
                    if (pf.hasCondition() && (rules != null))
                    {
                        if (!rules.isConditionTrue(pf.getCondition()))
                        {
                            if (!pf.isBackReference())
                            {
                                // skip, condition is not fulfilled
                                objIn.skip(pf.length());
                            }
                            continue;
                        }
                    }
                    if (OsConstraintHelper.oneMatchesCurrentSystem(pf.osConstraints()))
                    {
                        // We translate & build the path
                        String path = IoHelper.translatePath(pf.getTargetPath(), variableSubstitutor);
                        File pathFile = new File(path);
                        File dest = pathFile;
                        if (!pf.isDirectory())
                        {
                            dest = pathFile.getParentFile();
                        }

                        handleMkDirs(pf, dest);

                        // Add path to the log
                        udata.addFile(path, pack.uninstall);

                        if (pf.isDirectory())
                        {
                            continue;
                        }

                        // Custom action listener stuff --- beforeFile ----
                        informListeners(customActions, InstallerListener.BEFORE_FILE, pathFile, pf,
                                null);

                        handler.progress(j, path);

                        // if this file exists and should not be overwritten,
                        // check
                        // what to do
                        if ((pathFile.exists()) && (pf.override() != OverrideType.OVERRIDE_TRUE))
                        {
                            if (!isOverwriteFile(pf, pathFile))
                            {
                                if (!pf.isBackReference() && !(packs.get(i)).loose)
                                {
                                    if (pf.isPack200Jar())
                                    {
                                        objIn.skip(Integer.SIZE / 8);
                                    }
                                    else
                                    {
                                        objIn.skip(pf.length());
                                    }
                                }
                                continue;
                            }

                        }

                        handleOverrideRename(pf, pathFile);

                        // We copy the file
                        InputStream pis = objIn;
                        if (pf.isBackReference())
                        {
                            InputStream is = getPackAsStream(pf.previousPackId, pack.uninstall);
                            pis = new ObjectInputStream(is);
                            // must wrap for blockdata use by objectstream
                            // (otherwise strange result)
                            // skip on underlaying stream (for some reason not
                            // possible on ObjectStream)
                            is.skip(pf.offsetInPreviousPack - 4);
                            // but the stream header is now already read (== 4
                            // bytes)
                        }
                        else if ((packs.get(i)).loose)
                        {
                            /* Old way of doing the job by using the (absolute) sourcepath.
                            * Since this is very likely to fail and does not confirm to the documentation,
                            * prefer using relative path's
                           pis = new FileInputStream(pf.sourcePath);
                            */

                            File resolvedFile = new File(getAbsolutInstallSource(), pf
                                    .getRelativeSourcePath());
                            if (!resolvedFile.exists())
                            {
                                //try alternative destination - the current working directory
                                //user.dir is likely (depends on launcher type) the current directory of the executable or jar-file...
                                final File userDir = new File(System.getProperty("user.dir"));
                                resolvedFile = new File(userDir, pf.getRelativeSourcePath());
                            }
                            if (resolvedFile.exists())
                            {
                                pis = new FileInputStream(resolvedFile);
                                //may have a different length & last modified than we had at compiletime, therefore we have to build a new PackFile for the copy process...
                                pf = new PackFile(resolvedFile.getParentFile(), resolvedFile, pf.getTargetPath(), pf.osConstraints(), pf.override(), pf.overrideRenameTo(), pf.blockable(), pf.getAdditionals());
                            }
                            else
                            {
                                //file not found
                                //issue a warning (logging api pending)
                                //since this file was loosely bundled, we continue with the installation.
                                System.out.println("Could not find loosely bundled file: " + pf.getRelativeSourcePath());
                                if (!handler.emitWarning("File not found", "Could not find loosely bundled file: " + pf.getRelativeSourcePath()))
                                {
                                    throw new InstallerException("Installation cancelled");
                                }
                                continue;
                            }
                        }

                        File tmpFile = null;
                        if (blockableForCurrentOs(pf))
                        {
                            // If target file might be blocked the output file must first
                            // refer to a temporary file, because Windows Setup API
                            // doesn't work on streams but only on physical files
                            tmpFile = File.createTempFile("__FQ__", null, pathFile.getParentFile());
                            out = new FileOutputStream(tmpFile);
                        }
                        else
                        {
                            out = new FileOutputStream(pathFile);
                        }

                        if (pf.isPack200Jar())
                        {
                            int key = objIn.readInt();
                            InputStream pack200Input = resourceManager.getInputStream("/packs/pack200-" + key);
                            Pack200.Unpacker unpacker = getPack200Unpacker();
                            java.util.jar.JarOutputStream jarOut = new java.util.jar.JarOutputStream(out);
                            unpacker.unpack(pack200Input, jarOut);
                            jarOut.close();
                        }
                        else
                        {
                            byte[] buffer = new byte[5120];
                            long bytesCopied = 0;
                            while (bytesCopied < pf.length())
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
                                bytesCopied = writeBuffer(pf, buffer, out, pis, bytesCopied);
                            }
                            out.close();
                        }

                        if (pis != objIn)
                        {
                            pis.close();
                        }

                        handleTimeStamp( pf, pathFile, tmpFile);

                        fq = handleBlockable(pf, pathFile, tmpFile, fq, customActions);
                    }
                    else
                    {
                        if (!pf.isBackReference())
                        {
                            objIn.skip(pf.length());
                        }
                    }
                }

                // Load information about parsable files
                int numParsables = objIn.readInt();
                for (int k = 0; k < numParsables; k++)
                {
                    ParsableFile pf = (ParsableFile) objIn.readObject();
                    if (pf.hasCondition() && (rules != null))
                    {
                        if (!rules.isConditionTrue(pf.getCondition()))
                        {
                            // skip, condition is not fulfilled
                            continue;
                        }
                    }
                    pf.path = IoHelper.translatePath(pf.path, variableSubstitutor);
                    parsables.add(pf);
                }

                loadExecutables(objIn, executables);

                // Custom action listener stuff --- uninstall data ----
//                handleAdditionalUninstallData(udata, customActions);

                // Load information about updatechecks
                int numUpdateChecks = objIn.readInt();

                for (int k = 0; k < numUpdateChecks; k++)
                {
                    UpdateCheck updateCheck = (UpdateCheck) objIn.readObject();

                    updatechecks.add(updateCheck);
                }

                objIn.close();

                if (performInterrupted())
                { // Interrupt was initiated; perform it.
                    return;
                }

                // Custom action listener stuff --- afterPack ----
                informListeners(customActions, InstallerListener.AFTER_PACK, packs.get(i),
                        i, handler);
            }

            // Commit a file queue if there are potentially blocked files
            // Use one file queue for all packs
            if (fq != null)
            {
                fq.execute();
                idata.setRebootNecessary(fq.isRebootNecessary());
            }

            // We use the scripts parser
            ScriptParser parser = new ScriptParser(parsables, variableSubstitutor);
            parser.parseFiles();
            if (performInterrupted())
            { // Interrupt was initiated; perform it.
                return;
            }

            // We use the file executor
            FileExecutor executor = new FileExecutor(executables);
            if (executor.executeFiles(ExecutableFile.POSTINSTALL, handler) != 0)
            {
                handler.emitError("File execution failed", "The installation was not completed");
                this.result = false;
            }

            if (performInterrupted())
            { // Interrupt was initiated; perform it.
                return;
            }

            // update checks _after_ uninstaller was put, so we don't delete it
            performUpdateChecks(updatechecks);

            if (performInterrupted())
            { // Interrupt was initiated; perform it.
                return;
            }

            // Custom action listener stuff --- afterPacks ----
            informListeners(customActions, InstallerListener.AFTER_PACKS, idata, npacks, handler);
            if (performInterrupted())
            { // Interrupt was initiated; perform it.
                return;
            }

            // write installation information
            writeInstallationInformation();

            // The end :-)
            handler.stopAction();
        }
        catch (Exception err)
        {
            // TODO: finer grained error handling with useful error messages
            handler.stopAction();
            String message = err.getMessage();
            if ("Installation cancelled".equals(message))
            {
                handler.emitNotification("Installation cancelled");
            }
            else
            {
                if (message == null || "".equals(message))
                {
                    message = "Internal error occured : " + err.toString();
                }
                handler.emitError("An error occured", message);
                err.printStackTrace();
            }
            this.result = false;
            Housekeeper.getInstance().shutDown(4);
        }
        finally
        {
            removeFromInstances();
        }
    }

    private Pack200.Unpacker getPack200Unpacker()
    {
        if (unpacker == null)
        {
            unpacker = Pack200.newUnpacker();
        }
        return unpacker;
    }

    /**
     * Returns a stream to a pack, location depending on if it's web based.
     *
     * @param uninstall true if pack must be uninstalled
     * @return The stream or null if it could not be found.
     * @throws Exception Description of the Exception
     */
    private InputStream getPackAsStream(String packid, boolean uninstall) throws Exception
    {
        InputStream in;

        String webDirURL = idata.getInfo().getWebDirURL();

        packid = "-" + packid;

        if (webDirURL == null) // local
        {
            in = resourceManager.getInputStream("packs/pack" + packid);
        }
        else
        // web based
        {
            // TODO: Look first in same directory as primary jar
            // This may include prompting for changing of media
            // TODO: download and cache them all before starting copy process

            // See compiler.Packager#getJarOutputStream for the counterpart
            String baseName = idata.getInfo().getInstallerBase();
            String packURL = webDirURL + "/" + baseName + ".pack" + packid + ".jar";
            String tempFolder = IoHelper.translatePath(idata.getInfo().getUninstallerPath() + Unpacker.tempSubPath, variableSubstitutor);
            String tempfile;
            try
            {
                tempfile = WebRepositoryAccessor.getCachedUrl(packURL, tempFolder);
                udata.addFile(tempfile, uninstall);
            }
            catch (Exception e)
            {
                if ("Cancelled".equals(e.getMessage()))
                {
                    throw new InstallerException("Installation cancelled", e);
                }
                else
                {
                    throw new InstallerException("Installation failed", e);
                }
            }
            URL url = new URL("jar:" + tempfile + "!/packs/pack" + packid);

            //URL url = new URL("jar:" + packURL + "!/packs/pack" + packid);
            // JarURLConnection jarConnection = (JarURLConnection)
            // url.openConnection();
            // TODO: what happens when using an automated installer?
            in = new WebAccessor(null).openInputStream(url);
            // TODO: Fails miserably when pack jars are not found, so this is
            // temporary
            if (in == null)
            {
                throw new InstallerException(url.toString() + " not available", new FileNotFoundException(url.toString()));
            }
        }
        if (in != null && idata.getInfo().getPackDecoderClassName() != null)
        {
            Class<Object> decoder = (Class<Object>) Class.forName(idata.getInfo().getPackDecoderClassName());
            Class[] paramsClasses = new Class[1];
            paramsClasses[0] = Class.forName("java.io.InputStream");
            Constructor<Object> constructor = decoder.getDeclaredConstructor(paramsClasses);
            // Our first used decoder input stream (bzip2) reads byte for byte from
            // the source. Therefore we put a buffering stream between it and the
            // source.
            InputStream buffer = new BufferedInputStream(in);
            Object[] params = {buffer};
            Object instance = null;
            instance = constructor.newInstance(params);
            if (!InputStream.class.isInstance(instance))
            {
                throw new InstallerException("'" + idata.getInfo().getPackDecoderClassName()
                        + "' must be derived from "
                        + InputStream.class.toString());
            }
            in = (InputStream) instance;

        }
        return in;
    }

}
