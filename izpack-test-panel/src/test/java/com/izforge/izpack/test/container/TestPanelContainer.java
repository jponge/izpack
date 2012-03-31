package com.izforge.izpack.test.container;

import java.util.Properties;

import org.fest.swing.fixture.FrameFixture;
import org.mockito.Mockito;
import org.picocontainer.Characteristics;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoException;
import org.picocontainer.injectors.ProviderAdapter;
import org.picocontainer.parameters.ComponentParameter;

import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.api.exception.ContainerException;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.core.rules.ConditionContainer;
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


/**
 * Container for injecting mock for individual panel testing.
 */
public class TestPanelContainer extends AbstractContainer
{

    /**
     * Constructs a <tt>TestPanelContainer</tt>.
     *
     * @throws ContainerException if initialisation fails
     */
    public TestPanelContainer()
    {
        initialise();
    }

    /**
     * Invoked by {@link #initialise} to fill the container.
     *
     * @param container the underlying container
     * @throws ContainerException if initialisation fails
     * @throws PicoException      for any PicoContainer error
     */
    @Override
    protected void fillContainer(MutablePicoContainer container)
    {
        Properties properties = System.getProperties();
        addComponent(properties, properties);
        addComponent(VariableSubstitutor.class, VariableSubstitutorImpl.class);
        addComponent(ResourceManager.class);
        addComponent(InstallerController.class);
        addComponent(UninstallData.class);
        addComponent(ConditionContainer.class);
        addComponent(InstallDataConfiguratorWithRules.class);
        addComponent(UninstallDataWriter.class, Mockito.mock(UninstallDataWriter.class));
        addComponent(AutomatedInstaller.class);

        container.addComponent(FrameFixture.class, FrameFixture.class, new ComponentParameter(InstallerFrame.class));

        addComponent(ObjectFactory.class, Mockito.mock(ObjectFactory.class));
        addComponent(IUnpacker.class, Mockito.mock(IUnpacker.class));
        addComponent(Log.class, Mockito.mock(Log.class));
        addComponent(Housekeeper.class, Mockito.mock(Housekeeper.class));
        addComponent(PanelManager.class);
        addComponent("installerContainer", this);

        container.addConfig("title", "testPanel");

        container
                .addAdapter(new ProviderAdapter(new GUIInstallDataMockProvider()))
                .addAdapter(new ProviderAdapter(new IconsProvider()))
                .addAdapter(new ProviderAdapter(new RulesProvider()))
                .as(Characteristics.USE_NAMES).addComponent(InstallerFrame.class);
    }

}
