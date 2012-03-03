/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package com.izforge.izpack.compiler.container;

import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.installer.container.impl.ConsoleInstallerContainer;
import com.izforge.izpack.installer.container.impl.InstallerContainer;
import com.izforge.izpack.test.junit.UnloadJarRule;
import org.junit.Rule;
import org.junit.runners.model.FrameworkMethod;
import org.picocontainer.MutablePicoContainer;

/**
 * Enter descroption.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public abstract class AbstractTestInstallationContainer extends AbstractContainer
{
    protected Class klass;
    protected FrameworkMethod frameworkMethod;
    @Rule
    public UnloadJarRule unloadJarRule = new UnloadJarRule();

    public AbstractTestInstallationContainer(Class klass, FrameworkMethod frameworkMethod)
    {
        this.klass = klass;
        this.frameworkMethod = frameworkMethod;
    }

    @Override
    public void fillContainer(MutablePicoContainer picoContainer)
    {
        TestCompilationContainer testInstallationContainer = new TestCompilationContainer(klass, frameworkMethod);
        testInstallationContainer.initBindings();
        testInstallationContainer.launchCompilation();

        InstallerContainer installerContainer = createInstallerContainer();
        installerContainer.fillContainer(pico);
    }
    
    protected abstract InstallerContainer createInstallerContainer();
}
