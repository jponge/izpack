package org.codehaus.izpack.test.container;

import com.izforge.izpack.api.container.BindeableContainer;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.installer.automation.AutomatedInstaller;
import com.izforge.izpack.installer.base.InstallerFrame;
import com.izforge.izpack.installer.container.provider.IconsProvider;
import com.izforge.izpack.installer.container.provider.RulesProvider;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.installer.language.ConditionCheck;
import com.izforge.izpack.installer.manager.PanelManager;
import com.izforge.izpack.merge.resolve.PathResolver;
import com.izforge.izpack.util.substitutor.VariableSubstitutorImpl;
import org.codehaus.izpack.test.provider.GUIInstallDataMockProvider;
import org.fest.swing.fixture.FrameFixture;
import org.picocontainer.Characteristics;
import org.picocontainer.PicoBuilder;
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
    public void initBindings() throws Exception
    {
        pico = new PicoBuilder().withConstructorInjection().withCaching().build();
        pico.addComponent(System.getProperties());

        pico.addComponent(VariableSubstitutor.class, VariableSubstitutorImpl.class)
                .addComponent(ResourceManager.class)
                .addComponent(ConditionCheck.class)
                .addComponent(UninstallData.class)
                .addComponent(UninstallDataWriter.class)
                .addComponent(AutomatedInstaller.class)
                .addComponent(PathResolver.class)
                .addComponent(FrameFixture.class, FrameFixture.class, new ComponentParameter(InstallerFrame.class))
                .addComponent(PanelManager.class)
                .addComponent(BindeableContainer.class, this)
                .addConfig("title", "testPanel");
        pico
                .addAdapter(new ProviderAdapter(new GUIInstallDataMockProvider()))
                .addAdapter(new ProviderAdapter(new IconsProvider()))
                .addAdapter(new ProviderAdapter(new RulesProvider()))
                .as(Characteristics.USE_NAMES).addComponent(InstallerFrame.class);
    }


}
