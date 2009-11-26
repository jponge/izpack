package com.izforge.izpack.installer.data;

import org.picocontainer.injectors.ProviderAdapter;

/**
 * Install data loader
 */
public class InstallDataGuiProvider extends ProviderAdapter {
	public InstallData provideData(){
		InstallData installData = new InstallData();

		return installData;
	}
	
}
