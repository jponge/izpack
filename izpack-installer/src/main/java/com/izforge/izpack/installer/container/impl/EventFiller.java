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
import com.izforge.izpack.util.file.FileUtils;

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
     * Loads custom data.
     * <p/>
     * This includes:
     * <ul>
     *     <li>installer listeners</li>
     *     <li>uninstaller listeners</li>
     *     <li>uninstaller jars</li>
     *     <li>uninstaller native libraries</li>
     * </ul>
     */
    @SuppressWarnings("unchecked")
    public void loadCustomData()
    {
        List<CustomData> customData = (List<CustomData>) readObject("customData");
        installdata.setInstallerListener(new ArrayList<InstallerListener>());
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
                        uninstallData.addUninstallerListener(data);
                        break;
                    case CustomData.UNINSTALLER_JAR:
                        uninstallData.addJar(data);
                        break;
                    case CustomData.UNINSTALLER_LIB:
                        uninstallData.addNativeLibrary(data.contents.get(0));
                        break;
                }
            }
        }
    }

    /**
     * Adds an installer listener.
     * 
     * @param className the listener class name
     */
    @SuppressWarnings("unchecked")
    private void addInstallerListener(String className)
    {
        Class aClass = classPathCrawler.searchClassInClassPath(className);
        bindeableContainer.addComponent(aClass);
        List<InstallerListener> listeners = installdata.getInstallerListener();
        listeners.add((InstallerListener) bindeableContainer.getComponent(aClass));
    }

    /**
     * Reads an object given its resource path.
     * 
     * @param path the object's resource path
     * @return the object
     * @throws IzPackException if the object cannot be read
     */
    private Object readObject(String path)
    {
        Object model;
        ObjectInputStream objIn = null;
        try
        {
            InputStream inputStream = resourceManager.getInputStream(path);
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
            FileUtils.close(objIn);
        }
        return model;
    }
}
