package com.izforge.izpack.installer.provider;

import com.izforge.izpack.bootstrap.IPanelComponent;
import com.izforge.izpack.gui.IconsDatabase;
import com.izforge.izpack.installer.base.GUIInstaller;
import com.izforge.izpack.installer.base.InstallerFrame;
import com.izforge.izpack.installer.data.InstallData;
import com.izforge.izpack.rules.RulesEngine;
import com.izforge.izpack.util.VariableSubstitutor;
import com.izforge.izpack.util.VariableSubstitutorImpl;
import org.picocontainer.injectors.Provider;

/**
 * Provide installer frame instance
 */
public class InstallerFrameProvider implements Provider {
    public InstallerFrame provide(InstallData installdata, RulesEngine rules, IconsDatabase icons, IPanelComponent iPanelComponent) throws Exception {
        InstallerFrame installerFrame = new InstallerFrame(getTitle(installdata), installdata, rules, icons,iPanelComponent);
        installerFrame.init();
        return installerFrame;
    }

    private String getTitle(InstallData installData) {
        // Use a alternate message if defined.
        final String key = "installer.reversetitle";
        String message = installData.getLangpack().getString(key);
        // message equal to key -> no message defined.
        if (message.indexOf(key) > -1) {
            return installData.getLangpack().getString("installer.title")
                    + installData.getInfo().getAppName();
        } else { // Attention! The alternate message has to contain the whole message including
            // $APP_NAME and may be $APP_VER.
            VariableSubstitutor vs = new VariableSubstitutorImpl(installData.getVariables());
            return vs.substitute(message, null);
        }
    }
}
