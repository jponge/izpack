/*
 * IzPack Version 3.0.0 rc2 (build 2002.07.06)
 * Copyright (C) 2001 Johannes Lehtinen
 *
 * File :               Pack.java
 * Description :        Contains informations about a pack file.
 * Author's email :     johannes.lehtinen@iki.fi
 * Author's Website :   http://www.iki.fi/jle/
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.izforge.izpack;

import java.io.Serializable;

/**
 * Encloses information about a packed file. This class abstracts the
 * way file data is stored to package.
 *
 * @author Johannes Lehtinen <johannes.lehtinen@iki.fi>
 * @version $Revision$
 */
public class PackFile implements Serializable
{
    /** The full path name of the target file */
    public String targetPath = null;

    /** The target operation system of this file*/
    public String os = null;

    /** The length of the file in bytes */
    public long length = 0;

    /**
     * Constructs a new uninitialized instance.
     */
    public PackFile() {}

    /**
     * Constructs and initializes a new instance.
     *
     * @param targetFile the target file path
     * @param length the length of the file
     */
    public PackFile(String targetPath, long length)
    {
        this.targetPath = targetPath;
        this.length = length;
    }

    /**
     * Constructs and initializes a new instance.
     *
     * @param targetFile the target file path
     * @param this file should only be installed on  "targetOs" operating system
     * @param length the length of the file
     */
    public PackFile(String targetPath, String targetOs, long length) {
        this.targetPath = targetPath;
        this.length = length;
        this.os = targetOs;
    }
}

