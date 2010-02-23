package com.izforge.izpack.installer.container.impl;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.api.exception.ResourceNotFoundException;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.container.AbstractChildContainer;
import com.izforge.izpack.installer.base.InstallerFrame;
import com.izforge.izpack.installer.container.IInstallerContainer;
import com.izforge.izpack.installer.language.LanguageDialog;
import com.izforge.izpack.installer.manager.PanelManager;
import com.izforge.izpack.installer.unpacker.IUnpacker;
import org.picocontainer.Characteristics;

import javax.swing.*;
import java.awt.*;

/**
 * Container for panel level component
 */
public class InstallerContainer extends AbstractChildContainer implements IInstallerContainer
{

    public InstallerContainer(ApplicationContainer parent) throws ClassNotFoundException
    {
        super(parent);
        initBindings();
    }

    public void initBindings() throws ClassNotFoundException
    {
        pico.addComponent(IInstallerContainer.class, this)
                .addComponent(PanelManager.class);

        AutomatedInstallData installdata = pico.getComponent(AutomatedInstallData.class);
        VariableSubstitutor substitutor = pico.getComponent(VariableSubstitutor.class);
        String unpackerclassname = installdata.getInfo().getUnpackerClassName();
        Class<IUnpacker> unpackerclass = (Class<IUnpacker>) Class.forName(unpackerclassname);
        pico
                // Configuration of title parameter in InstallerFrame
                .addConfig("title", getTitle(installdata, substitutor))
                        // Configuration of frame parameter in languageDialog
                .addConfig("frame", initFrame());
        pico
                .addComponent(IUnpacker.class, unpackerclass)
                .as(Characteristics.USE_NAMES).addComponent(InstallerFrame.class)
                .as(Characteristics.USE_NAMES).addComponent(LanguageDialog.class);
    }


    private JFrame initFrame()
    {
        ResourceManager resourceManager = pico.getComponent(ResourceManager.class);
        // Dummy Frame
        JFrame frame = new JFrame();
        ImageIcon imageIcon = null;
        try
        {
            imageIcon = resourceManager.getImageIconResource("JFrameIcon");
        }
        catch (ResourceNotFoundException e)
        {
            imageIcon = new ImageIcon(this.getClass().getResource("/img/JFrameIcon.png"));
        }
        frame.setIconImage(imageIcon.getImage());

        Dimension frameSize = frame.getSize();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((screenSize.width - frameSize.width) / 2,
                (screenSize.height - frameSize.height) / 2 - 10);
        return frame;
    }

    private String getTitle(AutomatedInstallData automatedInstallData, VariableSubstitutor vs)
    {
        // Use a alternate message if defined.
        final String key = "installer.reversetitle";
        String message = automatedInstallData.getLangpack().getString(key);
        // message equal to key -> no message defined.
        if (message.indexOf(key) > -1)
        {
            return automatedInstallData.getLangpack().getString("installer.title")
                    + automatedInstallData.getInfo().getAppName();
        }
        else
        { // Attention! The alternate message has to contain the whole message including
            // $APP_NAME and may be $APP_VER.
            return vs.substitute(message);
        }
    }


}
