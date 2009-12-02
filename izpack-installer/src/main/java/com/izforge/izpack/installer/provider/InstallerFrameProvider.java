package com.izforge.izpack.installer.provider;

import com.izforge.izpack.bootstrap.IPanelComponent;
import com.izforge.izpack.data.ResourceManager;
import com.izforge.izpack.gui.IconsDatabase;
import com.izforge.izpack.installer.base.InstallerFrame;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.panels.PanelManager;
import com.izforge.izpack.rules.RulesEngine;
import com.izforge.izpack.util.VariableSubstitutor;
import com.izforge.izpack.util.VariableSubstitutorImpl;
import org.picocontainer.injectors.Provider;

/**
 * Provide installer frame instance
 */
public class InstallerFrameProvider implements Provider {
    private ResourceManager resourceManager;

    public InstallerFrame provide(IPanelComponent panelComponent, ResourceManager resourceManager, GUIInstallData installdata, RulesEngine rules, IconsDatabase icons, PanelManager panelManager, UninstallDataWriter uninstallDataWriter) throws Exception {
        InstallerFrame installerFrame = new InstallerFrame(getTitle(installdata), installdata, rules, icons, panelManager, uninstallDataWriter);

        this.resourceManager = resourceManager;

        installerFrame.init();
        return installerFrame;
    }

    private String getTitle(GUIInstallData installDataGUI) {
        // Use a alternate message if defined.
        final String key = "installer.reversetitle";
        String message = installDataGUI.getLangpack().getString(key);
        // message equal to key -> no message defined.
        if (message.indexOf(key) > -1) {
            return installDataGUI.getLangpack().getString("installer.title")
                    + installDataGUI.getInfo().getAppName();
        } else { // Attention! The alternate message has to contain the whole message including
            // $APP_NAME and may be $APP_VER.
            VariableSubstitutor vs = new VariableSubstitutorImpl(installDataGUI.getVariables());
            return vs.substitute(message, null);
        }
    }


}
