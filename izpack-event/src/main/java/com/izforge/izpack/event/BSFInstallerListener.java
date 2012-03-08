/*
 * IzPack - Copyright 2001-2009 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.PackFile;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.handler.AbstractUIProgressHandler;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.util.ExtendedUIProgressHandler;
import com.izforge.izpack.util.helper.SpecHelper;


public class BSFInstallerListener extends SimpleInstallerListener
{
    private static final Logger logger = Logger.getLogger(BSFInstallerListener.class.getName());

    public static final String SPEC_FILE_NAME = "BSFActionsSpec.xml";

    private HashMap<String, ArrayList<BSFAction>> actions = null;
    private ArrayList<BSFAction> uninstActions = null;
    private String currentPack = null;
    private AutomatedInstallData installdata = null;
    private VariableSubstitutor variableSubstitutor;
    private UninstallData uninstallData;

    public BSFInstallerListener(VariableSubstitutor variableSubstitutor, UninstallData uninstallData)
    {
        super(true);
        this.variableSubstitutor = variableSubstitutor;
        actions = new HashMap<String, ArrayList<BSFAction>>();
        uninstActions = new ArrayList<BSFAction>();
        this.uninstallData = uninstallData;
    }

    @Override
    public void beforePacks(AutomatedInstallData idata, Integer npacks, AbstractUIProgressHandler handler) throws Exception
    {
        if (installdata == null)
        {
            installdata = idata;
        }
        super.beforePacks(idata, npacks, handler);

        getSpecHelper().readSpec(SPEC_FILE_NAME, variableSubstitutor);

        if (getSpecHelper().getSpec() == null)
        {
            return;
        }

        for (Pack pack : idata.getSelectedPacks())
        {
            IXMLElement packElement = getSpecHelper().getPackForName(pack.name);
            if (packElement == null)
            {
                continue;
            }

            ArrayList<BSFAction> packActions = new ArrayList<BSFAction>();

            List<IXMLElement> scriptEntries = packElement.getChildrenNamed("script");
            if (scriptEntries != null && scriptEntries.size() >= 1)
            {
                for (IXMLElement scriptEntry : scriptEntries)
                {
                    BSFAction action = readAction(scriptEntry, idata);
                    if (action != null)
                    {
                        packActions.add(action);
                        String script = action.getScript().toLowerCase();
                        if (script.contains(BSFAction.BEFOREDELETE) ||
                                script.contains(BSFAction.AFTERDELETE) ||
                                script.contains(BSFAction.BEFOREDELETION) ||
                                script.contains(BSFAction.AFTERDELETION))
                        {
                            uninstActions.add(action);
                        }
                    }
                }

                if (packActions.size() > 0)
                {
                    this.setProgressBarCaller();
                }
            }

            actions.put(pack.name, packActions);

        }

        for (Pack pack : idata.getAvailablePacks())
        {
            String currentPack = pack.name;
            performAllActions(currentPack, ActionBase.BEFOREPACKS, null, new Object[]{idata, npacks, handler});
        }
    }

    @Override
    public void afterPack(Pack pack, Integer i, AbstractUIProgressHandler handler) throws Exception
    {
        performAllActions(pack.name, ActionBase.AFTERPACK, handler,
                new Object[]{pack, i, handler});
        currentPack = null;
    }

    @Override
    public void afterPacks(AutomatedInstallData idata, AbstractUIProgressHandler handler) throws Exception
    {
        if (informProgressBar())
        {
            handler.nextStep(getMsg("BSFAction.pack"), getProgressBarCallerId(), getActionCount(
                    idata));
        }
        for (Object selectedPack : idata.getSelectedPacks())
        {
            String currentPack = ((Pack) selectedPack).name;
            performAllActions(currentPack, ActionBase.AFTERPACKS, handler, new Object[]{idata, handler});
        }
        if (uninstActions.size() > 0)
        {
            uninstallData.addAdditionalData("bsfActions", uninstActions);
        }
        installdata = null;
    }

    @Override
    public void beforePack(Pack pack, Integer i, AbstractUIProgressHandler handler) throws Exception
    {
        currentPack = pack.name;
        performAllActions(pack.name, ActionBase.BEFOREPACK, handler, new Object[]{pack, i, handler});
    }

    @Override
    public void afterDir(File file, PackFile pack) throws Exception
    {
        performAllActions(currentPack, BSFAction.AFTERDIR, null, new Object[]{file, pack});

    }

    @Override
    public void afterFile(File file, PackFile pack) throws Exception
    {
        performAllActions(currentPack, BSFAction.AFTERFILE, null, new Object[]{file, pack});
    }

    @Override
    public void beforeDir(File file, PackFile pack) throws Exception
    {
        performAllActions(currentPack, BSFAction.BEFOREDIR, null, new Object[]{file, pack});
    }

    @Override
    public void beforeFile(File file, PackFile pack) throws Exception
    {
        performAllActions(currentPack, BSFAction.BEFOREFILE, null, new Object[]{file, pack});
    }

    @Override
    public boolean isFileListener()
    {
        return true;
    }

    protected ArrayList<BSFAction> getActions(String packName)
    {
        if (actions == null)
        {
            return null;
        }

        return actions.get(packName);
    }

    private int getActionCount(AutomatedInstallData idata)
    {
        int retval = 0;
        for (Object selectedPack : idata.getSelectedPacks())
        {
            String currentPack = ((Pack) selectedPack).name;
            ArrayList<BSFAction> actList = getActions(currentPack);
            if (actList != null)
            {
                retval += actList.size();
            }
        }
        return (retval);
    }

    private void performAllActions(String packName,
                                   String order,
                                   AbstractUIProgressHandler handler,
                                   Object callParams[])
            throws InstallerException
    {
        ArrayList<BSFAction> actList = getActions(packName);
        if (actList == null || actList.size() == 0)
        {
            return;
        }

        logger.fine("Executing all " + order + " BSF actions of pack " + packName + " ...");
        for (BSFAction act : actList)
        {
            // Inform progress bar if needed. Works only
            // on AFTER_PACKS
            if (informProgressBar() && handler != null
                    && handler instanceof ExtendedUIProgressHandler
                    && order.equals(ActionBase.AFTERPACKS))
            {
                ((ExtendedUIProgressHandler) handler)
                        .progress((act.getMessageID() != null) ? getMsg(act.getMessageID()) : "");
            }

            try
            {
                if (ActionBase.BEFOREPACKS.equalsIgnoreCase(order))
                {
                    act.init();
                }
                act.execute(order, callParams, installdata);
                if (ActionBase.AFTERPACKS.equalsIgnoreCase(order))
                {
                    act.destroy();
                }
            }
            catch (Exception e)
            {
                throw new InstallerException(e);
            }
        }

    }

    private BSFAction readAction(IXMLElement element, AutomatedInstallData idata) throws InstallerException
    {
        BSFAction action = new BSFAction();
        String src = element.getAttribute("src");
        if (src != null)
        {
            InputStream is = null;
            InputStream subis = null;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try
            {
                byte buf[] = new byte[10 * 1024];
                int read = 0;
                is = ResourceManager.getInstance().getInputStream(src);
                subis = new SpecHelper().substituteVariables(is, variableSubstitutor);

                while ((read = subis.read(buf, 0, 10 * 1024)) != -1)
                {
                    baos.write(buf, 0, read);
                }

                action.setScript(new String(baos.toByteArray()));
            }
            catch (Exception e)
            {
                throw new InstallerException(e);
            }
            finally
            {
                try
                {
                    if (subis != null)
                    {
                        subis.close();
                    }
                }
                catch (java.io.IOException e)
                {
                    e.printStackTrace();
                }
                try
                {
                    if (is != null)
                    {
                        is.close();
                    }
                }
                catch (java.io.IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        else
        {
            String script = element.getContent();
            if (script == null)
            {
                script = "";
            }
            action.setScript(script);
        }
        String language = element.getAttribute("language");
        action.setLanguage(language);
        return action;
    }
}
