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
package com.izforge.izpack;

import com.izforge.izpack.util.OsConstraint;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

/**
 * Extends the packfile by the information at which file position an entry is stored
 *
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 */
public class XPackFile extends PackFile
{
    private static final long serialVersionUID = 5875050264763504283L;
    protected long archivefileposition;

    /**
     * @param src
     * @param target
     * @param osList
     * @param override
     * @throws FileNotFoundException
     */
    public XPackFile(File baseDir, File src, String target, List<OsConstraint> osList, int override)
            throws FileNotFoundException
    {
        super(baseDir, src, target, osList, override);
        this.archivefileposition = 0;
    }

    /**
     * @param src
     * @param target
     * @param osList
     * @param override
     * @param additionals
     * @throws FileNotFoundException
     */
    public XPackFile(File baseDir, File src, String target, List<OsConstraint> osList, int override, Map additionals)
            throws FileNotFoundException
    {
        super(baseDir, src, target, osList, override, additionals);
        this.archivefileposition = 0;
    }

    public XPackFile(PackFile packf) throws FileNotFoundException
    {
        super(new File(packf.sourcePath), packf.relativePath, packf.getTargetPath(), packf.osConstraints(), packf
                .override(), packf.getAdditionals());
        this.archivefileposition = 0;
    }

    public long getArchivefileposition()
    {
        return archivefileposition;
    }

    public void setArchivefileposition(long archivefileposition)
    {
        this.archivefileposition = archivefileposition;
    }

    public PackFile getPackfile()
    {
        return this;
    }
}