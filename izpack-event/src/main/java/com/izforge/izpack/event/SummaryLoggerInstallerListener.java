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

import java.io.File;
import java.io.FileOutputStream;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.handler.AbstractUIProgressHandler;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.util.SummaryProcessor;
import com.izforge.izpack.util.IoHelper;

/**
 * Installer listener which writes the summary of all panels into the logfile which is defined by
 * info.summarylogfilepath. Default is $INSTALL_PATH/Uninstaller/InstallSummary.htm
 *
 * @author Klaus Bartz
 */
public class SummaryLoggerInstallerListener extends SimpleInstallerListener
{

    /**
     * Constructs a <tt>SummaryLoggerInstallerListener</tt>.
     *
     * @param resources the resources
     */
    public SummaryLoggerInstallerListener(Resources resources)
    {
        super(resources, false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.izforge.izpack.compiler.InstallerListener#afterPacks(com.izforge.izpack.installer.AutomatedInstallData,
     * com.izforge.izpack.api.handler.AbstractUIProgressHandler)
     */

    public void afterPacks(AutomatedInstallData idata, AbstractUIProgressHandler handler)
            throws Exception
    {
        if (idata instanceof GUIInstallData)
        {
            GUIInstallData installData = (GUIInstallData) idata;
            if (!installData.isInstallSuccess())
            {
                return;
            }
            // No logfile at automated installation because panels are not
            // involved.
            if (installData.getPanels().isEmpty())
            {
                return;
            }
            String path = installData.getInfo().getSummaryLogFilePath();
            if (path == null)
            {
                return;
            }
            path = IoHelper.translatePath(path, idata.getVariables());
            File parent = new File(path).getParentFile();

            if (!parent.exists())
            {
                parent.mkdirs();
            }

            String summary = SummaryProcessor.getSummary(installData);
            java.io.OutputStream out = new FileOutputStream(path);

            out.write(summary.getBytes("utf-8"));
            out.close();
        }
    }

}
