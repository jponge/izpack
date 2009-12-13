package com.izforge.izpack.container;

import com.izforge.izpack.data.AutomatedInstallData;
import com.izforge.izpack.data.ResourceManager;
import com.izforge.izpack.installer.base.InstallerFrame;
import com.izforge.izpack.installer.base.LanguageDialog;
import com.izforge.izpack.installer.unpacker.IUnpacker;
import com.izforge.izpack.panels.PanelManager;
import com.izforge.izpack.util.VariableSubstitutor;
import com.izforge.izpack.util.VariableSubstitutorImpl;
import org.picocontainer.parameters.ComponentParameter;
import org.picocontainer.parameters.ConstantParameter;

import javax.swing.*;
import java.awt.*;

/**
 * Container for panel level component
 */
public class InstallerContainer extends AbstractChildContainer {

    public InstallerContainer(IApplicationContainer parent) throws ClassNotFoundException {
        super(parent);
        initBindings();
    }

    public void initBindings() throws ClassNotFoundException {
        pico
                .addComponent(IInstallerContainer.class, this)
                .addComponent(CustomDataContainer.class)
                .addComponent(PanelManager.class);
        addVariablerComponent();
    }

    private void addVariablerComponent() throws ClassNotFoundException {
        AutomatedInstallData installdata = pico.getComponent(AutomatedInstallData.class);
        String unpackerclassname = installdata.getInfo().getUnpackerClassName();
        Class<IUnpacker> unpackerclass = (Class<IUnpacker>) Class.forName(unpackerclassname);
        pico
                .addComponent(IUnpacker.class, unpackerclass)
                .addComponent(InstallerFrame.class, InstallerFrame.class,
                        new ConstantParameter(getTitle(installdata)),
                        new ComponentParameter(),
                        new ComponentParameter(),
                        new ComponentParameter(),
                        new ComponentParameter(),
                        new ComponentParameter(),
                        new ComponentParameter()
                )
                .addComponent(LanguageDialog.class, LanguageDialog.class,
                        new ConstantParameter(initFrame()),
                        new ComponentParameter(),
                        new ComponentParameter(),
                        new ComponentParameter()
                );
    }


    private JFrame initFrame() {
        ResourceManager resourceManager = pico.getComponent(ResourceManager.class);
        // Dummy Frame
        JFrame frame = new JFrame();
        frame.setIconImage(
                resourceManager.getImageIconResource("/img/JFrameIcon.png").getImage()
        );
        Dimension frameSize = frame.getSize();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((screenSize.width - frameSize.width) / 2,
                (screenSize.height - frameSize.height) / 2 - 10);
        return frame;
    }

    private String getTitle(AutomatedInstallData automatedInstallData) {
        // Use a alternate message if defined.
        final String key = "installer.reversetitle";
        String message = automatedInstallData.getLangpack().getString(key);
        // message equal to key -> no message defined.
        if (message.indexOf(key) > -1) {
            return automatedInstallData.getLangpack().getString("installer.title")
                    + automatedInstallData.getInfo().getAppName();
        } else { // Attention! The alternate message has to contain the whole message including
            // $APP_NAME and may be $APP_VER.
            VariableSubstitutor vs = new VariableSubstitutorImpl(automatedInstallData.getVariables());
            return vs.substitute(message, null);
        }
    }


}
