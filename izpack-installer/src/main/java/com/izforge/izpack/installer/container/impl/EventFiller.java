package com.izforge.izpack.installer.container.impl;

import com.izforge.izpack.api.container.BindeableContainer;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.api.event.InstallerListener;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.data.CustomData;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.merge.resolve.ClassPathCrawler;
import com.izforge.izpack.util.OsConstraintHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Fill a container with custom data like events
 *
 * @author Anthonin Bonnefoy
 */
public class EventFiller
{
    private ResourceManager resourceManager;
    private ClassPathCrawler classPathCrawler;
    private BindeableContainer bindeableContainer;
    private AutomatedInstallData installdata;
    private final UninstallData uninstallData;

    public EventFiller(ResourceManager resourceManager, ClassPathCrawler classPathCrawler,
                       BindeableContainer bindeableContainer, AutomatedInstallData installdata,
                       UninstallData uninstallData)
    {
        this.resourceManager = resourceManager;
        this.classPathCrawler = classPathCrawler;
        this.bindeableContainer = bindeableContainer;
        this.installdata = installdata;
        this.uninstallData = uninstallData;
    }

    /**
     * Loads custom data like listener and lib references if exist and fills the installdata.
     */
    @SuppressWarnings("unchecked")
    public void loadCustomData()
    {
        List<CustomData> customData = (List<CustomData>) readObject("customData");
        List<CustomData> uninstallerListeners = new ArrayList<CustomData>();
        List<CustomData> uninstallerJars = new ArrayList<CustomData>();
        List<String> uninstallerLibs = new ArrayList<String>();
        for (CustomData data : customData)
        {
            if (data.osConstraints == null || OsConstraintHelper.oneMatchesCurrentSystem(data.osConstraints))
            {
                switch (data.type)
                {
                    case CustomData.INSTALLER_LISTENER:
                        addInstallerListener(data.listenerName);
                        break;
                    case CustomData.UNINSTALLER_LISTENER:
                        uninstallerListeners.add(data);
                        break;
                    case CustomData.UNINSTALLER_JAR:
                        uninstallerJars.add(data);
                        break;
                    case CustomData.UNINSTALLER_LIB:
                        uninstallerLibs.add(data.contents.get(0));
                        break;
                }
            }
        }
        uninstallData.getAdditionalData().put("uninstallerListeners", uninstallerListeners);
        uninstallData.getAdditionalData().put("uninstallerJars", uninstallerJars);
        uninstallData.getAdditionalData().put("__uninstallLibs__", uninstallerLibs);
    }

    @SuppressWarnings("unchecked")
    private void addInstallerListener(String listenerClass)
    {
        Class aClass = classPathCrawler.searchClassInClassPath(listenerClass);
        bindeableContainer.addComponent(aClass);
        installdata.getInstallerListener().add((InstallerListener) bindeableContainer.getComponent(aClass));
    }

    private Object readObject(String resourceId)
    {
        Object model;
        ObjectInputStream objIn = null;
        try
        {
            InputStream inputStream = resourceManager.getInputStream(resourceId);
            objIn = new ObjectInputStream(inputStream);
            model = objIn.readObject();
        }
        catch (ClassNotFoundException exception)
        {
           throw new IzPackException(exception);
        }
        catch (IOException exception)
        {
            throw new IzPackException(exception);
        }
        finally
        {
            if (objIn != null)
            {
                try
                {
                    objIn.close();
                }
                catch (IOException ignore)
                {
                    // do nothing
                }
            }
        }
        return model;
    }
}
