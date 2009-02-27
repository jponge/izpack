/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2003 Jonathan Halliday
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

package com.izforge.izpack.panels;

import com.izforge.izpack.installer.*;
import com.izforge.izpack.util.AbstractUIProgressHandler;
import com.izforge.izpack.adaptator.IXMLElement;

/**
 * Functions to support automated usage of the InstallPanel
 *
 * @author Jonathan Halliday
 */
public class InstallPanelAutomationHelper extends PanelAutomationHelper implements PanelAutomation,
        AbstractUIProgressHandler
{

    private int noOfPacks = 0;

    /**
     * Null op - this panel type has no state to serialize.
     *
     * @param installData unused.
     * @param panelRoot   unused.
     */
    public void makeXMLData(AutomatedInstallData installData, IXMLElement panelRoot)
    {
        // do nothing.
    }

    /**
     * Perform the installation actions.
     *
     * @param panelRoot The panel XML tree root.
     * @return true if the installation was successful.
     */
    public void runAutomated(AutomatedInstallData idata, IXMLElement panelRoot) throws InstallerException
    {
        /*
        Unpacker unpacker = new Unpacker(idata, this);
        unpacker.start();
        */
        IUnpacker unpacker = UnpackerFactory.getUnpacker(idata.info.getUnpackerClassName(), idata, this);
        Thread unpackerthread = new Thread(unpacker, "IzPack - Unpacker thread");
        unpacker.setRules(idata.getRules());
        unpackerthread.start();
        while (unpackerthread.isAlive())
        {
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
                // ignore it, we're waiting for the unpacker to finish...
            }
        }
        if(!unpacker.getResult()) {
            throw new InstallerException("Unpack failed (xml line "+ panelRoot.getLineNr() + ")");
        }
    }

    /**
     * Reports progress on System.out
     *
     * @see AbstractUIProgressHandler#startAction(String, int)
     */
    public void startAction(String name, int no_of_steps)
    {
        System.out.println("[ Starting to unpack ]");
        this.noOfPacks = no_of_steps;
    }

    /**
     * Sets state variable for thread sync.
     *
     * @see com.izforge.izpack.util.AbstractUIProgressHandler#stopAction()
     */
    public void stopAction()
    {
        System.out.println("[ Unpacking finished ]");
        boolean done = true;
    }

    /**
     * Null op.
     *
     * @param val
     * @param msg
     * @see com.izforge.izpack.util.AbstractUIProgressHandler#progress(int, String)
     */
    public void progress(int val, String msg)
    {
        // silent for now. should log individual files here, if we had a verbose
        // mode?
    }

    /**
     * Reports progress to System.out
     *
     * @param packName The currently installing pack.
     * @param stepno   The number of the pack
     * @param stepsize unused
     * @see com.izforge.izpack.util.AbstractUIProgressHandler#nextStep(String, int, int)
     */
    public void nextStep(String packName, int stepno, int stepsize)
    {
        System.out.print("[ Processing package: " + packName + " (");
        System.out.print(stepno);
        System.out.print('/');
        System.out.print(this.noOfPacks);
        System.out.println(") ]");
    }

    /**
     * {@inheritDoc}
     */
    public void setSubStepNo(int no_of_substeps)
    {
        // not used here
    }
}
