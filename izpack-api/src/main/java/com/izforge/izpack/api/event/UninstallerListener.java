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
import java.util.List;

import com.izforge.izpack.api.handler.AbstractUIProgressHandler;

/**
 * Implementations of this class are used to handle customizing uninstallation. The defined methods
 * are called from the destroyer at different, well defined points of uninstallation.
 *
 * @author Klaus Bartz
 */
public interface UninstallerListener
{

    // ------------------------------------------------------------------------
    // Constant Definitions
    // ------------------------------------------------------------------------
    @Deprecated
    public static final int BEFORE_DELETION = 1;

    @Deprecated
    public static final int AFTER_DELETION = 2;

    @Deprecated
    public static final int BEFORE_DELETE = 3;

    @Deprecated
    public static final int AFTER_DELETE = 4;

    /**
     * Invoked before files are deleted.
     *
     * @param files   all files which should be deleted
     * @param handler the UI progress handler
     * @throws Exception for any error
     */
    void beforeDeletion(List files, AbstractUIProgressHandler handler) throws Exception;

    /**
     * Determines if the listener should be notified of every file deletion.
     * <p/>
     * If <tt>true</tt>, the {@link #beforeDelete} and {@link #afterDelete} methods will be invoked for each file.
     *
     * @return <tt>true</tt> if this listener would be informed at every delete operation, else <tt>false</tt>
     */
    boolean isFileListener();

    /**
     * Invoked before a file is deleted.
     *
     * @param file    the file which will be deleted
     * @param handler the UI progress handler
     * @throws Exception for any error
     */
    void beforeDelete(File file, AbstractUIProgressHandler handler) throws Exception;

    /**
     * Invoked after a file is deleted.
     *
     * @param file    the file which was deleted
     * @param handler the UI progress handler
     * @throws Exception for any error
     */
    void afterDelete(File file, AbstractUIProgressHandler handler) throws Exception;

    /**
     * Invoked after files are deleted.
     *
     * @param files   the files which where deleted
     * @param handler the UI progress handler
     * @throws Exception for any error
     */
    void afterDeletion(List files, AbstractUIProgressHandler handler) throws Exception;

}
