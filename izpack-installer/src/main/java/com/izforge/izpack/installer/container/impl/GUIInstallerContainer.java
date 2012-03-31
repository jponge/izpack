package com.izforge.izpack.installer.container.impl;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.api.exception.ContainerException;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.gui.GUIPrompt;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.base.InstallerController;
import com.izforge.izpack.installer.base.InstallerFrame;
import com.izforge.izpack.installer.container.provider.GUIInstallDataProvider;
import com.izforge.izpack.installer.container.provider.IconsProvider;
import com.izforge.izpack.installer.language.LanguageDialog;
import com.izforge.izpack.installer.manager.PanelManager;
import org.picocontainer.Characteristics;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.injectors.ProviderAdapter;

import javax.swing.*;
import java.awt.*;

/**
 * GUI Installer container.
 */
public class GUIInstallerContainer extends InstallerContainer
{

    /**
     * Constructs a <tt>GUIInstallerContainer</tt>.
     *
     * @throws ContainerException if initialisation fails
     */
    public GUIInstallerContainer()
    {
        initialise();
    }

    /**
     * Constructs a <tt>GUIInstallerContainer</tt>.
     * <p/>
     * This constructor is provided for testing purposes.
     *
     * @param container the underlying container
     * @throws ContainerException if initialisation fails
     */
    protected GUIInstallerContainer(MutablePicoContainer container)
    {
        initialise(container);
    }

    /**
     * Registers components with the container.
     *
     * @param pico the container
     */
    @Override
    protected void registerComponents(MutablePicoContainer pico)
    {
        super.registerComponents(pico);
        pico
                .addAdapter(new ProviderAdapter(new GUIInstallDataProvider()))
                .addAdapter(new ProviderAdapter(new IconsProvider()));

        pico
                .addComponent(PanelManager.class)
                .addComponent(GUIPrompt.class)
                .addComponent(InstallerController.class)
                .addComponent(InstallerFrame.class)
                .addComponent(Log.class)
                .as(Characteristics.USE_NAMES).addComponent(LanguageDialog.class);
    }

    /**
     * Resolve components.
     *
     * @param pico the container
     */
    @Override
    protected void resolveComponents(MutablePicoContainer pico)
    {
        super.resolveComponents(pico);
        AutomatedInstallData installdata = pico.getComponent(AutomatedInstallData.class);
        VariableSubstitutor substitutor = pico.getComponent(VariableSubstitutor.class);
        pico
                // Configuration of title parameter in InstallerFrame
                .addConfig("title", getTitle(installdata, substitutor))
                        // Configuration of frame parameter in languageDialog
                .addConfig("frame", initFrame());
    }

    private JFrame initFrame()
    {
        ResourceManager resourceManager = getComponent(ResourceManager.class);
        // Dummy Frame
        JFrame frame = new JFrame();
        ImageIcon imageIcon;
        imageIcon = resourceManager.getImageIconResource("JFrameIcon", "/com/izforge/izpack/img/JFrameIcon.png");
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
        if (message.contains(key))
        {
            return automatedInstallData.getLangpack().getString("installer.title")
                    + automatedInstallData.getInfo().getAppName();
        }
        else
        { // Attention! The alternate message has to contain the whole message including
            // $APP_NAME and may be $APP_VER.
            try
            {
                return vs.substitute(message);
            }
            catch (Exception e)
            {
                // ignore
            }
        }
        return message;
    }

}
