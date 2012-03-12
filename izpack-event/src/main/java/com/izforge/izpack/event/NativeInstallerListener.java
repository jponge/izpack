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

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.api.exception.WrappedNativeLibException;
import com.izforge.izpack.api.handler.AbstractUIProgressHandler;

/**
 * This class implements some methods which are needed by installer custom actions with native
 * parts.
 *
 * @author Klaus Bartz
 */
public class NativeInstallerListener extends SimpleInstallerListener
{

    /**
     * Constructs a <tt>NativeInstallerListener</tt>.
     *
     * @param resources the resource manager
     */
    public NativeInstallerListener(ResourceManager resources)
    {
        super(resources);
    }

    /**
     * Constructs a <tt>NativeInstallerListener</tt>.
     *
     * @param resources the resource manager
     * @param useSpecHelper if <tt>true</tt> a specification helper will be created
     */
    public NativeInstallerListener(ResourceManager resources, boolean useSpecHelper)
    {
        super(resources, useSpecHelper);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.izforge.izpack.compiler.InstallerListener#beforePacks(com.izforge.izpack.installer.AutomatedInstallData,
     * int, com.izforge.izpack.api.handler.AbstractUIProgressHandler)
     */

    public void beforePacks(AutomatedInstallData idata, Integer npacks,
                            AbstractUIProgressHandler handler) throws Exception
    {
        super.beforePacks(idata, npacks, handler);

        if (idata.getLangpack()!= null)
        { // Initialize WrappedNativeLibException with the langpack for error messages.
            WrappedNativeLibException.setLangpack(idata.getLangpack());
        }

    }

}
