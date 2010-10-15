package com.izforge.izpack.installer.manager;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.api.container.BindeableContainer;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.api.handler.AbstractUIProgressHandler;
import com.izforge.izpack.api.merge.Mergeable;
import com.izforge.izpack.data.PanelAction;
import com.izforge.izpack.installer.base.IzPanel;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.unpacker.IUnpacker;
import com.izforge.izpack.merge.ClassResolver;
import com.izforge.izpack.merge.resolve.ClassPathCrawler;
import com.izforge.izpack.merge.resolve.MergeableResolver;
import com.izforge.izpack.merge.resolve.PathResolver;
import com.izforge.izpack.util.OsConstraintHelper;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Load panels in the container
 */
public class PanelManager
{
    private GUIInstallData installdata;
    private BindeableContainer installerContainer;
    private int lastVis;
    private ClassPathCrawler classPathCrawler;
    private final static Logger LOGGER = Logger.getLogger(PanelManager.class.getName());

    /**
     * Mapping from "raw" panel number to visible panel number.
     */
    protected ArrayList<Integer> visiblePanelMapping;
    private PathResolver pathResolver;

    public PanelManager(GUIInstallData installDataGUI, BindeableContainer installerContainer, PathResolver pathResolver, MergeableResolver mergeableResolver, ClassPathCrawler classPathCrawler) throws ClassNotFoundException
    {
        this.installdata = installDataGUI;
        this.installerContainer = installerContainer;
        this.pathResolver = pathResolver;
        this.classPathCrawler = classPathCrawler;
        visiblePanelMapping = new ArrayList<Integer>();
    }

    /**
     * Parse XML to search all used panels and add them in the pico installerContainer.
     *
     * @throws ClassNotFoundException
     */
    public PanelManager loadPanelsInContainer() throws ClassNotFoundException
    {
        // Initialisation
        // We load each of them
        List<Panel> panelsOrder = installdata.getPanelsOrder();
        List<Class> listPanelClass = new ArrayList<Class>();
        for (Panel panel : panelsOrder)
        {
            if (OsConstraintHelper.oneMatchesCurrentSystem(panel.getOsConstraints()))
            {
                final Class<? extends IzPanel> panelClass = classPathCrawler.searchClassInClassPath(panel.getClassName());
                listPanelClass.add(panelClass);
            }
        }
        loadClassesInSamePackage(listPanelClass);

        return this;
    }

    private void loadClassesInSamePackage(List<Class> listPanelClass)
    {
        Set<Mergeable> mergeableSet = new HashSet<Mergeable>();
        final Set<Package> packageSet = new HashSet<Package>();
        for (Class aClass : listPanelClass)
        {
            mergeableSet.addAll(pathResolver.getMergeablePackage(aClass.getPackage()));
            packageSet.add(aClass.getPackage());
        }

        for (Mergeable mergeable : mergeableSet)
        {
            List<File> files = mergeable.recursivelyListFiles(new FileFilter()
            {
                @Override
                public boolean accept(File pathname)
                {
                    return ClassResolver.isFilePathInsidePackageSet(pathname.getAbsolutePath(), packageSet);
                }
            });
            for (File file : files)
            {
                if (file.getAbsolutePath().endsWith(".class"))
                {
                    Class aClass = classPathCrawler.searchClassInClassPath(ClassResolver.processFileToClassName(file));
                    boolean isAbstract = (aClass.getModifiers() & Modifier.ABSTRACT) == Modifier.ABSTRACT;
                    if (!aClass.isInterface() && !isAbstract)
                    {
                        LOGGER.log(Level.INFO, "Adding class " + aClass + " in container");
                        installerContainer.addComponent(aClass);
                    }
                }
            }
        }
    }

    /**
     * Construct all panels present in the installerContainer.<br />
     * Executing prebuild, prevalidate, postvalidate and postconstruct actions.
     *
     * @throws ClassNotFoundException
     */
    public void instantiatePanels() throws ClassNotFoundException
    {
        List<Panel> panelsOrder = installdata.getPanelsOrder();
        int curVisPanelNumber = 0;
        lastVis = 0;
        int count = 0;
        for (Panel panel : panelsOrder)
        {
            if (OsConstraintHelper.oneMatchesCurrentSystem(panel.getOsConstraints()))
            {
                Class<? extends IzPanel> aClass = classPathCrawler.searchClassInClassPath(panel.getClassName());
                executePreBuildActions(panel);
                IzPanel izPanel = installerContainer.getComponent(aClass);
                izPanel.setMetadata(panel);
                String dataValidator = panel.getValidator();
                if (dataValidator != null)
                {
                    izPanel.setValidationService(DataValidatorFactory.createDataValidator(dataValidator));
                }
                izPanel.setHelpUrl(panel.getHelpUrl(installdata.getLocaleISO3()));

                preValidateAction(panel, izPanel);
                postValidateAction(panel, izPanel);

                installdata.getPanels().add(izPanel);
                if (izPanel.isHidden())
                {
                    visiblePanelMapping.add(count, -1);
                }
                else
                {
                    visiblePanelMapping.add(count, curVisPanelNumber);
                    curVisPanelNumber++;
                    lastVis = count;
                }
                count++;
                // We add the XML installDataGUI izPanel root
                IXMLElement panelRoot = new XMLElementImpl(panel.getClassName(), installdata.getXmlData());
                // if set, we add the id as an attribute to the panelRoot
                String panelId = panel.getPanelid();
                if (panelId != null)
                {
                    panelRoot.setAttribute("id", panelId);
                }
                installdata.getXmlData().addChild(panelRoot);
            }
            visiblePanelMapping.add(count, lastVis);
        }
    }


    public void executePreBuildActions(Panel panel)
    {
        List<String> preConstgructionActions = panel.getPreConstructionActions();
        if (preConstgructionActions != null)
        {
            for (String preConstgructionAction : preConstgructionActions)
            {
                PanelAction action = PanelActionFactory.createPanelAction(preConstgructionAction);
                action.initialize(panel.getPanelActionConfiguration(preConstgructionAction));
                action.executeAction(AutomatedInstallData.getInstance(), null);
            }
        }
    }

    private void preValidateAction(Panel panel, IzPanel izPanel)
    {
        List<String> preActivateActions = panel.getPreActivationActions();
        if (preActivateActions != null)
        {
            for (String panelActionClass : preActivateActions)
            {
                PanelAction action = PanelActionFactory.createPanelAction(panelActionClass);
                action.initialize(panel.getPanelActionConfiguration(panelActionClass));
                izPanel.addPreActivationAction(action);
            }
        }
    }

    private void postValidateAction(Panel panel, IzPanel izPanel)
    {
        List<String> postValidateActions = panel.getPostValidationActions();
        if (postValidateActions != null)
        {
            for (String panelActionClass : postValidateActions)
            {
                PanelAction action = PanelActionFactory.createPanelAction(panelActionClass);
                action.initialize(panel.getPanelActionConfiguration(panelActionClass));
                izPanel.addPostValidationAction(action);
            }
        }
    }

    public boolean isVisible(int panelNumber)
    {
        return !(visiblePanelMapping.get(panelNumber) == -1);
    }

    public boolean isLast(int panelNumber)
    {
        return (visiblePanelMapping.get(installdata.getPanels().size()) == panelNumber);
    }

    public int getPanelVisibilityNumber(int panel)
    {
        return visiblePanelMapping.get(panel);
    }

    public void setAbstractUIHandlerInContainer(AbstractUIHandler abstractUIHandlerInContainer)
    {
//        installerContainer.removeComponent(AbstractUIHandler.class);
//        installerContainer.addComponent(AbstractUIHandler.class, abstractUIHandlerInContainer);
    }

    public int getCountVisiblePanel()
    {
        return lastVis;
    }

    public IUnpacker getUnpacker(AbstractUIProgressHandler listener)
    {
        setAbstractUIHandlerInContainer(listener);
        return installerContainer.getComponent(IUnpacker.class);
    }
}
