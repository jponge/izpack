/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2004 Klaus Bartz
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

package com.izforge.izpack.api.event;

import java.io.File;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.PackFile;
import com.izforge.izpack.api.handler.AbstractUIProgressHandler;

/**
 * <p>
 * Implementations of this class are used to handle customizing installation. The defined methods
 * are called from the unpacker at different, well defined points of installation.
 * </p>
 *
 * @author Klaus Bartz
 */
public interface InstallerListener extends InstallListener
{

    // ------------------------------------------------------------------------
    // Constant Definitions
    // ------------------------------------------------------------------------

    @Deprecated
    public static final int BEFORE_FILE = 1;

    @Deprecated
    public static final int AFTER_FILE = 2;

    @Deprecated
    public static final int BEFORE_DIR = 3;

    @Deprecated
    public static final int AFTER_DIR = 4;

    @Deprecated
    public static final int BEFORE_PACK = 5;

    @Deprecated
    public static final int AFTER_PACK = 6;

    @Deprecated
    public static final int BEFORE_PACKS = 7;

    @Deprecated
    public static final int AFTER_PACKS = 8;

    /**
     * Invoked when the installer creates the listener instance, immediately after the install data is parsed.
     *
     * @param data the installation data
     * @throws Exception for any error
     */
    void afterInstallerInitialization(AutomatedInstallData data) throws Exception;

    /**
     * Invoked before packs are installed.
     *
     * @param data    the installation data
     * @param packs   the number of packs which are defined for this installation
     * @param handler the UI progress handler
     * @throws Exception for any error
     */
    void beforePacks(AutomatedInstallData data, Integer packs, AbstractUIProgressHandler handler)
            throws Exception;

    /**
     * Invoked before a pack is installed.
     *
     * @param pack    the pack
     * @param i       the pack number
     * @param handler the UI progress handler
     * @throws Exception for any error
     */
    void beforePack(Pack pack, Integer i, AbstractUIProgressHandler handler) throws Exception;

    /**
     * Determines if the listener should be notified of every file and directory installation.
     * <p/>
     * If <tt>true</tt>, the {@link #beforeFile} and {@link #afterFile} methods will be invoked for every installed
     * file, and {@link #beforeDir}, and {@link #afterDir} invoked for each directory creation.
     * <p/>
     * Listeners that return <tt>true</tt> should ensure they don't do any long running operations, to avoid
     * performance issues.
     *
     * @return <tt>true</tt> if the listener should be notified, otherwise <tt>false</tt>
     */
    boolean isFileListener();

    /**
     * Invoked before a directory is created.
     *
     * @param dir      the directory
     * @param packFile the corresponding pack file
     * @throws Exception for any error
     */
    void beforeDir(File dir, PackFile packFile) throws Exception;

    /**
     * Invoked after a directory is created.
     *
     * @param dir      the directory
     * @param packFile the corresponding pack file
     * @throws Exception for any error
     */
    void afterDir(File dir, PackFile packFile) throws Exception;

    /**
     * Invoked before a file is installed.
     *
     * @param file     the file
     * @param packFile the corresponding pack file
     * @throws Exception if the listener throws an exception
     */
    void beforeFile(File file, PackFile packFile) throws Exception;

    /**
     * Invoked after a file is installed.
     *
     * @param file     the file
     * @param packFile the corresponding pack file
     * @throws Exception for any error
     */
    void afterFile(File file, PackFile packFile) throws Exception;

    /**
     * Invoked after a pack is installed.
     *
     * @param pack    the pack
     * @param i       the pack number
     * @param handler the UI progress handler
     * @throws Exception for any error
     */
    void afterPack(Pack pack, Integer i, AbstractUIProgressHandler handler) throws Exception;

    /**
     * Invoked after packs are installed.
     *
     * @param data    the installation data
     * @param handler the UI progress handler
     * @throws Exception for any error
     */
    void afterPacks(AutomatedInstallData data, AbstractUIProgressHandler handler) throws Exception;

}
