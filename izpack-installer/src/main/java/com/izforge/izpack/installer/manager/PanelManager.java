package com.izforge.izpack.installer.manager;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.api.container.BindeableContainer;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.api.handler.AbstractUIProgressHandler;
import com.izforge.izpack.api.installer.DataValidator;
import com.izforge.izpack.api.merge.Mergeable;
import com.izforge.izpack.data.PanelAction;
import com.izforge.izpack.installer.base.IzPanel;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.unpacker.IUnpacker;
import com.izforge.izpack.merge.ClassResolver;
import com.izforge.izpack.merge.resolve.ClassPathCrawler;
import com.izforge.izpack.merge.resolve.PathResolver;
import com.izforge.izpack.merge.resolve.ResolveUtils;
import com.izforge.izpack.util.OsConstraintHelper;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Load panels in the container
 */
public class PanelManager
{
    private GUIInstallData installData;
    private BindeableContainer installerContainer;
    private int lastVis;
    private ClassPathCrawler classPathCrawler;

    /**
     * Mapping from "raw" panel number to visible panel number.
     */
    protected ArrayList<Integer> visiblePanelMapping;

    private PathResolver pathResolver;

    /**
     * The factory for {@link DataValidator} and {@link PanelAction} instances.
     */
    private final ObjectFactory factory;

    /**
     * The logger.
     */
    private final static Logger logger = Logger.getLogger(PanelManager.class.getName());


    /**
     * Constructs a <tt>PanelManager</tt>.
     *
     * @param installData        the installation data
     * @param installerContainer the installer container
     * @param pathResolver       the path resolver
     * @param classPathCrawler   the class path crawler
     * @param factory            the factory for {@link DataValidator} instances
     */
    public PanelManager(GUIInstallData installData, BindeableContainer installerContainer, PathResolver pathResolver,
                        ObjectFactory factory, ClassPathCrawler classPathCrawler)
    {
        this.installData = installData;
        this.installerContainer = installerContainer;
        this.pathResolver = pathResolver;
        this.factory = factory;
        this.classPathCrawler = classPathCrawler;
        visiblePanelMapping = new ArrayList<Integer>();
    }

    /**
     * Parse XML to search all used panels and add them in the pico installerContainer.
     *
     * @return this
     * @throws ClassNotFoundException if a panel implementation cannot be found
     */
    public PanelManager loadPanelsInContainer() throws ClassNotFoundException
    {
        // Initialisation
        // We load each of them
        List<Panel> panelsOrder = installData.getPanelsOrder();

        Map<Object, Class> mapPanel = new HashMap<Object, Class>();
        for (Panel panel : panelsOrder)
        {
            if (OsConstraintHelper.oneMatchesCurrentSystem(panel.getOsConstraints()))
            {
                Class<IzPanel> panelClass = getPanelClass(panel);
                if (panel.getPanelid() != null)
                {
                    mapPanel.put(panel.getPanelid(), panelClass);
                }
                else
                {
                    mapPanel.put(panelClass, panelClass);
                }
            }
        }
        loadClassesInSamePackage(mapPanel);

        return this;
    }

    // TODO - can't see why this is required. The only class that should need to be registered with the pico container
    // is the panel class. Everything else can be constructed within the panel as required
    private void loadClassesInSamePackage(Map<Object, Class> mapPanel)
    {
        Set<Mergeable> mergeableSet = new HashSet<Mergeable>();
        final Set<Package> packageSet = new HashSet<Package>();
        for (Map.Entry<Object, Class> entry : mapPanel.entrySet())
        {
            mergeableSet.addAll(pathResolver.getMergeablePackage(entry.getValue().getPackage()));
            packageSet.add(entry.getValue().getPackage());
        }

        Set<File> files = new HashSet<File>();
        for (Mergeable mergeable : mergeableSet)
        {
            files.addAll(mergeable.recursivelyListFiles(new FileFilter()
            {
                @Override
                public boolean accept(File pathname)
                {
                    return ClassResolver.isFilePathInsidePackageSet(ResolveUtils.convertPathToPosixPath(pathname), packageSet);
                }
            }));
        }
        processPanelFiles(files, mapPanel);
    }

    private void processPanelFiles(Set<File> files, Map<Object, Class> mapPanel)
    {
        for (File file : files)
        {
            if (file.getAbsolutePath().endsWith(".class"))
            {
                Class aClass = classPathCrawler.findClass(ClassResolver.processFileToClassName(file));
                boolean isAbstract = (aClass.getModifiers() & Modifier.ABSTRACT) == Modifier.ABSTRACT;
                if (!aClass.isInterface() && !isAbstract)
                {
                    mapPanel.put(aClass, aClass);
                }
            }
        }
        for (Map.Entry<Object, Class> entry : mapPanel.entrySet())
        {
            logger.fine("Adding class " + entry + " in container");
            installerContainer.addComponent(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Construct all panels present in the installerContainer.<br />
     * Executing prebuild, prevalidate, postvalidate and postconstruct actions.
     *
     * @throws ClassNotFoundException if the class cannot be found
     * @throws IllegalStateException  if the specified class does not extend {@link IzPanel}.
     */
    public void instantiatePanels() throws ClassNotFoundException
    {
        List<Panel> panelsOrder = installData.getPanelsOrder();
        int curVisPanelNumber = 0;
        lastVis = 0;
        int count = 0;
        for (Panel panel : panelsOrder)
        {
            if (OsConstraintHelper.oneMatchesCurrentSystem(panel.getOsConstraints()))
            {
                Class<IzPanel> panelClass = getPanelClass(panel);
                executePreBuildActions(panel);
                IzPanel izPanel;
                if (panel.getPanelid() != null)
                {
                    izPanel = (IzPanel) installerContainer.getComponent(panel.getPanelid());
                }
                else
                {
                    izPanel = installerContainer.getComponent(panelClass);
                }
                izPanel.setMetadata(panel);
                String dataValidator = panel.getValidator();
                if (dataValidator != null)
                {
                    izPanel.setValidationService(factory.create(dataValidator, DataValidator.class));
                }
                izPanel.setHelpUrl(panel.getHelpUrl(installData.getLocaleISO3()));

                preActivateActions(panel, izPanel);
                preValidateAction(panel, izPanel);
                postValidateAction(panel, izPanel);

                installData.getPanels().add(izPanel);
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
                IXMLElement panelRoot = new XMLElementImpl(panel.getClassName(), installData.getXmlData());
                // if set, we add the id as an attribute to the panelRoot
                String panelId = panel.getPanelid();
                if (panelId != null)
                {
                    panelRoot.setAttribute("id", panelId);
                }
                installData.getXmlData().addChild(panelRoot);
            }
            visiblePanelMapping.add(count, lastVis);
        }
    }


    public void executePreBuildActions(Panel panel)
    {
        List<String> preConstructionActions = panel.getPreConstructionActions();
        if (preConstructionActions != null)
        {
            for (String preConstructionAction : preConstructionActions)
            {
                PanelAction action = factory.create(preConstructionAction, PanelAction.class);
                action.initialize(panel.getPanelActionConfiguration(preConstructionAction));
                action.executeAction(installData, null);
            }
        }
    }

    private void preActivateActions(Panel panel, IzPanel izPanel)
    {
        List<String> classNames = panel.getPreActivationActions();
        if (classNames != null)
        {
            for (String className : classNames)
            {
                PanelAction action = factory.create(className, PanelAction.class);
                action.initialize(panel.getPanelActionConfiguration(className));
                izPanel.addPreActivationAction(action);
            }
        }
    }

    private void preValidateAction(Panel panel, IzPanel izPanel)
    {
        List<String> classNames = panel.getPreValidationActions();
        if (classNames != null)
        {
            for (String className : classNames)
            {
                PanelAction action = factory.create(className, PanelAction.class);
                action.initialize(panel.getPanelActionConfiguration(className));
                izPanel.addPreValidationAction(action);
            }
        }
    }

    private void postValidateAction(Panel panel, IzPanel izPanel)
    {
        List<String> classNames = panel.getPostValidationActions();
        if (classNames != null)
        {
            for (String className : classNames)
            {
                PanelAction action = factory.create(className, PanelAction.class);
                action.initialize(panel.getPanelActionConfiguration(className));
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
        return (visiblePanelMapping.get(installData.getPanels().size()) == panelNumber);
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

    /**
     * Returns the implementing class for a panel.
     *
     * @param panel the panel
     * @return the class that implements the panel
     * @throws ClassNotFoundException if the class cannot be found
     * @throws IllegalStateException  if the specified class does not extend {@link IzPanel}.
     */
    @SuppressWarnings("unchecked")
    private Class<IzPanel> getPanelClass(Panel panel) throws ClassNotFoundException
    {
        Class result = Class.forName(panel.getClassName()); // compiler emits fully qualified class names
        if (!IzPanel.class.isAssignableFrom(result))
        {
            throw new IllegalStateException("Class " + result + " does not extend "
                    + IzPanel.class.getName());
        }
        return result;
    }

}
