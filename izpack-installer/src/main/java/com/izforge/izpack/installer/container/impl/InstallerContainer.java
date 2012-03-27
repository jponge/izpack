package com.izforge.izpack.installer.container.impl;

import com.izforge.izpack.api.container.BindeableContainer;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.core.container.ConditionContainer;
import com.izforge.izpack.core.factory.DefaultObjectFactory;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;
import com.izforge.izpack.installer.base.InstallDataConfiguratorWithRules;
import com.izforge.izpack.installer.container.provider.RulesProvider;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.installer.requirement.InstallerRequirementChecker;
import com.izforge.izpack.installer.requirement.JDKChecker;
import com.izforge.izpack.installer.requirement.JavaVersionChecker;
import com.izforge.izpack.installer.requirement.LangPackChecker;
import com.izforge.izpack.installer.requirement.LockFileChecker;
import com.izforge.izpack.installer.requirement.RequirementsChecker;
import com.izforge.izpack.installer.unpacker.IUnpacker;
import com.izforge.izpack.merge.MergeManagerImpl;
import com.izforge.izpack.merge.resolve.MergeableResolver;
import com.izforge.izpack.merge.resolve.PathResolver;
import com.izforge.izpack.util.DefaultTargetPlatformFactory;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.Librarian;
import com.izforge.izpack.util.TargetFactory;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.injectors.ProviderAdapter;

import java.util.Properties;

/**
 * Installer container.
 */
public abstract class InstallerContainer extends AbstractContainer
{

    public void fillContainer(MutablePicoContainer pico)
    {
        this.pico = pico;
        registerComponents(pico);
        resolveComponents(pico);
    }

    /**
     * Registers components with the container.
     *
     * @param pico the container
     */
    protected void registerComponents(MutablePicoContainer pico)
    {
        pico.addAdapter(new ProviderAdapter(new RulesProvider()));
        pico.addComponent(InstallDataConfiguratorWithRules.class)
                .addComponent(InstallerRequirementChecker.class)
                .addComponent(JavaVersionChecker.class)
                .addComponent(JDKChecker.class)
                .addComponent(LangPackChecker.class)
                .addComponent(RequirementsChecker.class)
                .addComponent(LockFileChecker.class)
                .addComponent(MergeManagerImpl.class)
                .addComponent(UninstallData.class)
                .addComponent(MutablePicoContainer.class, pico)
                .addComponent(ConditionContainer.class)
                .addComponent(VariableSubstitutor.class, VariableSubstitutorImpl.class)
                .addComponent(Properties.class)
                .addComponent(ResourceManager.class)
                .addComponent(UninstallDataWriter.class)
                .addComponent(EventFiller.class)
                .addComponent(BindeableContainer.class, this)
                .addComponent(RegistryDefaultHandler.class)
                .addComponent(Housekeeper.class)
                .addComponent(Librarian.class)
                .addComponent(TargetFactory.class)
                .addComponent(DefaultTargetPlatformFactory.class)
                .addComponent(DefaultObjectFactory.class)
                .addComponent(PathResolver.class)
                .addComponent(MergeableResolver.class);
    }

    /**
     * Resolve components.
     *
     * @param pico the container
     */
    protected void resolveComponents(MutablePicoContainer pico)
    {
        AutomatedInstallData installData = pico.getComponent(AutomatedInstallData.class);
        String className = installData.getInfo().getUnpackerClassName();
        Class<IUnpacker> unpackerClass = getClass(className, IUnpacker.class);
        pico.addComponent(IUnpacker.class, unpackerClass);

        EventFiller eventFiller = pico.getComponent(EventFiller.class);
        try
        {
            eventFiller.loadCustomData();
        }
        catch (InstallerException exception)
        {
            throw new IzPackException(exception);
        }
    }

}
