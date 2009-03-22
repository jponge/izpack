/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2002 Jan Blok
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

import java.io.PrintWriter;
import java.util.Properties;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelConsole;
import com.izforge.izpack.installer.PanelConsoleHelper;
/**
 * Finish Panel console helper
 *
 * @author Mounir el hajj
 */
public class FinishPanelConsoleHelper extends PanelConsoleHelper implements PanelConsole {
	public boolean runGeneratePropertiesFile(AutomatedInstallData installData,PrintWriter printWriter) {
		return true;
	}
	
	public boolean runConsoleFromPropertiesFile(AutomatedInstallData installData, Properties p){
		return true;
	}

	public boolean runConsole(AutomatedInstallData idata) {
		if (idata.installSuccess) {
			System.out.println("Install was successeful");
			System.out.println("application installed on " + idata.getInstallPath());

		} else {
			System.out.println("Install Failed!!!");
		}
		return true;
	}
}
