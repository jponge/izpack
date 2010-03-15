package com.izforge.izpack.installer.manager;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.api.container.BindeableContainer;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.GUIInstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.api.handler.AbstractUIProgressHandler;
import com.izforge.izpack.data.PanelAction;
import com.izforge.izpack.installer.base.IzPanel;
import com.izforge.izpack.installer.unpacker.IUnpacker;
import com.izforge.izpack.merge.panel.PanelMerge;
import com.izforge.izpack.merge.resolve.PathResolver;
import com.izforge.izpack.util.OsConstraintHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Load panels in the container
 */
public class PanelManager
{
    private GUIInstallData installdata;
    private BindeableContainer installerContainer;
    private int lastVis;

    /**
     * Mapping from "raw" panel number to visible panel number.
     */
    protected ArrayList<Integer> visiblePanelMapping;
    private PathResolver pathResolver;

    public PanelManager(GUIInstallData installDataGUI, BindeableContainer installerContainer, PathResolver pathResolver) throws ClassNotFoundException
    {
        this.installdata = installDataGUI;
        this.installerContainer = installerContainer;
        this.pathResolver = pathResolver;
        visiblePanelMapping = new ArrayList<Integer>();
    }

    public Class<? extends IzPanel> resolveClassName(final String className) throws ClassNotFoundException
    {
        PanelMerge panelMerge = pathResolver.getPanelMerge(className);
        return (Class<? extends IzPanel>) Class.forName(panelMerge.getFullClassNameFromPanelName());
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
        java.util.List<Panel> panelsOrder = installdata.getPanelsOrder();
        for (Panel panel : panelsOrder)
        {
            if (OsConstraintHelper.oneMatchesCurrentSystem(panel.osConstraints))
            {
                Class<? extends IzPanel> aClass = resolveClassName(panel.getClassName());
                installerContainer.addComponent(aClass);
            }
        }
        return this;
    }

    /**
     * Construct all panels present in the installerContainer.<br />
     * Executing prebuild, prevalidate, postvalidate and postconstruct actions.
     *
     * @throws ClassNotFoundException
     */
    public void instantiatePanels() throws ClassNotFoundException
    {
        java.util.List<Panel> panelsOrder = installdata.getPanelsOrder();
        int curVisPanelNumber = 0;
        lastVis = 0;
        int count = 0;
        for (Panel panel : panelsOrder)
        {
            if (OsConstraintHelper.oneMatchesCurrentSystem(panel.osConstraints))
            {
                Class<? extends IzPanel> aClass = resolveClassName(panel.getClassName());
                executePreBuildActions(panel);
                IzPanel izPanel = installerContainer.getComponent(aClass);
                izPanel.setMetadata(panel);
                String dataValidator = panel.getValidator();
                if (dataValidator != null)
                {
                    izPanel.setValidationService(DataValidatorFactory.createDataValidator(dataValidator));
                }
                izPanel.setHelps(panel.getHelpsMap());

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
