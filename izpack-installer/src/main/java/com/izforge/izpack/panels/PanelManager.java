package com.izforge.izpack.panels;

import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.bootstrap.IPanelComponent;
import com.izforge.izpack.data.AutomatedInstallData;
import com.izforge.izpack.data.Panel;
import com.izforge.izpack.data.PanelAction;
import com.izforge.izpack.installer.DataValidatorFactory;
import com.izforge.izpack.installer.PanelActionFactory;
import com.izforge.izpack.installer.base.IzPanel;
import com.izforge.izpack.installer.data.InstallData;
import com.izforge.izpack.util.OsConstraint;

import java.util.ArrayList;
import java.util.List;

/**
 * Load panels in the container
 */
public class PanelManager {

    public static String CLASSNAME_PREFIX = "com.izforge.izpack.panels.";

    private InstallData installdata;
    private IPanelComponent panelComponent;
    private int lastVis;

    /**
     * Mapping from "raw" panel number to visible panel number.
     */
    protected ArrayList<Integer> visiblePanelMapping;

    public PanelManager(InstallData installData, IPanelComponent panelComponent) throws ClassNotFoundException {
        this.installdata = installData;
        this.panelComponent = panelComponent;
        visiblePanelMapping = new ArrayList<Integer>();
        loadPanelsInContainer();
    }

    public Class<? extends IzPanel> resolveClassName(String className) throws ClassNotFoundException {
        Class<?> aClass;
        if (!className.contains(".")) {
            aClass = Class.forName(CLASSNAME_PREFIX + className);
        } else {
            aClass = Class.forName(className);
        }
        return (Class<? extends IzPanel>) aClass;
    }

    public void loadPanelsInContainer() throws ClassNotFoundException {
        // Initialisation
        // We load each of them
        java.util.List<Panel> panelsOrder = installdata.getPanelsOrder();
        for (Panel panel : panelsOrder) {
            System.out.println(panel.getClassName());
            if (OsConstraint.oneMatchesCurrentSystem(panel.osConstraints)) {
                Class<? extends IzPanel> aClass = resolveClassName(panel.getClassName());
                panelComponent.addComponent(aClass);
            }
        }
    }

    public void instanciatePanels() throws ClassNotFoundException {
        java.util.List<Panel> panelsOrder = installdata.getPanelsOrder();
        int curVisPanelNumber = 0;
        lastVis = 0;
        int count = 0;
        for (Panel panel : panelsOrder) {
            if (OsConstraint.oneMatchesCurrentSystem(panel.osConstraints)) {
                Class<? extends IzPanel> aClass = resolveClassName(panel.getClassName());

                executePreBuildActions(panel);
                IzPanel izPanel = panelComponent.getComponent(aClass);
                String dataValidator = panel.getValidator();
                if (dataValidator != null) {
                    izPanel.setValidationService(DataValidatorFactory.createDataValidator(dataValidator));
                }
                izPanel.setHelps(panel.getHelpsMap());

                preValidateAction(panel, izPanel);
                postValidateAction(panel, izPanel);

                installdata.getPanels().add(izPanel);
                if (izPanel.isHidden()) {
                    visiblePanelMapping.add(count, -1);
                } else {
                    visiblePanelMapping.add(count, curVisPanelNumber);
                    curVisPanelNumber++;
                    lastVis = count;
                }
                count++;
                // We add the XML data izPanel root
                IXMLElement panelRoot = new XMLElementImpl(panel.getClassName(), installdata.getXmlData());
                // if set, we add the id as an attribute to the panelRoot
                String panelId = panel.getPanelid();
                if (panelId != null) {
                    panelRoot.setAttribute("id", panelId);
                }
                installdata.getXmlData().addChild(panelRoot);
            }
            visiblePanelMapping.add(count, lastVis);
        }
    }


    public void executePreBuildActions(Panel panel) {
        List<String> preConstgructionActions = panel.getPreConstructionActions();
        if (preConstgructionActions != null) {
            for (String preConstgructionAction : preConstgructionActions) {
                PanelAction action = PanelActionFactory.createPanelAction(preConstgructionAction);
                action.initialize(panel.getPanelActionConfiguration(preConstgructionAction));
                action.executeAction(AutomatedInstallData.getInstance(), null);
            }
        }
    }

    private void preValidateAction(Panel panel, IzPanel izPanel) {
        List<String> preActivateActions = panel.getPreActivationActions();
        if (preActivateActions != null) {
            for (String panelActionClass : preActivateActions) {
                PanelAction action = PanelActionFactory.createPanelAction(panelActionClass);
                action.initialize(panel.getPanelActionConfiguration(panelActionClass));
                izPanel.addPreActivationAction(action);
            }
        }
    }

    private void postValidateAction(Panel panel, IzPanel izPanel) {
        List<String> postValidateActions = panel.getPostValidationActions();
        if (postValidateActions != null) {
            for (String panelActionClass : postValidateActions) {
                PanelAction action = PanelActionFactory.createPanelAction(panelActionClass);
                action.initialize(panel.getPanelActionConfiguration(panelActionClass));
                izPanel.addPostValidationAction(action);
            }
        }
    }

    public boolean isVisible(int panelNumber) {
        return !(visiblePanelMapping.get(panelNumber) == -1);
    }

    public boolean isLast(int panelNumber) {
        return (visiblePanelMapping.get(installdata.getPanels().size()) == panelNumber);
    }

    public int getPanelVisibilityNumber(int panel) {
        return visiblePanelMapping.get(panel);
    }


//    visiblePanelMapping.get(installdata.getPanels().size()) == installdata.getCurPanelNumber()

    public int getCountVisiblePanel() {
        return lastVis;
    }
}
