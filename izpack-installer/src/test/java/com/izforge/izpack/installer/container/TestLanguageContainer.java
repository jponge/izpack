package com.izforge.izpack.installer.container;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.Arrays;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import org.fest.swing.fixture.DialogFixture;
import org.mockito.Mockito;
import org.picocontainer.Characteristics;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoException;
import org.picocontainer.injectors.ProviderAdapter;
import org.picocontainer.parameters.ComponentParameter;

import com.izforge.izpack.api.container.Container;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.exception.ContainerException;
import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.core.resource.DefaultLocales;
import com.izforge.izpack.core.resource.ResourceManager;
import com.izforge.izpack.installer.automation.AutomatedInstaller;
import com.izforge.izpack.installer.container.provider.IconsProvider;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.installer.language.LanguageDialog;
import com.izforge.izpack.installer.requirement.RequirementsChecker;
import com.izforge.izpack.merge.resolve.PathResolver;
import com.izforge.izpack.test.provider.GUIInstallDataMockProvider;

/**
 * Container for test language
 *
 * @author Anthonin Bonnefoy
 */
public class TestLanguageContainer extends AbstractContainer
{

    /**
     * Constructs a <tt>TestLanguageContainer</tt>.
     */
    public TestLanguageContainer()
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
        container.addComponent(System.getProperties());

        ResourceManager resourceManager = Mockito.mock(ResourceManager.class);
        Mockito.when(resourceManager.getObject("langpacks.info")).thenReturn(Arrays.asList("eng", "fra"));

        DefaultLocales locales = new DefaultLocales(resourceManager);
        container.addComponent(Variables.class, DefaultVariables.class)
                .addComponent(resourceManager)
                .addComponent(Mockito.mock(RequirementsChecker.class))
                .addComponent(Mockito.mock(UninstallData.class))
                .addComponent(Mockito.mock(UninstallDataWriter.class))
                .addComponent(Mockito.mock(AutomatedInstaller.class))
                .addComponent(Mockito.mock(PathResolver.class))
                .addComponent(locales)
                .addComponent(DialogFixture.class, DialogFixture.class, new ComponentParameter(LanguageDialog.class))
                .addComponent(Container.class, this)
                .as(Characteristics.USE_NAMES).addComponent(LanguageDialog.class)
                .addConfig("frame", initFrame())
                .addConfig("title", "testPanel");
        container
                .addAdapter(new ProviderAdapter(new GUIInstallDataMockProvider()))
                .addAdapter(new ProviderAdapter(new IconsProvider()));

        ImageIcon engFlag = new ImageIcon(getClass().getResource("/com/izforge/izpack/bin/langpacks/flags/eng.gif"));
        ImageIcon frFlag = new ImageIcon(getClass().getResource("/com/izforge/izpack/bin/langpacks/flags/fra.gif"));
        Mockito.when(resourceManager.getImageIcon("flag.eng")).thenReturn(engFlag);
        Mockito.when(resourceManager.getImageIcon("flag.fra")).thenReturn(frFlag);
        Mockito.when(resourceManager.getInputStream(Mockito.anyString())).thenReturn(
                getClass().getResourceAsStream("/com/izforge/izpack/bin/langpacks/installer/eng.xml"));
    }

    private JFrame initFrame()
    {
        // Dummy Frame
        JFrame frame = new JFrame();
        Dimension frameSize = frame.getSize();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((screenSize.width - frameSize.width) / 2,
                          (screenSize.height - frameSize.height) / 2 - 10);
        return frame;
    }
}
