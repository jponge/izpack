package com.izforge.izpack.core.container.filler;

import com.izforge.izpack.api.container.BindeableContainer;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.api.data.binding.IzpackProjectInstaller;
import com.izforge.izpack.api.data.binding.Listener;
import com.izforge.izpack.api.event.InstallerListener;
import com.izforge.izpack.api.exception.IzPackException;
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

    public EventFiller(ResourceManager resourceManager, ClassPathCrawler classPathCrawler, BindeableContainer bindeableContainer, AutomatedInstallData installdata)
    {
        this.resourceManager = resourceManager;
        this.classPathCrawler = classPathCrawler;
        this.bindeableContainer = bindeableContainer;
        this.installdata = installdata;
    }

    /**
     * Loads custom data like listener and lib references if exist and fills the installdata.
     *
     * @throws Exception
     */
    public void loadCustomData()
    {
        try
        {
            IzpackProjectInstaller izpackModel = (IzpackProjectInstaller) readObject("izpackInstallModel");
            List<InstallerListener> customActions = new ArrayList<InstallerListener>();
            for (Listener listener : izpackModel.getListeners())
            {
                if (!OsConstraintHelper.oneMatchesCurrentSystem(listener.getOs()))
                {
                    continue;
                }
                switch (listener.getStage())
                {
                    case install:
                        Class aClass = classPathCrawler.searchClassInClassPath(listener.getClassname());
                        bindeableContainer.addComponent(aClass);
                        customActions.add((InstallerListener) bindeableContainer.getComponent(aClass));
                        break;
                    case uninstall:
                }
            }
            installdata.setInstallerListener(customActions);
            // uninstallerLib list if exist
        }
        catch (IOException e)
        {
            throw new IzPackException("Error when reading custom data (events)", e);
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    public Object readObject(String resourceId) throws IOException, ClassNotFoundException
    {
        InputStream inputStream = resourceManager.getInputStream(resourceId);
        ObjectInputStream objIn = new ObjectInputStream(inputStream);
        Object model = objIn.readObject();
        objIn.close();
        return model;
    }
}
