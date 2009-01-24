/*
 * IzPack - Copyright 2001-2009 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2004 Klaus Bartz
 * Copyright 2004 Thomas Guenter
 * Copyright 2009 Matthew Inger
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

package com.izforge.izpack.event;

import com.izforge.izpack.util.AbstractUIProgressHandler;

import java.io.File;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.List;


public class BSFUninstallerListener extends SimpleUninstallerListener
{

    private List bsfActions = null;

    public BSFUninstallerListener()
    {
        super();
    }

    private void loadActions() throws Exception
    {
        InputStream in = getClass().getResourceAsStream("/bsfActions");
        if (in == null)
        {
            return;
        }
        ObjectInputStream objIn = new ObjectInputStream(in);
        bsfActions = (List) objIn.readObject();
        objIn.close();
        in.close();
    }

    public void beforeDeletion(List files, AbstractUIProgressHandler handler) throws Exception
    {
        loadActions();

        for (Object bsfAction : bsfActions)
        {
            BSFAction action = (BSFAction) bsfAction;
            action.init();
            action.executeUninstall(BSFAction.BEFOREDELETION, new Object[]{files, handler});
            action.destroy();
        }
    }


    public void afterDeletion(List files, AbstractUIProgressHandler handler) throws Exception
    {
        for (Object bsfAction : bsfActions)
        {
            BSFAction action = (BSFAction) bsfAction;
            action.init();
            action.executeUninstall(BSFAction.AFTERDELETION, new Object[]{files, handler});
            action.destroy();
        }
    }

    public void beforeDelete(File file, AbstractUIProgressHandler handler) throws Exception
    {
        for (Object bsfAction : bsfActions)
        {
            BSFAction action = (BSFAction) bsfAction;
            action.init();
            action.executeUninstall(BSFAction.BEFOREDELETE, new Object[]{file, handler});
            action.destroy();
        }
    }

    public void afterDelete(File file, AbstractUIProgressHandler handler) throws Exception
    {
        for (Object bsfAction : bsfActions)
        {
            BSFAction action = (BSFAction) bsfAction;
            action.init();
            action.executeUninstall(BSFAction.AFTERDELETE, new Object[]{file, handler});
            action.destroy();
        }
    }

    public boolean isFileListener()
    {
        return true;
    }


}
