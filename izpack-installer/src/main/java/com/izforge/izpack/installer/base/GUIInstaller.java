/*
 * $Id$
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
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

package com.izforge.izpack.installer.base;

import com.izforge.izpack.bootstrap.IApplicationComponent;
import com.izforge.izpack.data.ResourceManager;
import com.izforge.izpack.installer.data.GUIInstallData;

import javax.swing.*;

/**
 * The IzPack graphical installer class.
 *
 * @author Julien Ponge
 */
public class GUIInstaller extends InstallerBase {

    /**
     * The installation data.
     */
    private GUIInstallData installdata;

    /**
     * Checker for java version, JDK and running install
     */
    private ConditionCheck conditionCheck;
    /**
     * Application component
     */
    private IApplicationComponent applicationComponent;


    /**
     * The constructor.
     *
     * @param installdata
     * @throws Exception Description of the Exception
     */
    public GUIInstaller(GUIInstallData installdata, ResourceManager resourceManager, ConditionCheck conditionCheck) throws Exception {
        super(resourceManager);
        this.installdata = installdata;
        this.conditionCheck = conditionCheck;
//        initLangPack();
    }

    private void showFatalError(Throwable e) {
        try {
            JOptionPane.showMessageDialog(null, "Error: " + e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

}
