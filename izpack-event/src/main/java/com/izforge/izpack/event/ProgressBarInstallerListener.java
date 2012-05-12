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

package com.izforge.izpack.event;

import java.util.logging.Logger;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.handler.AbstractUIProgressHandler;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.util.ExtendedUIProgressHandler;

/**
 * Installer listener for reset the progress bar and initialize the simple installer listener to
 * support progress bar interaction. To support progress bar interaction add this installer listener
 * as first listener.
 *
 * @author Klaus Bartz
 */
public class ProgressBarInstallerListener extends SimpleInstallerListener
{
    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(ProgressBarInstallerListener.class.getName());

    /**
     * Constructs a <tt>ProgressBarInstallerListener</tt>.
     *
     * @param resources the resources
     */
    public ProgressBarInstallerListener(Resources resources)
    {
        super(resources, false);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.izforge.izpack.compiler.InstallerListener#afterPacks(com.izforge.izpack.installer.AutomatedInstallData,
     * com.izforge.izpack.api.handler.AbstractUIProgressHandler)
     */

    @Override
    public void afterPacks(AutomatedInstallData idata, AbstractUIProgressHandler handler)
            throws Exception
    {
        if (handler instanceof ExtendedUIProgressHandler && getProgressBarCallerCount() > 0)
        {
            String progress = getMsg("CustomActions.progress");
            String tip = getMsg("CustomActions.tip");
            if ("CustomActions.tip".equals(tip) || "CustomActions.progress".equals(progress))
            {
                logger.fine("No messages found for custom action progress bar interactions; skipped");
                return;
            }
            ((ExtendedUIProgressHandler) handler).restartAction("Configure", progress, tip,
                                                                getProgressBarCallerCount());

            // TODO - this is extremely smelly
            SimpleInstallerListener.doInformProgressBar = true;
        }
    }

}
