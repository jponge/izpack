/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2004 Klaus Bartz
 *
 *  File :               CompilerListener.java
 *  Description :        Custom action listener interface for compile time.
 *  Author's email :     klaus.bartz@coi.de
 *  Author's Website :   http://www.coi.de/
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.izforge.izpack.event;

import java.util.Map;

import net.n3.nanoxml.XMLElement;

import com.izforge.izpack.compiler.CompilerException;
import com.izforge.izpack.compiler.Packager;

/**
 * <p>
 * Implementations of this class are used to add extensions to the packs at
 * compilation.
 * </p>
 * 
 * @author Klaus Bartz
 * 
 */
public interface CompilerListener
{

    public final static int BEGIN = 1;

    public final static int END = 2;

    /**
     * This method is called from the compiler for each file (or dir) parsing.
     * The XMLElement is a node of the file related children of the XML element
     * "pack" (see installation.dtd). Current these are "file", "singlefile" or
     * "fileset". If an additional data should be set, it should be added to the
     * given data map (if exist). If no map exist a new should be created and
     * filled. The data map will be added to the PackFile object after all
     * registered CompilerListener are called. If the map contains an not common
     * object, it is necessary to add the needed class to the installer.
     * 
     * @param existentDataMap
     *            attribute set with previos setted attributes
     * @param element
     *            current file related XML node
     * @return the given or a new attribute set. If no attribute set is given
     *         and no attribute was added, null returns
     * @throws CompilerException
     */
    Map reviseAdditionalDataMap(Map existentDataMap, XMLElement element) throws CompilerException;

    /**
     * This method will be called from each step of packaging.
     * 
     * @param position
     *            name of the calling method, e.g. "addVariables"
     * @param state
     *            BEGIN or END
     * @param data
     *            current install data
     * @param packager
     *            current packager object
     */
    void notify(String position, int state, XMLElement data, Packager packager);

}
