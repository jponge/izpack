package com.izforge.izpack.panels;

import com.izforge.izpack.bootstrap.IPanelComponent;
import com.izforge.izpack.data.Panel;
import com.izforge.izpack.installer.data.InstallData;
import com.izforge.izpack.util.OsConstraint;

/**
 * Load panels in the container
 */
public class PanelManager {

    public static String CLASSNAME_PREFIX = "com.izforge.izpack.panels.";

    private InstallData installdata;
    private IPanelComponent panelComponent;

    public PanelManager(InstallData installData, IPanelComponent panelComponent) {
        this.installdata = installData;
        this.panelComponent = panelComponent;
    }

    public Class<?> resolveClassName(String className) throws ClassNotFoundException {
        Class<?> aClass;
        if (!className.contains(".")) {
            aClass = Class.forName(CLASSNAME_PREFIX + className);
        } else {
            aClass = Class.forName(className);
        }
        return aClass;
    }

    public void loadPanelsInContainer() throws ClassNotFoundException {
        // Initialisation
        java.util.List<Panel> panelsOrder = installdata.getPanelsOrder();
        for (Panel panel : panelsOrder) {
            if (!OsConstraint.oneMatchesCurrentSystem(panel.osConstraints)) {
                panelComponent.addComponent(resolveClassName(panel.getClassName()));
            }
        }
//
//        int i;
//        int size = panelsOrder.size();
//        String className;
//        Class objectClass;
//        Constructor constructor;
//        Object object;
//        IzPanel panel;
//        Class[] paramsClasses = new Class[2];
//        paramsClasses[0] = Class.forName("com.izforge.izpack.installer.base.InstallerFrame");
//        paramsClasses[1] = Class.forName("com.izforge.izpack.installer.data.InstallData");
//        Object[] params = {this, installdata};
//
//        // We load each of them
//        int curVisPanelNumber = 0;
//        int lastVis = 0;
//        int count = 0;
//        for (i = 0; i < size; i++) {
//            // We add the panel
//            Panel p = panelsOrder.get(i);

//            className = p.className;
//            String praefix = "com.izforge.izpack.panels.";
//            if (className.indexOf('.') > -1)
//            // Full qualified class name
//            {
//                praefix = "";
//            }
//            objectClass = Class.forName(praefix + className);
//            constructor = objectClass.getDeclaredConstructor(paramsClasses);
//            installdata.currentPanel = p; // A hack to use meta data in IzPanel constructor
//            // Do not call constructor of IzPanel or it's derived at an other place else
//            // metadata will be not set.
//            List<String> preConstgructionActions = p.getPreConstructionActions();
//            if (preConstgructionActions != null) {
//                for (int actionIndex = 0; actionIndex < preConstgructionActions.size(); actionIndex++) {
//                    PanelAction action = PanelActionFactory.createPanelAction(preConstgructionActions.get(actionIndex));
//                    action.initialize(p.getPanelActionConfiguration(preConstgructionActions.get(actionIndex)));
//                    action.executeAction(AutomatedInstallData.getInstance(), null);
//                }
//            }
//            object = constructor.newInstance(params);
//            panel = (IzPanel) object;
//            String dataValidator = p.getValidator();
//            if (dataValidator != null) {
//                panel.setValidationService(DataValidatorFactory.createDataValidator(dataValidator));
//            }
//
//            panel.setHelps(p.getHelpsMap());
//
//            List<String> preActivateActions = p.getPreActivationActions();
//            if (preActivateActions != null) {
//                for (int actionIndex = 0; actionIndex < preActivateActions.size(); actionIndex++) {
//                    String panelActionClass = preActivateActions.get(actionIndex);
//                    PanelAction action = PanelActionFactory.createPanelAction(panelActionClass);
//                    action.initialize(p.getPanelActionConfiguration(panelActionClass));
//                    panel.addPreActivationAction(action);
//                }
//            }
//            List<String> preValidateActions = p.getPreValidationActions();
//            if (preValidateActions != null) {
//                for (int actionIndex = 0; actionIndex < preValidateActions.size(); actionIndex++) {
//                    String panelActionClass = preValidateActions.get(actionIndex);
//                    PanelAction action = PanelActionFactory.createPanelAction(panelActionClass);
//                    action.initialize(p.getPanelActionConfiguration(panelActionClass));
//                    panel.addPreValidationAction(action);
//                }
//            }
//            List<String> postValidateActions = p.getPostValidationActions();
//            if (postValidateActions != null) {
//                for (int actionIndex = 0; actionIndex < postValidateActions.size(); actionIndex++) {
//                    String panelActionClass = postValidateActions.get(actionIndex);
//                    PanelAction action = PanelActionFactory.createPanelAction(panelActionClass);
//                    action.initialize(p.getPanelActionConfiguration(panelActionClass));
//                    panel.addPostValidationAction(action);
//                }
//            }
//
//            installdata.getPanels().add(panel);
//            if (panel.isHidden()) {
//                visiblePanelMapping.add(count, -1);
//            } else {
//                visiblePanelMapping.add(count, curVisPanelNumber);
//                curVisPanelNumber++;
//                lastVis = count;
//            }
//            count++;
//            // We add the XML data panel root
//            IXMLElement panelRoot = new XMLElementImpl(className, installdata.getXmlData());
//            // if set, we add the id as an attribute to the panelRoot
//            String panelId = p.getPanelid();
//            if (panelId != null) {
//                panelRoot.setAttribute("id", panelId);
//            }
//            installdata.getXmlData().addChild(panelRoot);
//        }
//        visiblePanelMapping.add(count, lastVis);
    }


}
