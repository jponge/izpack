package com.izforge.izpack.installer.container.impl;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.exception.ResourceNotFoundException;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.core.container.filler.ResolverContainerFiller;
import com.izforge.izpack.installer.automation.AutomatedInstaller;
import com.izforge.izpack.installer.base.InstallerFrame;
import com.izforge.izpack.installer.console.ConsoleInstaller;
import com.izforge.izpack.installer.container.provider.GUIInstallDataProvider;
import com.izforge.izpack.installer.container.provider.IconsProvider;
import com.izforge.izpack.installer.container.provider.RulesProvider;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.installer.language.ConditionCheck;
import com.izforge.izpack.installer.language.LanguageDialog;
import com.izforge.izpack.installer.manager.PanelManager;
import com.izforge.izpack.installer.unpacker.IUnpacker;
import com.izforge.izpack.merge.MergeManagerImpl;
import com.izforge.izpack.util.substitutor.VariableSubstitutorImpl;
import org.picocontainer.Characteristics;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.injectors.ProviderAdapter;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;

/**
 * Application Component. <br />
 * Encapsulate the pico provider for application level component.
 */
public class InstallerContainer extends AbstractContainer
{

    public void fillContainer(MutablePicoContainer pico)
    {
        this.pico = pico;
        pico
//                .addAdapter(new ProviderAdapter(new AutomatedInstallDataProvider()))
                .addAdapter(new ProviderAdapter(new GUIInstallDataProvider()))
                .addAdapter(new ProviderAdapter(new IconsProvider()))
                .addAdapter(new ProviderAdapter(new RulesProvider()));
        pico
                .addComponent(PanelManager.class)
                .addComponent(ConditionCheck.class)
                .addComponent(MergeManagerImpl.class)
                .addComponent(UninstallData.class)
                .addComponent(CustomDataContainer.class)
                .addComponent(VariableSubstitutor.class, VariableSubstitutorImpl.class)
                .addComponent(Properties.class)
                .addComponent(ResourceManager.class)
                .addComponent(ConsoleInstaller.class)
                .addComponent(UninstallDataWriter.class)
                .addComponent(AutomatedInstaller.class)
                .addComponent(this);

        new ResolverContainerFiller().fillContainer(pico);

        AutomatedInstallData installdata = pico.getComponent(AutomatedInstallData.class);
        VariableSubstitutor substitutor = pico.getComponent(VariableSubstitutor.class);
        String unpackerclassname = installdata.getInfo().getUnpackerClassName();
        Class<IUnpacker> unpackerclass = null;
        try
        {
            unpackerclass = (Class<IUnpacker>) Class.forName(unpackerclassname);
        }
        catch (ClassNotFoundException e)
        {
            throw new IzPackException(e);
        }
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
        ImageIcon imageIcon;
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
