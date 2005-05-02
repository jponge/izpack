/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2004 Klaus Bartz
 *
 *  File :               SimpleCompilerListener.java
 *  Description :        Custom action listener for compile time.
 *  Author's email :     klaus.bartz@coi.de
 *  Author's Website :   http://www.coi.de
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
import com.izforge.izpack.compiler.PackInfo;
import com.izforge.izpack.compiler.Packager;

/**
 * <p>
 * This class implements all methods of interface CompilerListener, but do not
 * do anything else. It can be used as base class to save implementation of
 * unneeded methods.
 * </p>
 * 
 * 
 * @author Klaus Bartz
 * 
 */
public class SimpleCompilerListener implements CompilerListener
{

    /**
     * Creates a newly object.
     */
    public SimpleCompilerListener()
    {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.izforge.izpack.compiler.CompilerListener#reviseAttributSetFile(java.util.Map,
     *      net.n3.nanoxml.XMLElement)
     */
    public Map reviseAdditionalDataMap(Map existentDataMap, XMLElement element)
            throws CompilerException
    {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.izforge.izpack.compiler.CompilerListener#AfterPack(com.izforge.izpack.compiler.Compiler.Pack,
     *      int, com.izforge.izpack.compiler.Packager)
     */
    public void afterPack(PackInfo pack, int packNumber, Packager packager)
            throws CompilerException
    {
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.izforge.izpack.compiler.CompilerListener#BeforePack(com.izforge.izpack.compiler.Compiler.Pack,
     *      int, com.izforge.izpack.compiler.Packager)
     */
    public void beforePack(PackInfo pack, int packNumber, Packager packager)
    {
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.izforge.izpack.compiler.CompilerListener#notify(java.lang.String,
     *      int, net.n3.nanoxml.XMLElement,
     *      com.izforge.izpack.compiler.Packager)
     */
    public void notify(String position, int state, XMLElement data, Packager packager)
    {
    }

}
