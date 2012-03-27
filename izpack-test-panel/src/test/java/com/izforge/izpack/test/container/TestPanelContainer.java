package com.izforge.izpack.test.container;

import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.core.container.ConditionContainer;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.automation.AutomatedInstaller;
import com.izforge.izpack.installer.base.InstallDataConfiguratorWithRules;
import com.izforge.izpack.installer.base.InstallerController;
import com.izforge.izpack.installer.base.InstallerFrame;
import com.izforge.izpack.installer.container.provider.IconsProvider;
import com.izforge.izpack.installer.container.provider.RulesProvider;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.installer.manager.PanelManager;
import com.izforge.izpack.installer.unpacker.IUnpacker;
import com.izforge.izpack.test.provider.GUIInstallDataMockProvider;
import com.izforge.izpack.util.Housekeeper;
import org.fest.swing.fixture.FrameFixture;
import org.mockito.Mockito;
import org.picocontainer.Characteristics;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.injectors.ProviderAdapter;
import org.picocontainer.parameters.ComponentParameter;

/**
 * Container for injecting mock for individual panel testing
 */
public class TestPanelContainer extends AbstractContainer
{

    /**
     * Init component bindings
     */
    public void fillContainer(MutablePicoContainer pico)
    {
        this.pico = pico;
        pico.addComponent(System.getProperties());
        pico.addComponent(VariableSubstitutor.class, VariableSubstitutorImpl.class)
                .addComponent(ResourceManager.class)
                .addComponent(InstallerController.class)
                .addComponent(UninstallData.class)
                .addComponent(MutablePicoContainer.class, pico)
                .addComponent(ConditionContainer.class)
                .addComponent(InstallDataConfiguratorWithRules.class)
                .addComponent(Mockito.mock(UninstallDataWriter.class))
                .addComponent(AutomatedInstaller.class)
                .addComponent(FrameFixture.class, FrameFixture.class, new ComponentParameter(InstallerFrame.class))
                .addComponent(Mockito.mock(ObjectFactory.class))
                .addComponent(Mockito.mock(IUnpacker.class))
                .addComponent(Mockito.mock(Log.class))
                .addComponent(Mockito.mock(Housekeeper.class))
                .as(Characteristics.USE_NAMES).addComponent(PanelManager.class)
                .addComponent("installerContainer", this)
                .addConfig("title", "testPanel");

        pico
                .addAdapter(new ProviderAdapter(new GUIInstallDataMockProvider()))
                .addAdapter(new ProviderAdapter(new IconsProvider()))
                .addAdapter(new ProviderAdapter(new RulesProvider()))
                .as(Characteristics.USE_NAMES).addComponent(InstallerFrame.class);
    }


}
