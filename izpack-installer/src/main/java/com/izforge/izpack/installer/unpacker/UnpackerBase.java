/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2007 Dennis Reil
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.Pack200;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.OverrideType;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.PackFile;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.event.InstallerListener;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.api.handler.AbstractUIProgressHandler;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.data.ExecutableFile;
import com.izforge.izpack.data.ParsableFile;
import com.izforge.izpack.data.UpdateCheck;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.event.InstallerListeners;
import com.izforge.izpack.installer.web.WebAccessor;
import com.izforge.izpack.installer.web.WebRepositoryAccessor;
import com.izforge.izpack.util.FileExecutor;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.IoHelper;
import com.izforge.izpack.util.Librarian;
import com.izforge.izpack.util.OsConstraintHelper;
import com.izforge.izpack.util.Platform;
import com.izforge.izpack.util.file.DirectoryScanner;
import com.izforge.izpack.util.file.FileUtils;
import com.izforge.izpack.util.file.GlobPatternMapper;
import com.izforge.izpack.util.file.types.FileSet;
import com.izforge.izpack.util.os.FileQueue;


/**
 * Abstract base class for all unpacker implementations.
 *
 * @author Dennis Reil, <izpack@reil-online.de>
 * @author Tim Anderson
 */
public abstract class UnpackerBase implements IUnpacker
{

    /**
     * The installation data.
     */
    private final AutomatedInstallData installData;

    /**
     * The uninstallation data.
     */
    private final UninstallData uninstallData;

    /**
     * The resources.
     */
    private final Resources resources;

    /**
     * The rules engine.
     */
    private final RulesEngine rules;

    /**
     * The variable replacer.
     */
    private final VariableSubstitutor variableSubstitutor;

    /**
     * The current platform.
     */
    private final Platform platform;

    /**
     * The librarian.
     */
    private final Librarian librarian;

    /**
     * The housekeeper.
     */
    private final Housekeeper housekeeper;

    /**
     * The listeners.
     */
    private final InstallerListeners listeners;

    /**
     * The installer listener.
     */
    private AbstractUIProgressHandler handler;

    /**
     * The absolute path of the source installation jar.
     */
    private File absoluteInstallSource;

    /**
     * The Pack200 unpacker.
     */
    private Pack200.Unpacker unpacker;

    /**
     * The result of the operation.
     */
    private boolean result = true;

    /**
     * Determines if unpack operations should be cancelled.
     */
    private final Cancellable cancellable;

    /**
     * The unpacking state.
     */
    private enum State
    {
        READY, UNPACKING, INTERRUPT, INTERRUPTED
    }

    /**
     * The current unpacking state.
     */
    private State state = State.READY;

    /**
     * If <tt>true</tt>, prevent interrupts.
     */
    private boolean disableInterrupt = false;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(UnpackerBase.class.getName());

    /**
     * Temporary directory.
     */
    private static final String tempSubPath = "/IzpackWebTemp";

    /**
     * Constructs an <tt>UnpackerBase</tt>.
     *
     * @param installData         the installation data
     * @param resources           the resources
     * @param rules               the rules engine
     * @param variableSubstitutor the variable substituter
     * @param uninstallData       the uninstallation data
     * @param platform            the current platform
     * @param librarian           the librarian
     * @param housekeeper         the housekeeper
     * @param listeners           the listeners
     */
    public UnpackerBase(AutomatedInstallData installData, Resources resources, RulesEngine rules,
                        VariableSubstitutor variableSubstitutor, UninstallData uninstallData, Platform platform,
                        Librarian librarian, Housekeeper housekeeper, InstallerListeners listeners)
    {
        this.installData = installData;
        this.resources = resources;
        this.rules = rules;
        this.variableSubstitutor = variableSubstitutor;
        this.uninstallData = uninstallData;
        this.platform = platform;
        this.librarian = librarian;
        this.housekeeper = housekeeper;
        this.listeners = listeners;
        cancellable = new Cancellable()
        {
            @Override
            public boolean isCancelled()
            {
                return isInterrupted();
            }
        };
    }

    /**
     * Sets the progress handler.
     *
     * @param handler the progress handler
     */
    @Override
    public void setHandler(AbstractUIProgressHandler handler)
    {
        this.handler = handler;
    }

    /**
     * Runs the unpacker.
     */
    public void run()
    {
        unpack();
    }

    /**
     * Unpacks the installation files.
     */
    public void unpack()
    {
        state = State.UNPACKING;
        try
        {
            List<ParsableFile> parsables = new ArrayList<ParsableFile>();
            List<ExecutableFile> executables = new ArrayList<ExecutableFile>();
            List<UpdateCheck> updateChecks = new ArrayList<UpdateCheck>();

            preUnpack();
            FileQueue queue = unpack(parsables, executables, updateChecks);
            postUnpack(queue, parsables, executables, updateChecks);
        }
        catch (Exception exception)
        {
            setResult(false);
            logger.log(Level.SEVERE, exception.getMessage(), exception);

            AbstractUIProgressHandler handler = getHandler();
            handler.stopAction();

            String message = exception.getMessage();
            if ("Installation cancelled".equals(message))
            {
                handler.emitNotification("Installation cancelled");
            }
            else
            {
                if (message == null || "".equals(message))
                {
                    message = "Internal error occurred : " + exception.toString();
                }
                handler.emitError("An error occurred", message);
            }
            // TODO - shouldn't do this. Should provide option to rollback changes
            housekeeper.shutDown(4);
        }
        finally
        {
            cleanup();
        }
    }

    /**
     * Return the state of the operation.
     *
     * @return true if the operation was successful, false otherwise.
     */
    public boolean getResult()
    {
        return result;
    }

    /**
     * Interrupts the unpacker, and waits for it to complete.
     * <p/>
     * If interrupts have been prevented ({@link #isInterruptDisabled} returns <tt>true</tt>), then this
     * returns immediately.
     *
     * @param timeout the maximum time to wait, in milliseconds
     * @return <tt>true</tt> if the interrupt will be performed, <tt>false</tt> if the interrupt will be discarded
     */
    @Override
    public boolean interrupt(long timeout)
    {
        boolean result;
        if (isInterruptDisabled())
        {
            result = false;
        }
        else
        {
            synchronized (this)
            {
                if (state != State.INTERRUPTED)
                {
                    state = State.INTERRUPT;
                    try
                    {
                        wait(timeout);
                    }
                    catch (InterruptedException ignore)
                    {
                        // do nothing
                    }
                }
                result = state == State.INTERRUPTED;
            }
        }
        return result;
    }

    /**
     * Determines if interrupts should be disabled.
     *
     * @param disable if <tt>true</tt> disable interrupts, otherwise enable them
     */
    @Override
    public synchronized void setDisableInterrupt(boolean disable)
    {
        if (state == State.INTERRUPT || state == State.INTERRUPTED)
        {
            throw new IllegalStateException("Cannot disable interrupts. Unpacking has already been interrupted");
        }
        disableInterrupt = disable;
    }

    /**
     * Determines if interrupts have been disabled or not.
     *
     * @return <tt>true</tt> if interrupts have been disabled, otherwise <tt>false</tt>
     */
    public synchronized boolean isInterruptDisabled()
    {
        return disableInterrupt;
    }

    /**
     * Invoked prior to unpacking.
     * <p/>
     * This notifies the {@link #getHandler() handler}, and any registered {@link InstallerListener listeners}.
     *
     * @throws Exception if the handler or listeners throw an exception
     */
    protected void preUnpack() throws Exception
    {
        AutomatedInstallData installData = getInstallData();
        AbstractUIProgressHandler handler = getHandler();
        int count = installData.getSelectedPacks().size();

        logger.fine("Unpacker starting");
        handler.startAction("Unpacking", count);

        listeners.beforePacks(installData, count, handler);
    }

    /**
     * Unpacks the selected packs.
     *
     * @param parsables    used to collect parsable files in the pack
     * @param executables  used to collect executable files files in the pack
     * @param updateChecks used to collect update checks in the pack
     * @return the file queue, or <tt>null</tt> if no queuing is required
     * @throws IOException for any I/O error
     * @throws Exception   for any other error
     */
    protected FileQueue unpack(List<ParsableFile> parsables, List<ExecutableFile> executables,
                               List<UpdateCheck> updateChecks) throws Exception
    {
        FileQueue queue = null;
        List<Pack> packs = getInstallData().getSelectedPacks();
        int count = packs.size();

        // Unpack the selected packs
        for (int i = 0; i < count; i++)
        {
            Pack pack = packs.get(i);
            if (shouldUnpack(pack))
            {
                listeners.beforePack(pack, i, handler);
                queue = unpack(pack, i, queue, parsables, executables, updateChecks);
                if (isInterrupted())
                {
                    break;
                }

                listeners.afterPack(pack, i, handler);
            }
        }
        return queue;
    }

    /**
     * Unpacks a pack.
     *
     * @param pack         the pack to unpack
     * @param packNo       the pack number
     * @param queue        the file queue. If <tt>null</tt>, and file queueing is required, one will be created
     * @param parsables    used to collect parsable files in the pack
     * @param executables  used to collect executable files files in the pack
     * @param updateChecks used to collect update checks in the pack
     * @return the file queue. May be <tt>null</tt>
     * @throws IOException for any I/O error
     * @throws Exception   for any other error
     */
    protected FileQueue unpack(Pack pack, int packNo, FileQueue queue, List<ParsableFile> parsables,
                               List<ExecutableFile> executables, List<UpdateCheck> updateChecks) throws Exception
    {
        InputStream in = null;
        ObjectInputStream packInputStream = null;
        try
        {
            in = getPackStream(pack.getName(), pack.isUninstall());
            packInputStream = new ObjectInputStream(in);

            int fileCount = packInputStream.readInt();

            AbstractUIProgressHandler handler = getHandler();
            String stepName = getStepName(pack);
            handler.nextStep(stepName, packNo + 1, fileCount);

            for (int i = 0; i < fileCount; ++i)
            {
                // read the header
                PackFile file = (PackFile) packInputStream.readObject();
                if ((file.hasCondition() && !isConditionTrue(file.getCondition()))
                        || !OsConstraintHelper.oneMatchesCurrentSystem(file.osConstraints()))
                {
                    // condition is not fulfilled, so skip it
                    skip(file, pack, packInputStream);
                }
                else
                {
                    // unpack the file
                    queue = unpack(file, packInputStream, i, pack, queue);
                }
            }
            readParsableFiles(packInputStream, parsables);
            readExecutableFiles(packInputStream, executables);
            readUpdateChecks(packInputStream, updateChecks);
        }
        finally
        {
            FileUtils.close(packInputStream);
            FileUtils.close(in);
        }
        return queue;
    }

    /**
     * Unpacks a pack file.
     *
     * @param file            the pack file
     * @param packInputStream the pack file input stream
     * @param fileNo          the pack file number
     * @param pack            the pack that the pack file comes from
     * @param queue           the file queue. If <tt>null</tt>, and file queueing is required, one will be created
     * @return the file queue. May be <tt>null</tt>
     * @throws IOException for any I/O error
     * @throws Exception   for any other error
     */
    protected FileQueue unpack(PackFile file, ObjectInputStream packInputStream, int fileNo, Pack pack, FileQueue queue)
            throws Exception
    {
        // translate & build the path
        Variables variables = getInstallData().getVariables();
        String path = IoHelper.translatePath(file.getTargetPath(), variables);
        File target = new File(path);
        File dir = target;
        if (!file.isDirectory())
        {
            dir = target.getParentFile();
        }

        createDirectory(dir, file);

        // Add path to the log
        getUninstallData().addFile(path, pack.isUninstall());

        if (file.isDirectory())
        {
            return queue;
        }

        listeners.beforeFile(target, file);

        AbstractUIProgressHandler handler = getHandler();
        handler.progress(fileNo, path);

        // if this file exists and should not be overwritten, check what to do
        if (target.exists() && (file.override() != OverrideType.OVERRIDE_TRUE))
        {
            if (!isOverwriteFile(file, target))
            {
                if (!file.isBackReference() && !pack.isLoose())
                {
                    if (file.isPack200Jar())
                    {
                        skip(packInputStream, Integer.SIZE / 8);
                    }
                    else
                    {
                        skip(packInputStream, file.length());
                    }
                }
                return queue;
            }
        }

        handleOverrideRename(file, target);
        queue = extract(file, target, packInputStream, pack, queue);

        return queue;
    }

    /**
     * Extracts a pack file.
     *
     * @param file            the pack file
     * @param target          the file to write to
     * @param packInputStream the pack file input stream
     * @param pack            the pack that the pack file comes from
     * @param queue           the file queue. If <tt>null</tt>, and file queueing is required, one will be created
     * @return the file queue. May be <tt>null</tt>
     * @throws IOException for any I/O error
     * @throws Exception   for any other error
     */
    protected FileQueue extract(PackFile file, File target, ObjectInputStream packInputStream, Pack pack,
                                FileQueue queue)
            throws Exception
    {
        ObjectInputStream packStream = packInputStream;
        InputStream in = null;
        try
        {
            FileUnpacker unpacker;

            if (!pack.isLoose() && file.isBackReference())
            {
                in = getPackStream(file.previousPackId, pack.isUninstall());
                packStream = new ObjectInputStream(in);
                // must wrap for blockdata use by ObjectStream (otherwise strange result)
                // skip on underlying stream (for some reason not possible on ObjectStream)
                skip(in, file.offsetInPreviousPack - 4);
                // but the stream header is now already read (== 4 bytes)
            }

            unpacker = createFileUnpacker(file, pack, queue, cancellable);
            unpacker.unpack(file, packStream, target);


            if (isInterrupted())
            {
                return queue;
            }

            if (!unpacker.isQueued())
            {
                listeners.afterFile(target, file);
            }
        }
        finally
        {
            FileUtils.close(in);
            if (packStream != packInputStream)
            {
                FileUtils.close(packStream);
            }
        }
        return queue;
    }

    /**
     * Skips a pack file.
     *
     * @param file            the pack file
     * @param pack            the pack
     * @param packInputStream the pack stream
     * @throws IOException if the file cannot be skipped
     */
    protected void skip(PackFile file, Pack pack, ObjectInputStream packInputStream) throws IOException
    {
        if (!pack.isLoose() && !file.isBackReference())
        {
            skip(packInputStream, file.length());
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
    protected FileUnpacker createFileUnpacker(PackFile file, Pack pack, FileQueue queue, Cancellable cancellable)
            throws IOException, InstallerException
    {
        FileUnpacker unpacker;
        if (pack.isLoose())
        {
            unpacker = new LooseFileUnpacker(getAbsoluteInstallSource(), cancellable, handler, queue, platform,
                                             librarian);
        }
        else if (file.isPack200Jar())
        {
            unpacker = new Pack200FileUnpacker(cancellable, handler, resources, getPack200Unpacker(), queue, platform,
                                               librarian);
        }
        else
        {
            unpacker = new DefaultFileUnpacker(cancellable, handler, queue, platform, librarian);
        }
        return unpacker;
    }

    /**
     * Invoked after each pack has been unpacked.
     *
     * @param queue        the file queue, or <tt>null</tt> if no file queuing was required during unpacking
     * @param parsables    used to collect parsable files in the pack
     * @param executables  used to collect executable files files in the pack
     * @param updateChecks used to collect update checks in the pack
     * @throws Exception for any listener error
     */
    protected void postUnpack(FileQueue queue, List<ParsableFile> parsables, List<ExecutableFile> executables,
                              List<UpdateCheck> updateChecks) throws Exception
    {
        AutomatedInstallData installData = getInstallData();
        AbstractUIProgressHandler handler = getHandler();

        // Commit a file queue if there are potentially blocked files
        // Use one file queue for all packs
        if (queue != null)
        {
            queue.execute();
            installData.setRebootNecessary(queue.isRebootNecessary());
        }
        if (isInterrupted())
        {
            return;
        }

        parseFiles(parsables);
        if (isInterrupted())
        {
            return;
        }

        if (!executeFiles(executables) || isInterrupted())
        {
            return;
        }

        // update checks should be done _after_ uninstaller was put, so we don't delete it. TODO
        performUpdateChecks(updateChecks);
        if (isInterrupted())
        {
            return;
        }

        listeners.afterPacks(installData, handler);
        if (isInterrupted())
        {
            return;
        }

        // write installation information
        writeInstallationInformation();

        // unpacking complete
        handler.stopAction();
    }

    /**
     * Invoked after unpacking has completed, in order to clean up.
     */
    protected void cleanup()
    {
        state = State.READY;
    }

    /**
     * Returns the installation data.
     *
     * @return the installation data
     */
    protected AutomatedInstallData getInstallData()
    {
        return installData;
    }

    /**
     * Returns the uninstallation data.
     *
     * @return the uninstallation data
     */
    protected UninstallData getUninstallData()
    {
        return uninstallData;
    }

    /**
     * Returns the resources.
     *
     * @return the resources
     */
    protected Resources getResources()
    {
        return resources;
    }

    /**
     * Returns the variable replacer.
     *
     * @return the variable replacer
     */
    protected VariableSubstitutor getVariableSubstitutor()
    {
        return variableSubstitutor;
    }

    /**
     * Returns the handler.
     *
     * @return the handler
     */
    protected AbstractUIProgressHandler getHandler()
    {
        return handler;
    }

    /**
     * Returns the platform.
     *
     * @return the platform
     */
    protected Platform getPlatform()
    {
        return platform;
    }

    /**
     * Returns the librarian.
     *
     * @return the librarian
     */
    protected Librarian getLibrarian()
    {
        return librarian;
    }

    /**
     * Determines if a pack should be unpacked.
     *
     * @param pack the pack
     * @return <tt>true</tt> if the pack should be unpacked, <tt>false</tt> if it should be skipped
     */
    protected boolean shouldUnpack(Pack pack)
    {
        boolean result = true;
        if (pack.hasCondition())
        {
            result = rules.isConditionTrue(pack.getCondition());
        }
        return result;
    }

    /**
     * Sets the result of the unpacking operation.
     *
     * @param result if <tt>true</tt> denotes success
     */
    protected void setResult(boolean result)
    {
        this.result = result;
    }

    protected boolean isConditionTrue(String id)
    {
        return rules.isConditionTrue(id);
    }

    /**
     * Returns the step name for a pack, for reporting purposes.
     *
     * @param pack the pack
     * @return the pack's step name
     */
    protected String getStepName(Pack pack)
    {
        String result = pack.getName();
        if (pack.isHidden())
        {
            // hide the pack name if pack is hidden
            result = "";
        }
        else if (pack.getLangPackId() != null && installData.getMessages() != null)
        {
            // the pack has an id - if there is a language pack entry for it, use it instead
            String id = installData.getMessages().get(pack.getLangPackId());
            if (!pack.getLangPackId().equals(id))
            {
                result = id;
            }
        }
        return result;
    }

    /**
     * Returns a stream to a pack, location depending on if it's web based.
     *
     * @param name      the pack name
     * @param uninstall <tt>true</tt> if pack must be uninstalled
     * @return the stream or null if it could not be found.
     * @throws Exception Description of the Exception
     */
    protected InputStream getPackStream(String name, boolean uninstall) throws Exception
    {
        InputStream in;

        String webDirURL = installData.getInfo().getWebDirURL();

        name = "-" + name;

        if (webDirURL == null)
        {
            // local
            in = resources.getInputStream("packs/pack" + name);
        }
        else
        {
            // web based
            // TODO: Look first in same directory as primary jar
            // This may include prompting for changing of media
            // TODO: download and cache them all before starting copy process

            // See compiler.Packager#getJarOutputStream for the counterpart
            String baseName = installData.getInfo().getInstallerBase();
            String packURL = webDirURL + "/" + baseName + ".pack" + name + ".jar";
            String tempFolder = IoHelper.translatePath(
                    installData.getInfo().getUninstallerPath() + tempSubPath, installData.getVariables());
            String tempFile;
            try
            {
                tempFile = WebRepositoryAccessor.getCachedUrl(packURL, tempFolder);
                uninstallData.addFile(tempFile, uninstall);
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
            URL url = new URL("jar:" + tempFile + "!/packs/pack" + name);

            //URL url = new URL("jar:" + packURL + "!/packs/pack" + packid);
            // JarURLConnection jarConnection = (JarURLConnection)
            // url.openConnection();
            // TODO: what happens when using an automated installer?
            in = new WebAccessor(null).openInputStream(url);
            // TODO: Fails miserably when pack jars are not found, so this is
            // temporary
            if (in == null)
            {
                throw new InstallerException(url.toString() + " not available",
                                             new FileNotFoundException(url.toString()));
            }
        }
        if (in != null && installData.getInfo().getPackDecoderClassName() != null)
        {
            Class<Object> decoder = (Class<Object>) Class.forName(installData.getInfo().getPackDecoderClassName());
            Class[] paramsClasses = new Class[1];
            paramsClasses[0] = Class.forName("java.io.InputStream");
            Constructor<Object> constructor = decoder.getDeclaredConstructor(paramsClasses);
            // Our first used decoder input stream (bzip2) reads byte for byte from
            // the source. Therefore we put a buffering stream between it and the
            // source.
            InputStream buffer = new BufferedInputStream(in);
            Object[] params = {buffer};
            Object instance = constructor.newInstance(params);
            if (!InputStream.class.isInstance(instance))
            {
                throw new InstallerException("'" + installData.getInfo().getPackDecoderClassName()
                                                     + "' must be derived from "
                                                     + InputStream.class.toString());
            }
            in = (InputStream) instance;
        }
        return in;
    }

    /**
     * Creates a directory including any necessary but nonexistent parent directories, associated with a pack file.
     * <p/>
     * If {@link InstallerListener}s are registered, these will be notified for each directory created.
     *
     * @param dir  the directory to create
     * @param file the pack file
     * @return <tt>true</tt> if the directories were created, otherwise <tt>false</tt>
     * @throws Exception for any listener error
     */
    protected boolean createDirectory(File dir, PackFile file) throws Exception
    {
        boolean ok = true;
        if (!dir.exists())
        {
            if (!listeners.isFileListener())
            {
                // Create it in one step.
                if (!dir.mkdirs())
                {
                    handler.emitError("Error creating directories",
                                      "Could not create directory\n" + dir.getPath());
                    handler.stopAction();
                    result = false;
                }
            }
            else
            {
                File parent = dir.getParentFile();
                if (parent != null)
                {
                    ok = createDirectory(parent, file);
                }
                if (ok)
                {
                    listeners.beforeDir(dir, file);
                    ok = dir.mkdir();
                    if (!ok)
                    {
                        handler.emitError("Error creating directories", "Could not create directory\n" + dir);
                        handler.stopAction();
                    }
                    else
                    {
                        listeners.afterDir(dir, file);
                    }
                }
            }
        }
        else
        {
            ok = true;
        }
        return ok;
    }

    /**
     * Parses {@link ParsableFile} instances collected during unpacking.
     *
     * @param files the files to parse
     * @throws Exception if parsing fails
     */
    private void parseFiles(List<ParsableFile> files) throws Exception
    {
        if (!files.isEmpty())
        {
            ScriptParser parser = new ScriptParser(getVariableSubstitutor());
            for (ParsableFile file : files)
            {
                parser.parse(file);
                if (isInterrupted())
                {
                    return;
                }
            }
        }
    }

    /**
     * Runs {@link ExecutableFile} instances collected during unpacking.
     *
     * @param executables the executables to run
     * @return <tt>true</tt> if execution was successful, otherwise <tt>false</tt>
     */
    private boolean executeFiles(List<ExecutableFile> executables)
    {
        boolean result = true;
        if (!executables.isEmpty())
        {
            FileExecutor executor = new FileExecutor(executables);
            if (executor.executeFiles(ExecutableFile.POSTINSTALL, handler) != 0)
            {
                result = false;
                setResult(false);
            }
        }
        return result;
    }

    /**
     * Determines if the unpacker has been interrupted.
     *
     * @return <tt>true</tt> if the unpacker has been interrupted, otherwise <tt>false</tt>
     */
    protected synchronized boolean isInterrupted()
    {
        boolean result = false;
        if (state == State.INTERRUPT)
        {
            setResult(false);
            state = State.INTERRUPTED;
            result = true;
            notifyAll(); // notify threads waiting in interrupt()
        }
        else
        {
            if (state == State.INTERRUPTED)
            {
                result = true;
            }
        }
        return result;
    }

    protected void performUpdateChecks(List<UpdateCheck> checks)
    {
        if (checks != null && !checks.isEmpty())
        {
            File absoluteInstallPath = new File(installData.getInstallPath()).getAbsoluteFile();
            FileSet fileset = new FileSet();
            List<File> filesToDelete = new ArrayList<File>();
            List<File> dirsToDelete = new ArrayList<File>();

            try
            {
                fileset.setDir(absoluteInstallPath);

                for (UpdateCheck check : checks)
                {
                    if (check.includesList != null)
                    {
                        for (String include : check.includesList)
                        {
                            fileset.createInclude().setName(variableSubstitutor.substitute(include));
                        }
                    }

                    if (check.excludesList != null)
                    {
                        for (String exclude : check.excludesList)
                        {
                            fileset.createExclude().setName(variableSubstitutor.substitute(exclude));
                        }
                    }
                }
                DirectoryScanner scanner = fileset.getDirectoryScanner();
                scanner.scan();
                String[] srcFiles = scanner.getIncludedFiles();
                String[] srcDirs = scanner.getIncludedDirectories();

                Set<File> installedFiles = new TreeSet<File>();

                for (String name : uninstallData.getInstalledFilesList())
                {
                    File file = new File(name);

                    if (!file.isAbsolute())
                    {
                        file = new File(absoluteInstallPath, name);
                    }

                    installedFiles.add(file);
                }
                for (String srcFile : srcFiles)
                {
                    File newFile = new File(scanner.getBasedir(), srcFile);

                    // skip files we just installed
                    if (!installedFiles.contains(newFile))
                    {
                        filesToDelete.add(newFile);
                    }
                }
                for (String srcDir : srcDirs)
                {
                    File newDir = new File(scanner.getBasedir(), srcDir);

                    // skip directories we just installed
                    if (!installedFiles.contains(newDir))
                    {
                        dirsToDelete.add(newDir);
                    }
                }
            }
            catch (Exception e)
            {
                handler.emitError("Error while performing update checks", e.getMessage());
            }

            for (File f : filesToDelete)
            {
                if (!f.delete())
                {
                    logger.warning("Failed to delete: " + f);
                }
            }
            for (File d : dirsToDelete)
            {
                // Only empty directories will be deleted
                if (!d.delete())
                {
                    logger.warning("Failed to delete: " + d);
                }
            }
        }
    }

    /**
     * Writes information about the installed packs and the variables at installation time.
     *
     * @throws IOException            for any I/O error
     * @throws ClassNotFoundException if deserialization fails
     */
    protected void writeInstallationInformation() throws IOException, ClassNotFoundException
    {
        if (!installData.getInfo().isWriteInstallationInformation())
        {
            logger.fine("Skip writing installation information");
            return;
        }
        logger.fine("Writing installation information");
        String installDir = installData.getInstallPath();

        List<Pack> installedPacks = new ArrayList<Pack>(installData.getSelectedPacks());

        File installationInfo = new File(installDir + File.separator + InstallData.INSTALLATION_INFORMATION);
        if (!installationInfo.exists())
        {
            logger.fine("Creating info file" + installationInfo.getAbsolutePath());
            File dir = new File(installData.getInstallPath());
            if (!dir.exists())
            {
                // if no packs have been installed, then the installation directory won't exist
                if (!dir.mkdirs())
                {
                    throw new IOException("Failed to create directory: " + dir);
                }
            }
            if (!installationInfo.createNewFile())
            {
                throw new IOException("Failed to create file: " + installationInfo);
            }
        }
        else
        {
            logger.fine("Previous installation information found");
            // read in old information and update
            FileInputStream fin = new FileInputStream(installationInfo);
            ObjectInputStream oin = new ObjectInputStream(fin);

            List<Pack> packs = (List<Pack>) oin.readObject();
            for (Pack pack : packs)
            {
                installedPacks.add(pack);
            }
            oin.close();
            fin.close();
        }

        FileOutputStream fout = new FileOutputStream(installationInfo);
        ObjectOutputStream oout = new ObjectOutputStream(fout);
        oout.writeObject(installedPacks);
        /*
        int selectedpackscount = installData.selectedPacks.size();
        for (int i = 0; i < selectedpackscount; i++)
        {
            Pack pack = (Pack) installData.selectedPacks.get(i);
            oout.writeObject(pack);
        }
        */
        oout.writeObject(installData.getVariables().getProperties());
        logger.fine("Writing installation information finished");
        oout.close();
        fout.close();
    }

    protected File getAbsoluteInstallSource() throws IOException, InstallerException
    {
        if (absoluteInstallSource == null)
        {
            URI uri;
            try
            {
                uri = getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
            }
            catch (URISyntaxException exception)
            {
                throw new InstallerException(exception);
            }
            if (!"file".equals(uri.getScheme()))
            {
                throw new InstallerException("Unexpected scheme in JAR file URI: " + uri);
            }
            absoluteInstallSource = new File(uri.getSchemeSpecificPart()).getAbsoluteFile();
            if (absoluteInstallSource.getName().endsWith(".jar"))
            {
                absoluteInstallSource = absoluteInstallSource.getParentFile();
            }
        }
        return absoluteInstallSource;
    }

    /**
     * Skips bytes in a stream.
     *
     * @param stream the stream
     * @param bytes  the no. of bytes to skip
     * @throws IOException for any I/O error, or if the no. of bytes skipped doesn't match that expected
     */
    protected void skip(InputStream stream, long bytes) throws IOException
    {
        long skipped = stream.skip(bytes);
        if (skipped != bytes)
        {
            throw new IOException("Expected to skip: " + bytes + " in stream but skipped: " + skipped);
        }
    }

    protected boolean isOverwriteFile(PackFile pf, File file)
    {
        boolean result = false;

        // don't overwrite file if the user said so
        if (pf.override() != OverrideType.OVERRIDE_FALSE)
        {
            if (pf.override() == OverrideType.OVERRIDE_TRUE)
            {
                result = true;
            }
            else if (pf.override() == OverrideType.OVERRIDE_UPDATE)
            {
                // check mtime of involved files
                // (this is not 100% perfect, because the
                // already existing file might
                // still be modified but the new installed
                // is just a bit newer; we would
                // need the creation time of the existing
                // file or record with which mtime
                // it was installed...)
                result = (file.lastModified() < pf.lastModified());
            }
            else
            {
                int def_choice = -1;

                if (pf.override() == OverrideType.OVERRIDE_ASK_FALSE)
                {
                    def_choice = AbstractUIHandler.ANSWER_NO;
                }
                if (pf.override() == OverrideType.OVERRIDE_ASK_TRUE)
                {
                    def_choice = AbstractUIHandler.ANSWER_YES;
                }

                Messages messages = installData.getMessages();
                int answer = handler.askQuestion(
                        messages.get("InstallPanel.overwrite.title") + " - " + file.getName(),
                        messages.get("InstallPanel.overwrite.question") + file.getAbsolutePath(),
                        AbstractUIHandler.CHOICES_YES_NO, def_choice);

                result = (answer == AbstractUIHandler.ANSWER_YES);
            }
        }

        return result;
    }

    protected void handleOverrideRename(PackFile pf, File file)
    {
        if (file.exists() && pf.overrideRenameTo() != null)
        {
            GlobPatternMapper mapper = new GlobPatternMapper();
            mapper.setFrom("*");
            mapper.setTo(pf.overrideRenameTo());
            mapper.setCaseSensitive(true);
            String[] newFileNameArr = mapper.mapFileName(file.getName());
            if (newFileNameArr != null)
            {
                String newFileName = newFileNameArr[0];
                File newPathFile = new File(file.getParent(), newFileName);
                if (newPathFile.exists())
                {
                    if (!newPathFile.delete())
                    {
                        logger.warning("Failed to delete: " + newPathFile);
                    }
                }
                if (!file.renameTo(newPathFile))
                {
                    handler.emitError("Error renaming file",
                                      "The file " + file + " could not be renamed to " + newPathFile);
                }
            }
            else
            {
                handler.emitError("Error renaming file", "File name " + file.getName()
                        + " cannot be mapped using the expression \""
                        + pf.overrideRenameTo() + "\"");
            }
        }
    }


    /**
     * Reads {@link ParsableFile parseable files} from the supplied stream.
     *
     * @param stream    the stream to read from
     * @param parsables used to collect the read objects
     * @throws IOException            for any I/O error
     * @throws ClassNotFoundException if the class of a serialised object cannot be found
     */
    protected void readParsableFiles(ObjectInputStream stream, List<ParsableFile> parsables)
            throws IOException, ClassNotFoundException
    {
        int count = stream.readInt();
        for (int i = 0; i < count; ++i)
        {
            ParsableFile file = (ParsableFile) stream.readObject();
            if (!file.hasCondition() || isConditionTrue(file.getCondition()))
            {
                file.path = IoHelper.translatePath(file.path, installData.getVariables());
                parsables.add(file);
            }
        }
    }

    /**
     * Reads {@link ExecutableFile executable files} from the supplied stream.
     *
     * @param stream      the stream to read from
     * @param executables used to collect the read objects
     * @throws IOException            for any I/O error
     * @throws ClassNotFoundException if the class of a serialised object cannot be found
     */
    protected void readExecutableFiles(ObjectInputStream stream, List<ExecutableFile> executables)
            throws IOException, ClassNotFoundException
    {
        // Load information about executable files
        int count = stream.readInt();
        for (int i = 0; i < count; ++i)
        {
            ExecutableFile file = (ExecutableFile) stream.readObject();
            if (!file.hasCondition() || isConditionTrue(file.getCondition()))
            {
                Variables variables = installData.getVariables();
                file.path = IoHelper.translatePath(file.path, variables);
                if (null != file.argList && !file.argList.isEmpty())
                {
                    for (int j = 0; j < file.argList.size(); j++)
                    {
                        String arg = file.argList.get(j);
                        arg = IoHelper.translatePath(arg, variables);
                        file.argList.set(j, arg);
                    }
                }
                executables.add(file);
                if (file.executionStage == ExecutableFile.UNINSTALL)
                {
                    uninstallData.addExecutable(file);
                }
            }
        }
    }

    /**
     * Reads {@link UpdateCheck update checks} from the supplied stream.
     *
     * @param stream       the stream to read from
     * @param updateChecks used to collect the read objects
     * @throws IOException            for any I/O error
     * @throws ClassNotFoundException if the class of a serialised object cannot be found
     */
    protected void readUpdateChecks(ObjectInputStream stream, List<UpdateCheck> updateChecks)
            throws IOException, ClassNotFoundException
    {
        int count = stream.readInt();
        for (int i = 0; i < count; ++i)
        {
            UpdateCheck check = (UpdateCheck) stream.readObject();
            updateChecks.add(check);
        }
    }

    /**
     * Returns the pack200 unpacker, creating it if required.
     *
     * @return the pack200 unpacker
     */
    private Pack200.Unpacker getPack200Unpacker()
    {
        if (unpacker == null)
        {
            unpacker = Pack200.newUnpacker();
        }
        return unpacker;
    }

    /**
     * Returns whether interrupt was initiate or not for this Unpacker.
     *
     * @return whether interrupt was initiate or not
     */
    private synchronized boolean shouldInterrupt()
    {
        return state == State.INTERRUPT || state == State.INTERRUPTED;
    }

}

